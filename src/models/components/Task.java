package models.components;

import models.components.base.HiveObject;

import java.util.HashMap;
import java.util.Map;


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
     * The map of needed items to be picked from the below rack.
     */
    private Map<Item, Integer> items = new HashMap<>();

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
     */
    public Task() {
        super(getTaskId());
    }

    /**
     * Returns the associated order with this task.
     *
     * @return the associated order.
     */
    public Order getOrder() {
        return this.order;
    }

    /**
     * Returns the delivery gate of this task.
     *
     * @return the delivery gate.
     */
    public Gate getDeliveryGate() {
        return (order != null ? order.getDeliveryGate() : null);
    }

    /**
     * Assigns the associated order of this task.
     *
     * @param order the associated order.
     */
    public void assignOrder(Order order) {
        this.order = order;
    }

    /**
     * Returns the assigned agent with this task.
     *
     * @return the assigned agent.
     */
    public Agent getAgent() {
        return this.agent;
    }

    /**
     * Assigns an agent for this task.
     *
     * @param agent the assigned agent.
     */
    public void assignAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     * Returns the assigned rack with this task.
     *
     * @return the assigned rack.
     */
    public Rack getRack() {
        return this.rack;
    }

    /**
     * Assigns a destination rack for this task.
     *
     * @param rack the assigned rack.
     */
    public void assignRack(Rack rack) {
        this.rack = rack;
    }

    /**
     * Assigns the terminals of this task.
     *
     * @param agent the assigned agent.
     * @param rack  the assigned rack.
     */
    public void assignTerminals(Agent agent, Rack rack) {
        this.agent = agent;
        this.rack = rack;
    }

    /**
     * Returns the map of items needed for fulfilling this task.
     *
     * @return the map of items of this task.
     */
    public Map<Item, Integer> getItems() {
        return this.items;
    }

    /**
     * Adds a new needed item for this task.
     *
     * @param item     the new item.
     * @param quantity the needed quantity.
     */
    public void addItem(Item item, int quantity) {
        if (quantity > 0) {
            this.items.put(item, quantity + items.getOrDefault(item, 0));
        }
    }

    /**
     * Fills this task with the maximum number of items needed for the
     * associated order that are available in the assigned rack.
     */
    public void fillItems() {
        if (order == null || rack == null) {
            return;
        }

        Map<Item, Integer> m1, m2;

        if (order.getItems().size() < rack.getItems().size()) {
            m1 = order.getItems();
            m2 = rack.getItems();
        } else {
            m1 = rack.getItems();
            m2 = order.getItems();
        }

        clearItems();

        for (Item item : m1.keySet()) {
            int quantity = Math.min(m1.getOrDefault(item, 0), m2.getOrDefault(item, 0));
            this.items.put(item, quantity);
        }
    }

    /**
     * Clears this task from all its assigned items.
     */
    public void clearItems() {
        this.items.clear();
    }
}
