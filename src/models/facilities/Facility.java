package models.facilities;

import algorithms.Planner;

import models.HiveObject;
import models.agents.Agent;
import models.maps.GuideGrid;
import models.maps.MapGrid;


/**
 * This {@code Facility} class is the base class of all the facility components
 * in our Hive Warehouse System.
 * <p>
 * A facility component is an objects in the warehouse maps that provide services or functions
 * to either an {@link Agent} or to a client.
 * A facility component is typically a static object with fixed location in the warehouse's grid.
 *
 * @see Rack
 * @see Gate
 * @see Station
 */
public class Facility extends HiveObject {

    //
    // Member Variables
    //

    /**
     * The guide maps grid of this {@code Facility}.
     */
    protected GuideGrid guideMap;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Facility} object with the given initial information.
     *
     * @param id  the id of the {@code Facility}.
     * @param row the row position of the {@code Facility}.
     * @param col the column position of the {@code Facility}.
     */
    public Facility(int id, int row, int col) {
        super(id, row, col);
    }

    /**
     * Returns a guide maps to reach this {@code Facility}.
     *
     * @return the {@code GuideGrid} of this {@code Facility}.
     */
    public GuideGrid getGuideMap() {
        return guideMap;
    }

    /**
     * Computes a guide maps to reach this {@code Facility}
     *
     * @param map the {@code MapGrid} of the warehouse where this {@code Facility} is located in.
     */
    public void computeGuideMap(MapGrid map) {
        guideMap = new GuideGrid(Planner.bfs(map, getPosition()));
    }
}
