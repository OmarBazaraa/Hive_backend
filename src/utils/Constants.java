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

    // Controller time step intervals
    public static final long MIN_TIME_STEP_INTERVAL = 1000;     // milli-seconds
    public static final long MIN_TIME_STEP_INTERVAL_EPS = 10;   // milli-seconds

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
    public static final char SHAPE_CELL_LOCKED = 'X';
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

    //
    // Error Codes
    //

    // Internal server error
    public static final int ERR_SERVER = 5000;

    // Invalid message format from the frontend
    //      - Invalid JSON format
    //      - Missing messages fields
    //      - Unknown message type
    public static final int ERR_MSG_FORMAT = 5001;

    // Received known message at unexpected time:
    //      - Received START message while the server is not in IDLE state
    //      - Received PAUSE message while the server is not in RUNNING state
    //      - Received RESUME message while the server is not in PAUSE state
    //      - Received ACK message while the server is IDLE state
    //      - Received multiple ACK messages
    //      - Received ORDER message while the server is not in RUNNING state
    public static final int ERR_MSG_UNEXPECTED = 5002;

    // Invalid object (map, robot, facility, item, order) arguments sent from the frontend:
    //      - Warehouse map with non positive dimensions
    //      - Duplicate objects at the same cell
    //      - Negative object ID
    //      - Duplicate IDs
    //      - Non positive capacities
    //      - Non positive item quantities
    //      - Negative weights
    //      - Non existing item ID
    //      - Non existing gate ID
    //      - Non existing rack ID
    //      - Order with no items
    //      - Invalid robot direction
    //      - Invalid order type
    //      - Invalid cell type
    public static final int ERR_INVALID_ARGS = 5003;

    // Rack weight exceed maximum capacity
    // Args: [rack id, the excess weight]
    public static final int ERR_RACK_CAP_EXCEEDED = 5004;

    // Infeasible collect order due to item shortage
    // Args: [order id, array the missing items]
    public static final int ERR_INFEASIBLE_COLLECT_ORDER_NO_ITEMS = 5005;

    // Infeasible refill order as items weight exceed rack capacity
    // Args: [order id, rack id, the excess weight]
    public static final int ERR_INFEASIBLE_REFILL_ORDER_NO_SPACE = 5006;

    // Infeasible refill order as the specified rack and gate are unreachable
    // Args: [order id, rack id, gate id]
    public static final int ERR_INFEASIBLE_REFILL_ORDER_RACK_GATE_UNREACHABLE = 5007;

    // Rack has no gate to reach
    // Args: [rack id]
    public static final int ERR_RACK_NO_GATE_REACHABLE = 5008;

    // Rack has no agent to load in its full capacity
    // (i.e. no reachable agent can load the rack in its full capacity)
    // Args: [rack id, max load capacity],
    // where "max load capacity" is the load capacity of the strongest reachable agent;
    // or -1 if there is no reachable agents
    public static final int ERR_RACK_NO_AGENT_REACHABLE = 5009;
}
