package models.maps;


/**
 * This {@code Cell} class is the base class of all the grid cells used by
 * the {@link Grid} class.
 *
 * @see MapCell
 */
abstract public class Cell {

    /**
     * Converts this {@code Cell} to a character symbol representing its shape.
     *
     * @return a character representing the shape of this cell.
     */
    abstract public char toShape();
}