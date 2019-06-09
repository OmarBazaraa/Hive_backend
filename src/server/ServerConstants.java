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

    // Main keys
    public static final String MSG_KEY_ID = "id";
    public static final String MSG_KEY_TYPE = "type";
    public static final String MSG_KEY_DATA = "data";

    // Configuration keys
    public static final String MSG_KEY_MAP = "map";
    public static final String MSG_KEY_WIDTH = "width";
    public static final String MSG_KEY_HEIGHT = "height";
    public static final String MSG_KEY_GRID = "grid";

    public static final String MSG_KEY_FACILITY = "facility";

    public static final String MSG_KEY_RACK = "rack";
    public static final String MSG_KEY_RACK_ID = "rack_id";
    public static final String MSG_KEY_RACK_POSITION = "rack_pos";
    public static final String MSG_KEY_RACK_CAPACITY = "capacity";

    public static final String MSG_KEY_STATION = "station";
    public static final String MSG_KEY_STATION_ID = "station_id";
    public static final String MSG_KEY_STATION_POSITION = "station_pos";

    public static final String MSG_KEY_GATE = "gate";
    public static final String MSG_KEY_GATE_ID = "gate_id";
    public static final String MSG_KEY_GATE_POSITION = "gate_pos";

    public static final String MSG_KEY_AGENT = "robot";
    public static final String MSG_KEY_AGENT_ID = "robot_id";
    public static final String MSG_KEY_AGENT_POS = "robot_pos";
    public static final String MSG_KEY_AGENT_LOAD_CAPACITY = "load_cap";
    public static final String MSG_KEY_AGENT_BATTERY_CAPACITY = "battery_cap";

    public static final String MSG_KEY_ITEMS = "items";
    public static final String MSG_KEY_ITEM = "item";
    public static final String MSG_KEY_ITEM_WEIGHT = "weight";
    public static final String MSG_KEY_ITEM_QUANTITY = "quantity";

    public static final String MSG_KEY_ORDER = "order";
    public static final String MSG_KEY_ORDER_ID = "order_id";

    public static final String MSG_KEY_TIME_STEP = "time_step";
    public static final String MSG_KEY_ACTIONS = "actions";
    public static final String MSG_KEY_LOGS = "logs";
    public static final String MSG_KEY_STATISTICS = "statistics";

    //
    // Communication messages types
    //

    // Control and main types
    public static final int MSG_TYPE_RUN = 1;
    public static final int MSG_TYPE_DEPLOY = 2;
    public static final int MSG_TYPE_PAUSE = 3;
    public static final int MSG_TYPE_STOP = 4;
    public static final int MSG_TYPE_EXIT = 5;
    public static final int MSG_TYPE_ACK = 6;
    public static final int MSG_TYPE_CONFIG = 7;
    public static final int MSG_TYPE_ORDER = 8;
    public static final int MSG_TYPE_UPDATE = 9;

    // Configuration types
    public static final int MSG_TYPE_CELL_EMPTY = 1;
    public static final int MSG_TYPE_CELL_OBSTACLE = 2;
    public static final int MSG_TYPE_CELL_RACK = 3;
    public static final int MSG_TYPE_CELL_GATE = 4;
    public static final int MSG_TYPE_CELL_STATION = 5;
    public static final int MSG_TYPE_CELL_AGENT = 6;

    // Action types
    public static final int MSG_TYPE_AGENT_MOVE_UP = 100;
    public static final int MSG_TYPE_AGENT_MOVE_RIGHT = 101;
    public static final int MSG_TYPE_AGENT_MOVE_DOWN = 102;
    public static final int MSG_TYPE_AGENT_MOVE_LEFT = 103;
    public static final int MSG_TYPE_AGENT_BIND_RACK = 104;
    public static final int MSG_TYPE_AGENT_UNBIND_RACK = 105;
    public static final int MSG_TYPE_AGENT_BIND_GATE = 106;
    public static final int MSG_TYPE_AGENT_UNBIND_GATE = 107;
    public static final int MSG_TYPE_AGENT_BIND_STATION = 108;
    public static final int MSG_TYPE_AGENT_UNBIND_STATION = 109;

    // Log types
    public static final int MSG_TYPE_TASK_ASSIGNED = 200;
    public static final int MSG_TYPE_TASK_COMPLETED = 201;
    public static final int MSG_TYPE_ORDER_FULFILLED = 202;
}