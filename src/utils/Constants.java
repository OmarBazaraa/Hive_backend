package utils;

public class Constants {

    // Hive system details
    public static final String PROJ_NAME = "Hive System";
    public static final String PROJ_VERSION = "0.0.0.1";
    public static final String PROJ_VERSION_DATE = "21 February 2019";

    // Agent default configurations
    public static final int AGENT_DEFAULT_CAPACITY = 100;
    public static final int AGENT_DEFAULT_CHARGE_CAPACITY = 100;

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
        CHARGE_SPOT,
        UNKNOWN
    }

    /**
     * Different status of the agents during their lifecycle in the system.
     */
    public enum AgentStatus {
        READY,
        ASSIGNED_TASK,
        ACTIVE,
        CHARGING,
        OUT_OF_SERVICE
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
     * Different supported order types.
     */
    public enum OrderType {
        GET,
        ADD
    }
}
