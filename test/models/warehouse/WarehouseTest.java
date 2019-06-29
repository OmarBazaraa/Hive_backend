package models.warehouse;

import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.AbstractTask;
import models.tasks.Task;
import models.tasks.orders.CollectOrder;
import models.tasks.orders.Order;
import models.tasks.orders.OrderListener;
import models.tasks.orders.RefillOrder;
import models.warehouses.Warehouse;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;


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
        Rack rack = warehouse.getRackById(1);
        Gate gate = warehouse.getGateById(1);
        Item item = warehouse.getItemById(1);

        // Print initial warehouse
        warehouse.print();

        // Create new order
        Order order = new CollectOrder(1, gate);
        order.add(item, 1);
        warehouse.addOrder(order);

        //
        // Initial checks
        //
        Assert.assertEquals(item.getReservedUnits(), 1);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        // Print final warehouse
        warehouse.print();

        //
        // Final checks
        //

        // Item 1
        Assert.assertEquals(item.getReservedUnits(), 0);
        Assert.assertEquals(item.getTotalUnits(), 9);
        Assert.assertEquals(item.getAvailableUnits(), 9);
        Assert.assertEquals(item.get(rack), 9);
        Assert.assertEquals(rack.get(item), 9);

        // order
        Assert.assertEquals(order.getStatus(), AbstractTask.TaskStatus.FULFILLED);
    }

    @Test
    public void singleAgentWithMultiRacksTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/1A_2R_1G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Rack rack1 = warehouse.getRackById(1);
        Rack rack2 = warehouse.getRackById(2);
        Gate gate = warehouse.getGateById(1);
        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);

        // Print initial warehouse
        warehouse.print();

        // Create new order
        Order order = new CollectOrder(1, gate);
        order.add(item1, 3);
        order.add(item2, 4);
        warehouse.addOrder(order);

        //
        // Initial checks
        //
        Assert.assertEquals(item1.getReservedUnits(), 3);
        Assert.assertEquals(item2.getReservedUnits(), 4);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        // Print final warehouse
        warehouse.print();

        //
        // Final checks
        //

        // Item 1
        Assert.assertEquals(item1.getReservedUnits(), 0);
        Assert.assertEquals(item1.getTotalUnits(), 2);
        Assert.assertEquals(item1.getAvailableUnits(), 2);
        Assert.assertEquals(item1.get(rack1), 2);
        Assert.assertEquals(rack1.get(item1), 2);

        // Item 2
        Assert.assertEquals(item2.getReservedUnits(), 0);
        Assert.assertEquals(item2.getTotalUnits(), 1);
        Assert.assertEquals(item2.getAvailableUnits(), 1);
        Assert.assertEquals(item2.get(rack2), 1);
        Assert.assertEquals(rack2.get(item2), 1);

        // order
        Assert.assertEquals(order.getStatus(), AbstractTask.TaskStatus.FULFILLED);
    }

    @Test
    public void multiAgentsCrossingTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/2A_2R_2G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Rack rack1 = warehouse.getRackById(1);
        Rack rack2 = warehouse.getRackById(2);
        Gate gate1 = warehouse.getGateById(1);
        Gate gate2 = warehouse.getGateById(2);
        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);

        // Print initial warehouse
        warehouse.print();

        // Create new orders
        Order order1 = new CollectOrder(1, gate2);
        order1.add(item1, 1);
        warehouse.addOrder(order1);

        Order order2 = new CollectOrder(2, gate1);
        order2.add(item2, 1);
        warehouse.addOrder(order2);

        //
        // Initial checks
        //
        Assert.assertEquals(item1.getReservedUnits(), 1);
        Assert.assertEquals(item2.getReservedUnits(), 1);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        // Print final warehouse
        warehouse.print();

        //
        // Final checks
        //

        // Item 1
        Assert.assertEquals(item1.getReservedUnits(), 0);
        Assert.assertEquals(item1.getTotalUnits(), 4);
        Assert.assertEquals(item1.getAvailableUnits(), 4);
        Assert.assertEquals(item1.get(rack1), 4);
        Assert.assertEquals(rack1.get(item1), 4);

        // Item 2
        Assert.assertEquals(item2.getReservedUnits(), 0);
        Assert.assertEquals(item2.getTotalUnits(), 4);
        Assert.assertEquals(item2.getAvailableUnits(), 4);
        Assert.assertEquals(item2.get(rack2), 4);
        Assert.assertEquals(rack2.get(item2), 4);

        // Orders
        Assert.assertEquals(order1.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order2.getStatus(), AbstractTask.TaskStatus.FULFILLED);
    }

    @Test
    public void multiOrdersMergedTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/1A_1R_2G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Gate gate1 = warehouse.getGateById(1);
        Gate gate2 = warehouse.getGateById(2);
        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);

        // Print initial warehouse
        warehouse.print();

        // Create new order
        Order order1 = new CollectOrder(1, gate1);
        order1.add(item1, 1);
        warehouse.addOrder(order1);

        Order order2 = new CollectOrder(2, gate2);
        order2.add(item2, 1);
        warehouse.addOrder(order2);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        Assert.assertEquals(order1.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order2.getStatus(), AbstractTask.TaskStatus.FULFILLED);
    }

    @Test
    public void crowdTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/4A_4R_4G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();

        // Print initial warehouse
        warehouse.print();

        // Create new order
        Order order1 = new CollectOrder(1, warehouse.getGateById(1));
        order1.add(warehouse.getItemById(1), 5);
        warehouse.addOrder(order1);

        Order order2 = new CollectOrder(2, warehouse.getGateById(2));
        order2.add(warehouse.getItemById(1), 5);
        warehouse.addOrder(order2);

        Order order3 = new CollectOrder(3, warehouse.getGateById(3));
        order3.add(warehouse.getItemById(1), 5);
        warehouse.addOrder(order3);

        Order order4 = new CollectOrder(4, warehouse.getGateById(4));
        order4.add(warehouse.getItemById(1), 5);
        warehouse.addOrder(order4);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        Assert.assertEquals(order1.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order2.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order3.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order4.getStatus(), AbstractTask.TaskStatus.FULFILLED);

        // Print final warehouse
        warehouse.print();
    }

    @Test
    public void multiAgentsSingleGateTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/same_gate.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();

        // Print initial warehouse
        warehouse.print();

        // Create new order
        Order order1 = new CollectOrder(1, warehouse.getGateById(1));
        order1.add(warehouse.getItemById(1), 1);
        warehouse.addOrder(order1);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        // Add a new order
        Order order2 = new CollectOrder(2, warehouse.getGateById(1));
        order2.add(warehouse.getItemById(1), 1);
        order2.add(warehouse.getItemById(2), 1);
        warehouse.addOrder(order2);

        // Run till no changes occur
        while (warehouse.run()) {
            System.out.println(warehouse);
        }

        Assert.assertEquals(order1.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order2.getStatus(), AbstractTask.TaskStatus.FULFILLED);

        // Print final warehouse
        warehouse.print();
    }

    @Test
    public void singleAgentMultiOrdersTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/multi_orders.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();

        // Print initial warehouse
        warehouse.print();

        // Create new order
        Order order1 = new CollectOrder(1, warehouse.getGateById(1));
        order1.add(warehouse.getItemById(1), 3);
        warehouse.addOrder(order1);

        Order order2 = new CollectOrder(2, warehouse.getGateById(1));
        order2.add(warehouse.getItemById(2), 10);
        warehouse.addOrder(order2);

        Order order3 = new CollectOrder(3, warehouse.getGateById(3));
        order3.add(warehouse.getItemById(1), 5);
        order3.add(warehouse.getItemById(3), 15);
        warehouse.addOrder(order3);

        Order order4 = new RefillOrder(4, warehouse.getGateById(2), warehouse.getRackById(2));
        order4.add(warehouse.getItemById(1), 15);
        warehouse.addOrder(order4);

        Order order5 = new CollectOrder(5, warehouse.getGateById(3));
        order5.add(warehouse.getItemById(3), 14);
        warehouse.addOrder(order5);

        // Run till no changes occur
        while (warehouse.run()) {
            // System.out.println(warehouse);
        }

        Assert.assertEquals(order1.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order2.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order3.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order4.getStatus(), AbstractTask.TaskStatus.FULFILLED);
        Assert.assertEquals(order5.getStatus(), AbstractTask.TaskStatus.FULFILLED);

        // Print final warehouse
        warehouse.print();
    }
}