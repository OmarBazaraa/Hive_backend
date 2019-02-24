import models.components.Task;
import models.Warehouse;

import java.io.FileReader;
import java.util.*;

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
        // Create warehouse object
        Scanner reader = new Scanner(new FileReader(configFilename));
        Warehouse warehouse = Warehouse.create(reader);
        reader.close();
        warehouse.print();

        // Read components
        reader = new Scanner(new FileReader(ordersFilename));
        List<Task> orders = new ArrayList<>();

        while (reader.hasNext()) {
            int time = reader.nextInt();
            int agentId = reader.nextInt();
            int rackId = reader.nextInt();

            warehouse.addTask(agentId, rackId);
        }

        while (warehouse.isActive()) {
            warehouse.run();
        }
    }
}
