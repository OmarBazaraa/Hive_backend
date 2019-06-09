package models.tasks;

import models.facilities.Gate;
import models.items.Item;
import models.items.QuantityAddable;
import models.warehouses.Warehouse;

import server.ServerConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


/**
 * This {@code Order} class represents an order in our Hive Warehousing System.
 * <p>
 * An order is defined by a list of needed {@link Item Items}, and a {@link Gate}
 * where the {@code Order} must be delivered.
 *
 * TODO: enable re-filling orders
 * TODO: enable order scheduling
 *
 * @see Task
 * @see Item
 * @see Gate
 */
public class Order extends AbstractTask implements QuantityAddable<Item>, TaskAssignable {

    //
    // Enums
    //

    /**
     * Different status of an {@code Order} during its lifecycle.
     */
    public enum OrderStatus {
        INACTIVE,       // Inactive order, meaning that its item has not been reserved
        ACTIVE,         // Active order with all its items has been reserved
        FULFILLED       // The order has been completed
    }

    // ===============================================================================================
    //
    // Member Variables
    //

    /**
     * The {@code Gate} where this {@code Order} must be delivered.
     */
    private Gate deliveryGate;

    /**
     * The number of pending units this {@code Order} is needing.
     */
    private int pendingUnits;

    /**
     * The map of pending items this {@code Order} is needing.<p>
     * The key is an {@code Item}.<p>
     * The mapped value represents the needed quantity of this {@code Item}.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The set of sub tasks for fulfilling this {@code Order}.
     */
    private Set<Task> subTasks = new HashSet<>();

    /**
     * The current status of this {@code Order}.
     */
    private OrderStatus status = OrderStatus.INACTIVE;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * Creates a new {@code Order} object from JSON data.
     *
     * TODO: add checks and throw exceptions
     *
     * @param data the un-parsed JSON data.
     *
     * @return an {@code Order} object.
     */
    public static Order create(JSONObject data) throws Exception {
        int id = data.getInt(ServerConstants.MSG_KEY_ID);
        JSONArray itemsJSON = data.getJSONArray(ServerConstants.MSG_KEY_ITEMS);

        Order ret = new Order(id);

        for (int i = 0; i < itemsJSON.length(); ++i) {
            JSONObject itemJSON = itemsJSON.getJSONObject(i);

            int itemId = itemJSON.getInt(ServerConstants.MSG_KEY_ID);
            int quantity = itemJSON.getInt(ServerConstants.MSG_KEY_ITEM_QUANTITY);
            Item item = Warehouse.getInstance().getItemById(itemId);

            if (quantity < 0) {
                throw new Exception("Invalid quantity to add to the order!");
            }

            if (item == null) {
                throw new Exception("Invalid item to add to the order!");
            }

            ret.add(item, quantity);
        }

        return ret;
    }

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
     * Pending units are these units that are not assigned to a {@code Task} yet.
     *
     * @return the number of pending units.
     */
    public int getPendingUnits() {
        return pendingUnits;
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
     * <p>
     * This function should be called with positive quantities only during
     * the construction of the {@code Order} object and with negative quantities
     * once per {@code Task} assignment.
     *
     * @param item     the {@code Item} to be updated.
     * @param quantity the quantity to be updated with.
     */
    @Override
    public void add(Item item, int quantity) {
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
     * Checks whether this {@code Order} is feasible of being fulfilled regarding
     * its needed items quantities.
     *
     * TODO: check feasibility during order construction and remove this function
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
     * Checks whether this {@code Order} still has some pending items or not.
     *
     * @return {@code true} if this {@code Order} is still pending; {@code false} otherwise.
     */
    public boolean isPending() {
        return (pendingUnits > 0);
    }

    /**
     * Checks whether this {@code Order} is currently active or not.
     *
     * @return {@code true} if this {@code Order} is active; {@code false} otherwise.
     */
    @Override
    public boolean isActive() {
        return (status == OrderStatus.ACTIVE);
    }

    /**
     * Checks whether this {@code Order} is fulfilled or not.
     *
     * @return {@code true} if this {@code Order} is fulfilled; {@code false} otherwise.
     */
    @Override
    public boolean isFulfilled() {
        return (pendingUnits <= 0 && subTasks.isEmpty());
    }

    /**
     * Activates this {@code Order} by reserving all the needed units to avoid
     * accepting infeasible orders in the future.
     * <p>
     * This function should be called only once per {@code Order} object.
     */
    @Override
    public void activate() {
        // Reserve all the needed items by the order
        for (Item item : items.keySet()) {
            item.reserve(this);
        }

        // Activate the order
        status = OrderStatus.ACTIVE;
    }

    /**
     * Terminates this {@code Order} after completion.
     * <p>
     * A callback function to be invoked when this {@code Order} has been completed.
     * Used to clear and finalize allocated resources.
     *
     * TODO: add order statistics finalization
     */
    @Override
    protected void terminate() {
        status = OrderStatus.FULFILLED;
        super.terminate();
    }

    /**
     * Assigns a new sub {@code Task} for fulfilling this {@code Order}.
     * <p>
     * This function should be called once per {@code Task} object after activating the {@code Order}.
     *
     * @param task the new {@code Task} to assign.
     */
    @Override
    public void assignTask(Task task) {
        // Remove task items from the pending items of the order
        for (Map.Entry<Item, Integer> pair : task) {
            add(pair.getKey(), -pair.getValue());
        }

        // Add task to the set of sub tasks
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
            terminate();
        }
    }
}