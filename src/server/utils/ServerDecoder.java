package server.utils;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.items.QuantityAddable;
import models.maps.MapCell;
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

    public static void decodeInitConfig(JSONObject data) throws JSONException, DataException {
        JSONObject mapJSON = data.getJSONObject(ServerConstants.KEY_MAP);
        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);

        warehouse.clear();
        decodeWarehouseItems(itemsJSON);
        decodeWarehouseMap(mapJSON);
        warehouse.init();
    }

    public static void decodeWarehouseItems(JSONArray data) throws JSONException, DataException {
        for (int i = 0; i < data.length(); ++i) {
            decodeItem(data.getJSONObject(i));
        }
    }

    public static void decodeWarehouseMap(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int h = data.getInt(ServerConstants.KEY_HEIGHT);
        int w = data.getInt(ServerConstants.KEY_WIDTH);
        JSONArray gridJSON = data.getJSONArray(ServerConstants.KEY_GRID);

        //
        // Checks
        //
        if (h < 1 || w < 1) {
            throw new DataException("Warehouse grid with invalid dimensions: (" + h + " x " + w + ").");
        }

        MapCell[][] grid = new MapCell[h][w];

        for (int i = 0; i < h; ++i) {
            JSONArray rowJSON = gridJSON.getJSONArray(i);
            for (int j = 0; j < w; ++j) {
                grid[i][j] = decodeMapCell(rowJSON.getJSONObject(j), i, j);
            }
        }

        // Update the warehouse
        warehouse.updateMap(grid);
    }

    public static MapCell decodeMapCell(JSONObject data, int row, int col) throws JSONException, DataException {
        MapCell ret = new MapCell();

        JSONArray objects = data.getJSONArray(ServerConstants.KEY_OBJECTS);

        for (int i = 0; i < objects.length(); ++i) {
            JSONObject obj = objects.getJSONObject(i);
            int type = obj.getInt(ServerConstants.KEY_TYPE);

            switch (type) {
                case ServerConstants.TYPE_CELL_AGENT:
                    ret.setAgent(decodeAgent(obj, row, col));
                    break;
                case ServerConstants.TYPE_CELL_OBSTACLE:
                    ret.setFacility(CellType.OBSTACLE, null);
                    break;
                case ServerConstants.TYPE_CELL_RACK:
                    ret.setFacility(CellType.RACK, decodeRack(obj, row, col));
                    break;
                case ServerConstants.TYPE_CELL_GATE:
                    ret.setFacility(CellType.GATE, decodeGate(obj, row, col));
                    break;
                case ServerConstants.TYPE_CELL_STATION:
                    ret.setFacility(CellType.STATION, decodeStation(obj, row, col));
                    break;
                default:
                    throw new DataException("Cell (" + row + ", " + col + ") with invalid facilities type: " + type + ".");
            }
        }

        //
        // Checks
        //
        if (ret.hasAgent() && ret.hasFacility()) {
            throw new DataException("Cell (" + row + ", " + col + ") has both agent and facilities. Expecting only one.");
        }

        return ret;
    }

    public static Agent decodeAgent(JSONObject data, int row, int col) throws JSONException, DataException {
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
            throw new DataException("Agent with negative id: " + id + ".");
        }
        if (warehouse.getAgentById(id) != null) {
            throw new DataException("Agent with duplicate id: " + id + ".");
        }
        if (cap < 1) {
            throw new DataException("Agent-" + id + " with non-positive load capacity: " + cap + ".");
        }
        if (dir < 0 || dir > 3) {
            throw new DataException("Agent-" + id + " with invalid direction: " + dir + ".");
        }

        // Create and add to the warehouse
        Agent ret = new Agent(id, cap, decodeDirection(dir));
        ret.setIpAddress(ip);
        ret.setPortNumber(port);
        ret.setPosition(row, col);
        warehouse.addAgent(ret);
        return ret;
    }

    public static Rack decodeRack(JSONObject data, int row, int col) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int cap = data.getInt(ServerConstants.KEY_RACK_CAPACITY);
        int weight = data.getInt(ServerConstants.KEY_RACK_CONTAINER_WEIGHT);
        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Rack with negative id: " + id + ".");
        }
        if (warehouse.getRackById(id) != null) {
            throw new DataException("Rack with duplicate id: " + id + ".");
        }
        if (cap < 1) {
            throw new DataException("Rack-" + id + " with non-positive capacity: " + cap + ".");
        }
        if (weight < 0) {
            throw new DataException("Rack-" + id + " with negative weight: " + weight + ".");
        }

        // Create rack
        Rack ret = new Rack(id, cap, weight);
        ret.setPosition(row, col);

        // Extract items
        decodeItemsList(itemsJSON, ret, "Rack-" + id);

        //
        // Checks
        //
        if (ret.getStoredWeight() > ret.getCapacity()) {
            throw new DataException("Rack-" + id + " exceeds the maximum storage capacity: " + ret.getStoredWeight() + ".");
        }

        // Add to the warehouse
        warehouse.addRack(ret);
        return ret;
    }

    public static Gate decodeGate(JSONObject data, int row, int col) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Gate with negative id: " + id + ".");
        }
        if (warehouse.getGateById(id) != null) {
            throw new DataException("Gate with duplicate id: " + id + ".");
        }

        // Create and add to the warehouse
        Gate ret = new Gate(id);
        ret.setPosition(row, col);
        warehouse.addGate(ret);
        return ret;
    }

    public static Station decodeStation(JSONObject data, int row, int col) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Station with negative id: " + id + ".");
        }
        if (warehouse.getStationById(id) != null) {
            throw new DataException("Station with duplicate id: " + id + ".");
        }

        // Create and add to the warehouse
        Station ret = new Station(id);
        ret.setPosition(row, col);
        warehouse.addStation(ret);
        return ret;
    }

    public static Item decodeItem(JSONObject data) throws JSONException, DataException {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int weight = data.getInt(ServerConstants.KEY_ITEM_WEIGHT);

        //
        // Checks
        //
        if (id < 0) {
            throw new DataException("Item with negative id: " + id + ".");
        }
        if (warehouse.getItemById(id) != null) {
            throw new DataException("Item with duplicate id: " + id + ".");
        }

        // Create and add to the warehouse
        Item ret = new Item(id, weight);
        warehouse.addItem(ret);
        return ret;
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
            throw new DataException("Order with negative id: " + id + ".");
        }
        if (warehouse.getOrderById(id) != null) {
            throw new DataException("Order with duplicate id: " + id + ".");
        }
        if (gate == null) {
            throw new DataException("Order-" + id + " is assigned invalid gate with id: " + gateId + ".");
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
                throw new DataException("Order-" + id + " with invalid type: " + type + ".");
        }

        // Extract items
        decodeItemsList(itemsJSON, ret, "Order-" + id);

        // Validate order feasibility
        checkOrderFeasibility(ret);

        // Add to the warehouse
        warehouse.addOrder(ret);
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
            throw new DataException("Order-" + id + " is assigned invalid rack with id: " + rackId + ".");
        }

        // Create refill order
        return new RefillOrder(id, gate, rack);
    }

    public static void decodeItemsList(JSONArray data, QuantityAddable<Item> cont, String name) throws JSONException, DataException {
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
                throw new DataException(name + " has invalid item with id: " + itemId + ".");
            }
            if (quantity < 1) {
                throw new DataException(name + " has item-" + itemId + " with non-positive quantity: " + quantity + ".");
            }

            // Add to the order
            cont.add(item, quantity);
        }
    }

    public static void checkOrderFeasibility(Order order) throws DataException {
        if (!order.isPending()) {
            throw new DataException("Order-" + order.getId() + " has no assigned items.");
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
                        " is currently infeasible due to shortage in items: " + list + ".");
            }
        } else {
            //
            // Refill order feasibility checks
            //

            int addedWeight = 0;

            for (var pair : order) {
                Item item = pair.getKey();
                int quantity = pair.getValue();

                addedWeight += item.getWeight() * quantity;
            }

            Rack rack = ((RefillOrder) order).getRefillRack();

            int totWeight = rack.getStoredWeight() + addedWeight;

            if (totWeight > rack.getCapacity()) {
                throw new DataException("Refill order-" + order.getId() +
                        " items weight exceed rack-" + rack.getId() + " capacity by: " +
                        (totWeight - rack.getCapacity()) + ".");
            }
        }
    }
}
