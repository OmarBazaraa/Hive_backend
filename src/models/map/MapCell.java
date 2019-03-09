package models.map;

import models.agents.Agent;
import models.facilities.Facility;
import utils.Constants;
import utils.Constants.*;


/**
 * This {@code MapCell} class represents a map grid cell used by {@link MapGrid} class.
 */
public class MapCell extends Cell {

    //
    // Member Variables
    //

    /**
     * The type of this {@code MapCell}.
     */
    private CellType type;

    /**
     * The {@code Facility} in this {@code MapCell} if exists; {@code null} otherwise.
     */
    private Facility facility;

    /**
     * The {@code Agent} in this {@code MapCell} if exists; {@code null} otherwise.
     */
    private Agent agent;

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
    public static MapCell[][] allocate2D(int n, int m) {
        MapCell[][] ret = new MapCell[n][m];

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                ret[i][j] = new MapCell();
            }
        }

        return ret;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new empty {@code MapCell}.
     */
    public MapCell() {
        this.type = CellType.EMPTY;
    }

    /**
     * Constructs a new {@code MapCell}.
     *
     * @param type the {@code CellType} of the cell.
     */
    public MapCell(CellType type) {
        this.type = type;
    }

    /**
     * Returns the type of this {@code MapCell}.
     *
     * @return the {@code CellType} of this {@code MapCell}.
     */
    public CellType getType() {
        return type;
    }

    /**
     * Sets the type of this {@code MapCell}.
     * TODO: check if the type is matching with the facility
     *
     * @param type the {@code CellType} of the cell.
     */
    public void setType(CellType type) {
        this.type = type;
    }

    /**
     * Returns the existing {@code Facility} in this {@code MapCell}.
     *
     * @return the {@code Facility} in this {@code MapCell} if exists; {@code null} otherwise.
     */
    public Facility getFacility() {
        return facility;
    }

    /**
     * Checks whether this {@code MapCell} has an existing {@code Facility} in it or not.
     *
     * @return {@code true} if this cell contains a {@code Facility}; {@code false} otherwise.
     */
    public boolean hasFacility() {
        return (facility != null);
    }

    /**
     * Sets the existing {@code Facility} in this {@code MapCell}.
     * TODO: check if the type is matching with the facility
     *
     * @param dstObj the {@code Facility} to setDistance.
     */
    public void setFacility(Facility dstObj) {
        this.facility = dstObj;
    }

    /**
     * Returns the existing {@code Agent} in this {@code MapCell}.
     *
     * @return the {@code Agent} in this {@code MapCell} if exists; {@code null} otherwise.
     */
    public Agent getAgent() {
        return agent;
    }

    /**
     * Checks whether this {@code MapCell} has an existing {@code Agent} in it or not.
     *
     * @return {@code true} if this cell contains an {@code Agent}; {@code false} otherwise.
     */
    public boolean hasAgent() {
        return (agent != null);
    }

    /**
     * Sets the existing {@code Agent} in this {@code MapCell}.
     *
     * @param agent the {@code Agent} to setDistance.
     */
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     * Checks whether this {@code MapCell} is statically empty or not.
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
     * Checks whether this {@code MapCell} is accessible or not.
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

    /**
     * Converts a cell shape to its corresponding {@code CellType} value.
     *
     * @param shape the shape of the grid cell to convert.
     *
     * @return the corresponding {@code CellType} of the given shape.
     */
    public static CellType toType(char shape) {
        switch (shape) {
            case Constants.SHAPE_CELL_EMPTY:
                return CellType.EMPTY;
            case Constants.SHAPE_CELL_OBSTACLE:
                return CellType.OBSTACLE;
            case Constants.SHAPE_CELL_RACK:
                return CellType.RACK;
            case Constants.SHAPE_CELL_GATE:
                return CellType.GATE;
            case Constants.SHAPE_CELL_STATION:
                return CellType.STATION;
            default:
                return CellType.UNKNOWN;
        }
    }

    /**
     * Converts this {@code MapCell} to a character symbol representing its shape.
     *
     * @return a character representing the shape of this cell.
     */
    @Override
    public char toShape() {
        if (hasAgent()) {
            return Constants.SHAPE_CELL_AGENT;
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