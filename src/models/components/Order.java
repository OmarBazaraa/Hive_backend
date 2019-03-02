package models.components;

import models.components.base.HiveObject;

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
     * The map of needed items for this order.
     * The keys are the needed items.
     * The values are the corresponding needed quantities.
     */
    private Map<Item, Integer> items;

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
     * @param id           the id of this order.
     * @param items        the map of needed items for fulfilling this order.
     * @param deliveryGate the gate where the order must be delivered to.
     */
    public Order(int id, Map<Item, Integer> items, Gate deliveryGate) {
        super(id);
        this.items = items;
        this.deliveryGate = deliveryGate;
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
