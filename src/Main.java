import utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class Main {

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


        List<Integer> l1 = new ArrayList<>();
        List<Integer> l2 = new ArrayList<>();
        List<Integer> l3 = new ArrayList<>();

        l1.add(1);
        l2.add(2);
        l2.add(3);
        l2.add(4);
        l3.add(5);
        l3.add(6);

        // Append the lists in order
        l1.addAll(l2);
        l1.addAll(l3);

        System.out.println(l1);
    }
}
