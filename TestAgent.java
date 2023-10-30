import Mobile.Agent;

/**
 * MyAgent is a test mobile agent that is injected to the 1st Mobile.Place platform to print the breath message,
 * migrates to the 2nd platform to say "Hello!", and even moves to the 3rd platform to say "Oi!".
 *
 * @author Ashish Nagar
 */
public class TestAgent extends Agent {
    public int hopCount = 0;
    public String[] destination = null;

    /**
     * The consturctor receives a String array as an argument from Mobile.Inject.
     *
     * @param args arguments passed from Mobile.Inject to this constructor
     */
    public TestAgent(String[] args) {
        destination = args;
    }

    /**
     * init( ) is the default method called upon an agent inject.
     */
    public void init() {
        System.out.println("TestAgent: agent (" + agentId + ") invoked init: " + "hop count = " + hopCount + ", next "
                + "dest = " + destination[hopCount]);
        String[] args = new String[1];
        args[0] = "TestAgent: Hello!";
        hopCount++;
        hop(destination[0], "step", args);
    }

    /**
     * step( ) is invoked upon an agent migration to destination[0] after init( ) calls hop( ).
     *
     * @param args arguments passed from init( ).
     */
    public void step(String[] args) {
        System.out.println("TestAgent: agent (" + agentId + ") invoked step: " + "hop count = " + hopCount + ", next "
                + "dest = " + destination[hopCount] + ", message = " + args[0]);
        args[0] = "TestAgent: Oi!";
        hopCount++;
        hop(destination[1], "jump", args);
    }

    /**
     * jump( ) is invoked upon an agent migration to destination[1] after step( ) calls hop( ).
     *
     * @param args arguments passed from step( ).
     */
    public void jump(String[] args) {
        System.out.println("TestAgent: agent (" + agentId + ") invoked jump: " + "hop count = " + hopCount + ", " +
                "message = " + args[0]);
        sendMessage("TestAgent: Completed hops from " + destination[0] + " and " + destination[1],
                getSpawnedHostName());
        System.out.println("TestAgent: Message sent to first spawned host.");
    }
}