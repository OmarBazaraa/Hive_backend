package models.components;

import models.components.base.HiveObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This {@code Order} class represents an order in our Hive Warehousing System.
 * <p>
 * The order is defined by the list of the needed {@code Item}, and the gate where the
 * order must be delivered to.
 */
public class Order extends HiveObject {

    //
    // Member Variables
    //

    /**
     * The sum of all pending needed quantities for this order.
     */
    private int totalQuantity;

    /**
     * The map of needed items for this order.
     * The keys are the needed items.
     * The values are the corresponding needed quantities.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The gate where the order must be delivered to.
     */
    private Gate deliveryGate;

    /**
     * The set of sub tasks for fulfilling this order.
     */
    private Set<Task> subTasks;

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
     * All the given item quantities must be positive integers.
     *
     * @param id           the id of this order.
     * @param items        the map of needed items for fulfilling this order.
     * @param deliveryGate the gate where the order must be delivered to.
     */
    public Order(int id, Map<Item, Integer> items, Gate deliveryGate) {
        super(id);
        this.items = items;
        this.deliveryGate = deliveryGate;
        this.init();
    }

    /**
     * Returns the map of items needed for fulfilling this order.
     *
     * @return the map of items of this order.
     */
    public Map<Item, Integer> getItems() {
        return this.items;
    }

    /**
     * Adds a new needed item for fulfilling this order.
     *
     * @param item     the new item.
     * @param quantity the needed quantity.
     */
    public void addItem(Item item, int quantity) {
        if (quantity > 0) {
            this.totalQuantity += quantity;
            this.items.put(item, quantity + items.getOrDefault(item, 0));
        }
    }

    /**
     * Removes an item from the order.
     *
     * @param item     the item to be removed.
     * @param quantity the quantity to be removed.
     */
    public void removeItem(Item item, int quantity) {
        int cnt = items.getOrDefault(item, 0);
        int net = cnt - quantity;

        if (net > 0) {
            this.totalQuantity -= quantity;
            this.items.put(item, net);
        } else {
            this.totalQuantity -= cnt;
            this.items.remove(item);
        }
    }

    /**
     * Adds a new sub task for fulfilling this order.
     *
     * @param task the new sub task to be added.
     */
    public void addTask(Task task) {
        // Get the items of the given task
        Map<Item, Integer> taskItems = task.getItems();

        //
        // Iterate over all the items in the given task
        //
        for (Map.Entry<Item, Integer> pair : taskItems.entrySet()) {
            // Get the current item
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Remove the current item from the pending items of this order
            removeItem(item, quantity);

            // Remove the current item from the assigned rack,
            // to avoid multiple tasks allocate the same items
            task.getRack().removeItem(item, quantity);
        }

        // Finally, add the given task to the set of active tasks of this order
        this.subTasks.add(task);
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
    public int totalPendingQuantities() {
        return this.totalQuantity;
    }

    /**
     * Checks whether this order is still pending or semi pending.
     * That is, there is still some pending items to be assigned.
     *
     * @return {@code true} if this order is still pending, {@code false} otherwise.
     */
    public boolean isPending() {
        return (this.totalQuantity > 0);
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
            this.totalQuantity += pair.getValue();
        }
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    /**
     * Interface definition for a callback to be invoked when an order is fulfilled.
     */
    public interface OnFulFillListener {

        /**
         * Called when an order is fulfilled.
         *
         * @param order the fulfilled order.
         */
        public void onFulfill(Order order);
    }
}
