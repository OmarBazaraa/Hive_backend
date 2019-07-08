package communicators.frontend;


/**
 * This {@code FrontendConstants} class contains some static constants values
 * to be used by the frontend communicator.
 */
public class FrontendConstants {

    //
    // Communication messages types
    //

    // Main message types to server
    public static final int TYPE_START = 0;
    public static final int TYPE_STOP = 1;
    public static final int TYPE_PAUSE = 2;
    public static final int TYPE_RESUME = 3;
    public static final int TYPE_ORDER = 4;
    public static final int TYPE_CONTROL = 5;
    public static final int TYPE_DONE = 6;

    // Main message types from server
    public static final int TYPE_ACK_START = 0;
    public static final int TYPE_ACK_RESUME = 1;
    public static final int TYPE_ACK_ORDER = 2;
    public static final int TYPE_ACTION = 3;
    public static final int TYPE_LOG = 4;
    public static final int TYPE_MSG = 6;

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

    // Agent control types
    public static final int TYPE_AGENT_ACTIVATE = 0;
    public static final int TYPE_AGENT_DEACTIVATE = 1;

    // Agent action types
    public static final int TYPE_AGENT_STOP = 0;
    public static final int TYPE_AGENT_MOVE = 1;
    public static final int TYPE_AGENT_ROTATE_RIGHT = 2;
    public static final int TYPE_AGENT_ROTATE_LEFT = 3;
    public static final int TYPE_AGENT_RETREAT = 4;
    public static final int TYPE_AGENT_LOAD = 5;
    public static final int TYPE_AGENT_OFFLOAD = 6;
    public static final int TYPE_AGENT_BIND = 7;
    public static final int TYPE_AGENT_UNBIND = 8;

    // Log types
    public static final int TYPE_LOG_TASK_ASSIGNED = 0;
    public static final int TYPE_LOG_TASK_COMPLETED = 1;
    public static final int TYPE_LOG_ORDER_FULFILLED = 2;
    public static final int TYPE_LOG_BATTERY_UPDATED = 3;

    // Statistics types
    // TODO

    // Other types
    public static final int TYPE_OK = 0;
    public static final int TYPE_INFO = 0;
    public static final int TYPE_ERROR = 1;

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
    public static final String KEY_AGENT_BATTERY_LEVEL = "battery";
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
}
