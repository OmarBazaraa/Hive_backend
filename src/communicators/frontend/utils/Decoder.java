package communicators.frontend.utils;

import communicators.frontend.FrontendConstants;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.items.QuantityAddable;
import models.tasks.orders.CollectOrder;
import models.tasks.orders.Order;
import models.tasks.orders.RefillOrder;
import models.warehouses.Warehouse;

import utils.exceptions.DataException;
import utils.Constants;
import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/**
 * This {@code Decoder} class contains useful static functions to decode
 * incoming messages from the frontend and update the {@code Warehouse} in accordance.
 */
public class Decoder {

    //
    // Static Variables
    //

    private static Warehouse sWarehouse = Warehouse.getInstance();

    private static RunningMode sMode;

    // ===============================================================================================
    //
    // Static Main Methods
    //

    public static Warehouse decodeWarehouse(JSONObject data, RunningMode mode) throws JSONException, DataException {
        // Extract received properties
        JSONObject mapJSON = data.getJSONObject(FrontendConstants.KEY_MAP);
        JSONArray gridJSON = mapJSON.getJSONArray(FrontendConstants.KEY_GRID);
        JSONArray itemsJSON = data.getJSONArray(FrontendConstants.KEY_ITEMS);
        int h = mapJSON.getInt(FrontendConstants.KEY_HEIGHT);
        int w = mapJSON.getInt(FrontendConstants.KEY_WIDTH);

        //
        // Checks
        //
        if (h < 1 || w < 1) {
            throw new DataException("Warehouse grid with invalid dimensions: (" + h + " x " + w + ").",
                    Constants.ERR_INVALID_ARGS);
        }

        // Configure warehouse
        sMode = mode;
        sWarehouse.configure(h, w);

        // Define new items in the warehouse
        for (int i = 0; i < itemsJSON.length(); ++i) {
            sWarehouse.addItem(decodeItem(itemsJSON.getJSONObject(i)));
        }

        // Decode warehouse grid cells
        for (int i = 0; i < h; ++i) {
            JSONArray rowJSON = gridJSON.getJSONArray(i);
            for (int j = 0; j < w; ++j) {
                updateWarehouseCell(rowJSON.getJSONObject(j), i, j);
            }
        }

        // Initialize and return the decoded warehouse
        sWarehouse.init();
        return sWarehouse;
    }

    public static void updateWarehouseCell(JSONObject data, int row, int col) throws JSONException, DataException {
        JSONArray objects = data.getJSONArray(FrontendConstants.KEY_OBJECTS);

        //
        // Checks
        //
        if (objects.length() > 1) {
            throw new DataException("Cell (" + row + ", " + col + ") has multiple objects. Expecting only one.",
                    Constants.ERR_INVALID_ARGS);
        }

        for (int i = 0; i < objects.length(); ++i) {
            JSONObject obj = objects.getJSONObject(i);
            int type = obj.getInt(FrontendConstants.KEY_TYPE);

            switch (type) {
                case FrontendConstants.TYPE_CELL_AGENT:
                    sWarehouse.addAgent(decodeAgent(obj), row, col);
                    break;
                case FrontendConstants.TYPE_CELL_RACK:
                    sWarehouse.addRack(decodeRack(obj), row, col);
                    break;
                case FrontendConstants.TYPE_CELL_GATE:
                    sWarehouse.addGate(decodeGate(obj), row, col);
                    break;
                case FrontendConstants.TYPE_CELL_STATION:
                    sWarehouse.addStation(decodeStation(obj), row, col);
                    break;
                case FrontendConstants.TYPE_CELL_OBSTACLE:
                    sWarehouse.addObstacle(row, col);
                    break;
                default:
                    throw new DataException("Cell (" + row + ", " + col + ") with invalid object type: " + type + ".",
                            Constants.ERR_INVALID_ARGS);
            }
        }
    }

    public static Agent decodeAgent(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(FrontendConstants.KEY_ID);
        int cap = data.getInt(FrontendConstants.KEY_AGENT_LOAD_CAPACITY);
        int dir = data.getInt(FrontendConstants.KEY_AGENT_DIRECTION);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Agent with negative id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (sWarehouse.getAgentById(id) != null) {
            throw new DataException("Agent with duplicate id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (cap < 1) {
            throw new DataException("Agent-" + id + " with non-positive load capacity: " + cap + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (dir < 0 || dir > 3) {
            throw new DataException("Agent-" + id + " with invalid direction: " + dir + ".",
                    Constants.ERR_INVALID_ARGS);
        }

        // Create new agent
        Agent ret = new Agent(id, cap);
        ret.setDirection(dir);

        // Deployment mode
        if (sMode == RunningMode.DEPLOYMENT) {
            String ipStr = data.getString(FrontendConstants.KEY_AGENT_IP);
            String portStr = data.getString(FrontendConstants.KEY_AGENT_PORT);

            try {
                ret.setIpAddress(InetAddress.getByName(ipStr));
                ret.setPortNumber(Integer.parseInt(portStr));
            } catch (UnknownHostException ex) {
                throw new DataException("Agent-" + id + " with invalid IP address: " + ipStr + ".",
                        Constants.ERR_INVALID_ARGS);
            } catch (NumberFormatException ex) {
                throw new DataException("Agent-" + id + " with invalid port number: " + portStr + ".",
                        Constants.ERR_INVALID_ARGS);
            }
        }

        // Return to be added into the warehouse
        return ret;
    }

    public static Rack decodeRack(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(FrontendConstants.KEY_ID);
        int cap = data.getInt(FrontendConstants.KEY_RACK_CAPACITY);
        int weight = data.getInt(FrontendConstants.KEY_RACK_CONTAINER_WEIGHT);
        JSONArray itemsJSON = data.getJSONArray(FrontendConstants.KEY_ITEMS);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Rack with negative id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (sWarehouse.getRackById(id) != null) {
            throw new DataException("Rack with duplicate id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (cap < 1) {
            throw new DataException("Rack-" + id + " with non-positive capacity: " + cap + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (weight < 0) {
            throw new DataException("Rack-" + id + " with negative weight: " + weight + ".",
                    Constants.ERR_INVALID_ARGS);
        }

        // Create rack and decode its items
        Rack ret = new Rack(id, cap, weight);
        fillItemsList(itemsJSON, ret, "Rack-" + id);

        //
        // Checks
        //
        if (ret.getStoredWeight() > ret.getCapacity()) {
            throw new DataException("Rack-" + id + " exceeds the maximum storage capacity by: " +
                    (ret.getStoredWeight() - ret.getCapacity()) + ".",
                    Constants.ERR_RACK_CAP_EXCEEDED, ret.getStoredWeight() - ret.getCapacity());
        }

        // Return to be added into the warehouse
        return ret;
    }

    public static Gate decodeGate(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(FrontendConstants.KEY_ID);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Gate with negative id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (sWarehouse.getGateById(id) != null) {
            throw new DataException("Gate with duplicate id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }

        // Create and return to be added into the warehouse
        return new Gate(id);
    }

    public static Station decodeStation(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(FrontendConstants.KEY_ID);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Station with negative id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (sWarehouse.getStationById(id) != null) {
            throw new DataException("Station with duplicate id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }

        // Create and return to be added into the warehouse
        return new Station(id);
    }

    public static Item decodeItem(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(FrontendConstants.KEY_ID);
        int weight = data.getInt(FrontendConstants.KEY_ITEM_WEIGHT);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Item with negative id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (sWarehouse.getItemById(id) != null) {
            throw new DataException("Item with duplicate id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }

        // Create and return to be added into the warehouse
        return new Item(id, weight);
    }

    public static Order decodeOrder(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(FrontendConstants.KEY_ID);
        int type = data.getInt(FrontendConstants.KEY_TYPE);
        int gateId = data.getInt(FrontendConstants.KEY_GATE_ID);
        Gate gate = sWarehouse.getGateById(gateId);
        JSONArray itemsJSON = data.getJSONArray(FrontendConstants.KEY_ITEMS);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Order with negative id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (sWarehouse.getOrderById(id) != null) {
            throw new DataException("Order with duplicate id: " + id + ".",
                    Constants.ERR_INVALID_ARGS);
        }
        if (gate == null) {
            throw new DataException("Order-" + id + " is assigned invalid gate with id: " + gateId + ".",
                    Constants.ERR_INVALID_ARGS);
        }

        //
        // Create order
        //
        Order ret;

        switch (type) {
            case FrontendConstants.TYPE_ORDER_COLLECT:
                ret = decodeCollectOrder(data, id, gate);
                break;
            case FrontendConstants.TYPE_ORDER_REFILL:
                ret = decodeRefillOrder(data, id, gate);
                break;
            default:
                throw new DataException("Order-" + id + " with invalid type: " + type + ".",
                        Constants.ERR_INVALID_ARGS);
        }

        // Check items
        if (itemsJSON.isEmpty()) {
            throw new DataException("Order-" + ret.getId() + " has no assigned items.",
                    Constants.ERR_INVALID_ARGS);
        }

        // Extract items
        fillItemsList(itemsJSON, ret, "Order-" + id);

        // Validate order feasibility
        checkOrderFeasibility(ret);

        // Return to be added into the warehouse
        return ret;
    }

    // ===============================================================================================
    //
    // Static Helper Methods
    //

    public static CollectOrder decodeCollectOrder(JSONObject data, int id, Gate gate) {
        // Create collect order
        return new CollectOrder(id, gate);
    }

    public static RefillOrder decodeRefillOrder(JSONObject data, int id, Gate gate) throws DataException {
        // Extract received properties
        int rackId = data.getInt(FrontendConstants.KEY_RACK_ID);
        Rack rack = sWarehouse.getRackById(rackId);

        //
        // Checks
        //
        if (rack == null) {
            throw new DataException("Order-" + id + " is assigned invalid rack with id: " + rackId + ".",
                    Constants.ERR_INVALID_ARGS);
        }

        // Create refill order
        return new RefillOrder(id, gate, rack);
    }

    public static void fillItemsList(JSONArray data, QuantityAddable<Item> cont, String name) throws JSONException, DataException {
        for (int i = 0; i < data.length(); ++i) {
            // Extract item properties
            JSONObject itemJSON = data.getJSONObject(i);
            int itemId = itemJSON.getInt(FrontendConstants.KEY_ID);
            int quantity = itemJSON.getInt(FrontendConstants.KEY_ITEM_QUANTITY);
            Item item = sWarehouse.getItemById(itemId);

            //
            // Checks
            //
            if (item == null) {
                throw new DataException(name + " has invalid item with id: " + itemId + ".",
                        Constants.ERR_INVALID_ARGS);
            }
            if (quantity < 1) {
                throw new DataException(name + " has item-" + itemId + " with non-positive quantity: " + quantity + ".",
                        Constants.ERR_INVALID_ARGS);
            }

            // Add to the order
            cont.add(item, quantity);
        }
    }

    public static void checkOrderFeasibility(Order order) throws DataException {
        if (order instanceof CollectOrder) {
            //
            // Collect order feasibility checks
            //

            List<Integer> list = new ArrayList<>();

            for (var pair : order) {
                Item item = pair.getKey();
                int quantity = pair.getValue();

                if (item.getAvailableUnits() < quantity) {
                    list.add(item.getId());
                }
            }

            if (list.size() > 0) {
                throw new DataException("Collect order-" + order.getId() +
                        " is currently infeasible due to shortage in items: " + list + ".",
                        Constants.ERR_INFEASIBLE_COLLECT_ORDER_NO_ITEMS, order.getId(), list);
            }
        } else {
            //
            // Refill order feasibility checks
            //

            RefillOrder ord = (RefillOrder) order;
            Rack rack = ord.getRefillRack();
            Gate gate = ord.getDeliveryGate();
            int totWeight = rack.getStoredWeight() + ord.getAddedWeight();

            if (totWeight > rack.getCapacity()) {
                throw new DataException("Refill order-" + order.getId() +
                        " items weight exceed rack-" + rack.getId() + " capacity by: " + (totWeight - rack.getCapacity()) + ".",
                        Constants.ERR_INFEASIBLE_REFILL_ORDER_NO_SPACE, order.getId(), rack.getId(), totWeight - rack.getCapacity());
            }

            if (!sWarehouse.isReachable(rack, gate)) {
                throw new DataException("Refill order-" + order.getId() +
                        " is infeasible due to rack-to-gate unreachability.",
                        Constants.ERR_INFEASIBLE_REFILL_ORDER_RACK_GATE_UNREACHABLE, order.getId(), rack.getId(), gate.getId());
            }
        }
    }
}
