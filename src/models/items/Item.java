package models.items;

import models.Entity;
import models.facilities.Rack;
import models.tasks.Order;
import models.tasks.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This {@code Item} class represents a sell item in our Hive Warehouse System.
 * <p>
 * An item is typically stored in a {@link Rack}.
 * Different items can be stored in a single {@link Rack}, and a single item type can
 * be stored in multiple {@link Rack} objects.
 *
 * @see Rack
 * @see Order
 * @see Task
 */
public class Item extends Entity implements QuantityAddable<Rack>, QuantityReservable<Item> {

    //
    // Member Variables
    //

    /**
     * The weight of one unit of this {@code Item}.
     */
    private int weight;

    /**
     * The number of reserved units of this {@code Item} across the racks of a {@code Warehouse}.
     */
    private int reservedUnits;

    /**
     * The total number of units of this {@code Item} across the racks of a {@code Warehouse}.
     */
    private int totalUnits;

    /**
     * The map of racks storing this {@code Item}.<p>
     * The key is a {@code Rack}.<p>
     * The mapped value represents the quantity of this {@code Item}.
     */
    private Map<Rack, Integer> racks = new HashMap<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Item}.
     *
     * @param id     the id of this item.
     * @param weight the weight of this item.
     */
    public Item(int id, int weight) {
        super(id);
        this.weight = weight;
    }

    /**
     * Returns the weight of one unit of this {@code Item}.
     *
     * @return the weight of this {@code Item}.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Returns the number of reserved units of this {@code Item} across
     * the racks of a {@code Warehouse}.
     *
     * @return the number of reserved units.
     */
    public int getReservedUnits() {
        return reservedUnits;
    }

    /**
     * Returns the total number of units of this {@code Item} across
     * the racks of a {@code Warehouse}.
     *
     * @return the total number of units.
     */
    public int getTotalUnits() {
        return totalUnits;
    }

    /**
     * Returns the number of available units of this {@code Item} across
     * the racks of a {@code Warehouse}.
     * <p>
     * That is, the total number of units minus the number of reserved units.
     *
     * @return the number of available units.
     */
    public int getAvailableUnits() {
        return totalUnits - reservedUnits;
    }

    /**
     * Returns the current quantity of this {@code Item} in a {@code Rack}.
     *
     * @param rack the {@code Rack} to get its quantity.
     *
     * @return the quantity in the given {@code Rack}.
     */
    @Override
    public int get(Rack rack) {
        return racks.getOrDefault(rack, 0);
    }

    /**
     * Adds or removes some units of this {@code Item} into/from the given {@code Rack}.
     * <p>
     * This function is used to add extra units of this {@code Item} if the given
     * quantity is positive,
     * and used to remove existing units if the given quantity is negative.
     * <p>
     * This function should be called with appropriate parameters so that:
     * <ol>
     * <li>no {@code Rack} exceeds the maximum capacity limit</li>
     * <li>no {@code Item} has negative number of units</li>
     * </ol>
     * <p>
     * This function should only be called from {@link Rack}.
     *
     * @param rack     the {@code Rack} to add into.
     * @param quantity the quantity to be updated with.
     */
    @Override
    public void add(Rack rack, int quantity) {
        QuantityAddable.update(racks, rack, quantity);
        totalUnits += quantity;
    }

    /**
     * Returns an {@code Iterator} to iterate over all {@code Racks} storing this {@code Item}.
     * <p>
     * Note that this iterator should be used in read-only operations;
     * otherwise undefined behaviour could arises.
     *
     * @return an {@code Iterator}.
     */
    @Override
    public Iterator<Map.Entry<Rack, Integer>> iterator() {
        return racks.entrySet().iterator();
    }

    /**
     * Reserves some units of this {@code Item} specified by the given {@code QuantityAddable} container.
     * <p>
     * This function should be called after ensuring that there are enough units
     * for reservation.
     * <p>
     * This function should only be called once per {@code Order} activation.
     *
     * @param container the {@code QuantityAddable} container.
     *
     * @see Item#confirmReservation(QuantityAddable)
     */
    @Override
    public void reserve(QuantityAddable<Item> container) {
        int reservedQuantity = container.get(this);
        reservedUnits += reservedQuantity;
    }

    /**
     * Confirms the previously assigned reservations specified by the given
     * {@code QuantityAddable} container.
     * <p>
     * This function should be called after reserving a same or a super container first;
     * otherwise un-expected behaviour could occur.
     * <p>
     * This function should only be called once per {@code Task} termination.
     *
     * @param container the {@code QuantityAddable} container.
     *
     * @see Item#reserve(QuantityAddable)
     */
    @Override
    public void confirmReservation(QuantityAddable<Item> container) {
        int reservedQuantity = container.get(this);
        reservedUnits -= reservedQuantity;
    }

    /**
     * Returns a string representation of this {@code Item}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Item}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder
                .append("Item: {")
                .append(" id: ").append(id).append(",")
                .append(" weight: ").append(weight)
                .append(" }");

        return builder.toString();
    }
}
