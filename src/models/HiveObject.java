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
public class HiveObject extends Entity {

    //
    // Member Variables
    //

    /**
     * The position of an object in terms of row, column pairs.
     */
    protected int row, col;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code HiveObject}.
     *
     * @param id  the id of the {@code HiveObject}.
     * @param row the row position of the {@code HiveObject}.
     * @param col the column position of the {@code HiveObject}.
     */
    public HiveObject(int id, int row, int col) {
        super(id);
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the row position of this {@code HiveObject} in the map's grid
     * of the {@code Warehouse}.
     *
     * @return the row position of this {@code HiveObject}.
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Returns the column position of this {@code HiveObject} in the map's grid
     * of the {@code Warehouse}.
     *
     * @return the column position of this {@code HiveObject}.
     */
    public int getCol() {
        return this.col;
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
