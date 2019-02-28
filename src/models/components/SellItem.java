package models.components;


import models.hive.Rack;

/**
 * This {@code SellItem} class represent a sell item in our Hive Warehousing System.
 */
public class SellItem {

    //
    // Member Variables
    //

    /**
     * The id of this sell item.
     */
    private int id;

    /**
     * The weight of one unit of this sell item.
     */
    private int weight;

    /**
     * The rack holding this sell item.
     */
    private Rack rack;

    //
    // Member Methods
    //

    /**
     * Constructs a new sell item.
     *
     * @param id     the id of this item.
     * @param weight the weight of this item.
     * @param rack   the rack holding this item.
     */
    public SellItem(int id, int weight, Rack rack) {
        this.id = id;
        this.weight = weight;
        this.rack = rack;
    }

    /**
     * Returns the id of this sell item.
     *
     * @return an integer unique id of this item.
     */
    public int getId() {
        return this.id;
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
     * Returns the rack hold this sell item.
     *
     * @return the {@code Rack} holding this item, or {@code null} if no holding rack is assigned yet.
     */
    public Rack getRack() {
        return this.rack;
    }

    /**
     * Puts this sell item into the given rack.
     *
     * @param rack the new holding rack of this item.
     */
    public void setRack(Rack rack) {
        this.rack = rack;
    }
}
