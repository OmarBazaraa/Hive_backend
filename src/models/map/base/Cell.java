package models.map.base;

import utils.Constants;


/**
 * This {@code Cell} class represents a grid cell used by {@link Grid} class.
 * <p>
 * This class is the parent class of {@link models.map.MapCell} and {@link models.map.GuideCell}.
 */
public class Cell {

    /**
     * Converts this {@code Cell} to a character symbol representing its shape.
     *
     * @return a character representing the shape of this cell.
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