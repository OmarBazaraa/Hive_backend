package models.tasks;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.orders.Order;
import models.tasks.orders.RefillOrder;
import models.warehouse.WarehouseHelper;
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
    public void refillOrderTest() throws Exception {
        WarehouseHelper.configureWarehouse("data/1A_1R_1G.hive");

        // Get components
        Warehouse warehouse = Warehouse.getInstance();
        Agent agent = warehouse.getAgentById(1);
        Rack rack = warehouse.getRackById(1);
        Gate gate = warehouse.getGateById(1);
        Item item = warehouse.getItemById(1);

        // Create refill order
        Order order = new RefillOrder(1, gate, rack);
        order.add(item, 5);

        // Check refill order properties
        Assert.assertEquals(order.getPendingUnits(), -5);
        Assert.assertEquals(order.get(item), -5);
        Assert.assertTrue(order.isPending());

        // Check items before and after order activation
        Assert.assertEquals(item.getAvailableUnits(), 1);
        order.activate();
        Assert.assertEquals(item.getReservedUnits(), -5);
        Assert.assertEquals(item.getTotalUnits(), 1);
        Assert.assertEquals(item.getAvailableUnits(), 6);
        Assert.assertEquals(rack.get(item), 1);

        // Create task to carry out this order
        Task task = new Task(agent, rack);
        task.addOrder(order);

        // Check items after task assignment
        Assert.assertEquals(item.getReservedUnits(), -5);
        Assert.assertEquals(item.getTotalUnits(), 1);
        Assert.assertEquals(item.getAvailableUnits(), 6);
        Assert.assertEquals(rack.get(item), 6);
        Assert.assertFalse(order.isPending());
    }
}
