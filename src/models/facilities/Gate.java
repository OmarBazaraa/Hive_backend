package models.facilities;

import org.json.JSONObject;


/**
 * This {@code Gate} class is a one of the {@link Facility} components
 * in our Hive Warehouse System.
 * <p>
 * A gate is a location in the {@link models.warehouses.Warehouse Warehouse} grid
 * where an {@link models.agents.Agent Agent} deliver {@link Rack} of {@link models.items.Item Items}.
 *
 * @see models.HiveObject HiveObject
 * @see models.agents.Agent Agent
 * @see models.facilities.Facility Facility
 * @see models.facilities.Rack Rack
 * @see models.facilities.Station Station
 * @see models.tasks.Task Task
 */
public class Gate extends Facility {

    //
    // Static Methods
    //

    /**
     * Creates a new {@code Gate} object from JSON data.
     *
     * @param data the un-parsed JSON data.
     * @param row  the row position of the {@code MapCell} to create.
     * @param col  the column position of the {@code MapCell} to create.
     *
     * @return an {@code Gate} object.
     */
    public static Gate create(JSONObject data, int row, int col) throws Exception {
        Gate ret = new Gate();
        ret.setPosition(row, col);
        return ret;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Gate} object.
     */
    public Gate() {
        super();
    }

    /**
     * Constructs a new {@code Gate} object.
     *
     * @param id  the id of the {@code Gate}.
     */
    public Gate(int id) {
        super(id);
    }
}
