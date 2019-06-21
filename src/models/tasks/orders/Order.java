package models.tasks.orders;

import communicators.frontend.FrontendCommunicator;

import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.items.QuantityAddable;
import models.tasks.AbstractTask;
import models.tasks.Task;
import models.tasks.TaskAssignable;
import models.warehouses.Warehouse;

import java.util.*;


/**
 * This {@code Order} class represents an order in our Hive Warehousing System.
 * <p>
 * An order is defined by a list of {@link Item Items}, and a {@link Gate}
 * where the {@code Order} must be delivered.
 * <p>
 * An order can be of one of the following types:
 * <p>
 * 1. Collect: where items are taken from the {@link Warehouse} to a {@link Gate}.
 * 2. Refill: where items are taken from a {@link Gate} into the {@link Warehouse}.
 *
 * @see Task
 * @see Item
 * @see Gate
 * @see CollectOrder
 * @see RefillOrder
 */
abstract public class Order extends AbstractTask implements QuantityAddable<Item>, TaskAssignable {

    //
    // Member Variables
    //

    /**
     * The {@code Gate} where this {@code Order} must be delivered.
     */
    protected Gate deliveryGate;

    /**
     * The number of pending units this {@code Order} is needing.
     */
    protected int pendingUnits;

    /**
     * The map of pending items this {@code Order} is needing.<p>
     * The key is an {@code Item}.<p>
     * The mapped value represents the needed quantity of this {@code Item}.
     */
    protected Map<Item, Integer> pendingItems = new HashMap<>();

    /**
     * The map of reserved items for this {@code Order} by each {@code Task}.<p>
     * The key is a {@code Task}.<p>
     * The mapped value represents a map of the reserved items by this {@code Order}.
     */
    protected Map<Task, Map<Item, Integer>> reservedItems = new HashMap<>();

    /**
     * The set of sub tasks for fulfilling this {@code Order}.
     */
    protected Set<Task> subTasks = new HashSet<>();

    /**
     * The time when this {@code Order} has been received.
     */
    protected long timeReceived = -1;

    /**
     * The time when this {@code Order} has been issued.
     */
    protected long timeStarted = -1;

    /**
     * The time when this {@code Order} has been completed.
     */
    protected long timeCompleted = -1;

    /**
     * The listener object to this {@code Order} events.
     */
    protected OrderListener listener;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Order} object.
     *
     * @param id   the id of the {@code Order}.
     * @param gate the delivery {@code Gate} of the {@code Order}.
     */
    public Order(int id, Gate gate) {
        super(id);
        this.deliveryGate = gate;
        this.timeReceived = Warehouse.getInstance().getTime();
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
     * Returns the number of pending units of this {@code Order} to be delivered
     * to the {@code Gate}. A negative number represents a refill order, where these units
     * should be taken from the {@code Gate} to the racks of the {@code Warehouse}.
     * <p>
     * Pending units are these units that are not assigned to a {@code Task} yet.
     *
     * @return the number of pending units.
     */
    public int getPendingUnits() {
        return pendingUnits;
    }

    /**
     * Checks whether this {@code Order} still has some pending items or not.
     * <p>
     * An {@code Order} is considered pending if there exist some needed units
     * not assigned to a {@code Task} yet.
     *
     * @return {@code true} if this {@code Order} is still pending; {@code false} otherwise.
     */
    public boolean isPending() {
        return (pendingUnits != 0);
    }

    /**
     * Registers a callback functions to be invoked when this {@code Order} produces any events.
     *
     * @param l the callback to run; {@code null} to unregister any listeners.
     */
    public void setListener(OrderListener l) {
        listener = l;
    }

    /**
     * Returns the pending quantity of the an {@code Item} of this {@code Order}.
     * A negative quantity represents a refill order, where these quantity
     * should be taken from the {@code Gate} to the racks of the {@code Warehouse}.
     *
     * @param item the needed {@code Item}.
     *
     * @return the pending quantity of the given {@code Item}.
     */
    @Override
    public int get(Item item) {
        return pendingItems.getOrDefault(item, 0);
    }

    /**
     * Updates the quantity of an {@code Item} in this {@code Order}.
     * <p>
     * This function is used to add extra units of the given {@code Item}
     * in the {@code Order} if the given quantity is positive,
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
        QuantityAddable.update(pendingItems, item, quantity);
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
        return pendingItems.entrySet().iterator();
    }

    /**
     * Terminates this {@code Order} after completion.
     * <p>
     * A callback function to be invoked when this {@code Order} has been completed.
     * Used to clear and finalize allocated resources.
     */
    @Override
    protected void terminate() {
        // TODO: add order statistics finalization
        super.terminate();
    }

    /**
     * Activates this {@code Order} by reserving all the needed units to avoid
     * accepting infeasible orders in the future.
     * <p>
     * This function should be called only once per {@code Order} object.
     */
    @Override
    public void activate() {
        reserveItems();
        super.activate();
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
        // Inform listener
        if (listener != null) {
            listener.onOrderTaskAssigned(this, task);
        }

        // Inform the frontend
        FrontendCommunicator.getInstance().enqueueTaskAssignedLog(task, this);

        // Add task to the order
        planItemsToReserve(task);
        reserveItemsInRack(task);
        subTasks.add(task);

        // Check if this is the first assigned task
        if (timeStarted == -1) {
            timeStarted = Warehouse.getInstance().getTime();

            // Inform listener
            if (listener != null) {
                listener.onOrderStarted(this);
            }
        }
    }

    /**
     * A callback function to be invoked when a sub {@code Task} of this {@code Order} is completed.
     *
     * @param task the completed {@code Task}.
     */
    @Override
    public void onTaskComplete(Task task) {
        // Inform listener
        if (listener != null) {
            listener.onOrderTaskCompleted(this, task);
        }

        // Inform the frontend
        FrontendCommunicator.getInstance().enqueueTaskCompletedLog(task, this, reservedItems.get(task));

        // Finalize completed task
        acquireReservedItemsInRack(task);
        subTasks.remove(task);

        // Check if no more pending units and all running tasks have been completed
        if (pendingUnits == 0 && subTasks.isEmpty()) {
            timeCompleted = Warehouse.getInstance().getTime();

            // Inform listener
            if (listener != null) {
                listener.onOrderFulfilled(this);
            }

            // Inform the frontend
            FrontendCommunicator.getInstance().enqueueOrderFulfilledLog(this);

            terminate();
        }
    }

    /**
     * Plans the set of items to reserve for the favor of this {@code Order}
     * by the given {@code Task}.
     *
     * @param task the {@code Task} responsible for carrying out the reservation.
     */
    abstract protected void planItemsToReserve(Task task);

    /**
     * Reserves the number of units in the {@code Warehouse} so as not to
     * accept infeasible orders in the future.
     * <p>
     * This function just reserve the number of units to add or remove,
     * not specific units in specific racks.
     */
    protected void reserveItems() {
        for (var pair : pendingItems.entrySet()) {
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // General reservation of needed item units
            item.reserve(quantity);
        }
    }

    /**
     * Reserves some of the items in the {@code Rack} specified by the
     * given {@code Task} for the favor of this {@code Order}, and removes
     * them from the {@code Order} pending items.
     */
    protected void reserveItemsInRack(Task task) {
        Rack rack = task.getRack();
        Map<Item, Integer> items = reservedItems.get(task);

        for (var pair : items.entrySet()) {
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Specific the reservation of the current item
            item.reserve(-quantity);            // Confirm previous general reservation of item units
            rack.reserve(item, quantity);       // Reserve items in the current rack

            // Remove those reserved items of this order
            QuantityAddable.update(pendingItems, item, -quantity);
            pendingUnits -= quantity;
        }
    }

    /**
     * Acquires the previously Reserves items in the {@code Rack} specified by the
     * given {@code Task} for the favor of this {@code Order}.
     */
    protected void acquireReservedItemsInRack(Task task) {
        Rack rack = task.getRack();
        Map<Item, Integer> items = reservedItems.get(task);

        for (var pair : items.entrySet()) {
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Acquire the current item from the rack
            rack.reserve(item, -quantity);      // Confirm previous specific reservation of item units in the rack
            rack.add(item, -quantity);          // Remove items from the rack
        }
    }
}
