package models.tasks;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.orders.CollectOrder;
import models.tasks.orders.Order;
import models.warehouse.WarehouseHelper;
import models.warehouses.Warehouse;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TaskTest extends Task {

    /**
     * Constructs a new {@code Task} object.
     *
     * @param agent the assigned {@code Agent}.
     * @param rack  the assigned {@code Rack}.
     */
    public TaskTest(Agent agent, Rack rack) {
        super(agent, rack);
    }

    /**
     * Initializes the required objects.
     */
    @BeforeClass
    public static void before() {

    }
}
