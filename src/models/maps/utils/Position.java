package models.maps.utils;

import utils.Constants;
import utils.Constants.*;


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
     * Calculates the next position if moving forward along the given direction
     * from a this {@code Position}.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param dir the {@code Direction} to move along.
     *
     * @return the next {@code Position}.
     */
    public Position next(Direction dir) {
        int i = dir.ordinal();
        int r = row + Constants.DIR_ROW[i];
        int c = col + Constants.DIR_COL[i];
        return new Position(r, c);
    }

    /**
     * Calculates the previous position if moving backward along the given direction
     * from a this {@code Position}.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param dir the {@code Direction} to move along.
     *
     * @return the previous {@code Position}.
     */
    public Position prev(Direction dir) {
        int i = dir.ordinal();
        int r = row - Constants.DIR_ROW[i];
        int c = col - Constants.DIR_COL[i];
        return new Position(r, c);
    }

    /**
     * Checks whether the given {@code Position} is adjacent to this one or not.
     *
     * @param pos the {@code Position} to check against.
     *
     * @return {@code true} if both positions are adjacent; {@code false} otherwise.
     */
    public boolean isAdjacent(Position pos) {
        return getDistanceTo(pos) == 1;
    }

    /**
     * Calculates the manhattan distance between this {@code Position} and the given one.
     *
     * @param pos the other {@code Position}.
     *
     * @return the manhattan distance.
     */
    public int getDistanceTo(Position pos) {
        return Math.abs(row - pos.row) + Math.abs(col - pos.col);
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

    /**
     * Returns a string representation of this {@code Position}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Position}.
     */
    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
