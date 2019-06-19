package server.utils;

import models.agents.Agent;

import models.items.Item;
import models.tasks.orders.Order;
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
    // Static Main Methods
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
        return encodeMsg(encodeAgentAction(action), data);
    }

    public static JSONObject encodeTaskAssignedLog(Task task, Order order) {
        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_ORDER_ID, order.getId());
        data.put(ServerConstants.KEY_AGENT_ID, task.getAgent().getId());
        data.put(ServerConstants.KEY_RACK_ID, task.getRack().getId());
        return encodeMsg(ServerConstants.TYPE_LOG_TASK_ASSIGNED, data);
    }

    public static JSONObject encodeTaskCompletedLog(Task task, Order order, Map<Item, Integer> items) {
        JSONArray itemsJSON = new JSONArray();

        for (var pair : items.entrySet()) {
            JSONObject item = new JSONObject();
            item.put(ServerConstants.KEY_ID, pair.getKey().getId());
            item.put(ServerConstants.KEY_ITEM_QUANTITY, -pair.getValue());
            itemsJSON.put(item);
        }

        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_ORDER_ID, order.getId());
        data.put(ServerConstants.KEY_AGENT_ID, task.getAgent().getId());
        data.put(ServerConstants.KEY_RACK_ID, task.getRack().getId());
        data.put(ServerConstants.KEY_ITEMS, itemsJSON);

        return encodeMsg(ServerConstants.TYPE_LOG_TASK_COMPLETED, data);
    }

    public static JSONObject encodeOrderLog(int type, Order order) {
        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_ID, order.getId());
        return encodeMsg(type, data);
    }

    public static JSONObject encodeStatistics(int key, double value) {
        JSONObject ret = new JSONObject();
        ret.put(ServerConstants.KEY_TYPE, key);
        ret.put(ServerConstants.KEY_DATA, value);
        return ret;
    }

    public static JSONObject encodeControlMsg(JSONArray activated, JSONArray deactivated, JSONArray blocked) {
        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_ACTIVATED, activated);
        data.put(ServerConstants.KEY_DEACTIVATED, deactivated);
        data.put(ServerConstants.KEY_BLOCKED, blocked);
        return encodeMsg(ServerConstants.TYPE_CONTROL, data);
    }

    public static JSONObject encodeAckMsg(int type, int status, int errCode, String errReason, Object... errArgs) {
        JSONObject data = new JSONObject();
        data.put(ServerConstants.KEY_STATUS, status);

        if (status == ServerConstants.TYPE_ERROR) {
            JSONObject msg = new JSONObject();
            msg.put(ServerConstants.KEY_REASON, errReason);
            msg.put(ServerConstants.KEY_ID, errCode);
            msg.put(ServerConstants.KEY_ARGS, errArgs);
            data.put(ServerConstants.KEY_MSG, msg);
        }

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

    public static int encodeAgentAction(AgentAction action) {
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
