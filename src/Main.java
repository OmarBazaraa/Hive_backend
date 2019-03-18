import server.Server;


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
        }
    }

    public static void run() throws Exception {
        // Create server object
        Server server = Server.getInstance();

        // Start the server
        server.start();

        // Keep running while EXIT is not hit
        while (server.isRunning()) {
            server.run();
        }

        // Close and finalize the server
        server.close();
    }
}
