package models;

import models.maps.utils.Position;


/**
 * This {@code HiveObject} class is the base class of all the basic components
 * in our Hive Warehouse System that exist in a {@link models.warehouses.Warehouse Warehouse}.
 * <p>
 * A {@code HiveObject} is represented by an id and a position in the warehouse grid.
 *
 * @see models.agents.Agent Agent
 * @see models.facilities.Facility Facility
 * @see models.facilities.Rack Rack
 * @see models.facilities.Gate Gate
 * @see models.facilities.Station Station
 */
abstract public class HiveObject extends Entity {

    //
    // Member Variables
    //

    /**
     * The position of an object in terms of row, column pairs.
     */
    protected int row, col;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * The number of objects in the system so far.
     */
    protected static int count = 0;

    /**
     * Returns the first available id for the next object and increments.
     *
     * @return the first available id.
     */
    protected static int getNextId() {
        return count++;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code HiveObject} with the given id.
     */
    public HiveObject() {
        super(getNextId());
    }

    /**
     * Constructs a new {@code HiveObject} with the given id.
     *
     * @param id the id of the {@code HiveObject}.
     */
    public HiveObject(int id) {
        super(id);
    }

    /**
     * Returns the row position of this {@code HiveObject} in the map's grid
     * of the {@code Warehouse}.
     *
     * @return the row position of this {@code HiveObject}.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column position of this {@code HiveObject} in the map's grid
     * of the {@code Warehouse}.
     *
     * @return the column position of this {@code HiveObject}.
     */
    public int getCol() {
        return col;
    }

    /**
     * Returns the position of this {@code HiveObject} in the map's grid
     * of the {@code Warehouse}.
     *
     * @return the {@code Position} of this {@code HiveObject}.
     */
    public Position getPosition() {
        return new Position(row, col);
    }

    /**
     * Sets the position of this {@code HiveObject} in the maps's grid
     * of the {@code Warehouse}.
     *
     * @param row the row position of this {@code HiveObject}.
     * @param col the column position of this {@code HiveObject}t.
     */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Sets the position of this {@code HiveObject} in the maps's grid
     * of the {@code Warehouse}.
     *
     * @param pos the {@code Position} of this {@code HiveObject}.
     */
    public void setPosition(Position pos) {
        this.row = pos.row;
        this.col = pos.col;
    }
}
