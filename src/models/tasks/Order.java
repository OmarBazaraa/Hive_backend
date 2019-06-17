package models.tasks;

import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.items.QuantityAddable;
import models.warehouses.Warehouse;
import server.Server;

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
public class Order extends AbstractTask implements QuantityAddable<Item>, TaskAssignable {

    //
    // Enums
    //

    /**
     * Different {@code Order} types.
     */
    public enum OrderType {
        COLLECT,
        REFILL
    }

    // ===============================================================================================
    //
    // Member Variables
    //

    /**
     * The type of this {@code Order}.
     */
    private OrderType type;

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
     * The time when this {@code Order} has been received.
     */
    private long timeReceived = -1;

    /**
     * The time when this {@code Order} has been issued.
     */
    private long timeIssued = -1;

    /**
     * The time when this {@code Order} has been completed.
     */
    private long timeCompleted = -1;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Order} object.
     *
     * @param id           the id of the {@code Order}.
     * @param type         the type of the {@code Order}.
     * @param deliveryGate the delivery {@code Gate} of the {@code Order}.
     */
    public Order(int id, OrderType type, Gate deliveryGate) {
        super(id);
        this.type = type;
        this.deliveryGate = deliveryGate;
        this.timeReceived = Warehouse.getInstance().getTime();
    }

    /**
     * Returns the type of this {@code Order}. Either collect or refill order type.
     *
     * @return the type of this {@code Order}.
     */
    public OrderType getType() {
        return type;
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
     *
     * @return {@code true} if this {@code Order} is still pending; {@code false} otherwise.
     */
    public boolean isPending() {
        return (pendingUnits != 0);
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
        return items.getOrDefault(item, 0);
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
        if (type == OrderType.REFILL) {
            quantity -= quantity;
        }

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
     * <p>
     * TODO: check agent to rack reach-ability
     * TODO: check agents availability
     * TODO: check REFILL order feasibility
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
     * Assigns a new sub {@code Task} for fulfilling this {@code Order}.
     * <p>
     * This function should be called once per {@code Task} object after activating the {@code Order}.
     *
     * @param task the new {@code Task} to assign.
     */
    @Override
    public void assignTask(Task task) {
        // Inform the frontend
        Server.getInstance().enqueueTaskAssignedLog(task, this);

        // Add task to the set of sub tasks
        reserveItemsInRack(task);
        subTasks.add(task);

        // Check if this is the first assigned task
        if (timeIssued == -1) {
            timeIssued = Warehouse.getInstance().getTime();
            Server.getInstance().enqueueOrderIssuedLog(this);
        }
    }

    /**
     * A callback function to be invoked when a sub {@code Task} of this {@code Order} is completed.
     *
     * @param task the completed {@code Task}.
     */
    @Override
    public void onTaskComplete(Task task) {
        // Inform the frontend
        Server.getInstance().enqueueTaskCompletedLog(task, this);

        // Remove completed task
        acquireReservedItemsInRack(task);
        subTasks.remove(task);

        // Check if no more pending units and all running tasks have been completed
        if (pendingUnits == 0 && subTasks.isEmpty()) {
            timeCompleted = Warehouse.getInstance().getTime();
            Server.getInstance().enqueueOrderFulfilledLog(this);
            terminate();
        }
    }

    /**
     * Reserves the needed number of units in the {@code Warehouse} so as not to
     * accept infeasible orders in the future.
     * <p>
     * This function just reserve the needed number of units, not specific units
     * in specific racks.
     */
    private void reserveItems() {
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
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
    private void reserveItemsInRack(Task task) {
        Rack rack = task.getRack();
        Map<Item, Integer> reservedItems = task.getReservedItems(this);

        for (Map.Entry<Item, Integer> pair : reservedItems.entrySet()) {
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Specific the reservation of the current item
            item.reserve(-quantity);            // Confirm previous general reservation of item units
            rack.reserve(item, quantity);       // Reserve items in the current rack

            // Remove those reserved items of this order
            add(item, -quantity);
        }
    }

    /**
     * Acquires the previously Reserves items in the {@code Rack} specified by the
     * given {@code Task} for the favor of this {@code Order}.
     */
    private void acquireReservedItemsInRack(Task task) {
        Rack rack = task.getRack();
        Map<Item, Integer> reservedItems = task.getReservedItems(this);

        for (Map.Entry<Item, Integer> pair : reservedItems.entrySet()) {
            int quantity = pair.getValue();
            Item item = pair.getKey();

            // Acquire the current item from the rack
            rack.reserve(item, -quantity);      // Confirm previous specific reservation of item units in the rack
            rack.add(item, -quantity);          // Remove items from the rack
        }
    }

    /**
     * Returns a string representation of this {@code Order}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Order}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder
                .append("Order: {")
                .append(" id: ").append(id).append(",")
                .append(" gate_id: ").append(deliveryGate.getId()).append(",")
                .append(" items: ").append(items.size())
                .append(" }");

        return builder.toString();
    }
}
