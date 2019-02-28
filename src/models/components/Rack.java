package models.components;

import models.components.base.DstHiveObject;
import utils.Constants;
import utils.Constants.*;

import java.util.HashMap;
import java.util.Map;


/**
 * This {@code Rack} class is a model for rack of items in our Hive System.
 * <p>
 * A rack is a position in the warehouse grid map where selling items are stored.
 * Each rack can hold at most one unique type of items.
 */
public class Rack extends DstHiveObject {

    //
    // Member Variables
    //

    /**
     * The current status of this rack.
     */
    private RackStatus status;

    /**
     * The map of all the items this rack is holding.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The total weight of all the items stored in this rack.
     */
    private int storedWeight;

    /**
     * The maximum storing capacity (in weight units) of this rack.
     */
    private int maxCapacity = Constants.RACK_DEFAULT_STORE_CAPACITY;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new rack of items.
     *
     * @param id  the id of the rack.
     * @param row the row position of the rack.
     * @param col the column position of the rack.
     */
    public Rack(int id, int row, int col) {
        super(id, row, col);
    }

    /**
     * Returns the current status of this rack.
     *
     * @return an {@code RackStatus} value representing the current status of the rack.
     */
    public RackStatus getStatus() {
        return this.status;
    }

    /**
     * Sets a new status to this rack.
     *
     * @param status the new status to set.
     */
    public void setStatus(RackStatus status) {
        this.status = status;
    }

    /**
     * Adds the given item into this rack.
     *
     * @param item     the id of the items stored in the rack.
     * @param quantity the number of copies to be added.
     *
     * @return {@code true} if the item with the given quantity is added successfully, {@code false} otherwise.
     */
    public boolean addItem(Item item, int quantity) {
        int weight = quantity * item.getWeight();

        if (0 < quantity && storedWeight + weight <= maxCapacity) {
            items.put(item, quantity + items.getOrDefault(item, 0));
            storedWeight += weight;
            item.addToRack(this, quantity);
            return true;
        }

        return false;
    }

    /**
     * Takes an item from this rack with the given quantity.
     * <p>
     * If the given quantity is greater than the quantity of the item stored in this rack,
     * then nothing will be taken.
     *
     * @param item     the item to be taken.
     * @param quantity the quantity to be taken.
     *
     * @return {@code true} if the item with the given quantity is taken successfully, {@code false} otherwise.
     */
    public boolean takeItem(Item item, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        int count = items.getOrDefault(item, 0);
        int net = count - quantity;

        if (net < 0) {
            return false;
        }

        if (net > 0) {
            items.put(item, net);
        } else {
            items.remove(item);
        }

        storedWeight -= quantity * item.getWeight();
        item.takeFromRack(this, quantity);
        return true;
    }

    /**
     * Returns the quantity of the given item this rack is storing.
     *
     * @param item the item to get its quantity.
     *
     * @return the quantity of the given item.
     */
    public int getItemQuantity(Item item) {
        return this.items.getOrDefault(item, 0);
    }

    /**
     * Clears and empties the current rack from all its items.
     */
    public void clear() {
        this.items = new HashMap<>();
        this.storedWeight = 0;
    }

    /**
     * Returns the current stored weight in this rack.
     *
     * @return the current stored weight.
     */
    public int getStoredWeight() {
        return storedWeight;
    }

    /**
     * Returns the maximum storing capacity (in weight units) of this rack.
     *
     * @return the maximum storing capacity of this rack.
     */
    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    /**
     * Sets the maximum storing capacity (in weight units) of this rack.
     * If the new maximum capacity is less than the current stored weight in the rack then
     * the rack is cleared first.
     *
     * @param capacity the new maximum storing capacity.
     */
    public void setMaxCapacity(int capacity) {
        if (capacity < getStoredWeight()) {
            clear();
        }

        this.maxCapacity = capacity;
    }
}
