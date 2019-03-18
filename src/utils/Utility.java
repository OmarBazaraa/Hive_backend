package utils;

import utils.Constants.*;


/**
 * This {@code Utility} class is a collection of a static utility functions
 * to be used across the entire project modules.
 */
public class Utility {

    /**
     * Returns the reverse (opposite) direction of the given one.
     *
     * @param dir the direction to get its reverse.
     *
     * @return the reverse direction.
     */
    public static Direction getReverseDir(Direction dir) {
        int i = dir.ordinal();
        Direction[] dirs = Direction.values();
        return dirs[(i + 2) % 4];
    }

    /**
     * Converts an {@code AgentAction} into its corresponding {@code Direction}.
     *
     * @param action the {@code AgentAction} to convert.
     *
     * @return the corresponding {@code Direction}.
     */
    public static Direction actionToDir(AgentAction action) {
        int i = action.ordinal();
        return Direction.values()[i % 4];
    }

    /**
     * Converts the given {@code Direction} into its corresponding {@code AgentAction}.
     *
     * @param dir the {@code Direction} to convert.
     *
     * @return the corresponding {@code AgentAction}.
     */
    public static AgentAction dirToAction(Direction dir) {
        int i = dir.ordinal();
        return AgentAction.values()[i];
    }

    /**
     * Converts an {@code AgentAction} into its corresponding sever message type id.
     *
     * @param action the {@code AgentAction} to convert.
     *
     * @return the serve message type id.
     */
    public static int actionToServerType(AgentAction action) {
        switch (action) {
            case MOVE_UP:
                return Constants.MSG_TYPE_AGENT_MOVE_UP;
            case MOVE_RIGHT:
                return Constants.MSG_TYPE_AGENT_MOVE_RIGHT;
            case MOVE_DOWN:
                return Constants.MSG_TYPE_AGENT_MOVE_DOWN;
            case MOVE_LEFT:
                return Constants.MSG_TYPE_AGENT_MOVE_LEFT;
            case BIND_RACK:
                return Constants.MSG_TYPE_AGENT_BIND_RACK;
            case UNBIND_RACK:
                return Constants.MSG_TYPE_AGENT_UNBIND_RACK;
            case BIND_GATE:
                return Constants.MSG_TYPE_AGENT_BIND_GATE;
            case UNBIND_GATE:
                return Constants.MSG_TYPE_AGENT_UNBIND_GATE;
            case BIND_STATION:
                return Constants.MSG_TYPE_AGENT_BIND_STATION;
            case UNBIND_STATION:
                return Constants.MSG_TYPE_AGENT_UNBIND_STATION;
        }

        return -1;
    }
}
