package utils;


/**
 * This {@code Constants} class contains some static constants values
 * to be used across the entire project modules.
 */
public class Constants {

    //
    // General Configurations
    //

    // Project details
    public static final String PROJ_NAME = "Hive System";
    public static final String PROJ_VERSION = "0.0.1.0";
    public static final String PROJ_VERSION_DATE = "8 March 2019";

    //
    // Map Grid
    //

    /**
     * Different supported grid cell types.
     * These represents the static cells in the warehouse but not the agents.
     */
    public enum CellType {
        EMPTY,
        OBSTACLE,
        RACK,
        GATE,
        STATION,
        UNKNOWN
    }

    // Map grid cell shapes
    public static final char SHAPE_CELL_EMPTY = '.';
    public static final char SHAPE_CELL_OBSTACLE = '#';
    public static final char SHAPE_CELL_GATE = 'G';
    public static final char SHAPE_CELL_RACK = '$';
    public static final char SHAPE_CELL_AGENT = '@';
    public static final char SHAPE_CELL_STATION = 'S';
    public static final char SHAPE_CELL_UNKNOWN = '?';

    //
    // Directions
    //

    /**
     * Directions in clockwise order.
     */
    public enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    // Direction arrays in the same order as {@code Direction} enum above
    public static final int DIR_ROW[] = { -1, 0, 1, 0 };
    public static final int DIR_COL[] = { 0, 1, 0, -1 };

    // Direction shapes
    public static final char SHAPE_DIR_UP = '^';
    public static final char SHAPE_DIR_RIGHT = '>';
    public static final char SHAPE_DIR_DOWN = 'v';
    public static final char SHAPE_DIR_LEFT = '<';
    public static final char SHAPE_DIR_STILL = '.';
    public static final char SHAPE_DIR_UNKNOWN = '?';

    //
    // Agent Constants
    //

    /**
     * Different actions that can be done by an {@code Agent}.
     * Note that the first 4 values should be in the same order as in {@code Direction} enum.
     */
    public enum AgentAction {
        MOVE_UP,
        MOVE_RIGHT,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE,
        MOVE_FORWARD,
        MOVE_BACKWARD,
        ROTATE_LEFT,
        ROTATE_RIGHT,
        BIND_RACK,
        UNBIND_RACK,
        BIND_GATE,
        UNBIND_GATE,
        BIND_STATION,
        UNBIND_STATION,
        NOTHING
    }

    // Agent default configurations
    public static final int AGENT_DEFAULT_LOAD_CAPACITY = 500;
    public static final int AGENT_DEFAULT_CHARGE_PERCENTAGE = 70;
    public static final Direction AGENT_DEFAULT_DIRECTION = Direction.RIGHT;

    //
    // Facility Constants
    //

    // Rack default configurations
    public static final int RACK_DEFAULT_STORE_CAPACITY = 100;
    public static final int RACK_DEFAULT_CONTAINER_WEIGHT = 100;
}
