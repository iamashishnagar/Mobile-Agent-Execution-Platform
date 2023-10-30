package Mobile;

import java.io.*;
import java.rmi.*;
import java.lang.reflect.*;

/**
 * Mobile.Agent is the base class of all user-define mobile agents. It carries an agent identifier, the next host IP and
 * port, the name of the function to invoke at the next host, arguments passed to this function, its class name, and its
 * byte code. It runs as an independent thread that invokes a given function upon migrating the next host.
 *
 * @author Munehiro Fukuda
 * @version %I% %G%
 * @since 1.0
 */
public class Agent implements Serializable, Runnable {
    // live data to carry with the agent upon a migration
    protected int agentId = -1;    // this agent's identifier
    private String _hostname = null;  // the next host name to migrate
    private String _function = null;  // the function to invoke upon a move
    private int _port = 0;     // the next host port to migrate
    private String[] _arguments = null;  // arguments pass to _function
    private String _classname = null;  // this agent's class name
    private byte[] _bytecode = null;  // this agent's byte code
    private String spawnedHostName = null;

    /**
     * setPort() sets a port that is used to contact a remote Mobile.Place.
     *
     * @param port a port to be set.
     */
    public void setPort(int port) {
        this._port = port;
    }

    /**
     * setId() sets this agent identifier: agentId.
     *
     * @param id an identifier to set to this agent.
     */
    public void setId(int id) {
        this.agentId = id;
    }

    /**
     * getId( ) returns this agent identifier: agentId.
     */
    public int getId() {
        return agentId;
    }

    /**
     * getByteCode() reads a byte code from the file whose name is given in "classname.class".
     *
     * @param classname the name of a class to read from local disk.
     *
     * @return a byte code of a given class.
     */
    public static byte[] getByteCode(String classname) {
        // create the file name
        String filename = classname + ".class";

        // allocate the buffer to read this agent's bytecode in
        File file = new File(filename);
        byte[] bytecode = new byte[(int) file.length()];

        // read this agent's bytecode from the file.
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
            bis.read(bytecode, 0, bytecode.length);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // now you got a byte code from the file. just return it.
        return bytecode;
    }

    /**
     * getByteCode() reads this agent's byte code from the corresponding file.
     *
     * @return a byte code of this agent.
     */
    public byte[] getByteCode() {
        if (_bytecode != null) // bytecode has been already read from a file
            return _bytecode;

        // get this agent's class name and file name
        _classname = this.getClass().getName();
        _bytecode = getByteCode(_classname);

        return _bytecode;
    }

    /**
     * run() is the body of Mobile.Agent that is executed upon an injection or a migration as an independent thread.
     * run() identifies the method with a given function name and arguments and invokes it. The invoked method may
     * include hop() that transfers this agent to a remote host or simply returns back to run() that terminates the
     * agent.
     */
    public void run() {
        try {
            // find a remote place through Naming.lookup()
            PlaceInterface place = (PlaceInterface) Naming.lookup("rmi://localhost:" + _port + "/place");
            String message = place.receive();
            // print messages if any
            if (!message.isEmpty())
                System.out.println("[Agent " + agentId + ": Received the following messages: \n" + message + "]");

            Method method;
            // invoke a method with a given function name and arguments
            if (_arguments == null) {
                method = this.getClass().getMethod(_function);
                method.invoke(this);
            }
            // invoke a method with a given function name and arguments
            else {
                method = this.getClass().getMethod(_function, String[].class);
                method.invoke(this, (Object) _arguments);
            }
        } catch (Exception e) {
            // Print stack trace if the exception is not caused by "ThreadDeath"
            if (!e.getCause().toString().equals("java.lang.ThreadDeath"))
                e.printStackTrace();
        }
    }

    /**
     * hop() transfers this agent to a given host, and invokes a given function of this agent.
     *
     * @param hostname the IP name of the next host machine to migrate
     * @param function the name of a function to invoke upon a migration
     */
    public void hop(String hostname, String function) {
        hop(hostname, function, null);
    }

    /**
     * hop() transfers this agent to a given host, and invokes a given function of this agent as passing given arguments
     * to it.
     *
     * @param hostname the IP name of the next host machine to migrate
     * @param function the name of a function to invoke upon a migration
     * @param args     the arguments passed to a function called upon a migration.
     */
    @SuppressWarnings ("deprecation")
    public void hop(String hostname, String function, String[] args) {
        _bytecode = getByteCode();
        _hostname = hostname;
        _function = function;
        _arguments = args;

        try {
            // Serialize this agent into a byte array
            byte[] serializedAgent = serialize();
            // Find a remote place through Naming.lookup()
            PlaceInterface place = (PlaceInterface) Naming.lookup("rmi://" + hostname + ":" + _port + "/place");
            // Invoke an RMI call
            place.transfer(_classname, _bytecode, serializedAgent);
            // Kill this agent with Thread.currentThread().stop(), which is deprecated but do so anyway
            Thread.currentThread().stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * serialize() serializes this agent into a byte array.
     *
     * @return a byte array to contain this serialized agent.
     */
    private byte[] serialize() {
        try {
            // instantiate an object output stream.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);

            // write myself to this object output stream
            os.writeObject(this);

            return out.toByteArray(); // convert the stream to a byte array
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** setSpawnedHostName() sets the name of the host where this agent is spawned. */
    public void setSpawnedHostName(String spawnedHostName) {
        this.spawnedHostName = spawnedHostName;
    }

    /** getSpawnedHostName() returns the name of the host where this agent is spawned. */
    public String getSpawnedHostName() {
        return spawnedHostName;
    }

    /**
     * sendMessage() sends a message to a given host.
     *
     * @param message  a message to send
     * @param hostname the IP name of the host to send a message to.
     */
    public void sendMessage(String message, String hostname) {
        PlaceInterface place;
        try {
            //getting the place interface of host to send message to.
            place = (PlaceInterface) Naming.lookup("rmi://" + hostname + ":" + _port + "/place");
            place.send(agentId, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}