import communicators.frontend.FrontendCommunicator;


/**
 * This is the main entry class of the project.
 */
public class Main {

    /**
     * The main entry function of the project.
     *
     * @param args external system arguments.
     */
    public static void main(String[] args) throws Exception {
        // Welcome screen
        System.out.println();
        System.out.println("+---------------------+");
        System.out.println("|     Hive System     |");
        System.out.println("+---------------------+");
        System.out.println();

        // Run Hive system
        try {
            run();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void run() throws Exception {
        // Create server object
        FrontendCommunicator server = FrontendCommunicator.getInstance();

        // Start the server and allocate the port
        server.start();

        // Keep running till EXIT is hit
        while (server.isRunning()) {
            server.run();
        }

        // Close and finalize the server
        server.close();
    }
}
