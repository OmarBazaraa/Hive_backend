package models.components.base;

import models.map.base.Position;


/**
 * This {@code TerminalHiveObject} class is the base class of all the basic terminal Hive System's components
 * such as {@code Agent}, {@code Rack}, {@code Gate}, {@code Station}, ..etc.
 */
public class TerminalHiveObject extends HiveObject {

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
     * Constructs a new Hive object.
     *
     * @param id  the id of the Hive object.
     * @param row the row position of the Hive object.
     * @param col the column position of the Hive object.
     */
    public TerminalHiveObject(int id, int row, int col) {
        super(id);
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the row position of the Hive object in the map's grid.
     *
     * @return an integer representing the row position of this Hive object.
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Returns the column position of the Hive object in the map's grid.
     *
     * @return an integer representing the column position of this Hive object.
     */
    public int getCol() {
        return this.col;
    }

    /**
     * Returns the position of the Hive object in the map's grid.
     *
     * @return a {@code Position} object holding the coordinates of this Hive object.
     */
    public Position getPosition() {
        return new Position(this.row, this.col);
    }

    /**
     * Sets the position of the Hive object in the map's grid.
     *
     * @param row the row position of the Hive object.
     * @param col the column position of the Hive object.
     */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Sets the position of the Hive object in the map's grid.
     *
     * @param pos the position of the Hive object.
     */
    public void setPosition(Position pos) {
        this.row = pos.row;
        this.col = pos.col;
    }
}
