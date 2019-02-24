package models;

import utils.Position;


public class HiveObject {

    //
    // Member Variables
    //

    protected int id;
    protected int row, col;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new Hive object.
     *
     * @param id the id of the Hive object.
     */
    public HiveObject(int id) {
        this.id = id;
    }

    /**
     * Constructs a new Hive object.
     *
     * @param id  the id of the Hive object.
     * @param row the row position of the Hive object.
     * @param id  the column position of the Hive object.
     */
    public HiveObject(int id, int row, int col) {
        this.id = id;
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the id of this Hive object.
     *
     * @return an integer unique id of this Hive object.
     */
    public int getId() {
        return this.id;
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
     * Returns the position of the position of the Hive object in the map's grid.
     *
     * @return a {@code Position} object holding the coordinates of this Hive object.
     */
    public Position getPosition() {
        return new Position(this.row, this.col);
    }
}
