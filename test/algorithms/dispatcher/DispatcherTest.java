package algorithms.dispatcher;

import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.orders.CollectOrder;
import models.tasks.orders.Order;
import models.warehouses.WarehouseHelper;
import models.warehouses.Warehouse;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class DispatcherTest {

    /**
     * Initializes the required objects.
     */
    @BeforeClass
    public static void before() throws Exception {

    }

    @Test
    public void RackSelectorTest1() throws Exception {
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
    public void RackSelectorTest2() throws Exception {
        // Adding new tasks to the currently allocated racks
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

    @Test
    public void RackSelectorTest3() throws Exception {
        // Unfulfilled order yet due to lack of racks
        WarehouseHelper.configureWarehouse("data/dispatcher_test/2A_3R_2G.hive");
        Warehouse warehouse = Warehouse.getInstance();

        Gate gate1 = warehouse.getGateById(1);
        Gate gate2 = warehouse.getGateById(2);

        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);
        Item item3 = warehouse.getItemById(3);

        Order order1 = new CollectOrder(1, gate2);
        order1.add(item1, 10);
        order1.add(item2, 10);
        warehouse.addOrder(order1);

        warehouse.run();

        Order order2 = new CollectOrder(2, gate1);
        order2.add(item3, 5);
        warehouse.addOrder(order2);

        warehouse.run();

        assertEquals(warehouse.getOrderById(2).getPendingUnits(), 5);
    }

    @Test
    public void RackSelectorTest4() throws Exception {
        // Notice It's a different map from the previous test
        // Unfulfilled order yet due to no agents carrying racks of needed items even if there
        // is a single unallocated rack having this item.
        WarehouseHelper.configureWarehouse("data/dispatcher_test/2A_3R_2G_T.hive");
        Warehouse warehouse = Warehouse.getInstance();

        Gate gate1 = warehouse.getGateById(1);
        Gate gate2 = warehouse.getGateById(2);

        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);
        Item item3 = warehouse.getItemById(3);

        Order order1 = new CollectOrder(1, gate2);
        order1.add(item1, 10);
        order1.add(item2, 10);
        warehouse.addOrder(order1);

        warehouse.run();

        Order order2 = new CollectOrder(2, gate1);
        order2.add(item3, 5);
        warehouse.addOrder(order2);

        warehouse.run();

        assertEquals(warehouse.getOrderById(2).getPendingUnits(), 5);

        warehouse.getRackById(2).add(item3, 5);

        warehouse.run();

        // Unfulfilled order yet as the agent carrying rack of needed items cannot load more than
        // the load capacity of 100.
        assertEquals(warehouse.getOrderById(2).getPendingUnits(), 5);

        // TODO @Samir be able to change the load capacity to test order fulfilling.
    }

    @Test
    public void RackSelectorTest5() throws Exception {
        // Only subset of an order is satisfied due to obstacles in the warehouse map.
        WarehouseHelper.configureWarehouse("data/dispatcher_test/4A_4R_2G.hive");
        Warehouse warehouse = Warehouse.getInstance();

        Gate gate1 = warehouse.getGateById(1);
        Gate gate2 = warehouse.getGateById(2);

        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);
        Item item3 = warehouse.getItemById(3);

        Order order1 = new CollectOrder(1, gate1);
        order1.add(item1, 10);
        warehouse.addOrder(order1);

        warehouse.run();

        assertEquals(warehouse.getRackById(1).getAllocatingAgent().getId(), 1);

        // Test physically impossible to fulfill this order.
        Order order2 = new CollectOrder(2, gate1);
        order2.add(item3, 10);
        warehouse.addOrder(order2);

        warehouse.run();

        // TODO @Samir remove order if it is dismissed many times.
        assertTrue(warehouse.getOrderById(2).isPending());
        assertFalse(warehouse.getRackById(3).isAllocated());
    }


    @Test
    public void RackSelectorTest6() throws Exception {
        // Only subset of an order is satisfied due to obstacles in the warehouse map.
        WarehouseHelper.configureWarehouse("data/dispatcher_test/4A_4R_2G.hive");
        Warehouse warehouse = Warehouse.getInstance();

        Gate gate1 = warehouse.getGateById(1);
        Gate gate2 = warehouse.getGateById(2);

        Item item1 = warehouse.getItemById(1);
        Item item2 = warehouse.getItemById(2);
        Item item3 = warehouse.getItemById(3);

        Order order1 = new CollectOrder(1, gate1);
        order1.add(item1, 11);
        order1.add(item2, 11);
        warehouse.addOrder(order1);

        warehouse.run();

        assertEquals(warehouse.getRackById(1).getAllocatingAgent().getId(), 1);
        assertNull(warehouse.getRackById(2).getAllocatingAgent());
        assertEquals(warehouse.getRackById(4).getAllocatingAgent().getId(), 2);
    }
}