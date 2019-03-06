package models.map;

import models.components.base.HiveObject;
import models.map.base.BaseCell;
import utils.Constants;
import utils.Constants.*;


/**
 * This {@code Cell} class represents a grid cell in our Hive System's map.
 */
public class Cell extends BaseCell {

    //
    // Member Variables
    //

    /**
     * Type of the cell.
     */
    public CellType type;

    /**
     * The Hive object in this cell if exists; {@code null} otherwise.
     */
    public HiveObject obj;

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
     * Constructs a new cell.
     */
    public Cell() {
        this.type = CellType.UNKNOWN;
        this.obj = null;
    }

    /**
     * Constructs a new grid cell.
     *
     * @param type the type of the cell.
     * @param obj  the existing Hive object in the cell.
     */
    public Cell(CellType type, HiveObject obj) {
        this.type = type;
        this.obj = obj;
    }

    /**
     * Sets the parameters of this cell.
     *
     * @param type the type of the cell.
     * @param obj  the existing Hive object in the cell.
     */
    public void set(CellType type, HiveObject obj) {
        this.type = type;
        this.obj = obj;
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