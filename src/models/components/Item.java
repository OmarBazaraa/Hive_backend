package models.components;

import models.components.base.HiveObject;

import java.util.HashMap;
import java.util.Map;


/**
 * This {@code Item} class represents a sell item in our Hive Warehousing System.
 */
public class Item extends HiveObject {

    //
    // Member Variables
    //

    /**
     * The weight of one unit of this sell item.
     */
    private int weight;

    /**
     * The total reserved quantity of this item across the warehouse's racks.
     */
    private int reservedQuantity;

    /**
     * The total available quantity of this item across the warehouse's racks.
     */
    private int totalQuantity;

    /**
     * The racks holding this sell item.
     * The keys are the racks,
     * The values are the corresponding quantities of this item in the racks.
     */
    private Map<Rack, Integer> racks = new HashMap<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new sell item.
     *
     * @param id     the id of this item.
     * @param weight the weight of this item.
     */
    public Item(int id, int weight) {
        super(id);
        this.weight = weight;
    }

    /**
     * Returns the weight of this sell item.
     *
     * @return an integer value representing the weight of this item.
     */
    public int getWeight() {
        return this.weight;
    }

    /**
     * Sets the weight of one unit of this item.
     *
     * @param weight the weight of this item.
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Returns the reserved quantity of this item across the warehouse's racks.
     *
     * @return an integer value representing the reserved quantity of this item.
     */
    public int getReservedQuantity() {
        return this.reservedQuantity;
    }

    /**
     * Returns the total available quantity of this item across the warehouse's racks.
     *
     * @return an integer value representing the total quantity of this item.
     */
    public int getTotalQuantity() {
        return this.totalQuantity;
    }

    /**
     * Returns the available quantity of this item across the warehouse's racks.
     * That is, the total quantity minus the reserved quantity.
     *
     * @return an integer value representing the available quantity of this item.
     */
    public int getAvailableQuantity() {
        return this.totalQuantity - this.reservedQuantity;
    }

    /**
     * Returns the quantity of this item in the given rack.
     *
     * @param rack the rack.
     *
     * @return the quantity of this item in the given rack.
     */
    public int getQuantityInRack(Rack rack) {
        return this.racks.getOrDefault(rack, 0);
    }

    /**
     * Returns the map of racks hold this sell item.
     *
     * @return a map of {@code Rack} holding this item, where the key is {@code Rack} and the value is the quantity.
     */
    public Map<Rack, Integer> getRacks() {
        return this.racks;
    }

    /**
     * Puts this item into the given rack.
     *
     * @param rack     the rack to add into.
     * @param quantity the quantity to be added.
     *
     * @throws Exception when passing non-positive quantity.
     */
    public void addToRack(Rack rack, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        this.totalQuantity += quantity;
        this.racks.put(rack, quantity + this.racks.getOrDefault(rack, 0));
    }

    /**
     * Removes the given quantity of this item from the given rack.
     *
     * @param rack     the rack of this item to remove from.
     * @param quantity the quantity to be taken.
     *
     * @throws Exception when the given quantity is greater than the current quantity in the rack.
     */
    public void removeFromRack(Rack rack, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        int count = racks.getOrDefault(rack, 0);

        if (quantity > count) {
            throw new Exception("No enough items to remove from the rack!");
        }

        // TODO:
        release(quantity);

        totalQuantity -= quantity;

        if (count > quantity) {
            racks.put(rack, count - quantity);
        } else {
            racks.remove(rack);
        }
    }

    /**
     * Reserves a quantity of units of this item across the warehouse's racks.
     * This function just marks a number of available item quantity as reserved
     * to avoid accepting infeasible orders.
     *
     * @param quantity the quantity to reserve.
     *
     * @throws Exception when trying to reserve more quantity than currently available.
     */
    public void reserve(int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        if (quantity > getAvailableQuantity()) {
            throw new Exception("No enough item quantity to reserve!");
        }

        this.reservedQuantity += quantity;
    }

    /**
     * Releases a number of reserved units of this item across the warehouse's racks.
     *
     * @param quantity the quantity to release.
     *
     * @throws Exception when trying to release more quantity than currently reserved.
     */
    public void release(int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        if (quantity > getReservedQuantity()) {
            throw new Exception("No enough item quantity to allocate!");
        }

        this.reservedQuantity -= quantity;
    }
}
