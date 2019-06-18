package models.warehouse;

import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.orders.CollectOrder;
import models.tasks.orders.Order;
import models.warehouses.Warehouse;

import org.junit.Assert;
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
    }

    @Test
    public void multiOrdersMergedTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/1A_1R_2G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Rack rack = warehouse.getRackById(1);
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
        Assert.assertEquals(item1.get(rack), 4);
        Assert.assertEquals(rack.get(item1), 4);

        // Item 2
        Assert.assertEquals(item2.getReservedUnits(), 0);
        Assert.assertEquals(item2.getTotalUnits(), 4);
        Assert.assertEquals(item2.getAvailableUnits(), 4);
        Assert.assertEquals(item2.get(rack), 4);
        Assert.assertEquals(rack.get(item2), 4);
    }
}