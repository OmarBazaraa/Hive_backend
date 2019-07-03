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
    public static final String PROJ_VERSION = "0.2.0.0";
    public static final String PROJ_VERSION_DATE = "2 July 2019";

    // Communication details
    public static final int FRONTEND_COMM_PORT = 1337;
    public static final int HARDWARE_COMM_PORT = 12344;

    //
    // Server controller constants
    //

    /**
     * Different supported running modes of the server.
     */
    public enum RunningMode {
        SIMULATION,
        DEPLOYMENT
    }

    /**
     * Different states of the {@code Server} during its lifecycle.
     */
    public enum ServerState {
        IDLE,
        RUNNING,
        PAUSE
    }

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
    public static final char SHAPE_CELL_RACK = 'R';
    public static final char SHAPE_CELL_AGENT = '@';
    public static final char SHAPE_CELL_STATION = 'S';
    public static final char SHAPE_CELL_UNKNOWN = '?';

    //
    // Directions
    //

    /**
     * The array of available directions in anti-clockwise order.
     */
    public static final int[] DIRECTIONS = {
            Constants.DIR_RIGHT,
            Constants.DIR_UP,
            Constants.DIR_LEFT,
            Constants.DIR_DOWN
    };

    // Directions in anti-clockwise order
    public static final int DIR_RIGHT = 0;
    public static final int DIR_UP = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_DOWN = 3;
    public static final int DIR_COUNT = 4;

    // Direction arrays in the same order as the above directions
    public static final int DIR_ROW[] = { 0, -1, 0, 1 };
    public static final int DIR_COL[] = { 1, 0, -1, 0 };

    // Direction shapes
    public static final char SHAPE_DIR_RIGHT = '>';
    public static final char SHAPE_DIR_UP = '^';
    public static final char SHAPE_DIR_LEFT = '<';
    public static final char SHAPE_DIR_DOWN = 'v';
    public static final char SHAPE_DIR_STILL = '.';
    public static final char SHAPE_DIR_UNKNOWN = '?';

    //
    // Agent Constants
    //

    /**
     * Different actions that can be done by an {@code Agent}.
     */
    public enum AgentAction {
        MOVE,
        ROTATE_RIGHT,
        ROTATE_LEFT,
        RETREAT,
        LOAD,
        OFFLOAD,
        BIND,
        UNBIND,
        NOTHING
    }

    /**
     * The array of move actions that can be done by an {@code Agent}.
     */
    public static final AgentAction[] MOVE_ACTIONS = {
            AgentAction.MOVE,
            AgentAction.ROTATE_RIGHT,
            AgentAction.ROTATE_LEFT
    };

    // Agent default configurations
    public static final int AGENT_DEFAULT_LOAD_CAPACITY = 500;
    public static final int AGENT_DEFAULT_BATTERY_LEVEL = 7;
    public static final int AGENT_BATTERY_THRESHOLD = 2;
    public static final int AGENT_DEFAULT_DIRECTION = DIR_RIGHT;

    //
    // Facility Constants
    //

    // Rack default configurations
    public static final int RACK_DEFAULT_STORE_CAPACITY = 100;
    public static final int RACK_DEFAULT_CONTAINER_WEIGHT = 100;
}
