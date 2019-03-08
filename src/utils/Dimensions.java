package utils;

/**
 * This {@code Dimensions} class holds the dimensions of a 2D grid.
 * <p>
 * The dimensions of a grid is described by its number of rows and number of columns.
 */
public class Dimensions implements Comparable<Dimensions> {

    /**
     * The dimensions in terms of the number of rows and columns.
     */
    public int rows, cols;

    /**
     * Constructs a {@code Dimensions} object with the given dimensions.
     *
     * @param rows the number of rows.
     * @param cols the number of columns.
     */
    public Dimensions(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Indicates whether some other object is equal to this one.
     *
     * @param obj the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        // Not the same object type
        if (!(obj instanceof Dimensions)) {
            return false;
        }
        // Cast, then compare coordinates
        Dimensions rhs = (Dimensions) obj;
        return (rows == rhs.rows && cols == rhs.cols);
    }

    /**
     * Compares whether some other object is less than, equal to, or greater than this one.
     *
     * @param rhs the reference object with which to compare.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Dimensions rhs) {
        if (rows == rhs.rows) {
            return cols - rhs.cols;
        } else {
            return rows - rhs.rows;
        }
    }
}
