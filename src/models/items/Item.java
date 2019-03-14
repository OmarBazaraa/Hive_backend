package models.items;

import models.Entity;
import models.facilities.Rack;
import models.orders.Order;
import models.tasks.Task;

import utils.Constants;
import utils.Constants.*;

import org.json.JSONObject;

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
    // Static Methods
    //

    /**
     * Creates a new {@code Item} object from JSON data.
     *
     * @param data the un-parsed JSON data.
     *
     * @return an {@code Item} object.
     */
    public static Item create(JSONObject data) throws Exception {
        int id = data.getInt(Constants.MSG_KEY_ID);
        int weight = data.getInt(Constants.MSG_KEY_WEIGHT);
        return new Item(id, weight);
    }

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
     * Sets the weight of one unit of this {@code Item}.
     *
     * @param w the weight to set.
     */
    public void setWeight(int w) {
        weight = w;
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
     * Updates the quantity of this {@code Item} in a {@code Rack}.
     * <p>
     * This function is used to add extra units of this {@code Item} if the given
     * quantity is positive,
     * and used to remove existing units if the given quantity is negative.
     *
     * TODO: make this function accessible only to rack class
     *
     * @param rack     the {@code Rack} to add into.
     * @param quantity the quantity to be updated with.
     */
    @Override
    public void add(Rack rack, int quantity) throws Exception {
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
     *
     * @param container the {@code QuantityAddable} container.
     */
    @Override
    public void reserve(QuantityAddable<Item> container) throws Exception {
        int reserveQuantity = container.get(this);

        if (reserveQuantity > getAvailableUnits()) {
            throw new Exception("No enough item units to be reserved!");
        }

        reservedUnits += reserveQuantity;
    }

    /**
     * Confirms the previously assigned reservations specified by the given
     * {@code QuantityAddable} container, and removes those reserved units from this object.
     *
     * @param container the {@code QuantityAddable} container.
     */
    @Override
    public void confirmReservation(QuantityAddable<Item> container) throws Exception {
        int reserveQuantity = container.get(this);

        if (reserveQuantity > reservedUnits) {
            throw new Exception("No enough item units to be reserved!");
        }

        reservedUnits -= reserveQuantity;
    }
}
