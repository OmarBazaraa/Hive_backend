import communicator.Communicator;


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

        // Get warehouse config and components filename from arguments
        String configFilename = args[0];
        String ordersFilename = args[1];

        // Run Hive system
        try {
            run(configFilename, ordersFilename);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void run(String configFilename, String ordersFilename) throws Exception {

        Communicator.getInstance();
    }
}
