package models.components.base;

import algorithms.Planner;
import models.map.MapGrid;
import models.map.GuideCell;
import models.map.base.Position;


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
    public void computeGuideMap(MapGrid map) {
        guideMap = Planner.bfs(map, getPosition());
    }

    /**
     * Returns the guide cell at the given position.
     *
     * @param row the row position of the needed guide cell.
     * @param col the column position of the needed guide cell.
     *
     * @return the needed guide cell.
     */
    public GuideCell getGuideAt(int row, int col) {
        return (this.guideMap != null ? this.guideMap[row][col] : null);
    }

    /**
     * Returns the guide cell at the given position.
     *
     * @param pos the position of the needed guide cell.
     *
     * @return the needed guide cell.
     */
    public GuideCell getGuideAt(Position pos) {
        return this.getGuideAt(pos.row, pos.col);
    }

    /**
     * Returns the estimated distance from the given position to this {@code DstHiveObject}.
     *
     * @param row the row position of the needed guide cell.
     * @param col the column position of the needed guide cell.
     *
     * @return the estimated distance to the given position.
     */
    public int getEstimatedDistance(int row, int col) {
        return (this.guideMap != null ? this.guideMap[row][col].distance : Integer.MAX_VALUE);
    }

    /**
     * Returns the estimated distance from the given position to this {@code DstHiveObject}.
     *
     * @param pos the position of the needed guide cell.
     *
     * @return the estimated distance to the given position.
     */
    public int getEstimatedDistance(Position pos) {
        return this.getEstimatedDistance(pos.row, pos.col);
    }
}
