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
     * The racks holding this sell item.
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
     * Returns the map of racks hold this sell item.
     *
     * @return a map of {@code Rack} holding this item, where the key is {@code Rack} and the value is the quantity.
     */
    public Map<Rack, Integer> getRacks() {
        return this.racks;
    }

    /**
     * Puts this sell item into the given rack.
     *
     * @param rack the new holding rack of this item.
     */
    public void addToRack(Rack rack, int quantity) {
        quantity += racks.getOrDefault(rack, 0);
        racks.put(rack, quantity);
    }
}
