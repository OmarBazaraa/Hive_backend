package models.maps;

import models.agents.Agent;
import models.maps.utils.Position;


/**
 * This {@code TimeGrid} class represents a timeline map of the {@link Agent agents}
 * in our Hive Warehouse System.
 */
public class TimeGrid extends Grid<TimeCell> {

    //
    // Member Methods
    //

    /**
     * Constructs a new empty {@code TimeGrid} object with the given dimensions.
     *
     * @param rows the number of rows.
     * @param cols the number of columns.
     */
    public TimeGrid(int rows, int cols) {
        super(TimeCell.allocate2D(rows, cols));
    }

    /**
     * Constructs a new {@code TimeGrid} object with the given 2D {@code TimeCell} array.
     *
     * @param grid a 2D array of {@code TimeCell} objects.
     */
    public TimeGrid(TimeCell[][] grid) {
        super(grid);
    }

    /**
     * Returns the {@code Agent} passing though a {@code TimeCell} at the given time.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     * @param time the time to get at.
     *
     * @return the {@code Agent} if exists; {@code null} otherwise.
     */
    public Agent getAgentAt(int row, int col, long time) {
        return grid[row][col].getAgentAt(time);
    }

    /**
     * Returns the {@code Agent} passing though a {@code TimeCell} at the given time.
     *
     * @param pos the {@code Position} of the cell to check.
     * @param time the time to get at.
     *
     * @return the {@code Agent} if exists; {@code null} otherwise.
     */
    public Agent getAgentAt(Position pos, long time) {
        return getAgentAt(pos.row, pos.col, time);
    }

    /**
     * Checks whether a {@code TimeCell} is empty from agents at the given time or not.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     * @param time the time to check at.
     *
     * @return {@code true} if the cell is empty; {@code false} otherwise.
     */
    public boolean isEmptyAt(int row, int col, long time) {
        return grid[row][col].isEmptyAt(time);
    }

    /**
     * Checks whether a {@code TimeCell} is empty from agents at the given time or not.
     *
     * @param pos the {@code Position} of the cell to check.
     * @param time the time to check at.
     *
     * @return {@code true} if the cell is empty; {@code false} otherwise.
     */
    public boolean isEmptyAt(Position pos, long time) {
        return isEmptyAt(pos.row, pos.col, time);
    }

    /**
     * Checks whether a {@code TimeCell} is occupied by an {@code Agent} at the given time or not.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     * @param time the time to check at.
     *
     * @return {@code true} if the cell is occupied; {@code false} otherwise.
     */
    public boolean isOccupiedAt(int row, int col, long time) {
        return grid[row][col].isOccupiedAt(time);
    }

    /**
     * Checks whether a {@code TimeCell} is occupied by an {@code Agent} at the given time or not.
     *
     * @param pos the {@code Position} of the cell to check.
     * @param time the time to check at.
     *
     * @return {@code true} if the cell is occupied; {@code false} otherwise.
     */
    public boolean isOccupiedAt(Position pos, long time) {
        return isOccupiedAt(pos.row, pos.col, time);
    }
}
