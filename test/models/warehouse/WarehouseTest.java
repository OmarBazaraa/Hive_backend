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
    public void singleAgentSingleRackTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/simple_1.hive");

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
    public void singleAgentMultiRacksTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/single_agent_multi_racks.hive");

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
}