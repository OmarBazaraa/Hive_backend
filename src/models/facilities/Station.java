package models.facilities;


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

    /**
     * Constructs a new {@code Station} object.
     *
     * @param id  the id of the {@code Station}.
     * @param row the row position of the {@code Station}.
     * @param col the column position of the {@code Station}.
     */
    public Station(int id, int row, int col) {
        super(id, row, col);
    }
}
