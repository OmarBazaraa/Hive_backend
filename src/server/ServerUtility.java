package server;

import utils.Constants;


/**
 * This {@code ServerUtility} class is a collection of a static utility functions
 * to be used by the front-end server.
 */
public class ServerUtility {


    /**
     * Converts an {@code AgentAction} into its corresponding sever message type id.
     *
     * @param action the {@code AgentAction} to convert.
     *
     * @return the serve message type id.
     */
    public static int actionToServerType(Constants.AgentAction action) {
        switch (action) {
            case MOVE_UP:
                return ServerConstants.TYPE_AGENT_MOVE_UP;
            case MOVE_RIGHT:
                return ServerConstants.TYPE_AGENT_MOVE_RIGHT;
            case MOVE_DOWN:
                return ServerConstants.TYPE_AGENT_MOVE_DOWN;
            case MOVE_LEFT:
                return ServerConstants.TYPE_AGENT_MOVE_LEFT;
            case BIND_RACK:
                return ServerConstants.TYPE_AGENT_BIND_RACK;
            case UNBIND_RACK:
                return ServerConstants.TYPE_AGENT_UNBIND_RACK;
            case BIND_GATE:
                return ServerConstants.TYPE_AGENT_BIND_GATE;
            case UNBIND_GATE:
                return ServerConstants.TYPE_AGENT_UNBIND_GATE;
            case BIND_STATION:
                return ServerConstants.TYPE_AGENT_BIND_STATION;
            case UNBIND_STATION:
                return ServerConstants.TYPE_AGENT_UNBIND_STATION;
        }

        return -1;
    }
}
