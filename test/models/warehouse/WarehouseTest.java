package models.warehouse;

import models.facilities.Gate;
import models.items.Item;
import models.tasks.Order;
import models.tasks.Order.OrderType;
import models.warehouses.Warehouse;

import org.junit.BeforeClass;
import org.junit.Test;


public class WarehouseTest {

    /**
     * Initializes the required objects.
     */
    @BeforeClass
    public static void before() {

    }

    @Test
    public void singleAgentSimpleTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/1A_1R_1G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Gate gate = warehouse.getGateById(1);
        Item item = warehouse.getItemById(1);

        // Print initial warehouse
        warehouse.print();

        // Create new order
        Order order = new Order(1, OrderType.COLLECT, gate);
        order.add(item, 1);
        warehouse.addOrder(order);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        // Print final warehouse
        warehouse.print();
    }

    @Test
    public void singleAgentWithMultiRacksTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/1A_2R_1G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Gate gate = warehouse.getGateById(1);
        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);

        // Print initial warehouse
        warehouse.print();

        // Create new order
        Order order = new Order(1, OrderType.COLLECT, gate);
        order.add(item1, 3);
        order.add(item2, 4);
        warehouse.addOrder(order);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        // Print final warehouse
        warehouse.print();
    }

    @Test
    public void multiAgentsCrossingTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/2A_2R_2G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Gate gate1 = warehouse.getGateById(1);
        Gate gate2 = warehouse.getGateById(2);
        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);

        // Print initial warehouse
        warehouse.print();

        // Create new orders
        Order order1 = new Order(1, OrderType.COLLECT, gate2);
        order1.add(item1, 1);
        warehouse.addOrder(order1);

        Order order2 = new Order(2, OrderType.COLLECT, gate1);
        order2.add(item2, 1);
        warehouse.addOrder(order2);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        // Print final warehouse
        warehouse.print();
    }
}