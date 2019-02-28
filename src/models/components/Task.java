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
     * The item needed to be delivered.
     */
    private Item item;

    /**
     * The delivery gate of this task.
     */
    private Gate gate;

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
     * @param item  the needed item.
     * @param gate  the delivery gate.
     * @param order the associated order.
     */
    public Task(Agent agent, Item item, Gate gate, Order order) {
        super(getTaskId());
        this.agent = agent;
        this.item = item;
        this.gate = gate;
        this.order = order;
    }
}
