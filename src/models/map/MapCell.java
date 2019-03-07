package models.map;

import models.components.base.DstHiveObject;
import models.components.base.SrcHiveObject;
import models.map.base.Cell;
import utils.Constants;
import utils.Constants.*;


/**
 * This {@code MapCell} class represents a grid cell in our Hive System's map.
 */
public class MapCell extends Cell {

    //
    // Member Variables
    //

    /**
     * Type of the cell.
     */
    public CellType type;

    /**
     * The {@code SrcHiveObject} in this cell if exists; {@code null} otherwise.
     */
    public SrcHiveObject srcObj;

    /**
     * The {@code DstHiveObject} in this cell if exists; {@code null} otherwise.
     */
    public DstHiveObject dstObj;

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
     * Constructs a new cell.
     */
    public MapCell() {
        this.type = CellType.UNKNOWN;
        this.srcObj = null;
        this.dstObj = null;
    }

    /**
     * Constructs a new grid cell.
     *
     * @param type the type of the cell.
     */
    public MapCell(CellType type) {
        this.type = type;
    }

    /**
     * Constructs a new grid cell.
     *
     * @param type   the type of the cell.
     * @param srcObj the existing {@code SrcHiveObject} in the cell.
     * @param dstObj the existing {@code DstHiveObject} in the cell.
     */
    public MapCell(CellType type, SrcHiveObject srcObj, DstHiveObject dstObj) {
        this.type = type;
        this.srcObj = srcObj;
        this.dstObj = dstObj;
    }

    /**
     * Sets the source object in this cell.
     *
     * @param srcObj the existing {@code SrcHiveObject} in the cell.
     */
    public void setSrcObject(SrcHiveObject srcObj) {
        this.srcObj = srcObj;
    }

    /**
     * Sets the source object in this cell.
     *
     * @param dstObj the existing {@code DstHiveObject} in the cell.
     */
    public void setDstObject(DstHiveObject dstObj) {
        this.dstObj = dstObj;
    }

    /**
     * Sets the parameters of this cell.
     *
     * @param type   the type of the cell.
     * @param srcObj the existing {@code SrcHiveObject} in the cell.
     * @param dstObj the existing {@code DstHiveObject} in the cell.
     */
    public void set(CellType type, SrcHiveObject srcObj, DstHiveObject dstObj) {
        this.type = type;
        this.srcObj = srcObj;
        this.dstObj = dstObj;
    }

    /**
     * Checks whether this cell has a destination object in it or not.
     *
     * @return {@code true} if this cell contains {@code DstHiveObject}, {@code false} otherwise.
     */
    public boolean hasDestination() {
        return dstObj != null;
    }

    /**
     * Checks whether this cell has an agent in it or not.
     *
     * @return {@code true} if this cell contains {@code Agent}, {@code false} otherwise.
     */
    public boolean hasAgent() {
        return srcObj != null;
    }

    /**
     * Checks whether this cell is empty or not.
     * A cell is considered empty if it's currently empty or it's occupied by an agent.
     *
     * @return {@code true} if this cell is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return type == CellType.EMPTY || type == CellType.AGENT;
    }

    /**
     * Checks whether this cell is movable or not.
     * A cell is considered movable if the agents can move into it.
     *
     * @return {@code true} if this cell is movable, {@code false} otherwise.
     */
    public boolean isMovable() {
        return type == CellType.EMPTY || type == CellType.RACK || type == CellType.GATE || type == CellType.STATION;
    }

    /**
     * Converts a given cell shape to a {@code CellType} value.
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
            case Constants.SHAPE_CELL_GATE:
                return CellType.GATE;
            case Constants.SHAPE_CELL_RACK:
                return CellType.RACK;
            case Constants.SHAPE_CELL_AGENT:
                return CellType.AGENT;
            case Constants.SHAPE_CELL_STATION:
                return CellType.STATION;
            default:
                return CellType.UNKNOWN;
        }
    }

    /**
     * Converts this cell to a character symbol representing its type.
     *
     * @return a {@code char} representing this cell's type.
     */
    @Override
    public char toShape() {
        switch (type) {
            case EMPTY:
                return Constants.SHAPE_CELL_EMPTY;
            case OBSTACLE:
                return Constants.SHAPE_CELL_OBSTACLE;
            case GATE:
                return Constants.SHAPE_CELL_GATE;
            case RACK:
                return Constants.SHAPE_CELL_RACK;
            case AGENT:
                return Constants.SHAPE_CELL_AGENT;
            case STATION:
                return Constants.SHAPE_CELL_STATION;
            default:
                return Constants.SHAPE_CELL_UNKNOWN;
        }
    }
}