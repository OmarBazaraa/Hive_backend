package models.warehouse;

import communicators.frontend.utils.Decoder;

import utils.Constants;
import utils.Constants.*;
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
        Decoder.decodeWarehouse(new JSONObject(config), RunningMode.SIMULATION);
    }
}
