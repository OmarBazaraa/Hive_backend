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

    // Lights colors
    public static final int TYPE_LIGHT_RED = 5;
    public static final int TYPE_LIGHT_BLUE = 6;

    // Lights control
    public static final int TYPE_LIGHT_OFF = 0;
    public static final int TYPE_LIGHT_ON = 1;
    public static final int TYPE_LIGHT_FLASH = 2;

    // Others
    public static final int HARDWARE_CONFIG_INTERVAL = 6000;
    public static final String HARDWARE_LOG_FILE = "out/hardware_logs.txt";
}
