package communicators.frontend.utils;

import communicators.frontend.FrontendConstants;

import models.agents.Agent;
import models.items.Item;
import models.tasks.orders.Order;
import models.tasks.Task;

import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;


/**
 * This {@code Decoder} class contains useful static functions to encode
 * messages from the backend to be sent to the frontend.
 */
public class Encoder {

    //
    // Static Main Methods
    //

    public static JSONObject encodeAgentControl(Agent agent, boolean deactivated) {
        JSONObject data = new JSONObject();
        data.put(FrontendConstants.KEY_TYPE, deactivated ? FrontendConstants.TYPE_AGENT_DEACTIVATE : FrontendConstants.TYPE_AGENT_ACTIVATE);
        data.put(FrontendConstants.KEY_ID, agent.getId());
        return encodeMsg(FrontendConstants.TYPE_ACTION, data);
    }

    public static JSONObject encodeAgentAction(Agent agent, int action) {
        JSONObject data = new JSONObject();
        data.put(FrontendConstants.KEY_TYPE, action);
        data.put(FrontendConstants.KEY_ID, agent.getId());
        return encodeMsg(FrontendConstants.TYPE_ACTION, data);
    }

    public static JSONObject encodeAgentBatteryUpdatedLog(Agent agent) {
        JSONObject data = new JSONObject();
        data.put(FrontendConstants.KEY_ID, agent.getId());
        data.put(FrontendConstants.KEY_AGENT_BATTERY_LEVEL, agent.getBatteryLevel());
        return encodeMsg(FrontendConstants.TYPE_LOG, encodeMsg(FrontendConstants.TYPE_LOG_BATTERY_UPDATED, data));
    }

    public static JSONObject encodeOrderTaskAssignedLog(Order order, Task task) {
        JSONObject data = new JSONObject();
        data.put(FrontendConstants.KEY_ORDER_ID, order.getId());
        data.put(FrontendConstants.KEY_AGENT_ID, task.getAgent().getId());
        data.put(FrontendConstants.KEY_RACK_ID, task.getRack().getId());
        return encodeMsg(FrontendConstants.TYPE_LOG, encodeMsg(FrontendConstants.TYPE_LOG_TASK_ASSIGNED, data));
    }

    public static JSONObject encodeOrderTaskCompletedLog(Order order, Task task, Map<Item, Integer> items) {
        JSONArray itemsJSON = new JSONArray();

        for (var pair : items.entrySet()) {
            JSONObject item = new JSONObject();
            item.put(FrontendConstants.KEY_ID, pair.getKey().getId());
            item.put(FrontendConstants.KEY_ITEM_QUANTITY, -pair.getValue());
            itemsJSON.put(item);
        }

        JSONObject data = new JSONObject();
        data.put(FrontendConstants.KEY_ORDER_ID, order.getId());
        data.put(FrontendConstants.KEY_AGENT_ID, task.getAgent().getId());
        data.put(FrontendConstants.KEY_RACK_ID, task.getRack().getId());
        data.put(FrontendConstants.KEY_ITEMS, itemsJSON);

        return encodeMsg(FrontendConstants.TYPE_LOG, encodeMsg(FrontendConstants.TYPE_LOG_TASK_COMPLETED, data));
    }

    public static JSONObject encodeOrderFulfilledLog(Order order) {
        JSONObject data = new JSONObject();
        data.put(FrontendConstants.KEY_ID, order.getId());
        return encodeMsg(FrontendConstants.TYPE_LOG, encodeMsg(FrontendConstants.TYPE_LOG_ORDER_FULFILLED, data));
    }

    public static JSONObject encodeAckMsg(int type, int status, int errCode, String errReason, Object... errArgs) {
        JSONObject data = new JSONObject();
        data.put(FrontendConstants.KEY_STATUS, status);

        if (status == FrontendConstants.TYPE_ERROR) {
            JSONObject msg = new JSONObject();
            msg.put(FrontendConstants.KEY_REASON, errReason);
            msg.put(FrontendConstants.KEY_ID, errCode);
            msg.put(FrontendConstants.KEY_ARGS, errArgs);
            data.put(FrontendConstants.KEY_MSG, msg);
        }

        return encodeMsg(type, data);
    }

    public static JSONObject encodeMsg(int type, JSONObject data) {
        JSONObject ret = new JSONObject();
        ret.put(FrontendConstants.KEY_TYPE, type);
        ret.put(FrontendConstants.KEY_DATA, data);
        return ret;
    }

    // ===============================================================================================
    //
    // Static Helper Methods
    //

    public static int encodeAgentActionType(AgentAction action) {
        switch (action) {
            case MOVE:
                return FrontendConstants.TYPE_AGENT_MOVE;
            case ROTATE_RIGHT:
                return FrontendConstants.TYPE_AGENT_ROTATE_RIGHT;
            case ROTATE_LEFT:
                return FrontendConstants.TYPE_AGENT_ROTATE_LEFT;
            case RETREAT:
                return FrontendConstants.TYPE_AGENT_RETREAT;
            case LOAD:
                return FrontendConstants.TYPE_AGENT_LOAD;
            case OFFLOAD:
                return FrontendConstants.TYPE_AGENT_OFFLOAD;
            case BIND:
                return FrontendConstants.TYPE_AGENT_BIND;
            case UNBIND:
                return FrontendConstants.TYPE_AGENT_UNBIND;
        }
        return -1;
    }
}
