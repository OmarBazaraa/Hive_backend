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
     * The maximum storing capacity (in weight units) of this rack.
     */
    private int maxCapacity = Constants.RACK_DEFAULT_STORE_CAPACITY;

    /**
     * The total weight of all the items stored in this rack.
     */
    private int storedWeight;

    /**
     * The map of all the items this rack is holding.
     */
    private Map<Item, Integer> items = new HashMap<>();

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
     * Returns the maximum storing capacity (in weight units) of this rack.
     *
     * @return the maximum storing capacity of this rack.
     */
    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    /**
     * Sets the maximum storing capacity (in weight units) of this rack.
     *
     * @param capacity the new maximum storing capacity.
     *
     * @throws Exception when the new capacity is less than the currently stored weight.
     */
    public void setMaxCapacity(int capacity) throws Exception {
        if (capacity < storedWeight) {
            throw new Exception("Unable to reduce the size of the rack!");
        }

        this.maxCapacity = capacity;
    }

    /**
     * Returns the current stored weight in this rack.
     *
     * @return the current stored weight.
     */
    public int getStoredWeight() {
        return this.storedWeight;
    }

    /**
     * Returns the quantity of the given item in this rack.
     *
     * @param item the item to get its quantity.
     *
     * @return the quantity of the given item.
     */
    public int getItemQuantity(Item item) {
        return this.items.getOrDefault(item, 0);
    }

    /**
     * Returns the map of items available in this rack.
     *
     * @return the map of items of this rack.
     */
    public Map<Item, Integer> getItems() {
        return this.items;
    }

    /**
     * Adds the given item into this rack.
     *
     * @param item     the id of the items stored in the rack.
     * @param quantity the number of copies to be added.
     *
     * @throws Exception when adding the new items results in exceeding the storing limits of this rack.
     */
    public void addItem(Item item, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        int weight = quantity * item.getWeight();

        if (storedWeight + weight > maxCapacity) {
            throw new Exception("The rack exceeded its storing capacity limits!");
        }

        storedWeight += weight;
        items.put(item, quantity + items.getOrDefault(item, 0));
        item.addToRack(this, quantity);
    }

    /**
     * Removes an item from this rack with the given quantity.
     *
     * @param item     the item to be taken.
     * @param quantity the quantity to be taken.
     *
     * @throws Exception when the given quantity is greater than the current quantity in the rack.
     */
    public void removeItem(Item item, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        int count = items.getOrDefault(item, 0);

        if (quantity > count) {
            throw new Exception("No enough items to remove from the rack!");
        }

        storedWeight -= quantity * item.getWeight();

        if (count > quantity) {
            items.put(item, count - quantity);
        } else {
            items.remove(item);
        }

        item.removeFromRack(this, quantity);
    }

    /**
     * Clears and empties the current rack from all its items.
     */
    public void clear() throws Exception {
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            removeItem(pair.getKey(), pair.getValue());
        }
    }
}
