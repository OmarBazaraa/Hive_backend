package algorithms.dispatcher;

import algorithms.dispatcher.task_allocator.RackSelector;
import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.TaskTest;
import models.tasks.orders.CollectOrder;
import models.tasks.orders.Order;
import models.warehouse.WarehouseHelper;
import models.warehouses.Warehouse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RackSelectorTest {
    @BeforeClass
    public static void before() {

    }

    @AfterClass
    public static void after() {

    }

    @Test
    public void RackSelectorTestOne() throws Exception {
        WarehouseHelper.configureWarehouse("data/dispatcher_test/0A_6R_1G.hive");

        Warehouse warehouse = Warehouse.getInstance();
        Gate gate = warehouse.getGateById(1);
        Item item = warehouse.getItemById(1);

        // Create new order
        Order order = new CollectOrder(1, gate);
        order.add(item, 7);
        warehouse.addOrder(order);

        List<Rack> selectedRacks = Dispatcher.selectRacks(order, null);

        assertEquals(selectedRacks.size(), 1);
        assertEquals(selectedRacks.get(0).getStoredWeight(), 7);
    }

    @Test
    public void RackSelectorTestTwo() throws Exception {
        WarehouseHelper.configureWarehouse("data/dispatcher_test/3A_3R_2G.hive");
        Warehouse warehouse = Warehouse.getInstance();

        Gate gate1 = warehouse.getGateById(1);
        Gate gate2 = warehouse.getGateById(2);

        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);
        Item item3 = warehouse.getItemById(3);

        Order order1 = new CollectOrder(1, gate2);
        order1.add(item1, 10);
        order1.add(item2, 10);
        order1.add(item3, 10);
        warehouse.addOrder(order1);

        warehouse.run();

        assertEquals(warehouse.getRackById(1).getAllocatingAgent().getId(), 1);
        assertEquals(warehouse.getRackById(2).getAllocatingAgent().getId(), 2);
        assertEquals(warehouse.getRackById(3).getAllocatingAgent().getId(), 3);

        Order order2 = new CollectOrder(2, gate1);
        order2.add(item1, 10);
        order2.add(item2, 10);
        order2.add(item3, 10);
        warehouse.addOrder(order2);

        warehouse.run();
        // TODO @Samir55
//        assertEquals(warehouse.getAgentById(1).getActiveTask().orders.size(), 2);
//        assertEquals(warehouse.getAgentById(2).getActiveTask().orders.size(), 2);
//        assertEquals(warehouse.getAgentById(3).getActiveTask().orders.size(), 2);
    }
}
