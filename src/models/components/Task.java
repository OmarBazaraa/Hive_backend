package models.components;

import models.components.base.HiveObject;


/**
 * This {@code Task} class represents a basic task for our robot agents
 * in our Hive Warehousing System.
 */
public class Task extends HiveObject {

    //
    // Member Variables
    //

    /**
     * The robot agent assigned for this task.
     */
    private Agent agent;

    /**
     * The rack needed to be delivered.
     */
    private Rack rack;

    /**
     * The order in which this task is a part of.
     */
    private Order order;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * The number of tasks in the system so far.
     */
    private static int tasksCount = 0;

    /**
     * Returns the next available id for the next task.
     *
     * @return the next available id.
     */
    private static int getTaskId() {
        return tasksCount++;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new task.
     *
     * @param agent the assigned agent.
     * @param rack  the needed rack to be delivered.
     * @param order the associated order.
     */
    public Task(Agent agent, Rack rack, Order order) {
        super(getTaskId());
        this.agent = agent;
        this.rack = rack;
        this.order = order;
    }
}
