package utils;

public class Constants {

    // Hive system details
    public static final String PROJ_NAME = "Hive System";
    public static final String PROJ_VERSION = "0.0.0.1";
    public static final String PROJ_VERSION_DATE = "21 February 2019";

    // Grid cell shapes
    public static final char SHAPE_CELL_EMPTY = '.';
    public static final char SHAPE_CELL_OBSTACLE = '#';
    public static final char SHAPE_CELL_GATE = 'G';
    public static final char SHAPE_CELL_RACK = '$';
    public static final char SHAPE_CELL_AGENT = '@';
    public static final char SHAPE_CELL_STATION = 'S';
    public static final char SHAPE_CELL_UNKNOWN = '?';
    
    // Direction shapes
    public static final char SHAPE_DIR_UP = '^';
    public static final char SHAPE_DIR_RIGHT = '>';
    public static final char SHAPE_DIR_DOWN = 'v';
    public static final char SHAPE_DIR_LEFT = '<';
    public static final char SHAPE_DIR_STILL = '.';
    public static final char SHAPE_DIR_UNKNOWN = '?';

    // Agent default configurations
    public static final int AGENT_DEFAULT_CAPACITY = 100;
    public static final int AGENT_DEFAULT_CHARGE_CAPACITY = 100;

    // Rack default configurations
    public static final int RACK_DEFAULT_INIT_WEIGHT = 100;
    public static final int RACK_DEFAULT_STORE_CAPACITY = 100;

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
        LEFT,
        STILL
    }

    /**
     * Different supported grid cell types.
     */
    public enum CellType {
        EMPTY,
        OBSTACLE,
        GATE,
        RACK,
        AGENT,
        STATION,
        UNKNOWN
    }

    /**
     * Different status of the agents during their lifecycle in the system.
     */
    public enum AgentStatus {
        READY,
        ACTIVE,
        CHARGING,
        OUT_OF_SERVICE
    }

    /**
     * Different actions that can be done by the agents
     */
    public enum AgentAction {
        NOTHING,
        MOVE,
        PICK,
        RELEASE,
        WAIT
    }

    /**
     * Different status of the racks during their lifecycle in the system.
     */
    public enum RackStatus {
        READY,      // The rack is static, and ready to be shipped
        RESERVED,   // The rack is currently reserved by a certain task
        ACTIVE      // The rack is currently being shipped
    }

    /**
     * Different status during the lifecycle of the order.
     */
    public enum OrderStatus {
        PENDING,
        SEMI_ASSIGNED,
        ASSIGNED,
        DELIVERED
    }

    /**
     * Different status during the lifecycle of the task.
     */
    public enum TaskStatus {
        PENDING,
        FETCH,
        PICK,
        DELIVER,
        WAIT,
        RETURN,
        RELEASE,
        DONE
    }

    /**
     * Different supported order types.
     */
    public enum OrderType {
        GET,
        PUT
    }
}
