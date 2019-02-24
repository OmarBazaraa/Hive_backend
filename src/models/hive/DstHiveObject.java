package models.hive;

import algorithms.Planner;
import models.map.Grid;
import models.map.GuideCell;


/**
 * This {@code DstHiveObject} class is the base class of all the destination terminal Hive System's components
 * such as {@code Rack}, {@code Gate}, {@code ChargingStation}, ..etc.
 * <p>
 * Destination Hive object's are by default static object with fixed position in the Hive's map.
 */
public class DstHiveObject extends HiveObject {

    //
    // Member Variables
    //

    /**
     * The guide map of this {@code DstHiveObject}.
     */
    protected GuideCell[][] guideMap;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new destination Hive object.
     *
     * @param id the id of the Hive object.
     */
    public DstHiveObject(int id) {
        super(id);
    }

    /**
     * Constructs a new destination Hive object.
     *
     * @param id  the id of the Hive object.
     * @param row the row position of the Hive object.
     * @param id  the column position of the Hive object.
     */
    public DstHiveObject(int id, int row, int col) {
        super(id, row, col);
    }

    /**
     * Computes the guide map of this {@code DstHiveObject}
     *
     * @param map the grid map of our Hive System.
     */
    public void computeGuideMap(Grid map) {
        guideMap = Planner.bfs(map, getPosition());
    }
}
