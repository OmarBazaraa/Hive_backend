package models.facilities;


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

    /**
     * Constructs a new {@code Gate} object.
     *
     * @param id  the id of the {@code Gate}.
     * @param row the row position of the {@code Gate}.
     * @param col the column position of the {@code Gate}.
     */
    public Gate(int id, int row, int col) {
        super(id, row, col);
    }
}
