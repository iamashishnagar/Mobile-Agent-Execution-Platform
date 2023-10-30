# Mobile-Agent-Execution-Platform
A Java-based mobile-agent platform utilizing RPC, dynamic linking, and object serialization, optimizing task execution across distributed networks.

This project implements a mobile-agent execution platform that is in general facilitated by three distributed computing technologies: RPC, dynamic linking, and object serialization/deserialization. We exercise how to use these technologies in Java, which correspond to RMI, class loader and reflection, and Java object input/output streams.

Process: 
Step 1: Injection - The agent is instantiated where a user injects it through the Mobile.Inject program, (i.e. the computing node local to the user), and receives a String array as the constructor argument. At this time, an agent is still an ordinary (passive) Java object. 
Step 2: System-initiated migration - Upon an instantiation, the agent is dispatched to the computing node that has been specified with the Mobile.Inject program. Dispatched there, the agent starts to run as an independent thread and automatically invokes its init( ) method. If init( ) has no hop( ) method call, a return from init( ) means the termination of this agent. 
Step 3: User-initiated migration - If the agent invokes the hop( destination, function, arguments ) function within init( ), it will migrate to the next computing-node, (i.e., destination) specified in hop( ). Upon each user-initiated migration, the agent will resume its execution as an independent thread and invoke the function specified in hop( ). 
Step 4: Termination - If the agent returns from the function that was invoked upon a migration (including init( )), the thread to run this agent is stopped and the object is garbage-collected by the system.

Methods:
1. public void hop( String host, String function ) Transfers this agent to a given host and invokes a given function of this agent.
2. public void hop( String host, String function,String[] arguments ) Transfers this agent to a given host, and invokes a given function of this agent as passing given arguments to it.
3. public void run( ) Is the body of Mobile.Agent that is executed upon an injection or a migration as an independent thread. The run( ) method identifies the function and arguments given in hop( ), and invokes it. The invoked function may include hop( ) to further transfer the calling agent to a remote host or simply return to run( ) that terminates the agent.
4. public void setPort( int port ) Sets a port that is used to contact a remote RMI server when migrating there.
5. public void setId( int id ) Sets this agent identifier, (i.e., id).
6. public int getId( ) Returns this agent identifier, (i.e., id).
7. public static byte[] getByteCode( String className ) Reads a byte code from the file whose name is className + “.class”.
8. public byte[] getByteCode( ) Reads this agent's byte code from the corresponding file.
9. private byte[] serialize( ) Serializes this agent into a byte array.

Agent.run( ) - performs the following tasks:
1. Find the method to invoke, through this.getClass( ).getMethod( ).
2. Invoke this method through Method.invoke( ).

Agent.hop( String hostname, String function, String[] args ) - performs the following tasks:
1. Load this agent’s byte code into the memory.
2. Serialize this agent into a byte array.
3. Find a remote place through Naming.lookup( ).
4. Invoke an RMI call.
5. Kill this agent with Thread.currentThread( ).stop( ), which is deprecated but do so anyway.

Methods:
1. public static void main( String[] args ) Starts an RMI registry in local, instantiates a Mobile.Place object, (i.e., an agent execution platform), and registers into the registry. The main( ) should receive the port #, (i.e., 5001-65535) to launch its local rmiresitry.
2. private static void startRegistry( int port ) throwsRemoteException Is called from main( ) and starts an RMI registry in local to this Place.
3. public Place( ) throws RemoteException Instantiates an AgentLoader object that should be passed to AgentInputStream to deserialize an incoming agent.
4. public boolean transfer( String classname, byte[]bytecode, byte[] entity ) throws RemoteException Is called from Agent.hop( ) remotely. The transfer( ) method receives this calling agent, deserializes it, sets this agent’s identifier if it has not yet been set, instantiates a Thread object as passing this agent to its constructor, (in other words, the agent is an Runnable interface), and invokes this thread’s start( ) method. If everything goes well, transfer( ) should return true, otherwise false.
5. private Agent deserialize( byte[] buf ) Receives a byte array of an agent, and deserializes it from the array.

Place.main( String args[] ) - performs the following tasks:
1. Read args[0] as the port number and checks its validity.
2. Invoke startRegistry( int port ).
3. Instantiate a Place object.
4. Register it into rmiregistry through Naming,rebind( ).

Place.transfer( String classname, byte[] bytecode, byte[] entity ) - performs the following tasks:
1. Register this calling agent’s classname and bytecode into AgentLoader.
2. Deserialize this agent’s entity through deserialize( entity ).
3. Set this agent’s identifier if it has not yet been set. How to give a new agent id is up to your implementation. An example is to have each Place maintain a sequencer, to generate a unique agent id with a combination of the Place IP address and this sequence number, and increment the sequencer.
4. Instantiate a Thread object as passing the deserialized agent to the constructor.
5. Invoke this thread’s start( ) method. (6) Return true if everything is done in success, otherwise false.

Documentation:

AgentLoader.java - Defines the class of an incoming agent and registers it into its local class hash.

AgentInputStream.java - Reads a byte array from ObjectInputStream and deserializes it into an agent object, using the AgentLoader class.

Inject.java - It reads a given agent class from a local disk, instantiates a new object from it, and transfers this agent to a given destination IP where the agent starts with the init( ) function.

PlaceInterface.java - Defines Place's RMI method that will be called from an Mobile.Agent.hop( ) to transfer an agent.

Agent.java - The Agent class in the Mobile package is a base class for user-defined mobile agents. The class runs as an independent thread and is responsible for invoking a specific function upon migrating to the next host. The run() method is the main execution body of the Agent class. It identifies the method to invoke based on the provided function name and arguments, and then invokes it. The invoked method can include a call to the hop() method, which transfers the agent to a remote host, or it may simply return to the run() method, terminating the agent. If any exception occurs during execution, the stack trace is printed unless the exception is caused by "ThreadDeath", in which case it is ignored.
The hop(String hostname, String function) method transfers the agent to the specific host and invokes the given function of the agent. It uses the default set of arguments (null). The hop(String hostname, String function, String[] args) method is an overloaded version that allows passing arguments to the function being invoked upon migration.

Place.java - The Place class represents a mobile-agent execution platform that allows the transfer and execution of mobile agents. It is responsible for receiving an agent transferred by the Mobile.Agent.hop() method, deserializing it, and launching it as an independent thread.
The transfer(String classname, byte[] bytecode, byte[] entity) method accepts an incoming agent, represented by its class name, bytecode, and serialized entity. It registers the agent's class and bytecode, deserializes the agent's entity, assigns an identity to the agent if it doesn't have one, creates a new thread with the agent as its target, and starts the thread. The method returns true if the transfer and execution were successful; otherwise, it returns false.
The main(String[] args) method is the entry point of the Place class. It starts an RMI registry on the specific port, instantiates a Place object, and registers it in the registry. It takes a port number as a command-line argument.

TestAgent.java - The TestAgent program is a Java class that extends the Mobile.Agent class and serves as a test mobile agent. This agent is designed to migrate to multiple Mobile.Place platforms and print messages on each platform. The hopCount variable is used to keep track of the number of hops the agent has taken, while the destination variable is an array of the platform destinations to be visited by the agent.
The program has three methods, init(), step(), and jump(), that are invoked upon the agent's migration to different destinations. The init() method is called upon agent injection and initializes the agent by printing a message and then calling the hop() method to migrate to the first platform. The step() method is called after the agent has migrated to the first platform and prints a message before calling the hop() method to migrate to the second platform. The jump() method is called after the agent has migrated to the second platform and sends a message before terminating.

The TestAgent program enables indirect inter-agent communication via RMI to Place. It's a mobile agent that migrates to multiple Mobile.Place platforms, printing messages at each destination. The agent maintains a hopCount to track the number of hops and utilizes a destination array to determine the platforms to visit.
Upon injection, the init() method initializes the agent, prints a message, and migrates to the first platform using the hop() method. After reaching the first platform, the step() method is invoked, printing a message and migrating to the second platform. Finally, the jump() method is called upon reaching the second platform to send a message and terminate the agent.
This program showcases the capabilities of mobile agents in networked environments, demonstrating migration, inter-agent communication, and distributed tasks.

Additional Features:
The Mobile-Agent Execution Platform provides a flexible framework for developing and executing mobile agents in distributed environments. It supports agent migration and execution on different hosts and facilitates inter-agent communication. An additional feature is the ability for agents to communicate indirectly via RMI calls to Place.
This feature leverages the Place class, which introduces 'send' and 'receive' methods for communication and agent spawning. Agents can read messages stored in a HashMap by making RMI calls to Place. A TestAgent.java class demonstrates this functionality by sending a message in its jump() method to indicate the completion of hops.
The platform's flexibility allows for customization based on specific use cases. Place.java remains unchanged, enabling agents to send and receive messages as required. This indirect communication mechanism enhances the platform's capabilities for distributed agent systems.
To enhance the Mobile-Agent Execution Platform, an appealing addition would be the capability to load and unload agents during runtime dynamically. This feature would enable the system to scale dynamically and manage resources efficiently without requiring a restart.
