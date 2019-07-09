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

import java.util.LinkedList;
import java.util.List;


/**
 * This {@code GridCell} class represents a cell of the grid of the {@link Warehouse}.
 * <p>
 * A cell in the {@link Warehouse} grid may be empty, may hold an {@link Agent} and/or
 * {@link Facility}, or may be an obstacle cell.
 */
public class GridCell {

    //
    // Member Variables
    //

    /**
     * The type of this {@code GridCell}.
     */
    private CellType type = CellType.EMPTY;

    /**
     * The {@code Facility} in this {@code GridCell} if exists; {@code null} otherwise.
     */
    private Facility facility;

    /**
     * The {@code Agent} in this {@code GridCell} if exists; {@code null} otherwise.
     */
    private Agent agent;

    /**
     * The list of agents locking this {@code GridCell}.
     */
    private List<Agent> lockingAgents = new LinkedList<>();

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
    public static GridCell[][] allocate2D(int n, int m) {
        GridCell[][] ret = new GridCell[n][m];

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                ret[i][j] = new GridCell();
            }
        }

        return ret;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new empty {@code GridCell}.
     */
    public GridCell() {

    }

    /**
     * Returns the type of this {@code GridCell}.
     *
     * @return the {@code CellType} of this {@code GridCell}.
     */
    public CellType getType() {
        return type;
    }

    /**
     * Returns the existing {@code Facility} in this {@code GridCell}.
     *
     * @return the {@code Facility} in this {@code GridCell} if exists; {@code null} otherwise.
     */
    public Facility getFacility() {
        return facility;
    }

    /**
     * Checks whether this {@code GridCell} has an existing {@code Facility} in it or not.
     *
     * @return {@code true} if this cell contains a {@code Facility}; {@code false} otherwise.
     */
    public boolean hasFacility() {
        return (facility != null);
    }

    /**
     * Checks whether this {@code GridCell} has a free {@code Rack} that is
     * currently not loaded by an {@code Agent}.
     *
     * @return {@code true} if this cell currently contains a {@code Rack}; {@code false} otherwise.
     */
    public boolean hasRack() {
        return (type == CellType.RACK && !facility.isBound());
    }

    /**
     * Sets the existing {@code Facility} in this {@code GridCell}.
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
     * Returns the existing {@code Agent} in this {@code GridCell}.
     *
     * @return the {@code Agent} in this {@code GridCell} if exists; {@code null} otherwise.
     */
    public Agent getAgent() {
        return agent;
    }

    /**
     * Checks whether this {@code GridCell} has an existing {@code Agent} in it or not.
     *
     * @return {@code true} if this cell contains an {@code Agent}; {@code false} otherwise.
     */
    public boolean hasAgent() {
        return (agent != null);
    }

    /**
     * Sets the existing {@code Agent} in this {@code GridCell}.
     *
     * @param agent the {@code Agent} to set.
     */
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     * Checks whether this {@code GridCell} is blocked or not.
     * The cell is considered blocked if either it is locked or it holds an obstacle.
     *
     * @return {@code true} if this cell is an blocked; {@code false} otherwise.
     */
    public boolean isBlocked() {
        return (type == CellType.OBSTACLE || lockingAgents.size() > 0 || (agent != null && (
                agent.isLocked() || agent.isBlocked() || agent.isDeactivated()
        )));
    }

    /**
     * Checks whether this {@code GridCell} is empty or not.
     * <p>
     * A cell is considered empty even it is currently occupied by an {@code Agent}.
     *
     * @return {@code true} if this cell is statically empty; {@code false} otherwise.
     */
    public boolean isEmpty() {
        return (type == CellType.EMPTY);
    }

    /**
     * Checks whether this {@code GridCell} is an obstacle or not.
     *
     * @return {@code true} if this cell is an obstacle; {@code false} otherwise.
     */
    public boolean isObstacle() {
        return (type == CellType.OBSTACLE);
    }

    /**
     * Checks whether this {@code GridCell} has been locked or not.
     *
     * @return {@code true} if this cell is locked; {@code false} otherwise.
     */
    public boolean isLocked() {
        return lockingAgents.size() > 0;
    }

    /**
     * Locks this {@code GridCell} by the given {@code Agent}.
     *
     * @param agent the {@code Agent} locking this {@code GridCell}
     */
    public void lock(Agent agent) {
        lockingAgents.add(agent);
    }

    /**
     * Unlocks this {@code GridCell} by the given {@code Agent}.
     *
     * @param agent the {@code Agent} unlocking this {@code GridCell}.
     */
    public void unlock(Agent agent) {
        lockingAgents.remove(agent);
    }

    /**
     * Returns a list of agents locking this {@code GridCell}.
     *
     * @return the list of locking agents.
     */
    public List<Agent> getLockingAgents() {
        return lockingAgents;
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Converts this {@code GridCell} to a character symbol representing its shape.
     *
     * @return a character representing the shape of this cell.
     */
    public char toShape() {
        if (hasAgent()) {
            if (agent.isBlocked() || agent.isDeactivated()) {
                return Constants.SHAPE_CELL_LOCKED;
            } else {
                return Utility.dirToShape(agent.getDirection());
            }
        }

        if (isLocked()) {
            return Constants.SHAPE_CELL_LOCKED;
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