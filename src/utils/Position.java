package utils;

/**
 * This {@code Position} class hold the 2D coordinates of an object
 * in the map of the Hive system.
 * <p>
 * The position of an object is described by its row and column in the map's grid.
 */
public class Position implements Comparable<Position> {

    /**
     * The position of an object in terms of row, column pairs.
     */
    public int r, c;

    /**
     * Constructs a {@code Position} object with the given coordinates.
     *
     * @param r the row position of the object
     * @param c the column position of the object
     */
    public Position(int r, int c) {
        this.r = r;
        this.c = c;
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
        return (r == rhs.r && c == rhs.c);
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
        if (r == rhs.r) {
            return c - rhs.c;
        } else {
            return r - rhs.r;
        }
    }
}
