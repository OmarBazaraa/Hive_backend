package server;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.maps.MapCell;
import models.tasks.Order;
import models.warehouses.Warehouse;

import utils.Constants;
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

    public static void decodeInitConfig(JSONObject data) {
        int mode = data.getInt(ServerConstants.KEY_MODE);
        JSONObject stateJSON = data.getJSONObject(ServerConstants.KEY_STATE);
        JSONObject mapJSON = stateJSON.getJSONObject(ServerConstants.KEY_MAP);
        JSONArray itemsJSON = stateJSON.getJSONArray(ServerConstants.KEY_ITEMS);

        decodeItems(itemsJSON);
        decodeMap(mapJSON);
    }

    public static void decodeItems(JSONArray data) {
        for (int i = 0; i < data.length(); ++i) {
            decodeItem(data.getJSONObject(i));
        }
    }

    public static void decodeMap(JSONObject data) {
        int h = data.getInt(ServerConstants.KEY_HEIGHT);
        int w = data.getInt(ServerConstants.KEY_WIDTH);

        JSONArray gridJSON = data.getJSONArray(ServerConstants.KEY_GRID);

        MapCell[][] grid = new MapCell[h][w];

        for (int i = 0; i < h; ++i) {
            JSONArray rowJSON = gridJSON.getJSONArray(i);
            for (int j = 0; j < w; ++j) {
                grid[i][j] = decodeCell(rowJSON.getJSONObject(j), i, j);
            }
        }

        warehouse.updateMap(grid);
    }

    public static MapCell decodeCell(JSONObject data, int row, int col) {
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
            }
        }

        return ret;
    }

    public static Agent decodeAgent(JSONObject data, int row, int col) {
        int id = data.getInt(ServerConstants.KEY_ID);
        int cap = data.getInt(ServerConstants.KEY_AGENT_LOAD_CAPACITY);
        int dir = data.getInt(ServerConstants.KEY_AGENT_DIRECTION);
        Agent ret = new Agent(id, cap, Direction.values()[dir]);
        ret.setPosition(row, col);
        warehouse.addAgent(ret);
        return ret;
    }

    public static Rack decodeRack(JSONObject data, int row, int col) {
        int id = data.getInt(ServerConstants.KEY_ID);
        int cap = data.getInt(ServerConstants.KEY_RACK_CAPACITY);
        int weight = data.getInt(ServerConstants.KEY_RACK_CONTAINER_WEIGHT);
        Rack ret = new Rack(id, cap, weight);
        ret.setPosition(row, col);

        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);

        for (int i = 0; i < itemsJSON.length(); ++i) {
            JSONObject itemJSON = itemsJSON.getJSONObject(i);

            int itemId = itemJSON.getInt(ServerConstants.KEY_ID);
            int quantity = itemJSON.getInt(ServerConstants.KEY_ITEM_QUANTITY);
            Item item = warehouse.getItemById(itemId);

            ret.add(item, quantity);
        }

        warehouse.addRack(ret);
        return ret;
    }

    public static Gate decodeGate(JSONObject data, int row, int col) {
        int id = data.getInt(ServerConstants.KEY_ID);
        Gate ret = new Gate(id);
        ret.setPosition(row, col);
        warehouse.addGate(ret);
        return ret;
    }

    public static Station decodeStation(JSONObject data, int row, int col) {
        int id = data.getInt(ServerConstants.KEY_ID);
        Station ret = new Station(id);
        ret.setPosition(row, col);
        warehouse.addStation(ret);
        return ret;
    }

    public static Item decodeItem(JSONObject data) {
        int id = data.getInt(ServerConstants.KEY_ID);
        int weight = data.getInt(ServerConstants.KEY_ITEM_WEIGHT);
        Item ret = new Item(id, weight);
        warehouse.addItem(ret);
        return ret;
    }

    public static Order decodeOrder(JSONObject data) {
        int id = data.getInt(ServerConstants.KEY_ID);
        int startTime = data.getInt(ServerConstants.KEY_ORDER_START_TIME);
        int type = data.getInt(ServerConstants.KEY_TYPE);
        int gateId = data.getInt(ServerConstants.KEY_GATE_ID);
        int rackId = data.optInt(ServerConstants.KEY_RACK_ID);
        Order ret = new Order(id);

        JSONArray itemsJSON = data.getJSONArray(ServerConstants.KEY_ITEMS);

        for (int i = 0; i < itemsJSON.length(); ++i) {
            JSONObject itemJSON = itemsJSON.getJSONObject(i);

            int itemId = itemJSON.getInt(ServerConstants.KEY_ID);
            int quantity = itemJSON.getInt(ServerConstants.KEY_ITEM_QUANTITY);
            Item item = warehouse.getItemById(itemId);

            ret.add(item, quantity);
        }

        return ret;
    }
}
