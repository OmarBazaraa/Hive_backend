package utils;

import utils.Constants.*;


/**
 * This {@code Utility} class is a collection of a static utility functions
 * to be used across the entire project modules.
 */
public class Utility {

    /**
     * Returns the reverse (opposite) direction of the given one.
     *
     * @param dir the direction to get its reverse.
     *
     * @return the reverse direction.
     */
    public static Direction getReverseDir(Direction dir) {
        if (dir == Direction.STILL) {
            return dir;
        }

        int i = dir.ordinal();
        Direction[] dirs = Direction.values();
        return dirs[(i + 2) % 4];
    }

    /**
     * Converts an {@code AgentAction} into its corresponding {@code Direction}.
     *
     * @param action the {@code AgentAction} to convert.
     *
     * @return the corresponding {@code Direction}.
     */
    public static Direction actionToDir(AgentAction action) {
        int i = action.ordinal();

        if (i < 4) {
            return Direction.values()[i];
        } else {
            return Direction.STILL;
        }
    }

    /**
     * Converts the given {@code Direction} into its corresponding {@code AgentAction}.
     *
     * @param dir the {@code Direction} to convert.
     *
     * @return the corresponding {@code AgentAction}.
     */
    public static AgentAction dirToAction(Direction dir) {
        int i = dir.ordinal();

        if (i < 4) {
            return AgentAction.values()[i];
        } else {
            return AgentAction.NOTHING;
        }
    }
}
