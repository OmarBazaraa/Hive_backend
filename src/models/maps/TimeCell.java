package models.maps;

import models.agents.Agent;

import java.util.TreeMap;


/**
 * This {@code TimeCell} class represents the timeline of the passing {@link Agent agents}
 * through a cell in the {@link models.warehouses.Warehouse Warehouse}.
 * <p>
 * A time cell guides an {@link Agent} towards its associated target,
 * typically a {@link models.facilities.Facility Facility}.
 */
public class TimeCell extends Cell {

    //
    // Member Variables
    //

    /**
     * The timeline of passing agents in this {@code TimeCell}.
     */
    private TreeMap<Long, Agent> timeline = new TreeMap<>();

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * Allocates and initializes a 2D array of {@code TimeCell}.
     *
     * @param n the first dimension of the array.
     * @param m the second dimension of the array.
     *
     * @return the allocated array.
     */
    public static TimeCell[][] allocate2D(int n, int m) {
        TimeCell[][] ret = new TimeCell[n][m];

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                ret[i][j] = new TimeCell();
            }
        }

        return ret;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code TimeCell} with empty timeline.
     */
    public TimeCell() {

    }

    /**
     * Returns the {@code Agent} passing though this {@code TimeCell} at the given time.
     *
     * @return the {@code Agent} if exists; {@code null} otherwise.
     */
    public Agent getAgentAt(long time) {
        return timeline.get(time);
    }

    /**
     * Sets the {@code Agent} passing though this {@code TimeCell} at the given time.
     *
     * @param time the time of passing.
     * @param agent the passing {@code Agent}.
     */
    public void setAgentAt(long time, Agent agent) {
        timeline.put(time, agent);
    }

    /**
     * Clears this {@code TimeCell} and removes any agents from its timeline at the given time.
     *
     * @param time the time to clear.
     */
    public void clearAt(long time) {
        timeline.remove(time);
    }

    /**
     * Checks whether this {@code TimeCell} is empty from agents at the given time or not.
     *
     * @return {@code true} if the cell is empty; {@code false} otherwise.
     */
    public boolean isEmptyAt(long time) {
        return !timeline.containsKey(time);
    }

    /**
     * Checks whether this {@code TimeCell} is occupied by an {@code Agent} at the given time or not.
     *
     * @return {@code true} if the cell is occupied; {@code false} otherwise.
     */
    public boolean isOccupiedAt(long time) {
        return timeline.containsKey(time);
    }
}
