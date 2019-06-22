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
    public static final int TYPE_ACK = 0;
    public static final int TYPE_BATTERY = 1;
    public static final int TYPE_BLOCKED = 2;

    // Main message types from server
    public static final int TYPE_STOP = 0;
    public static final int TYPE_MOVE = 1;
    public static final int TYPE_RETREAT = 2;
    public static final int TYPE_ROTATE_LEFT = 3;
    public static final int TYPE_ROTATE_RIGHT = 4;
    public static final int TYPE_LIGHT_RED = 5;
    public static final int TYPE_LIGHT_BLUE = 5;
}
