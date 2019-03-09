package models.components;

import utils.Entity;
import models.facilities.Gate;
import models.tasks.Task;

import java.util.*;


/**
 * This {@code Order} class represents an order in our Hive Warehousing System.
 * <p>
 * The order is defined by the list of the needed {@code Item}, and the gate where the
 * order must be delivered to.
 */
public class Order extends Entity {

    //
    // Member Variables
    //

    /**
     * The gate where the order must be delivered to.
     */
    private Gate deliveryGate;

    /**
     * The sum of all pending needed quantities for this order.
     */
    private int pendingQuantities;

    /**
     * The maps of needed items for this order.
     * The keys are the needed items.
     * The values are the corresponding needed quantities.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The setDistance of sub tasks for fulfilling this order.
     */
    private Set<Task> subTasks = new HashSet<>();

    /**
     * The listener to be invoked when this order has been fulfilled.
     */
    private OnFulFillListener fulFillListener;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new order.
     *
     * @param id the id of this order.
     */
    public Order(int id) {
        super(id);
    }

    /**
     * Constructs a new order.
     *
     * @param id           the id of this order.
     * @param deliveryGate the gate where the order must be delivered to.
     */
    public Order(int id, Gate deliveryGate) {
        super(id);
        this.deliveryGate = deliveryGate;
    }

    /**
     * Constructs a new order.
     * All the given item quantities must be positive integers.
     *
     * @param id           the id of this order.
     * @param deliveryGate the gate where the order must be delivered to.
     * @param items        the maps of needed items for fulfilling this order.
     */
    public Order(int id, Gate deliveryGate, Map<Item, Integer> items) {
        super(id);
        this.deliveryGate = deliveryGate;
        this.items = items;
        this.init();
    }

    /**
     * Returns the gate where the order must be delivered to.
     *
     * @return the delivery gate of this order.
     */
    public Gate getDeliveryGate() {
        return this.deliveryGate;
    }

    /**
     * Sets the gate where this order must be delivered to.
     *
     * @param deliveryGate the delivery gate of this order.
     */
    public void setDeliveryGate(Gate deliveryGate) {
        this.deliveryGate = deliveryGate;
    }

    /**
     * Returns the total number of quantities of the pending items needed for this order.
     *
     * @return the total pending quantities needed for this order.
     */
    public int getTotalPendingQuantities() {
        return this.pendingQuantities;
    }

    /**
     * Checks whether this order is still pending or semi pending.
     * That is, there is still some pending items to be assigned.
     *
     * @return {@code true} if this order is still pending; {@code false} otherwise.
     */
    public boolean isPending() {
        return (this.pendingQuantities > 0);
    }

    /**
     * Checks whether this order is fulfilled or not.
     *
     * @return {@code true} if this order is fulfilled; {@code false} otherwise.
     */
    public boolean isFulfilled() {
        return (this.pendingQuantities <= 0 && this.subTasks.isEmpty());
    }

    /**
     * Checks whether this order is feasible of being fulfilled regarding
     * its items and quantities.
     *
     * @return {@code true} if the given order is feasible; {@code false} otherwise.
     */
    public boolean isFeasible() {
        //
        // Iterate over every item in the order
        //
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            // Get needed item and its quantity
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // If needed quantity is greater than the overall available units
            // then this order is infeasible
            if (quantity > item.getAvailableQuantity()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Reserves all the needed units of this order to avoid accepting infeasible order in the future.
     */
    public void reserve() throws Exception {
        //
        // Iterate over every item in the order
        //
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            // Get needed item and its quantity
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Reserve the needed quantity of this item
            item.reserve(quantity);
        }
    }

    /**
     * Returns the first pending item in this order.
     *
     * @return the first item in this order, or {@code null} if no more pending items.
     */
    public Map.Entry<Item, Integer> getFirstItem() {
        return (this.items.isEmpty() ? null : this.items.entrySet().iterator().next());
    }

    /**
     * Returns the needed pending quantity of the given item in this order.
     *
     * @param item the needed item.
     *
     * @return the quantity of the given item.
     */
    public int getItemQuantity(Item item) {
        return this.items.getOrDefault(item, 0);
    }

    /**
     * Returns the maps of items needed for fulfilling this order.
     *
     * @return the maps of items of this order.
     */
    public Map<Item, Integer> getItems() {
        return this.items;
    }

    /**
     * Adds a new needed item for fulfilling this order.
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

        this.pendingQuantities += quantity;
        this.items.put(item, quantity + items.getOrDefault(item, 0));
    }

    /**
     * Removes an item from the order.
     *
     * @param item     the item to be removed.
     * @param quantity the quantity to be removed.
     *
     * @throws Exception when the given quantity is greater than the current quantity in the order.
     */
    public void removeItem(Item item, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        int count = items.getOrDefault(item, 0);

        if (quantity > count) {
            throw new Exception("No enough items to remove from the order!");
        }

        pendingQuantities -= quantity;

        if (count > quantity) {
            items.put(item, count - quantity);
        } else {
            items.remove(item);
        }
    }

    /**
     * Adds a new sub task for fulfilling this order.
     *
     * @param task the new sub task to be added.
     */
    public void addTask(Task task) {
        this.subTasks.add(task);
    }

    /**
     * A callback function to be invoked when a sub-task of this order has been completed.
     *
     * @param task the task that has been complete.
     */
    public void onTaskCompleted(Task task) {
        this.subTasks.remove(task);

        if (isFulfilled()) {
            if (fulFillListener != null) {
                fulFillListener.onOrderFulfill(this);
            }
        }
    }

    /**
     * Sets the listener to be invoked when this order has been fulfilled.
     *
     * @param listener the {@code OnFulFillListener} object.
     */
    public void setOnFulfillListener(OnFulFillListener listener) {
        this.fulFillListener = listener;
    }

    /**
     * Initializes and pre-computes some required values.
     */
    private void init() {
        // Iterate over all the needed items to compute the sum of all the quantities
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            this.pendingQuantities += pair.getValue();
        }
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    /**
     * Interface definition for a callback to be invoked when an {@link Order} is fulfilled.
     */
    public interface OnFulFillListener {

        /**
         * Called when an {@code Order} has been fulfilled.
         *
         * @param order the fulfilled {@code Order}.
         */
        void onOrderFulfill(Order order);
    }
}
