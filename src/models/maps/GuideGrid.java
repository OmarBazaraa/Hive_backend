package models.maps;

import models.maps.utils.Position;

import models.warehouses.Warehouse;
import utils.Constants.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * This {@code GuideGrid} class represents a guide map to reach a target object.
 * <p>
 * A guide map guides an {@link models.agents.Agent Agent} towards its associated target,
 * typically a {@link models.facilities.Facility Facility}.
 *
 * @see Grid
 * @see MapGrid
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
        return isInBound(row, col) ? grid[row][col].getDistance() : Integer.MAX_VALUE;
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

    /**
     * Returns the guide directions to reach associated target sorted by their chance.
     * <p>
     * The directions are sorted so that a {@code Direction} with smaller distance to
     * the target comes before a {@code Direction} with larger distance.
     * <p>
     * Among those directions with the same distance to the target, a {@code Direction} leading
     * to an empty cell comes before a {@code Direction} leading to a cell with an {@code Agent}.
     *
     * @param row the row position of the cell.
     * @param col the column position of the cell.
     *
     * @return a list of guide directions.
     */
    public List<Direction> getGuideDirections(int row, int col) {
        LinkedList<Direction> l1 = new LinkedList<>();
        LinkedList<Direction> l2 = new LinkedList<>();

        // Get current distance to the target
        int curDis = getDistance(row, col);

        // Get warehouse map grid
        MapGrid map = Warehouse.getInstance().getMap();

        //
        // Iterate over all direction and select the ones that lead to the target
        //
        for (Direction dir : Direction.values()) {
            // Get next position
            Position nxt = next(row, col, dir);
            int nxtDis = getDistance(nxt);

            // Skip if unreachable
            if (nxtDis == Integer.MAX_VALUE) {
                continue;
            }

            // Get next cell
            MapCell cell = map.get(nxt);

            // Add current direction to the corresponding list depending on its distance
            // and whether there is an agent or not
            if (nxtDis < curDis) {
                if (cell.hasAgent()) {
                    l1.addLast(dir);
                } else {
                    l1.addFirst(dir);
                }
            } else {
                if (cell.hasAgent()) {
                    l2.addLast(dir);
                } else {
                    l2.addFirst(dir);
                }
            }
        }

        // Append the lists in order
        l1.addAll(l2);

        return l1;
    }

    /**
     * Returns the guide directions to reach associated target sorted by their chance.
     * <p>
     * The directions are sorted so that a {@code Direction} with smaller distance to
     * the target comes before a {@code Direction} with larger distance.
     * <p>
     * Among those directions with the same distance to the target, a {@code Direction} leading
     * to an empty cell comes before a {@code Direction} leading to a cell with an {@code Agent}.
     *
     * @param pos the {@code Position} of the cell.
     *
     * @return a list of guide directions.
     */
    public List<Direction> getGuideDirections(Position pos) {
        return getGuideDirections(pos.row, pos.col);
    }
}
