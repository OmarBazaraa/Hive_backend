package models.orders;

import models.Entity;
import models.facilities.Gate;
import models.items.Item;
import models.items.QuantityAddable;
import models.tasks.Task;
import models.tasks.TaskAssignable;

import utils.Constants.*;

import java.util.*;


/**
 * This {@code Order} class represents an order in our Hive Warehousing System.
 * <p>
 * An order is defined by a list of needed {@link Item Items}, and a {@link Gate}
 * where the {@code Order} must be delivered.
 *
 * @see Task
 * @see Item
 * @see Gate
 */
public class Order extends Entity implements QuantityAddable<Item>, TaskAssignable {

    //
    // Member Variables
    //

    /**
     * The {@code Gate} where this {@code Order} must be delivered.
     */
    private Gate deliveryGate;

    /**
     * The number of pending units needed by this {@code Order}.
     */
    private int pendingUnits;

    /**
     * The map of items this {@code Order} is needing.<p>
     * The key is an {@code Item}.<p>
     * The mapped value represents the needed quantity of this {@code Item}.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The current status of this {@code Order}.
     */
    private OrderStatus status = OrderStatus.INACTIVE;

    /**
     * The set of sub tasks for fulfilling this {@code Order}.
     */
    private Set<Task> subTasks = new HashSet<>();

    /**
     * The listener to be invoked when this {@code Order} has been fulfilled.
     */
    private OnFulFillListener fulFillListener;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Order} object.
     *
     * @param id the id of the {@code Order}.
     */
    public Order(int id) {
        super(id);
    }

    /**
     * Returns the {@code Gate} where this {@code Order} must be delivered.
     *
     * @return the delivery {@code Gate} of this {@code Order}.
     */
    public Gate getDeliveryGate() {
        return deliveryGate;
    }

    /**
     * Sets the {@code Gate} where this {@code Order} must be delivered.
     *
     * @param gate the delivery {@code Gate} of this {@code Order}.
     */
    public void setDeliveryGate(Gate gate) {
        deliveryGate = gate;
    }

    /**
     * Returns the total number of pending units needed by this {@code Order}.
     * <p>
     * Pending units are these units that are not assigned to be delivered yet.
     *
     * @return the number of pending units.
     */
    public int getPendingUnits() {
        return pendingUnits;
    }

    /**
     * Checks whether this {@code Order} still has some pending items or not.
     *
     * @return {@code true} if this {@code Order} is still pending; {@code false} otherwise.
     */
    public boolean isPending() {
        return (pendingUnits > 0);
    }

    /**
     * Checks whether this {@code Order} is fulfilled or not.
     *
     * @return {@code true} if this {@code Order} is fulfilled; {@code false} otherwise.
     */
    public boolean isFulfilled() {
        return (pendingUnits <= 0 && subTasks.isEmpty());
    }

    /**
     * Checks whether this {@code Order} is feasible of being fulfilled regarding
     * its needed items quantities.
     *
     * @return {@code true} if this {@code Order} is feasible; {@code false} otherwise.
     */
    public boolean isFeasible() {
        //
        // Iterate over every needed item in the order
        //
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            // Get needed item and its quantity
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // If needed quantity is greater than the overall available units
            // then this order is infeasible
            if (quantity > item.getAvailableUnits()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the pending quantity of the an {@code Item} needed by this {@code Order}.
     *
     * @param item the needed {@code Item}.
     *
     * @return the pending quantity of the given {@code Item}.
     */
    @Override
    public int get(Item item) {
        return items.getOrDefault(item, 0);
    }

    /**
     * Updates the quantity of an {@code Item} in this {@code Order}.
     * <p>
     * This function is used to add extra units of the given {@code Item} if the given
     * quantity is positive,
     * and used to remove existing units if the given quantity is negative.
     *
     * TODO: prevent adding item after activation the order
     * TODO: prevent adding/removing from outside this class
     *
     * @param item     the {@code Item} to be updated.
     * @param quantity the quantity to be updated with.
     */
    @Override
    public void add(Item item, int quantity) throws Exception {
        QuantityAddable.update(items, item, quantity);
        pendingUnits += quantity;
    }

    /**
     * Returns an {@code Iterator} to iterate over the pending needed items in this {@code Order}.
     * <p>
     * Note that this iterator should be used in read-only operations;
     * otherwise undefined behaviour could arises.
     *
     * @return an {@code Iterator}.
     */
    @Override
    public Iterator<Map.Entry<Item, Integer>> iterator() {
        return items.entrySet().iterator();
    }

    /**
     * Returns the current status of this {@code Order}.
     *
     * @return the {@code OrderStatus} of this {@code Order}.
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Activates this {@code Order} by reserving all the needed units of this to avoid
     * accepting infeasible orders in the future.
     */
    public void activate() throws Exception {
        // Skip re-activating already activated orders
        if (status != OrderStatus.INACTIVE) {
            return;
        }

        // Reserve all the needed items by the order
        for (Item item : items.keySet()) {
            item.reserve(this);
        }

        // Activate the order
        status = OrderStatus.ACTIVE;
    }

    /**
     * Assigns a new sub {@code Task} for fulfilling this {@code Order}.
     *
     * @param task the new {@code Task} to assign.
     */
    @Override
    public void assignTask(Task task) throws Exception {
        if (status != OrderStatus.ACTIVE) {
            throw new Exception("The order is not activated yet!");
        }

        // Remove task items from the pending items of the order
        for (Map.Entry<Item, Integer> pair : task) {
            add(pair.getKey(), -pair.getValue());
        }

        // Add task to the list of sub tasks
        subTasks.add(task);
    }

    /**
     * A callback function to be invoked when a sub {@code Task} of this {@code Order} is completed.
     *
     * @param task the completed {@code Task}.
     */
    @Override
    public void onTaskComplete(Task task) {
        subTasks.remove(task);

        if (isFulfilled()) {
            status = OrderStatus.FULFILLED;

            if (fulFillListener != null) {
                fulFillListener.onOrderFulfill(this);
            }
        }
    }

    /**
     * Sets the listener to be invoked when this {@code Order} is fulfilled.
     *
     * @param listener the {@code OnFulFillListener} object.
     */
    public void setOnFulfillListener(OnFulFillListener listener) {
        fulFillListener = listener;
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
