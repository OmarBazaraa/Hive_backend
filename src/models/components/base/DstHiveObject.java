package models.components.base;

import algorithms.Planner;
import models.map.GuideGrid;
import models.map.MapGrid;
import models.map.GuideCell;
import models.map.base.Position;
import utils.Constants;


/**
 * This {@code DstHiveObject} class is the base class of all the destination terminal Hive System's components
 * such as {@code Rack}, {@code Gate}, {@code Station}, ..etc.
 * <p>
 * Destination Hive object's are by default static object with fixed position in the Hive's map.
 */
public class DstHiveObject extends TerminalHiveObject {

    //
    // Member Variables
    //

    /**
     * The guide map grid of this {@code DstHiveObject}.
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
     * @param id  the column position of the Hive object.
     */
    public DstHiveObject(int id, int row, int col) {
        super(id, row, col);
    }

    /**
     * Returns the guide map of this {@code DstHiveObject}.
     *
     * @return the guide map of this {@code DstHiveObject}.
     */
    public GuideGrid getGuideMap() {
        return guideMap;
    }

    /**
     * Computes the guide map of this {@code DstHiveObject}
     *
     * @param map the grid map of our Hive System.
     */
    public void computeGuideMap(MapGrid map) {
        guideMap = new GuideGrid(Planner.bfs(map, getPosition()));
    }
}
