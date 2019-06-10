package server;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.items.QuantityAddable;
import models.maps.MapCell;
import models.tasks.Order;
import models.warehouses.Warehouse;

import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;


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
    // Static Methods
    //

    /**
     * Decodes the incoming initial configuration message from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static void decodeInitConfig(JSONObject data) throws Exception {
        int mode = data.getInt(ServerConstants.KEY_MODE);
        JSONObject stateJSON = data.getJSONObject(ServerConstants.KEY_STATE);
        JSONObject mapJSON = stateJSON.getJSONObject(ServerConstants.KEY_MAP);
        JSONArray itemsJSON = stateJSON.getJSONArray(ServerConstants.KEY_ITEMS);
        decodeWarehouseItems(itemsJSON);
        decodeWarehouseMap(mapJSON);
    }

    /**
     * Decodes the incoming items from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static void decodeWarehouseItems(JSONArray data) throws Exception {
        for (int i = 0; i < data.length(); ++i) {
            decodeItem(data.getJSONObject(i));
        }
    }

    /**
     * Decodes the incoming map grid from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static void decodeWarehouseMap(JSONObject data) throws Exception {
        // Extract received properties
        int h = data.getInt(ServerConstants.KEY_HEIGHT);
        int w = data.getInt(ServerConstants.KEY_WIDTH);
        JSONArray gridJSON = data.getJSONArray(ServerConstants.KEY_GRID);

        //
        // Checks
        //
        if (h < 1 || w < 1) {
            throw new Exception("Warehouse grid with invalid dimensions: (" + h + " x " + w + ").");
        }

        MapCell[][] grid = new MapCell[h][w];

        for (int i = 0; i < h; ++i) {
            JSONArray rowJSON = gridJSON.getJSONArray(i);
            for (int j = 0; j < w; ++j) {
                grid[i][j] = decodeCell(rowJSON.getJSONObject(j), i, j);
            }
        }

        // Update the warehouse
        warehouse.updateMap(grid);
    }

    /**
     * Decodes the incoming grid cell from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static MapCell decodeCell(JSONObject data, int row, int col) throws Exception {
        MapCell ret = new MapCell();

        if (data.has(ServerConstants.KEY_AGENT)) {
            JSONObject agentJSON = data.getJSONObject(ServerConstants.KEY_AGENT);
            ret.setAgent(decodeAgent(agentJSON, row, col));
        }

        if (data.has(ServerConstants.KEY_FACILITY)) {
            JSONObject facilityJSON = data.getJSONObject(ServerConstants.KEY_FACILITY);
            int type = data.getInt(ServerConstants.KEY_TYPE);

            switch (type) {
                case ServerConstants.TYPE_CELL_OBSTACLE:
                    ret.setFacility(CellType.OBSTACLE, null);
                    break;
                case ServerConstants.TYPE_CELL_RACK:
                    ret.setFacility(CellType.RACK, decodeRack(facilityJSON, row, col));
                    break;
                case ServerConstants.TYPE_CELL_GATE:
                    ret.setFacility(CellType.GATE, decodeGate(facilityJSON, row, col));
                    break;
                case ServerConstants.TYPE_CELL_STATION:
                    ret.setFacility(CellType.STATION, decodeStation(facilityJSON, row, col));
                    break;
                default:
                    throw new Exception("Cell (" + row + ", " + col + ") with invalid facility type: " + type + ".");
            }
        }

        //
        // Checks
        //
        if (ret.hasAgent() && ret.hasFacility()) {
            throw new Exception("Cell (" + row + ", " + col + ") has both agent and facility. Expecting only one.");
        }

        return ret;
    }

    /**
     * Decodes the incoming {@code Agent} from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static Agent decodeAgent(JSONObject data, int row, int col) throws Exception {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int cap = data.getInt(ServerConstants.KEY_AGENT_LOAD_CAPACITY);
        int dir = data.getInt(ServerConstants.KEY_AGENT_DIRECTION);

        //
        // Checks
        //
        if (id < 0) {
            throw new Exception("Agent with negative id: " + id + ".");
        }
        if (warehouse.getAgentById(id) != null) {
            throw new Exception("Agent with duplicate id: " + id + ".");
        }
        if (cap < 1) {
            throw new Exception("Agent-" + id + " with non-positive load capacity: " + cap + ".");
        }
        if (dir < 0 || dir > 3) {
            throw new Exception("Agent-" + id + " with invalid direction: " + dir + ".");
        }

        // Create and add to the warehouse
        Agent ret = new Agent(id, cap, Direction.values()[dir]);
        ret.setPosition(row, col);
        warehouse.addAgent(ret);
        return ret;
    }

    /**
     * Decodes the incoming {@code Rack} from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static Rack decodeRack(JSONObject data, int row, int col) throws Exception {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int cap = data.getInt(ServerConstants.KEY_RACK_CAPACITY);
        int weight = data.getInt(ServerConstants.KEY_RACK_CONTAINER_WEIGHT);
        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);

        //
        // Checks
        //
        if (id < 0) {
            throw new Exception("Rack with negative id: " + id + ".");
        }
        if (warehouse.getRackById(id) != null) {
            throw new Exception("Rack with duplicate id: " + id + ".");
        }
        if (cap < 1) {
            throw new Exception("Rack-" + id + " with non-positive capacity: " + cap + ".");
        }
        if (weight < 0) {
            throw new Exception("Rack-" + id + " with negative weight: " + weight + ".");
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
            throw new Exception("Rack-" + id + " exceeds the maximum storage capacity: " + ret.getStoredWeight() + ".");
        }

        // Add to the warehouse
        warehouse.addRack(ret);
        return ret;
    }

    /**
     * Decodes the incoming {@code Gate} from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static Gate decodeGate(JSONObject data, int row, int col) throws Exception {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);

        //
        // Checks
        //
        if (id < 0) {
            throw new Exception("Gate with negative id: " + id + ".");
        }
        if (warehouse.getGateById(id) != null) {
            throw new Exception("Gate with duplicate id: " + id + ".");
        }

        // Create and add to the warehouse
        Gate ret = new Gate(id);
        ret.setPosition(row, col);
        warehouse.addGate(ret);
        return ret;
    }

    /**
     * Decodes the incoming {@code Station} from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static Station decodeStation(JSONObject data, int row, int col) throws Exception {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);

        //
        // Checks
        //
        if (id < 0) {
            throw new Exception("Station with negative id: " + id + ".");
        }
        if (warehouse.getStationById(id) != null) {
            throw new Exception("Station with duplicate id: " + id + ".");
        }

        // Create and add to the warehouse
        Station ret = new Station(id);
        ret.setPosition(row, col);
        warehouse.addStation(ret);
        return ret;
    }

    /**
     * Decodes the incoming {@code Item} from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static Item decodeItem(JSONObject data) throws Exception {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int weight = data.getInt(ServerConstants.KEY_ITEM_WEIGHT);

        //
        // Checks
        //
        if (id < 0) {
            throw new Exception("Item with negative id: " + id + ".");
        }
        if (warehouse.getItemById(id) != null) {
            throw new Exception("Item with duplicate id: " + id + ".");
        }

        // Create and add to the warehouse
        Item ret = new Item(id, weight);
        warehouse.addItem(ret);
        return ret;
    }

    /**
     * Decodes the incoming {@code Order} from the frontend,
     * and updates the {@code Warehouse} in accordance.
     *
     * @param data the JSON data to decode.
     */
    public static Order decodeOrder(JSONObject data) throws Exception {
        // Extract received properties
        int id = data.getInt(ServerConstants.KEY_ID);
        int startTime = data.getInt(ServerConstants.KEY_ORDER_START_TIME);
        int type = data.getInt(ServerConstants.KEY_TYPE);
        int gateId = data.getInt(ServerConstants.KEY_GATE_ID);
        int rackId = data.optInt(ServerConstants.KEY_RACK_ID);
        Gate gate = warehouse.getGateById(gateId);
        Gate rack = warehouse.getGateById(rackId);
        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);

        //
        // Checks
        //
        if (id < 0) {
            throw new Exception("Order with negative id: " + id + ".");
        }
        if (warehouse.getOrderById(id) != null) {
            throw new Exception("Order with duplicate id: " + id + ".");
        }
        if (startTime < 0) {
            throw new Exception("Order-" + id + " with negative start time: " + startTime + ".");
        }
        if (type != ServerConstants.TYPE_ORDER_COLLECT && type != ServerConstants.TYPE_ORDER_REFILL) {
            throw new Exception("Order-" + id + " with invalid type: " + type + ".");
        }
        if (gate == null) {
            throw new Exception("Order-" + id + " is assigned invalid gate with id: " + gateId + ".");
        }
        if (rack == null && type == ServerConstants.TYPE_ORDER_REFILL) {
            throw new Exception("Order-" + id + " is assigned invalid rack with id: " + gateId + ".");
        }

        // Create order
        Order ret = new Order(id);

        // Extract items
        decodeItemsList(itemsJSON, ret, "Order-" + id);

        //
        // Checks
        //
        if (!ret.isFeasible()) {
            throw new Exception("Order-" + id + " is currently infeasible due to items shortage.");
        }

        // Add to the warehouse
        warehouse.addOrder(ret);
        return ret;
    }

    /**
     * Decodes the incoming array of items and adds them to the given container.
     *
     * @param data a JSON array of items to be decoded.
     * @param cont the container to add the decode items into.
     * @param name the name of the item container.
     */
    public static void decodeItemsList(JSONArray data, QuantityAddable<Item> cont, String name) throws Exception {
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
                throw new Exception(name + " has invalid item with id: " + itemId + ".");
            }
            if (quantity < 1) {
                throw new Exception(name + " has item-" + itemId + " with non-positive quantity: " + quantity + ".");
            }

            // Add to the order
            cont.add(item, quantity);
        }
    }
}
