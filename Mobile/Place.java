package Mobile;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

/**
 * Mobile.Place is our mobile-agent execution platform that accepts an agent transferred by Mobile.Agent.hop( ),
 * deserializes it, and resumes it as an independent thread.
 *
 * @author Munehiro Fukuda
 * @version %I% %G$
 * @since 1.0
 */
public class Place extends UnicastRemoteObject implements PlaceInterface {
    private AgentLoader loader = null;  // a loader to define a new agent class
    private int agentSequencer = 0;     // a sequencer to give a unique agentId

    private Map<Integer, List<String>> messages = new HashMap<>();

    /**
     * This constructor instantiates a Mobile.AgentLoader object that is used to define a new agen class coming from
     * remotely.
     */
    public Place() throws RemoteException {
        super();
        loader = new AgentLoader();
    }

    /**
     * deserialize( ) deserializes a given byte array into a new agent.
     *
     * @param buf a byte array to be deserialized into a new Agent object.
     *
     * @return a deserialized Agent object
     */
    private Agent deserialize(byte[] buf) throws IOException, ClassNotFoundException {
        // converts buf into an input stream
        ByteArrayInputStream in = new ByteArrayInputStream(buf);

        // AgentInputStream identify a new agent class and deserialize
        // a ByteArrayInputStream into a new object
        AgentInputStream input = new AgentInputStream(in, loader);
        return (Agent) input.readObject();
    }

    /**
     * transfer() accepts an incoming agent and launches it as an independent thread.
     *
     * @param classname The class name of an agent to be transferred.
     * @param bytecode  The byte code of an agent to be transferred.
     * @param entity    The serialized object of an agent to be transferred.
     *
     * @return true if an agent was accepted in success, otherwise false.
     */
    public boolean transfer(String classname, byte[] bytecode, byte[] entity) throws RemoteException {
        boolean success = false;
        try {
            // Register the incoming agent's classname and bytecode
            loader.loadClass(classname, bytecode);

            // Deserialize the incoming agent's entity
            Agent incomingAgent = deserialize(entity);

            // Set the incoming agent's identifier if it has not yet been set
            if (incomingAgent.getId() == -1) {
                incomingAgent.setId(InetAddress.getLocalHost().hashCode() + agentSequencer++);
            }

            // Instantiate a Thread object and pass the deserialized agent to the constructor
            Thread agentThread = new Thread(incomingAgent);

            // Invoke the thread's start() method
            agentThread.start();

            // Return true if everything is done successfully
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * main() starts an RMI registry in local, instantiates a Mobile.Place agent execution platform, and registers it
     * into the registry.
     *
     * @param args receives a port, (i.e., 5001-65535).
     */
    public static void main(String[] args) {
        // check if a port number is given
        if (args.length != 1) {
            System.out.println("Usage: java Place <port>");
            System.exit(0);
        }

        // check if the port number is valid
        int port = Integer.parseInt(args[0]);
        if (port < 5001 || port > 65535) {
            System.out.println("Port number must be between 5001 and 65535.");
            System.exit(0);
        }

        // start an RMI registry in local
        try {
            startRegistry(port);
        } catch (RemoteException e) {
            System.out.println("Exception starting RMI registry:");
            e.printStackTrace();
            System.exit(0);
        }

        // instantiate a Place object
        try {
            Place place = new Place();
            Naming.rebind("rmi://localhost:" + port + "/place", place);
            System.out.println("Place ready...");
        } catch (Exception e) {
            System.out.println("Place exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * startRegistry() starts an RMI registry process in local to this Place.
     *
     * @param port the port to which this RMI should listen.
     */
    private static void startRegistry(int port) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list();
        } catch (RemoteException e) {
            Registry registry = LocateRegistry.createRegistry(port);
        }
    }

    /**
     * send() stores a message from a sender agent.
     *
     * @param senderAgentId the identifier of a sender agent.
     * @param message       a message to be stored.
     */
    public void send(int senderAgentId, String message) {
        messages.computeIfAbsent(senderAgentId, k -> new ArrayList<>()).add(message);
    }

    /**
     * receive() returns all messages stored in this Place.
     *
     * @return all messages stored in this Place.
     */
    public String receive() {
        StringBuilder messages = new StringBuilder();
        for (Map.Entry<Integer, List<String>> entry : this.messages.entrySet()) {
            int senderAgentId = entry.getKey();
            List<String> senderMessages = entry.getValue();
            messages.append("Messages from agent ").append(senderAgentId).append(":");
            for (String message : senderMessages) {
                messages.append("\n").append(message);
            }
        }
        this.messages.clear();
        return messages.toString();
    }
}