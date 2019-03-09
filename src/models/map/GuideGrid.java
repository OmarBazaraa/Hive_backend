package models.map;

import models.facilities.Facility;
import utils.Position;
import utils.Constants.*;

import java.util.ArrayList;
import java.util.List;


/**
 * This {@code GuideGrid} class represents a guide map to reach a target object.
 * <p>
 * A guide map guides an {@code agent} towards its associated target,
 * typically a {@link Facility}.
 */
public class GuideGrid extends Grid<GuideCell> {

    //
    // Member Methods
    //

    /**
     * Constructs a new {@code GuideMap} object with the given 2D {@code GuideCell} array.
     *
     * @param grid a 2D array of {@code GuideCell} objects.
     */
    public GuideGrid(GuideCell[][] grid) {
        super(grid);
    }

    /**
     * Returns the guide distance to reach the associated target
     * from a {@code GuideCell} in this {@code GuideMap} and vice versa.
     *
     * @param row the row position of the cell.
     * @param col the column position of the cell.
     *
     * @return the distance to reach the target.
     */
    public int getDistance(int row, int col) {
        return isInBound(row, col) ? grid[row][col].distance : Integer.MAX_VALUE;
    }

    /**
     * Returns the guide distance to reach the associated target
     * from a given {@code GuideCell} and vice versa.
     *
     * @param pos the {@code Position} of the cell.
     *
     * @return the distance to reach the target.
     */
    public int getDistance(Position pos) {
        return getDistance(pos.row, pos.col);
    }


    //
    // TODO
    //

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
