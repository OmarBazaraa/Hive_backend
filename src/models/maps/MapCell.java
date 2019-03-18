package models.maps;

import models.agents.Agent;
import models.facilities.Facility;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;

import utils.Constants;
import utils.Constants.*;

import org.json.JSONObject;


/**
 * This {@code MapCell} class represents a map grid cell used by {@link MapGrid} class.
 *
 * @see Cell
 * @see GuideCell
 */
public class MapCell extends Cell {

    //
    // Member Variables
    //

    /**
     * The type of this {@code MapCell}.
     */
    private CellType type = CellType.EMPTY;

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
     * Creates a new {@code MapCell} object from JSON data.
     *
     * @param data the un-parsed JSON data.
     * @param row  the row position of the {@code MapCell} to create.
     * @param col  the column position of the {@code MapCell} to create.
     *
     * @return an {@code MapCell} object.
     */
    public static MapCell create(JSONObject data, int row, int col) throws Exception {
        MapCell ret = new MapCell();

        if (data.isEmpty()) {
            return ret;
        }

        if (data.has(Constants.MSG_KEY_AGENT)) {
            ret.setAgent(Agent.create(data, row, col));
            return ret;
        }

        if (data.has(Constants.MSG_KEY_FACILITY)) {
            int type = data.getInt(Constants.MSG_KEY_TYPE);

            switch (type) {
                case Constants.MSG_TYPE_CELL_OBSTACLE:
                    ret.setFacility(null, CellType.OBSTACLE);
                    break;
                case Constants.MSG_TYPE_CELL_RACK:
                    ret.setFacility(Rack.create(data, row, col), CellType.RACK);
                    break;
                case Constants.MSG_TYPE_CELL_GATE:
                    ret.setFacility(Gate.create(data, row, col), CellType.GATE);
                    break;
                case Constants.MSG_TYPE_CELL_STATION:
                    ret.setFacility(Station.create(data, row, col), CellType.STATION);
                    break;
            }
        }

        throw new Exception("Unknown cell type!");
    }

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

    }

    /**
     * Constructs a new {@code MapCell} with the given {@code Agent} and {@code Facility}.
     *
     * @param agent    the {@code Agent} of the cell.
     * @param facility the {@code Facility} of the cell.
     * @param type     the {@code CellType} of the cell.
     */
    public MapCell(Agent agent, Facility facility, CellType type) {
        setFacility(facility, type);
        setAgent(agent);
    }

    /**
     * Constructs a new {@code MapCell} with the given {@code Facility}.
     *
     * @param facility the {@code Facility} of the cell.
     * @param type     the {@code CellType} of the cell.
     */
    public MapCell(Facility facility, CellType type) {
        setFacility(facility, type);
    }

    /**
     * Constructs a new {@code MapCell} with the given {@code Agent}.
     *
     * @param agent the {@code Agent} of the cell.
     */
    public MapCell(Agent agent) throws Exception {
        setAgent(agent);
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
     *
     * @param facility the {@code Facility} to set.
     * @param type     the {@code CellType} of {@code Facility}.
     */
    public void setFacility(Facility facility, CellType type) {
        this.facility = facility;
        this.type = type;
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
     * @param agent the {@code Agent} to set.
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