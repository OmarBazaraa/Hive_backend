package models.maps;

import models.facilities.Facility;


/**
 * This {@code GuideCell} class represents a guide cell used by {@link GuideGrid} class.
 * <p>
 * A guide cell guides an {@code agent} towards its associated target,
 * typically a {@link Facility}.
 */
public class GuideCell extends Cell {

    //
    // Member Variables
    //

    /**
     * The guide distance to reach the associated target.
     */
    public int distance;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * Allocates and initializes a 2D array of {@code GuideCell}.
     *
     * @param n the first dimension of the array.
     * @param m the second dimension of the array.
     *
     * @return the allocated array.
     */
    public static GuideCell[][] allocate2D(int n, int m) {
        GuideCell[][] ret = new GuideCell[n][m];

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                ret[i][j] = new GuideCell();
            }
        }

        return ret;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code GuideCell} with default guide distance.
     */
    public GuideCell() {
        this.distance = Integer.MAX_VALUE;
    }

    /**
     * Constructs a new {@code GuideCell} with the given guide distance.
     *
     * @param distance the distance to reach the target.
     */
    public GuideCell(int distance) {
        this.distance = distance;
    }

    /**
     * Returns the guide distance of this {@code GuideCell} to reach the associated target.
     *
     * @return the guide distance to reach the target.
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Sets the guide distance of this {@code GuideCell} to reach the associated target.
     *
     * @param distance the distance to reach the target.
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * Checks whether the target is reachable from this {@code GuideCell} and vice versa.
     *
     * @return {@code true} if the target is reachable; {@code false} otherwise.
     */
    public boolean isReachable() {
        return (distance < Integer.MAX_VALUE);
    }

    /**
     * Checks whether the target is unreachable from this {@code GuideCell} and vice versa.
     *
     * @return {@code true} if the target is unreachable; {@code false} otherwise.
     */
    public boolean isUnreachable() {
        return (distance == Integer.MAX_VALUE);
    }
}
