package utils;


/**
 * This {@code Constants} class contains some static constants values
 * to be used across the entire project modules.
 *
 * TODO: refactor?! :: move types and enums to their corresponding classes
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
    // Communication details
    //

    // WebSocket server details
    public static final String SERVER_PATH = "/";
    public static final int SERVER_PORT = 8080;

    // Messages keys
    public static final String MSG_KEY_TYPE = "type";
    public static final String MSG_KEY_DATA = "data";
    public static final String MSG_KEY_MAP = "map";
    public static final String MSG_KEY_ITEMS = "items";
    public static final String MSG_KEY_ID = "id";
    public static final String MSG_KEY_GRID = "grid";
    public static final String MSG_KEY_WIDTH = "width";
    public static final String MSG_KEY_HEIGHT = "height";
    public static final String MSG_KEY_WEIGHT = "weight";
    public static final String MSG_KEY_QUANTITY = "quantity";
    public static final String MSG_KEY_CAPACITY = "capacity";

    // Messages types
    public static final int MSG_TYPE_CONFIG = 1;
    public static final int MSG_TYPE_ORDER = 2;
    public static final int MSG_TYPE_CELL_EMPTY = 1;
    public static final int MSG_TYPE_CELL_OBSTACLE = 2;
    public static final int MSG_TYPE_CELL_RACK = 3;
    public static final int MSG_TYPE_CELL_GATE = 4;
    public static final int MSG_TYPE_CELL_STATION = 5;
    public static final int MSG_TYPE_CELL_AGENT = 6;

    //
    // Map Grid
    //

    // Map grid cell shapes
    // TODO: refactor?! :: SHAPE_CELL_* <-> CELL_SHAPE_*
    public static final char SHAPE_CELL_EMPTY = '.';
    public static final char SHAPE_CELL_OBSTACLE = '#';
    public static final char SHAPE_CELL_GATE = 'G';
    public static final char SHAPE_CELL_RACK = '$';
    public static final char SHAPE_CELL_AGENT = '@';
    public static final char SHAPE_CELL_STATION = 'S';
    public static final char SHAPE_CELL_UNKNOWN = '?';

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

    //
    // Directions
    //
    
    // Direction shapes
    // TODO: refactor?! :: SHAPE_DIR_* <-> DIR_SHAPE_*
    public static final char SHAPE_DIR_UP = '^';
    public static final char SHAPE_DIR_RIGHT = '>';
    public static final char SHAPE_DIR_DOWN = 'v';
    public static final char SHAPE_DIR_LEFT = '<';
    public static final char SHAPE_DIR_STILL = '.';
    public static final char SHAPE_DIR_UNKNOWN = '?';

    // Direction arrays in the same order as {@code Direction} enum below
    public static final int DIR_ROW[] = { -1, 0, 1, 0, 0 };
    public static final int DIR_COL[] = { 0, 1, 0, -1, 0 };

    /**
     * Directions in clockwise order.
     */
    public enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    //
    // Agent Constants
    //

    // Agent default configurations
    // TODO: refactor?! :: AGENT_DEFAULT_* <-> DEFAULT_AGENT_*
    public static final int AGENT_DEFAULT_CAPACITY = 100;
    public static final int AGENT_DEFAULT_CHARGE_CAPACITY = 100;

    /**
     * Different status of an {@code Agent} during its lifecycle in the system.
     */
    public enum AgentStatus {
        IDLE,                   // Idle and doing nothing
        ACTIVE,                 // Active performing a task
        ACTIVE_LOADED,          // Active performing a task and also holding a rack to deliver
        CHARGING,               // Charging at a charging station
        MANUALLY_CONTROLLED,    // Manually controlled by the frontend
        OUT_OF_SERVICE          // Out of service
    }

    /**
     * Different actions that can be done by an {@code Agent}.
     * Note that the first 4 values should be in the same order as in {@code Direction} enum.
     * TODO: may need to add rotation
     */
    public enum AgentAction {
        MOVE_UP,                // Move up
        MOVE_RIGHT,             // Move right
        MOVE_DOWN,              // Move down
        MOVE_LEFT,              // Move left
        MOVE,                   // Move in the current direction (reduction of all the 4 move actions)
        LOAD,                   // Load a rack
        OFFLOAD,                // Offload a rack
        WAIT,                   // Wait in front a gate
        NOTHING                 // Do nothing
    }

    //
    // Rack Constants
    //

    // Rack default configurations
    // TODO: refactor?! :: RACK_DEFAULT_* <-> DEFAULT_RACK_*
    public static final int RACK_DEFAULT_INIT_WEIGHT = 100;
    public static final int RACK_DEFAULT_STORE_CAPACITY = 100;

    /**
     * Different status of a {@code Rack} during its lifecycle in the system.
     */
    public enum RackStatus {
        IDLE,           // Idle and ready to be shipped
        RESERVED,       // Reserved by a certain task
        LOADED          // Loaded by an agent to be delivered
    }

    //
    // Order and Task Constants
    //

    /**
     * Different supported types of an {@code Order}.
     * TODO: may be another order class
     */
    public enum OrderType {
        GET,            // Take from a rack to a gate
        PUT             // Take from a gate to a rack
    }

    /**
     * Different status of an {@code Order} during its lifecycle of the order.
     * TODO: may be removed
     */
    public enum OrderStatus {
        INACTIVE,       // Inactive order, meaning that its item has not been reserved
        ACTIVE,         // Active order with all its items has been reserved
        FULFILLED       // The order has been fulfilled
    }

    /**
     * Different status of a {@code Task} during its lifecycle of the task.
     * TODO: think of a more general way to combine multiple tasks together
     */
    public enum TaskStatus {
        INACTIVE,       // The task is still pending not active yet
        FETCHING,       // Agent is moving to fetch the assigned rack
        DELIVERING,     // Agent is moving to deliver the loaded rack to a gate
        OFFLOADING,     // Agent is offloading the needed items at the gate
        RETURNING,      // Agent is returning the rack back
        COMPLETED       // The task has been completed
    }
}
