package models.maps;


/**
 * This {@code GuideCell} class represents a guide cell used by {@link GuideGrid} class.
 * <p>
 * A guide cell guides an {@link models.agents.Agent Agent} towards its associated target,
 * typically a {@link models.facilities.Facility Facility}.
 */
public class GuideCell extends Cell {

    //
    // Member Variables
    //

    /**
     * The guide distance to reach the associated target.
     */
    private int distance = Integer.MAX_VALUE;

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
     * @param dis the guide distance to set.
     */
    public void setDistance(int dis) {
        distance = dis;
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
