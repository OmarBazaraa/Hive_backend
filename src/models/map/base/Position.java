package models.map.base;

/**
 * This {@code Position} class holds the coordinates of an object in a 2D grid.
 * <p>
 * The position of an object is described by its row and column in the grid.
 */
public class Position implements Comparable<Position> {

    /**
     * The position of an object in terms of row, column pairs.
     */
    public int row, col;

    /**
     * Constructs a {@code Position} object with the given coordinates.
     *
     * @param row the row position of the object.
     * @param col the column position of the object.
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
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
        if (!(obj instanceof Position)) {
            return false;
        }
        // Cast, then compare coordinates
        Position rhs = (Position) obj;
        return (row == rhs.row && col == rhs.col);
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
    public int compareTo(Position rhs) {
        if (row == rhs.row) {
            return col - rhs.col;
        } else {
            return row - rhs.row;
        }
    }
}
