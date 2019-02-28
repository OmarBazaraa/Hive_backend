package models.map;

import models.components.base.HiveObject;
import utils.Constants;
import utils.Constants.*;


/**
 * This {@code Cell} class represents a grid cell in our Hive System's map.
 */
public class Cell {

    /**
     * Type of the cell.
     */
    public CellType type;

    /**
     * The Hive object in this cell if exists; {@code null} otherwise.
     */
    public HiveObject obj;

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
     * Checks whether this cell is empty or not.
     * A cell is considered empty if it's currently empty or it's occupied by an agent.
     *
     * @return {@code true} if this cell is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return type == CellType.EMPTY || type == CellType.AGENT;
    }

    /**
     * Converts this cell to a character symbol representing its type.
     *
     * @return a {@code char} representing this cell's type.
     */
    public char toShape() {
        switch (type) {
            case EMPTY:
                return Constants.CELL_SHAPE_EMPTY;
            case OBSTACLE:
                return Constants.CELL_SHAPE_OBSTACLE;
            case GATE:
                return Constants.CELL_SHAPE_GATE;
            case RACK:
                return Constants.CELL_SHAPE_RACK;
            case AGENT:
                return Constants.CELL_SHAPE_AGENT;
            case CHARGING_STATION:
                return Constants.CELL_SHAPE_CHARGING_STATION;
            default:
                return Constants.CELL_SHAPE_UNKNOWN;
        }
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
            case Constants.CELL_SHAPE_EMPTY:
                return CellType.EMPTY;
            case Constants.CELL_SHAPE_OBSTACLE:
                return CellType.OBSTACLE;
            case Constants.CELL_SHAPE_GATE:
                return CellType.GATE;
            case Constants.CELL_SHAPE_RACK:
                return CellType.RACK;
            case Constants.CELL_SHAPE_AGENT:
                return CellType.AGENT;
            case Constants.CELL_SHAPE_CHARGING_STATION:
                return CellType.CHARGING_STATION;
            default:
                return CellType.UNKNOWN;
        }
    }

    /**
     * Returns a string representation of object.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of object.
     */
    @Override
    public String toString() {
        return Character.toString(toShape());
    }
}