package server.utils;

import models.agents.Agent;

import models.facilities.Rack;
import models.items.Item;
import models.tasks.Order;
import models.tasks.Task;

import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;


/**
 * This {@code ServerDecoder} class contains useful static functions to encode
 * messages from the backend to be sent to the frontend.
 */
public class ServerEncoder {

    //
    // Static Encoding Methods
    //

    public static JSONObject encodeUpdateMsg(long time, JSONArray actions, JSONArray logs, JSONArray statistics) {
        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_TIME_STEP, time);
        data.put(ServerConstants.KEY_ACTIONS, actions);
        data.put(ServerConstants.KEY_LOGS, logs);
        data.put(ServerConstants.KEY_STATISTICS, statistics);
        return encodeMsg(ServerConstants.TYPE_UPDATE, data);
    }

    public static JSONObject encodeAgentAction(Agent agent, AgentAction action) {
        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_ID, agent.getId());
        data.put(ServerConstants.KEY_ROW, agent.getRow());
        data.put(ServerConstants.KEY_COL, agent.getCol());
        return encodeMsg(agentActionToType(action), data);
    }

    public static JSONObject encodeTaskAssignedLog(Task task) {
        Agent agent = task.getAgent();
        Rack rack = task.getRack();

        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_AGENT_ID, agent.getId());
        data.put(ServerConstants.KEY_AGENT_ROW, agent.getRow());
        data.put(ServerConstants.KEY_AGENT_COL, agent.getCol());
        data.put(ServerConstants.KEY_RACK_ID, rack.getId());
        data.put(ServerConstants.KEY_RACK_ROW, rack.getRow());
        data.put(ServerConstants.KEY_RACK_COL, rack.getCol());
        return encodeMsg(ServerConstants.TYPE_TASK_ASSIGNED, data);
    }

    public static JSONObject encodeTaskCompletedLog(Task task) {
        Agent agent = task.getAgent();
        Rack rack = task.getRack();
        Order order = task.getOrder();

        JSONArray items = new JSONArray();

        for (Map.Entry<Item, Integer> pair : task) {
            JSONObject item = new JSONObject();
            item.put(ServerConstants.KEY_ID, pair.getKey().getId());
            item.put(ServerConstants.KEY_ITEM_QUANTITY, pair.getValue());
            items.put(item);
        }

        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_AGENT_ID, agent.getId());
        data.put(ServerConstants.KEY_AGENT_ROW, agent.getRow());
        data.put(ServerConstants.KEY_AGENT_COL, agent.getCol());
        data.put(ServerConstants.KEY_RACK_ID, rack.getId());
        data.put(ServerConstants.KEY_RACK_ROW, rack.getRow());
        data.put(ServerConstants.KEY_RACK_COL, rack.getCol());
        data.put(ServerConstants.KEY_ORDER_ID, order.getId());
        data.put(ServerConstants.KEY_ITEMS, items);

        return encodeMsg(ServerConstants.TYPE_TASK_COMPLETED, data);
    }

    public static JSONObject encodeOrderLog(int type, Order order) {
        JSONObject ret = new JSONObject();
        ret.put(ServerConstants.KEY_TYPE, type);
        ret.put(ServerConstants.KEY_DATA, order.getId());
        return ret;
    }

    public static JSONObject encodeStatistics(int key, double value) {
        JSONObject ret = new JSONObject();
        ret.put(ServerConstants.KEY_TYPE, key);
        ret.put(ServerConstants.KEY_DATA, value);
        return ret;
    }

    public static JSONObject encodeAckMsg(int type, int status, String msg) {
        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_STATUS, status);
        data.put(ServerConstants.KEY_MSG, msg);
        return encodeMsg(type, data);
    }

    public static JSONObject encodeMsg(int type, JSONObject data) {
        JSONObject ret = new JSONObject();
        ret.put(ServerConstants.KEY_TYPE, type);
        ret.put(ServerConstants.KEY_DATA, data);
        return ret;
    }

    // ===============================================================================================
    //
    // Static Helper Methods
    //

    public static int agentActionToType(AgentAction action) {
        switch (action) {
            case MOVE:
                return ServerConstants.TYPE_AGENT_MOVE;
            case ROTATE_RIGHT:
                return ServerConstants.TYPE_AGENT_ROTATE_RIGHT;
            case ROTATE_LEFT:
                return ServerConstants.TYPE_AGENT_ROTATE_LEFT;
            case RETREAT:
                return ServerConstants.TYPE_AGENT_RETREAT;
            case LOAD:
                return ServerConstants.TYPE_AGENT_LOAD;
            case OFFLOAD:
                return ServerConstants.TYPE_AGENT_OFFLOAD;
            case BIND:
                return ServerConstants.TYPE_AGENT_BIND;
            case UNBIND:
                return ServerConstants.TYPE_AGENT_UNBIND;
        }
        return -1;
    }
}
