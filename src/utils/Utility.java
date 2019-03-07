package utils;

import utils.Constants.*;


/**
 * This {@code Utility} class is a collection of utility functions to be used across the entire project modules.
 * <p>
 * Note that all methods in this class must be static methods.
 */
public class Utility {

    /**
     * Returns the reverse direction of the given one.
     *
     * @param dir the direction to get its reverse.
     *
     * @return the reverse direction.
     */
    public static Direction getReverseDirection(Direction dir) {
        if (dir == Direction.STILL) {
            return dir;
        }
        int i = dir.ordinal();
        Direction dirs[] = Direction.values();
        return dirs[(i + 2) % 4];
    }

    /**
     * Converts the given agent action into its corresponding direction.
     *
     * @param action the agent action.
     *
     * @return the corresponding agent {@code Direction}.
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
     * Converts the given agent direction into its corresponding action.
     *
     * @param dir the agent direction.
     *
     * @return the corresponding agent {@code AgentAction}.
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
