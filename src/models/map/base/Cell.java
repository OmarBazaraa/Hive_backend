package models.map.base;

import utils.Constants;


/**
 * This {@code Cell} class represents a grid cell in our Hive System's map.
 */
public class Cell {

    /**
     * Converts this cell to a character symbol representing its shape.
     *
     * @return a {@code char} representing this cell's type.
     */
    public char toShape() {
        return Constants.SHAPE_CELL_UNKNOWN;
    }

    /**
     * Returns a string representation of object.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of object.
     */
    @Override
    public String toString() {
        return Character.toString(toShape());
    }
}