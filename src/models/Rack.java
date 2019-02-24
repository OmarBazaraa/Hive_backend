package models;


/**
 * This {@code Rack} class is a model for rack of items in our Hive System.
 */
public class Rack extends HiveObject {

    //
    // Member Variables
    //

    private int itemId;
    private int itemCount;

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
     * @param id        the id of the rack.
     * @param row       the row position of the rack.
     * @param col       the column position of the rack.
     * @param itemId    the id of the item stored in the rack.
     * @param itemCount the count of the item stored in the rack.
     */
    public Rack(int id, int row, int col, int itemId, int itemCount) {
        this(id, row, col);
        this.itemId = itemId;
        this.itemCount = itemCount;
    }

    /**
     * Returns the id of the item stored in this rack.
     *
     * @return an integer representing the id of the stored item.
     */
    public int getItemId() {
        return this.itemId;
    }

    /**
     * Returns the count of the item stored in this rack.
     *
     * @return an integer representing the count of the stored item.
     */
    public int getItemCount() {
        return this.itemCount;
    }
}
