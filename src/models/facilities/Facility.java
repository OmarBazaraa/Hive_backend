package models.facilities;

import algorithms.Planner;
import models.TerminalHiveObject;
import models.map.GuideGrid;
import models.map.MapGrid;


/**
 * This {@code Facility} class is the base class of all the facility components
 * in our Hive Warehouse System.
 * <p>
 * Facility component is an objects in the warehouse map that provide services or functions
 * to either an {@code Agent} or to a client. Facility component is typically static object
 * with fixed location in the warehouse's grid.
 * <p>
 * @see Rack
 * @see Gate
 * @see Station
 */
public class Facility extends TerminalHiveObject {

    //
    // Member Variables
    //

    /**
     * The guide map grid of this {@code Facility}.
     */
    protected GuideGrid guideMap;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new destination Hive object.
     *
     * @param id  the id of the Hive object.
     * @param row the row position of the Hive object.
     * @param col the column position of the Hive object.
     */
    public Facility(int id, int row, int col) {
        super(id, row, col);
    }

    /**
     * Returns the guide map of this {@code Facility}.
     *
     * @return the guide map of this {@code Facility}.
     */
    public GuideGrid getGuideMap() {
        return guideMap;
    }

    /**
     * Computes the guide map of this {@code Facility}
     *
     * @param map the grid map of our Hive System.
     */
    public void computeGuideMap(MapGrid map) {
        guideMap = new GuideGrid(Planner.bfs(map, getPosition()));
    }
}
