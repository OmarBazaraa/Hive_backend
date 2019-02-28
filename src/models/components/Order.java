package models.components;

import models.hive.Gate;

import java.util.List;


/**
 * This {@code Order} class represents an order in our Hive Warehousing System.
 * <p>
 * The order is defined by the list of the needed {@code Item}, and the gate where the
 * order must be delivered to.
 */
public class Order {

    //
    // Member Variables
    //

    /**
     * The id of this order.
     */
    private int id;

    /**
     * The list of needed items for this order.
     */
    private List<Item> items;

    /**
     * The gate where the order must be delivered to.
     */
    private Gate deliveryGate;

    /**
     * The list of sub tasks for fulfilling this order.
     */
    private List<Task> subTasks;

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
     * @param id           the id of this order.
     * @param items        the list of needed items for fulfilling this order.
     * @param deliveryGate the gate where the order must be delivered to.
     */
    public Order(int id, List<Item> items, Gate deliveryGate) {
        this.id = id;
        this.items = items;
        this.deliveryGate = deliveryGate;
    }

    /**
     * Returns the id of this order.
     *
     * @return an integer unique id of this order.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Returns the list of items needed for fulfilling this order.
     *
     * @return the list of items of this order.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Returns the gate where the order must be delivered to.
     *
     * @return the delivery gate of this order.
     */
    public Gate getDeliveryGate() {
        return deliveryGate;
    }

    /**
     * Sets the listener to be invoked when this order has been fulfilled.
     *
     * @param listener the {@code OnFulFillListener} object.
     */
    public void setOnFulfillListener(OnFulFillListener listener) {
        this.fulFillListener = listener;
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
