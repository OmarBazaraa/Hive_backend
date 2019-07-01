package models.tasks;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.orders.CollectOrder;
import models.tasks.orders.Order;
import models.tasks.orders.RefillOrder;
import models.warehouses.WarehouseHelper;
import models.warehouses.Warehouse;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class OrderTest {

    /**
     * Initializes the required objects.
     */
    @BeforeClass
    public static void before() {

    }

    @Test
    public void collectOrderTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/1A_1R_1G.hive");

        // Items distribution
        // item1 -> 10 units
        // item2 -> 10 units

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Agent agent = warehouse.getAgentById(1);
        Rack rack = warehouse.getRackById(1);
        Gate gate = warehouse.getGateById(1);
        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);

        // Create refill order
        Order order = new CollectOrder(1, gate);
        order.add(item1, 5);
        order.add(item2, 4);

        // Check collect order properties
        Assert.assertEquals(order.getPendingUnits(), 9);
        Assert.assertEquals(order.get(item1), 5);
        Assert.assertEquals(order.get(item2), 4);
        Assert.assertTrue(order.isPending());

        // Check items before and after order activation
        Assert.assertEquals(item1.getAvailableUnits(), 10);
        Assert.assertEquals(item2.getAvailableUnits(), 10);

        order.activate();

        Assert.assertEquals(item1.getReservedUnits(), 5);
        Assert.assertEquals(item1.getTotalUnits(), 10);
        Assert.assertEquals(item1.getAvailableUnits(), 5);
        Assert.assertEquals(rack.get(item1), 10);

        Assert.assertEquals(item2.getReservedUnits(), 4);
        Assert.assertEquals(item2.getTotalUnits(), 10);
        Assert.assertEquals(item2.getAvailableUnits(), 6);
        Assert.assertEquals(rack.get(item2), 10);

        // Create task to carry out this order
        Task task = new Task(agent, rack);
        task.addOrder(order);

        // Check items after task assignment
        Assert.assertFalse(order.isPending());

        Assert.assertEquals(item1.getReservedUnits(), 5);
        Assert.assertEquals(item1.getTotalUnits(), 10);
        Assert.assertEquals(item1.getAvailableUnits(), 5);
        Assert.assertEquals(rack.get(item1), 5);

        Assert.assertEquals(item2.getReservedUnits(), 4);
        Assert.assertEquals(item2.getTotalUnits(), 10);
        Assert.assertEquals(item2.getAvailableUnits(), 6);
        Assert.assertEquals(rack.get(item2), 6);
    }

    @Test
    public void refillOrderTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/1A_1R_1G.hive");

        // Items distribution
        // item1 -> 10 units
        // item2 -> 10 units

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Agent agent = warehouse.getAgentById(1);
        Rack rack = warehouse.getRackById(1);
        Gate gate = warehouse.getGateById(1);
        Item item = warehouse.getItemById(1);

        // Create refill order
        RefillOrder order = new RefillOrder(1, gate, rack);
        order.add(item, 5);

        // Check refill order properties
        Assert.assertEquals(order.getPendingUnits(), -5);
        Assert.assertEquals(order.get(item), -5);
        Assert.assertEquals(order.getAddedWeight(), 5 * item.getWeight());
        Assert.assertTrue(order.isPending());

        // Check items before and after order activation
        Assert.assertEquals(item.getAvailableUnits(), 10);
        order.activate();
        Assert.assertEquals(item.getReservedUnits(), -5);
        Assert.assertEquals(item.getTotalUnits(), 10);
        Assert.assertEquals(item.getAvailableUnits(), 15);
        Assert.assertEquals(rack.get(item), 10);

        // Create task to carry out this order
        Task task = new Task(agent, rack);
        task.addOrder(order);

        // Check items after task assignment
        Assert.assertEquals(item.getReservedUnits(), -5);
        Assert.assertEquals(item.getTotalUnits(), 10);
        Assert.assertEquals(item.getAvailableUnits(), 15);
        Assert.assertEquals(rack.get(item), 15);
        Assert.assertFalse(order.isPending());
    }
}
