package server;


/**
 * This {@code ServerConstants} class contains some static constants values
 * to be used by the front-end server.
 */
public class ServerConstants {

    //
    // Communication details
    //

    // WebSocket server details
    public static final String SERVER_PATH = "/";
    public static final int SERVER_PORT = 8080;

    //
    // Communication messages keys
    //

    // Control and main message keys
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATA = "data";

    public static final String KEY_TIME_STEP = "timestep";
    public static final String KEY_ACTIONS = "actions";
    public static final String KEY_LOGS = "logs";
    public static final String KEY_STATISTICS = "statistics";

    public static final String KEY_MODE = "mode";
    public static final String KEY_STATE = "state";

    public static final String KEY_STATUS = "status";
    public static final String KEY_MSG = "msg";

    public static final String KEY_ID = "id";
    public static final String KEY_ROW = "row";
    public static final String KEY_COL = "col";

    // Configuration keys
    public static final String KEY_MAP = "map";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_GRID = "grid";

    public static final String KEY_FACILITY = "facility";

    public static final String KEY_RACK = "rack";
    public static final String KEY_RACK_ID = "rack_id";
    public static final String KEY_RACK_CAPACITY = "capacity";
    public static final String KEY_RACK_CONTAINER_WEIGHT = "container_weight";

    public static final String KEY_GATE = "gate";
    public static final String KEY_GATE_ID = "gate_id";

    public static final String KEY_STATION = "station";
    public static final String KEY_STATION_ID = "station_id";

    public static final String KEY_AGENT = "robot";
    public static final String KEY_AGENT_ID = "robot_id";
    public static final String KEY_AGENT_LOAD_CAPACITY = "load_cap";
    public static final String KEY_AGENT_CHARGE_PERCENTAGE = "battery_cap";
    public static final String KEY_AGENT_DIRECTION = "direction";

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

    // Control and main message types
    public static final int TYPE_START = 1;
    public static final int TYPE_STOP = 4;
    public static final int TYPE_RESUME = 2;
    public static final int TYPE_PAUSE = 3;
    public static final int TYPE_EXIT = 5;
    public static final int TYPE_ACK = 6;
    public static final int TYPE_ACK_START = 7;
    public static final int TYPE_ACK_RESUME = 8;
    public static final int TYPE_ACK_ORDER = 9;
    public static final int TYPE_ORDER = 10;
    public static final int TYPE_UPDATE = 11;

    // TODO: to be revised
    public static final int TYPE_AGENT_DEACTIVATE = 8;
    public static final int TYPE_AGENT_ACTIVATE = 8;
    public static final int TYPE_AGENT_BLOCKED = 8;

    // Running-mode types
    public static final int TYPE_MODE_SIMULATE = 1;
    public static final int TYPE_MODE_DEPLOY = 1;

    // Configuration types
    public static final int TYPE_CELL_EMPTY = 1;
    public static final int TYPE_CELL_OBSTACLE = 2;
    public static final int TYPE_CELL_RACK = 3;
    public static final int TYPE_CELL_GATE = 4;
    public static final int TYPE_CELL_STATION = 5;

    // Order types
    public static final int TYPE_ORDER_COLLECT = 1;
    public static final int TYPE_ORDER_REFILL = 1;

    // Action types
    public static final int TYPE_AGENT_MOVE_UP = 100;
    public static final int TYPE_AGENT_MOVE_RIGHT = 101;
    public static final int TYPE_AGENT_MOVE_DOWN = 102;
    public static final int TYPE_AGENT_MOVE_LEFT = 103;
    public static final int TYPE_AGENT_BIND_RACK = 104;
    public static final int TYPE_AGENT_UNBIND_RACK = 105;
    public static final int TYPE_AGENT_BIND_GATE = 106;
    public static final int TYPE_AGENT_UNBIND_GATE = 107;
    public static final int TYPE_AGENT_BIND_STATION = 108;
    public static final int TYPE_AGENT_UNBIND_STATION = 109;

    // Log types
    public static final int TYPE_TASK_ASSIGNED = 200;
    public static final int TYPE_TASK_COMPLETED = 200;
    public static final int TYPE_ORDER_ISSUED = 200;
    public static final int TYPE_ORDER_FULFILLED = 202;
    public static final int TYPE_ITEM_DELIVERED = 201;
    public static final int TYPE_RACK_ADJUSTED = 201;

    // Statistics types

    // Other types
    public static final int TYPE_OK = 1;
    public static final int TYPE_INFO = 1;
    public static final int TYPE_ERROR = 1;
}
