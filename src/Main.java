import controller.Controller;


/**
 * This is the main entry class of the project.
 */
public class Main {

    /**
     * The main entry function of the project.
     *
     * @param args external system arguments.
     */
    public static void main(String[] args) {
        // Welcome screen
        System.out.println();
        System.out.println("+---------------------+");
        System.out.println("|     Hive System     |");
        System.out.println("+---------------------+");
        System.out.println();

        // Run Hive system
        try {
            Controller controller = new Controller();
            controller.start();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
