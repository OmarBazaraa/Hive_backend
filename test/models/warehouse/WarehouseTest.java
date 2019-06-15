package models.warehouse;

import models.facilities.Gate;
import models.items.Item;
import models.tasks.Order;
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
    public void simpleTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/simple_1.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Gate gate = warehouse.getGateById(1);
        Item item = warehouse.getItemById(1);

        // Print
        System.out.println(warehouse);

        // Create new order
        Order order = new Order(1, Order.OrderType.COLLECT, gate);
        order.add(item, 1);
        warehouse.addOrder(order);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }
    }
}