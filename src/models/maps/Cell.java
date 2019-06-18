package models.maps;

import models.agents.Agent;
import models.facilities.Facility;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;

import models.warehouses.Warehouse;
import utils.Constants;
import utils.Constants.*;
import utils.Utility;

import java.util.TreeMap;


/**
 * This {@code Cell} class represents a cell of the grid of the {@link Warehouse}.
 * <p>
 * A cell in the {@link Warehouse} grid may be empty, may hold an {@link Agent} and/or
 * {@link Facility}, or may be an obstacle cell.
 */
public class Cell {

    //
    // Member Variables
    //

    /**
     * The type of this {@code Cell}.
     */
    private CellType type = CellType.EMPTY;

    /**
     * The {@code Facility} in this {@code Cell} if exists; {@code null} otherwise.
     */
    private Facility facility;

    /**
     * The {@code Agent} in this {@code Cell} if exists; {@code null} otherwise.
     */
    private Agent agent;

    /**
     * The timeline of the scheduled agents in this {@code Cell}.
     */
    private TreeMap<Long, Agent> timeline = new TreeMap<>();

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
    public static Cell[][] allocate2D(int n, int m) {
        Cell[][] ret = new Cell[n][m];

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                ret[i][j] = new Cell();
            }
        }

        return ret;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new empty {@code Cell}.
     */
    public Cell() {

    }

    /**
     * Returns the type of this {@code Cell}.
     *
     * @return the {@code CellType} of this {@code Cell}.
     */
    public CellType getType() {
        return type;
    }

    /**
     * Returns the existing {@code Facility} in this {@code Cell}.
     *
     * @return the {@code Facility} in this {@code Cell} if exists; {@code null} otherwise.
     */
    public Facility getFacility() {
        return facility;
    }

    /**
     * Checks whether this {@code Cell} has an existing {@code Facility} in it or not.
     *
     * @return {@code true} if this cell contains a {@code Facility}; {@code false} otherwise.
     */
    public boolean hasFacility() {
        return (facility != null);
    }

    /**
     * Sets the existing {@code Facility} in this {@code Cell}.
     * <p>
     * Note that the cell type and the facilities object type should be consistent in the following manner:
     * <ul>
     * <li>{@link CellType#EMPTY} type with {@code null} object.</li>
     * <li>{@link CellType#OBSTACLE} type with {@code null} object.</li>
     * <li>{@link CellType#RACK} type with {@link Rack} object.</li>
     * <li>{@link CellType#GATE} type with {@link Gate} object.</li>
     * <li>{@link CellType#STATION} type with {@link Station} object.</li>
     * <li>{@link CellType#UNKNOWN} type with {@code null} object.</li>
     * </ul>
     *
     * @param type     the {@code CellType} of {@code Facility}.
     * @param facility the {@code Facility} to set.
     */
    public void setFacility(CellType type, Facility facility) {
        this.type = type;
        this.facility = facility;
    }

    /**
     * Returns the existing {@code Agent} in this {@code Cell}.
     *
     * @return the {@code Agent} in this {@code Cell} if exists; {@code null} otherwise.
     */
    public Agent getAgent() {
        return agent;
    }

    /**
     * Checks whether this {@code Cell} has an existing {@code Agent} in it or not.
     *
     * @return {@code true} if this cell contains an {@code Agent}; {@code false} otherwise.
     */
    public boolean hasAgent() {
        return (agent != null);
    }

    /**
     * Sets the existing {@code Agent} in this {@code Cell}.
     *
     * @param agent the {@code Agent} to set.
     */
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     * Checks whether this {@code Cell} is statically empty or not.
     * <p>
     * A cell is considered statically empty if it is empty or it is currently
     * occupied by an {@code Agent}.
     *
     * @return {@code true} if this cell is statically empty; {@code false} otherwise.
     */
    public boolean isEmpty() {
        return (type == CellType.EMPTY);
    }

    /**
     * Checks whether this {@code Cell} is an obstacle or not.
     *
     * @return {@code true} if this cell is an obstacle; {@code false} otherwise.
     */
    public boolean isObstacle() {
        return (type == CellType.OBSTACLE);
    }

    /**
     * Checks whether this {@code Cell} is accessible or not.
     * <p>
     * A cell is considered accessible if it is empty, currently occupied by an {@code Agent},
     * or its type matches one of the given types.
     *
     * @param accessTypes a list of accessible cell types.
     *
     * @return {@code true} if this cell is accessible; {@code false} otherwise.
     */
    public boolean isAccessible(CellType... accessTypes) {
        if (isEmpty()) {
            return true;
        }

        for (CellType t : accessTypes) {
            if (type == t) {
                return true;
            }
        }

        return false;
    }

    // ===============================================================================================
    //
    // Timeline-Related Methods
    //

    /**
     * Returns the {@code Agent} scheduled to pass though this {@code Cell} at the given time.
     *
     * @param time the time to get at.
     *
     * @return the {@code Agent} if exists; {@code null} otherwise.
     */
    public Agent getScheduledAt(long time) {
        return timeline.get(time);
    }

    /**
     * Checks whether this {@code Cell} is occupied by an {@code Agent} at the given time or not.
     *
     * @param time the time to check at.
     *
     * @return {@code true} if the cell is occupied; {@code false} otherwise.
     */
    public boolean hasSceduleAt(long time) {
        return timeline.containsKey(time);
    }

    /**
     * Schedules an {@code Agent} to pass though this {@code Cell} at the given time.
     *
     * @param time  the time to set at.
     * @param agent the passing {@code Agent}.
     */
    public void setScheduleAt(long time, Agent agent) {
        timeline.put(time, agent);
    }

    /**
     * Clears the timeline schedule of this {@code Cell} at the given time.
     *
     * @param time the time to clear at.
     */
    public void clearScheduleAt(long time) {
        timeline.remove(time);
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Converts this {@code Cell} to a character symbol representing its shape.
     *
     * @return a character representing the shape of this cell.
     */
    public char toShape() {
        if (hasAgent()) {
            return Utility.dirToShape(agent.getDirection());
        }

        switch (type) {
            case EMPTY:
                return Constants.SHAPE_CELL_EMPTY;
            case OBSTACLE:
                return Constants.SHAPE_CELL_OBSTACLE;
            case RACK:
                return Constants.SHAPE_CELL_RACK;
            case GATE:
                return Constants.SHAPE_CELL_GATE;
            case STATION:
                return Constants.SHAPE_CELL_STATION;
            default:
                return Constants.SHAPE_CELL_UNKNOWN;
        }
    }
}