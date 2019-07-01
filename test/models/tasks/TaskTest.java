package models.tasks;

import models.agents.Agent;
import models.facilities.Rack;
import org.junit.BeforeClass;

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
