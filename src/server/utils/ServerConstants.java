package server.utils;


/**
 * This {@code ServerConstants} class contains some static constants values
 * to be used by the frontend server.
 */
public class ServerConstants {

    //
    // Communication details
    //

    /**
     * Different states of the {@code Server} during its lifecycle.
     */
    public enum ServerStates {
        IDLE,
        RUNNING,
        PAUSE,
        EXIT
    }

    // WebSocket server details
    public static final String SERVER_PATH = "/";
    public static final int SERVER_PORT = 1337;

    //
    // Communication messages keys
    //

    // General message keys
    public static final String KEY_TYPE = "type";
    public static final String KEY_STATUS = "status";
    public static final String KEY_MSG = "msg";
    public static final String KEY_DATA = "data";
    public static final String KEY_ARGS = "args";
    public static final String KEY_REASON = "reason";

    // Main start configuration keys
    public static final String KEY_MODE = "mode";
    public static final String KEY_STATE = "state";

    // Update message keys
    public static final String KEY_TIME_STEP = "timestep";
    public static final String KEY_ACTIONS = "actions";
    public static final String KEY_LOGS = "logs";
    public static final String KEY_STATISTICS = "statistics";

    // Control message keys
    public static final String KEY_ACTIVATED = "activated";
    public static final String KEY_DEACTIVATED = "deactivated";
    public static final String KEY_BLOCKED = "blocked";

    // Warehouse-related keys
    public static final String KEY_MAP = "map";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_GRID = "grid";

    public static final String KEY_ID = "id";
    public static final String KEY_ROW = "row";
    public static final String KEY_COL = "col";

    public static final String KEY_OBJECTS = "objects";

    public static final String KEY_FACILITY = "facilities";

    public static final String KEY_RACK = "rack";
    public static final String KEY_RACK_ID = "rack_id";
    public static final String KEY_RACK_ROW = "rack_row";
    public static final String KEY_RACK_COL = "rack_col";
    public static final String KEY_RACK_CAPACITY = "capacity";
    public static final String KEY_RACK_CONTAINER_WEIGHT = "weight";

    public static final String KEY_GATE = "gate";
    public static final String KEY_GATE_ID = "gate_id";
    public static final String KEY_GATE_ROW = "gate_row";
    public static final String KEY_GATE_COL = "gate_col";

    public static final String KEY_STATION = "station";
    public static final String KEY_STATION_ID = "station_id";
    public static final String KEY_STATION_ROW = "station_row";
    public static final String KEY_STATION_COL = "station_col";

    public static final String KEY_AGENT = "robot";
    public static final String KEY_AGENT_ID = "robot_id";
    public static final String KEY_AGENT_ROW = "robot_row";
    public static final String KEY_AGENT_COL = "robot_col";
    public static final String KEY_AGENT_LOAD_CAPACITY = "load_cap";
    public static final String KEY_AGENT_CHARGE_PERCENTAGE = "battery_cap";
    public static final String KEY_AGENT_DIRECTION = "direction";
    public static final String KEY_AGENT_IP = "ip";
    public static final String KEY_AGENT_PORT = "port";

    public static final String KEY_ITEMS = "items";
    public static final String KEY_ITEM = "item";
    public static final String KEY_ITEM_WEIGHT = "weight";
    public static final String KEY_ITEM_QUANTITY = "quantity";

    public static final String KEY_ORDER = "order";
    public static final String KEY_ORDER_ID = "order_id";
    public static final String KEY_ORDER_START_TIME = "start_timestep";

    //
    // Communication messages types
    //

    // Main message types to server
    public static final int TYPE_START = 0;
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_PAUSE = 2;
    public static final int TYPE_STOP = 3;
    public static final int TYPE_RESUME = 4;
    public static final int TYPE_ACK_UPDATE = 5;
    public static final int TYPE_CONTROL = 6;
    public static final int TYPE_EXIT = 7;

    // Main message types from server
    public static final int TYPE_ACK_START = 0;
    public static final int TYPE_ACK_RESUME = 1;
    public static final int TYPE_ACK_ORDER = 2;
    public static final int TYPE_UPDATE = 3;
    public static final int TYPE_MSG = 6;

    // Control message types to server
    public static final int TYPE_CONTROL_ACTIVATE = 0;
    public static final int TYPE_CONTROL_DEACTIVATE = 1;

    // Running-mode types
    public static final int TYPE_MODE_SIMULATE = 0;
    public static final int TYPE_MODE_DEPLOY = 1;

    // Configuration types
    public static final int TYPE_CELL_GATE = 0;
    public static final int TYPE_CELL_AGENT = 1;
    public static final int TYPE_CELL_RACK = 2;
    public static final int TYPE_CELL_STATION = 3;
    public static final int TYPE_CELL_OBSTACLE = 4;

    // Order types
    public static final int TYPE_ORDER_COLLECT = 0;
    public static final int TYPE_ORDER_REFILL = 1;

    // Action types
    public static final int TYPE_AGENT_MOVE = 0;
    public static final int TYPE_AGENT_ROTATE_RIGHT = 1;
    public static final int TYPE_AGENT_ROTATE_LEFT = 2;
    public static final int TYPE_AGENT_RETREAT = 3;
    public static final int TYPE_AGENT_LOAD = 4;
    public static final int TYPE_AGENT_OFFLOAD = 5;
    public static final int TYPE_AGENT_BIND = 6;
    public static final int TYPE_AGENT_UNBIND = 7;

    // Log types
    public static final int TYPE_LOG_TASK_ASSIGNED = 0;
    public static final int TYPE_LOG_TASK_COMPLETED = 1;
    public static final int TYPE_LOG_ORDER_FULFILLED = 2;

    // Statistics types
    // TODO

    // Other types
    public static final int TYPE_OK = 0;
    public static final int TYPE_INFO = 0;
    public static final int TYPE_ERROR = 1;

    //
    // Error Codes
    //

    //
    // TODO: select better error names
    //

    // Internal server error
    public static final int ERR_SERVER = 5000;

    // Invalid message format from the frontend
    //      - Invalid JSON format
    //      - Missing messages fields
    //      - Unknown message type      TODO
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
    //      - Invalid robot direction   TODO
    //      - Invalid order type        TODO
    //      - Invalid cell type         TODO
    public static final int ERR_INVALID_ARGS = 5003;

    // Rack weight exceed maximum capacity
    // Args: [rack id, the excess weight]
    public static final int ERR_RACK_CAP_EXCEEDED = 5004;

    // Infeasible collect order due to item shortage
    // Args: [order id, array the missing items]
    public static final int ERR_ORDER_INFEASIBLE_COLLECT = 5005;

    // Infeasible refill order as items weight exceed rack capacity
    // Args: [order id, rack id, the excess weight]
    public static final int ERR_ORDER_INFEASIBLE_REFILL = 5006;
}
