package models.tasks.orders;

import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.Task;
import models.warehouses.Warehouse;

import utils.Utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This {@code RefillOrder} class represents an order of refill type in our Hive Warehousing System.
 * <p>
 * A refill order is an {@link Order} used to refill a specific {@link Rack}
 * in the {@link Warehouse} with a set of {@link Item Items} delivered from a specified {@link Gate}.
 *
 * @see Task
 * @see Item
 * @see Gate
 * @see Rack
 * @see Order
 * @see CollectOrder
 */
public class RefillOrder extends Order {

    //
    // Member Variables
    //

    /**
     * The {@code Rack} where this {@code RefillOrder} must be refill.
     */
    private Rack refillRack;

    /**
     * The total weight added to the associated {@code Rack} after refilling.
     */
    private int addedWeight;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code RefillOrder} object.
     *
     * @param id   the id of the {@code Order}.
     * @param gate the delivery {@code Gate} of the {@code Order}.
     * @param rack the {@code Rack} to refill.
     */
    public RefillOrder(int id, Gate gate, Rack rack) {
        super(id, gate);
        this.refillRack = rack;
    }

    /**
     * Returns the total weight added to the associated {@code Rack} after refilling.
     *
     * @return the added weight.
     */
    public int getAddedWeight() {
        return addedWeight;
    }

    /**
     * Returns the {@code Rack} where this {@code RefillOrder} must be refill.
     *
     * @return the refill {@code Rack} of this {@code RefillOrder}.
     */
    public Rack getRefillRack() {
        return refillRack;
    }

    /**
     * Returns the set of candidate racks that can supply this {@code Order}.
     *
     * @return a set of all candidate racks.
     */
    @Override
    public Set<Rack> getCandidateRacks() {
        Set<Rack> ret = new HashSet<>(1);
        ret.add(refillRack);
        return ret;
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
        super.add(item, -quantity);
        addedWeight += item.getWeight() * quantity;
    }

    /**
     * Plans the set of items to reserve for the favor of this {@code Order}
     * by the given {@code Task}.
     * <p>
     * In case of refill order, a single {@code Task} will be issued to fulfill
     * the order.
     * <p>
     * No infeasible {@code RefillOrder} should be accepted or activated.
     * That is, a {@code RefillOrder} with a {@code Rack} out of space should not
     * be activated.
     *
     * @param task the {@code Task} responsible for carrying out the reservation.
     */
    @Override
    protected void planItemsToReserve(Task task) {
        Map<Item, Integer> items = new HashMap<>();

        for (var pair : pendingItems.entrySet()) {
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Just plan all the pending units to be reserved to be refilled
            // as only a single task with feasible rack will be issued for a refill order
            if (quantity != 0) {
                items.put(item, quantity);
            }
        }

        reservedItems.put(task, items);
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

        builder.append("RefillOrder: {");
        builder.append(" id: ").append(id).append(",");
        builder.append(" gate_id: ").append(deliveryGate.getId()).append(",");
        builder.append(" rack_id: ").append(refillRack.getId()).append(",");
        builder.append(" items: ").append(Utility.stringifyItemQuantities(pendingItems));
        builder.append(" }");

        return builder.toString();
    }
}
