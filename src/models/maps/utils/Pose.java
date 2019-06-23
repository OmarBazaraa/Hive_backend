package models.maps.utils;

import utils.Constants;
import utils.Constants.*;
import utils.Utility;


/**
 * This {@code Pose} class holds the coordinates and direction of an object in a 2D grid.
 * <p>
 * The pose of an object is described by its row, column, and orientation in the grid.
 */
public class Pose extends Position {

    /**
     * The orientation of an object.
     */
    public Direction dir;

    /**
     * Constructs a {@code Pose} object with the given coordinates and orientation.
     *
     * @param row the row position of the object.
     * @param col the column position of the object.
     * @param dir the orientation of the object.
     */
    public Pose(int row, int col, Direction dir) {
        super(row, col);
        this.dir = dir;
    }

    /**
     * Calculates the next pose if applying the given action.
     *
     * @param action the action to apply.
     *
     * @return the next {@code Pose}.
     */
    public Pose next(AgentAction action) {
        if (action == AgentAction.MOVE) {
            int i = dir.ordinal();
            int r = row + Constants.DIR_ROW[i];
            int c = col + Constants.DIR_COL[i];
            return new Pose(r, c, dir);
        }

        if (action == AgentAction.RETREAT) {
            return new Pose(row, col, Utility.getReverseDir(dir));
        }

        if (action == AgentAction.ROTATE_RIGHT || action == AgentAction.ROTATE_LEFT) {
            return new Pose(row, col, Utility.nextDir(dir, action));
        }

        return new Pose(row, col, dir);
    }

    /**
     * Calculates the previous pose if applying the given action in reverse manner.
     *
     * @param action the action to apply in reverse.
     *
     * @return the previous {@code Pose}.
     */
    public Pose previous(AgentAction action) {
        if (action == AgentAction.MOVE) {
            int i = dir.ordinal();
            int r = row - Constants.DIR_ROW[i];
            int c = col - Constants.DIR_COL[i];
            return new Pose(r, c, dir);
        }

        if (action == AgentAction.RETREAT) {
            return new Pose(row, col, Utility.getReverseDir(dir));
        }

        if (action == AgentAction.ROTATE_RIGHT || action == AgentAction.ROTATE_LEFT) {
            return new Pose(row, col, Utility.prevDir(dir, action));
        }

        return new Pose(row, col, dir);
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
        if (!(obj instanceof Pose)) {
            return false;
        }
        // Cast, then compare coordinates
        Pose rhs = (Pose) obj;
        return (row == rhs.row && col == rhs.col && dir == rhs.dir);
    }

    /**
     * Returns a string representation of this {@code Pose}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Pose}.
     */
    @Override
    public String toString() {
        return "(" + row + ", " + col + ", " + dir + ")";
    }
}
