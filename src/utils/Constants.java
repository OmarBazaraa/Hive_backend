package utils;

public class Constants {

    // Hive system details
    public static final String PROJ_NAME = "Hive System";
    public static final String PROJ_VERSION = "0.0.0.1";
    public static final String PROJ_VERSION_DATE = "21 February 2019";

    //
    public static final int AGENT_DEFAULT_CAPACITY = 100;
    public static final int AGENT_DEFAULT_CHARGE_CAPACITY = 100;

    // Directions in clockwise order
    public enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    //
    public enum OrderType {
        GET,
        ADD
    }

    //
    public enum OrderStatus {
        PENDING,
        SEMI_PENDING,
        ACTIVE,
        DELIVERED
    }
}
