package server.utils;

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

import server.exceptions.DataException;

import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


/**
 * This {@code ServerDecoder} class contains useful static functions to decode
 * incoming messages from the frontend and update the {@code Warehouse} in accordance.
 */
public class ServerDecoder {

    //
    // Static Variables
    //

    private static Warehouse warehouse = Warehouse.getInstance();

    // ===============================================================================================
    //
    // Static Main Methods
    //

    public static Warehouse decodeWarehouse(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        JSONObject mapJSON = data.getJSONObject(ServerConstants.KEY_MAP);
        JSONArray gridJSON = mapJSON.getJSONArray(ServerConstants.KEY_GRID);
        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);
        int h = mapJSON.getInt(ServerConstants.KEY_HEIGHT);
        int w = mapJSON.getInt(ServerConstants.KEY_WIDTH);

        //
        // Checks
        //
        if (h < 1 || w < 1) {
            throw new DataException("Warehouse grid with invalid dimensions: (" + h + " x " + w + ").",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        // Configure warehouse
        warehouse.configure(h, w);

        // Define new items in the warehouse
        for (int i = 0; i < itemsJSON.length(); ++i) {
            warehouse.addItem(decodeItem(itemsJSON.getJSONObject(i)));
        }

        // Decode warehouse grid cells
        for (int i = 0; i < h; ++i) {
            JSONArray rowJSON = gridJSON.getJSONArray(i);
            for (int j = 0; j < w; ++j) {
                updateWarehouseCell(rowJSON.getJSONObject(j), i, j);
            }
        }

        // Initialize and return the decoded warehouse
        warehouse.init();
        return warehouse;
    }

    public static void updateWarehouseCell(JSONObject data, int row, int col) throws JSONException, DataException {
        JSONArray objects = data.getJSONArray(ServerConstants.KEY_OBJECTS);

        //
        // Checks
        //
        if (objects.length() > 1) {
            throw new DataException("Cell (" + row + ", " + col + ") has multiple objects. Expecting only one.",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        for (int i = 0; i < objects.length(); ++i) {
            JSONObject obj = objects.getJSONObject(i);
            int type = obj.getInt(ServerConstants.KEY_TYPE);

            switch (type) {
                case ServerConstants.TYPE_CELL_AGENT:
                    warehouse.addAgent(decodeAgent(obj), row, col);
                    break;
                case ServerConstants.TYPE_CELL_RACK:
                    warehouse.addRack(decodeRack(obj), row, col);
                    break;
                case ServerConstants.TYPE_CELL_GATE:
                    warehouse.addGate(decodeGate(obj), row, col);
                    break;
                case ServerConstants.TYPE_CELL_STATION:
                    warehouse.addStation(decodeStation(obj), row, col);
                    break;
                case ServerConstants.TYPE_CELL_OBSTACLE:
                    warehouse.addObstacle(row, col);
                    break;
                default:
                    throw new DataException("Cell (" + row + ", " + col + ") with invalid object type: " + type + ".",
                            ServerConstants.ERR_INVALID_ARGS);
            }
        }
    }

    public static Agent decodeAgent(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int cap = data.getInt(ServerConstants.KEY_AGENT_LOAD_CAPACITY);
        int dir = data.getInt(ServerConstants.KEY_AGENT_DIRECTION);
        String ip = data.getString(ServerConstants.KEY_AGENT_IP);
        String port = data.getString(ServerConstants.KEY_AGENT_PORT);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Agent with negative id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (warehouse.getAgentById(id) != null) {
            throw new DataException("Agent with duplicate id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (cap < 1) {
            throw new DataException("Agent-" + id + " with non-positive load capacity: " + cap + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (dir < 0 || dir > 3) {
            throw new DataException("Agent-" + id + " with invalid direction: " + dir + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        // Create and return to be added into the warehouse
        Agent ret = new Agent(id, cap, decodeDirection(dir));
        ret.setIpAddress(ip);
        ret.setPortNumber(port);
        return ret;
    }

    public static Rack decodeRack(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int cap = data.getInt(ServerConstants.KEY_RACK_CAPACITY);
        int weight = data.getInt(ServerConstants.KEY_RACK_CONTAINER_WEIGHT);
        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Rack with negative id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (warehouse.getRackById(id) != null) {
            throw new DataException("Rack with duplicate id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (cap < 1) {
            throw new DataException("Rack-" + id + " with non-positive capacity: " + cap + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (weight < 0) {
            throw new DataException("Rack-" + id + " with negative weight: " + weight + ".",
                    ServerConstants.ERR_INVALID_ARGS);
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
                    ServerConstants.ERR_RACK_CAP_EXCEEDED, ret.getStoredWeight() - ret.getCapacity());
        }

        // Return to be added into the warehouse
        return ret;
    }

    public static Gate decodeGate(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Gate with negative id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (warehouse.getGateById(id) != null) {
            throw new DataException("Gate with duplicate id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        // Create and return to be added into the warehouse
        return new Gate(id);
    }

    public static Station decodeStation(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Station with negative id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (warehouse.getStationById(id) != null) {
            throw new DataException("Station with duplicate id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        // Create and return to be added into the warehouse
        return new Station(id);
    }

    public static Item decodeItem(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int weight = data.getInt(ServerConstants.KEY_ITEM_WEIGHT);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Item with negative id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (warehouse.getItemById(id) != null) {
            throw new DataException("Item with duplicate id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        // Create and return to be added into the warehouse
        return new Item(id, weight);
    }

    public static Order decodeOrder(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int type = data.getInt(ServerConstants.KEY_TYPE);
        int gateId = data.getInt(ServerConstants.KEY_GATE_ID);
        Gate gate = warehouse.getGateById(gateId);
        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Order with negative id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (warehouse.getOrderById(id) != null) {
            throw new DataException("Order with duplicate id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }
        if (gate == null) {
            throw new DataException("Order-" + id + " is assigned invalid gate with id: " + gateId + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        //
        // Create order
        //
        Order ret;

        switch (type) {
            case ServerConstants.TYPE_ORDER_COLLECT:
                ret = decodeCollectOrder(data, id, gate);
                break;
            case ServerConstants.TYPE_ORDER_REFILL:
                ret = decodeRefillOrder(data, id, gate);
                break;
            default:
                throw new DataException("Order-" + id + " with invalid type: " + type + ".",
                        ServerConstants.ERR_INVALID_ARGS);
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

    public static Direction decodeDirection(int dir) {
        return Direction.values()[dir];
    }

    public static CollectOrder decodeCollectOrder(JSONObject data, int id, Gate gate) {
        // Create collect order
        return new CollectOrder(id, gate);
    }

    public static RefillOrder decodeRefillOrder(JSONObject data, int id, Gate gate) throws DataException {
        // Extract received properties
        int rackId = data.getInt(ServerConstants.KEY_RACK_ID);
        Rack rack = warehouse.getRackById(rackId);

        //
        // Checks
        //
        if (rack == null) {
            throw new DataException("Order-" + id + " is assigned invalid rack with id: " + rackId + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        // Create refill order
        return new RefillOrder(id, gate, rack);
    }

    public static void fillItemsList(JSONArray data, QuantityAddable<Item> cont, String name) throws JSONException, DataException {
        for (int i = 0; i < data.length(); ++i) {
            // Extract item properties
            JSONObject itemJSON = data.getJSONObject(i);
            int itemId = itemJSON.getInt(ServerConstants.KEY_ID);
            int quantity = itemJSON.getInt(ServerConstants.KEY_ITEM_QUANTITY);
            Item item = warehouse.getItemById(itemId);

            //
            // Checks
            //
            if (item == null) {
                throw new DataException(name + " has invalid item with id: " + itemId + ".",
                        ServerConstants.ERR_INVALID_ARGS);
            }
            if (quantity < 1) {
                throw new DataException(name + " has item-" + itemId + " with non-positive quantity: " + quantity + ".",
                        ServerConstants.ERR_INVALID_ARGS);
            }

            // Add to the order
            cont.add(item, quantity);
        }
    }

    public static void checkOrderFeasibility(Order order) throws DataException {
        if (!order.isPending()) {
            throw new DataException("Order-" + order.getId() + " has no assigned items.",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        // TODO: check agent-to-rack reach-ability
        // TODO: check agents availability
        // TODO: check agents ability to load racks of this order

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
                        ServerConstants.ERR_ORDER_INFEASIBLE_COLLECT, order.getId(), list);
            }
        } else {
            //
            // Refill order feasibility checks
            //

            RefillOrder ord = (RefillOrder) order;
            Rack rack = ord.getRefillRack();
            int totWeight = rack.getStoredWeight() + ord.getAddedWeight();

            if (totWeight > rack.getCapacity()) {
                throw new DataException("Refill order-" + order.getId() +
                        " items weight exceed rack-" + rack.getId() + " capacity by: " +
                        (totWeight - rack.getCapacity()) + ".",
                        ServerConstants.ERR_ORDER_INFEASIBLE_REFILL, order.getId(), rack.getId(), totWeight - rack.getCapacity());
            }
        }
    }
}
