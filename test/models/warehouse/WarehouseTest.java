package models.warehouse;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.maps.MapGrid;
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
        MapGrid map = warehouse.getMap();
        Agent agent = warehouse.getAgentById(1);
        Rack rack = warehouse.getRackById(1);
        Gate gate = warehouse.getGateById(1);
        Item item = warehouse.getItemById(1);

        // Create new order
        Order order = new Order(1, Order.OrderType.COLLECT, gate);
        order.add(item, 1);
        warehouse.addOrder(order);

        // Run 22 steps
        for (int i = 0; i < 22; ++i) {
            System.out.println(map);
            warehouse.run();
        }
    }
}