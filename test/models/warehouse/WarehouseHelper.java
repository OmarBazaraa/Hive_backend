package models.warehouse;

import org.json.JSONObject;
import server.utils.ServerDecoder;

import java.io.BufferedReader;
import java.io.FileReader;


public class WarehouseHelper {

    /**
     * Configures a new {@code Warehouse} configuration using the given file.
     *
     * @param path the path of the configuration file.
     */
    public static void configureWarehouse(String path) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        String line;
        StringBuilder builder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        String config = builder.toString();

        ServerDecoder.decodeInitConfig(new JSONObject(config));
    }
}
