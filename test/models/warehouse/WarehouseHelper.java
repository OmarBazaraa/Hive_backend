package models.warehouse;

import server.utils.ServerDecoder;

import utils.Utility;

import org.json.JSONObject;


public class WarehouseHelper {

    /**
     * Configures a new {@code Warehouse} configuration using the given file.
     *
     * @param path the path of the configuration file.
     */
    public static void configureWarehouse(String path) throws Exception {
        String config = Utility.readFile(path);
        ServerDecoder.decodeWarehouse(new JSONObject(config));
    }
}
