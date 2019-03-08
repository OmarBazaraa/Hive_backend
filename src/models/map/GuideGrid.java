package models.map;

import models.map.base.Grid;
import models.map.base.Position;
import utils.Constants.*;
import utils.Utility;

import java.util.ArrayList;
import java.util.List;


/**
 * This {@code GuideGrid} class represents the guide map of {@code DstHiveObject}.
 */
public class GuideGrid extends Grid<GuideCell> {

    //
    // Member Variables
    //

    private CellType accessibleCellType;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new grid.
     *
     * @param grid the grid configurations.
     */
    public GuideGrid(GuideCell[][] grid, CellType accessibleCellType) {
        super(grid);
        this.accessibleCellType = accessibleCellType;
    }

    /**
     * Returns the accessible {@code CellType} of this guide map.
     *
     * @return the accessible {@code CellType} of this guide map.
     */
    public CellType getAccessibleCellType() {
        return accessibleCellType;
    }

    /**
     * Sets the accessible {@code CellType} of this guide map.
     *
     * @param accessibleCellType the accessible {@code CellType} to set.
     */
    public void setAccessibleCellType(CellType accessibleCellType) {
        this.accessibleCellType = accessibleCellType;
    }

    /**
     * Returns the minimum distance to reach the associated {@code DstHiveObject}
     * from the given cell and vice versa.
     *
     * @param row the row position of the needed cell.
     * @param col the column position of the needed cell.
     *
     * @return the minimum distance to the given position.
     */
    public int getDistance(int row, int col) {
        return isInBound(row, col) ? grid[row][col].distance : Integer.MAX_VALUE;
    }

    /**
     * Returns the minimum distance to reach the associated {@code DstHiveObject}
     * from the given cell and vice versa.
     *
     * @param pos the position of the needed cell.
     *
     * @return the minimum distance to the given position.
     */
    public int getDistance(Position pos) {
        return getDistance(pos.row, pos.col);
    }


    public List<Direction> getGuideDirections(Position pos) {
        List<Direction> ret = new ArrayList<>();

        //
        // Iterate over all direction and select the ones that lead to the target
        //
        for (Direction dir : Direction.values()) {
            // Get next position
            Position nxt = next(pos, dir);

            // Skip if moving away from the target
            if (getDistance(nxt) >= getDistance(pos)) {
                continue;
            }

            // Add the current direction
            ret.add(dir);
        }

        return ret;
    }

    public List<Direction> getSortedDirections(Position pos) {
        List<Direction> l1 = new ArrayList<>();
        List<Direction> l2 = new ArrayList<>();
        List<Direction> l3 = new ArrayList<>();

        int curDis = getDistance(pos);

        //
        // Iterate over all direction and select the ones that lead to the target
        //
        for (Direction dir : Direction.values()) {
            // Get next position
            Position nxt = next(pos, dir);
            int nxtDis = getDistance(nxt);

            // Skip if unreachable
            if (nxtDis == Integer.MAX_VALUE) {
                continue;
            }

            // Add current direction to the corresponding list depending on its distance
            if (nxtDis < curDis) {
                l1.add(dir);
            } else if (nxtDis == curDis) {
                l2.add(dir);
            } else {
                l3.add(dir);
            }
        }

        // Append the lists in order
        l1.addAll(l2);
        l1.addAll(l3);

        return l1;
    }
}
