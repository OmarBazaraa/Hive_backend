package models;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import models.agents.AgentTest;
import models.facilities.FacilityTest;
import models.facilities.GateTest;
import models.facilities.RackTest;
import models.facilities.StationTest;
import models.items.ItemTest;
import models.tasks.OrderTest;
import models.tasks.TaskTest;
import models.warehouses.WarehouseTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        AgentTest.class,
        FacilityTest.class, GateTest.class, RackTest.class, StationTest.class,
        ItemTest.class,
        TaskTest.class, OrderTest.class,
        WarehouseTest.class
})
public class ModelsTestRunner {
    @BeforeClass
    public static void before() {

    }

    @AfterClass
    public static void after() {

    }
}