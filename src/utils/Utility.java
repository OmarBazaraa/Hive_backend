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
}
