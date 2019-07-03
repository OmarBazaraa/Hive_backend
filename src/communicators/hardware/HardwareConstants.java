package communicators.hardware;


/**
 * This {@code HardwareConstants} class contains some static constants values
 * to be used by the hardware communicator.
 */
public class HardwareConstants {

    //
    // Communication messages types
    //

    // Main message types to server
    public static final int TYPE_DONE = 0;
    public static final int TYPE_BATTERY = 1;
    public static final int TYPE_BLOCKED = 2;
    public static final int TYPE_ERROR = 3;

    // Main message types from server
    public static final int TYPE_CONFIG = 0;
    public static final int TYPE_ACTION = 1;
    public static final int TYPE_LIGHTS = 2;

    // Actions types
    public static final int TYPE_STOP = 0;
    public static final int TYPE_MOVE = 1;
    public static final int TYPE_ROTATE_RIGHT = 2;
    public static final int TYPE_ROTATE_LEFT = 3;
    public static final int TYPE_RETREAT = 4;
    public static final int TYPE_LOAD = 5;
    public static final int TYPE_OFFLOAD = 6;

    // Actions states
    public static final int ACTION_NORMAL = 0;
    public static final int ACTION_RECOVER = 1;

    // Lights colors
    public static final int LIGHT_RED = 0;
    public static final int LIGHT_BLUE = 1;

    // Lights control
    public static final int LIGHT_MODE_OFF = 0;
    public static final int LIGHT_MODE_ON = 1;
    public static final int LIGHT_MODE_FLASH = 2;

    // Error types from the hardware robots
    public static final int ERR_EXCEED_ALLOWED_DIS = 0;
    public static final int ERR_UNKNOWN = 1;

    //
    // Others
    //
    public static final int CONFIG_INTERVAL = 5000;    // milli-seconds
    public static final int TIMEOUT_INTERVAL = 1500;   // milli-seconds
    public static final String LOG_FILE = "out/hardware_logs.txt";
}
