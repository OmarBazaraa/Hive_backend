package models.components;

import models.components.base.HiveObject;
import utils.Constants.*;

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
     * The order in which this task is a part of.
     */
    private Order order;

    /**
     * The robot agent assigned for this task.
     */
    private Agent agent;

    /**
     * The rack needed to be delivered.
     */
    private Rack rack;

    /**
     * The map of needed items to be picked from the below rack.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * Flag to indicate whether this task has been activated or not.
     */
    private boolean activated = false;

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
        assignAgent(agent);
        assignRack(rack);
    }

    /**
     * Returns the quantity of the given item needed in this task.
     *
     * @param item the item to get its quantity.
     *
     * @return the quantity of the given item.
     */
    public int getItemQuantity(Item item) {
        return this.items.getOrDefault(item, 0);
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
     *
     * @throws Exception when passing non-positive quantity.
     */
    public void addItem(Item item, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        this.items.put(item, quantity + items.getOrDefault(item, 0));
    }

    /**
     * Fills this task with the maximum number of items needed by the
     * associated order that are available in the assigned rack.
     */
    public void fillItems() throws Exception {
        if (order == null || rack == null) {
            throw new Exception("No order and/or rack is assigned yet to the task!");
        }

        Map<Item, Integer> m1, m2;

        // Assign the smaller set of items to 'm1'
        if (order.getItems().size() < rack.getItems().size()) {
            m1 = order.getItems();
            m2 = rack.getItems();
        } else {
            m1 = rack.getItems();
            m2 = order.getItems();
        }

        clearItems();

        // Get the intersection between the items of the order and the items in the rack
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

    /**
     * Returns whether this task has been activated or not.
     *
     * @return {@code true} if this task has been activated, {@code false} otherwise.
     */
    public boolean isActivated() {
        return this.activated;
    }

    /**
     * Activates this task and allocates its assigned resources.
     */
    public void activate() throws Exception {
        // Task information must be complete
        if (order == null || agent == null || rack == null) {
            throw new Exception("No order, agent and/or order is assigned yet to the task!");
        }

        // Skip re-activating already activated tasks
        if (activated) {
            return;
        }

        // 0. Set task as activated
        activated = true;

        // 1. Remove the items from the pending item of the associated order

        // 2. Allocate the task items of the assigned rack

        // 3. Activate the assigned agent

        // 4. Allocate the assigned rack
    }
}
