package models.hive;

import models.components.SellItem;
import utils.Constants;


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
     * The selling item type associated with this rack.
     */
    private SellItem item;

    /**
     * The number of units stored in this rack of the associated selling item.
     */
    private int itemsUnitsCount;

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
     * @param id the id of the rack.
     */
    public Rack(int id) {
        super(id);
    }

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
     * Constructs a new rack of items.
     *
     * @param id             the id of the rack.
     * @param row            the row position of the rack.
     * @param col            the column position of the rack.
     * @param item           the id of the item stored in the rack.
     * @param itemUnitsCount the count of the item stored in the rack.
     */
    public Rack(int id, int row, int col, SellItem item, int itemUnitsCount) {
        this(id, row, col);
        setItem(item, itemUnitsCount);
    }

    /**
     * Returns the item stored in this rack.
     *
     * @return an {@code SellItem} object representing the associated item in this rack,
     * or {@code null} if the rack is empty.
     */
    public SellItem getStoredItem() {
        return this.item;
    }

    /**
     * Returns the count of the stored units in this rack of the associated item.
     *
     * @return an integer representing the count of the stored units.
     */
    public int getItemsUnitsCount() {
        return this.itemsUnitsCount;
    }

    /**
     * Set the associated item with this rack.
     *
     * @param item           the id of the item stored in the rack.
     * @param itemUnitsCount the count of the item stored in the rack.
     */
    public void setItem(SellItem item, int itemUnitsCount) {
        if (itemUnitsCount > 0) {
            this.item = item;
            this.itemsUnitsCount = itemUnitsCount;
        }
    }

    /**
     * Clears and empties the current rack from all its items.
     */
    public void clear() {
        this.item = null;
        this.itemsUnitsCount = 0;
    }

    /**
     * Returns the current stored weight in this rack.
     *
     * @return the current stored weight.
     */
    public int getStoredWeight() {
        return (item != null ? itemsUnitsCount * item.getWeight() : 0);
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
