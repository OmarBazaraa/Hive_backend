package models.facilities;

import org.json.JSONObject;


/**
 * This {@code Station} class is a one of the {@link Facility} components
 * in our Hive Warehouse System.
 * <p>
 * A station is a location in the {@link models.warehouses.Warehouse Warehouse} grid
 * where an {@link models.agents.Agent Agent} go to re-charge its batteries.
 *
 * @see models.HiveObject HiveObject
 * @see models.agents.Agent Agent
 * @see models.facilities.Facility Facility
 * @see models.facilities.Rack Rack
 * @see models.facilities.Gate Gate
 * @see models.tasks.Task Task
 */
public class Station extends Facility {

    //
    // Static Methods
    //

    /**
     * Creates a new {@code Station} object from JSON data.
     *
     * @param data the un-parsed JSON data.
     * @param row  the row position of the {@code MapCell} to create.
     * @param col  the column position of the {@code MapCell} to create.
     *
     * @return an {@code Station} object.
     */
    public static Station create(JSONObject data, int row, int col) throws Exception {
        Station ret = new Station();
        ret.setPosition(row, col);
        return ret;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Station} object.
     */
    public Station() {
        super();
    }

    /**
     * Constructs a new {@code Station} object.
     *
     * @param id the id of the {@code Station}.
     */
    public Station(int id) {
        super(id);
    }
}
