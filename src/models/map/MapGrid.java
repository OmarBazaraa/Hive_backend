package models.map;

import models.map.base.Grid;
import models.map.base.Position;
import utils.Constants;


/**
 * This {@code MapGrid} class represents the map grid of our Hive Warehouse System.
 */
public class MapGrid extends Grid<MapCell> {

    //
    // Member Methods
    //

    /**
     * Constructs a new grid.
     *
     * @param grid the grid configurations.
     */
    public MapGrid(MapCell[][] grid) {
        super(grid);
    }

    /**
     * Checks whether the given cell is statically empty or not.
     * A cell is considered statically empty if it is currently empty or it's occupied by an agent.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     *
     * @return {@code true} if the given cell is inside grid boundaries and empty, {@code false} otherwise.
     */
    public boolean isEmpty(int row, int col) {
        return isInBound(row, col) && grid[row][col].isEmpty();
    }

    /**
     * Checks whether the given cell is statically empty or not.
     * A cell is considered statically empty if it is currently empty or it's occupied by an agent.
     *
     * @param pos the position of the cell to check.
     *
     * @return {@code true} if the given cell is inside grid boundaries and empty, {@code false} otherwise.
     */
    public boolean isEmpty(Position pos) {
        return isEmpty(pos.row, pos.col);
    }

    /**
     * Checks whether the given cell is accessible or not.
     * A cell is considered accessible if it is empty, occupied by an agent,
     * or its type matches one of the given types.
     *
     * @param row             the row position of the cell to check.
     * @param col             the column position of the cell to check.
     * @param accessibleTypes the list of accessible cell types.
     *
     * @return {@code true} if this cell is accessible, {@code false} otherwise.
     */
    public boolean isAccessible(int row, int col, Constants.CellType... accessibleTypes) {
        return isInBound(row, col) && grid[row][col].isAccessible(accessibleTypes);
    }

    /**
     * Checks whether the given cell is accessible or not.
     * A cell is considered accessible if it is empty, occupied by an agent,
     * or its type matches one of the given types.
     *
     * @param pos             the position of the cell to check.
     * @param accessibleTypes the list of accessible cell types.
     *
     * @return {@code true} if this cell is accessible, {@code false} otherwise.
     */
    public boolean isAccessible(Position pos, Constants.CellType... accessibleTypes) {
        return isAccessible(pos.row, pos.col, accessibleTypes);
    }

    /**
     * Checks whether the given cell is accessible by an agent to move into or not.
     * A cell is considered agent accessible if it is not occupied by an agent, and
     * it is empty or its type matches one of the given types.
     *
     * @param row             the row position of the cell to check.
     * @param col             the column position of the cell to check.
     * @param accessibleTypes the list of accessible cell types.
     *
     * @return {@code true} if this cell is agent accessible, {@code false} otherwise.
     */
    public boolean isAgentAccessible(int row, int col, Constants.CellType... accessibleTypes) {
        return isInBound(row, col) && grid[row][col].isAgentAccessible(accessibleTypes);
    }

    /**
     * Checks whether the given cell is accessible by an agent to move into or not.
     * A cell is considered agent accessible if it is not occupied by an agent, and
     * it is empty or its type matches one of the given types.
     *
     * @param pos             the position of the cell to check.
     * @param accessibleTypes the list of accessible cell types.
     *
     * @return {@code true} if this cell is agent accessible, {@code false} otherwise.
     */
    public boolean isAgentAccessible(Position pos, Constants.CellType... accessibleTypes) {
        return isAgentAccessible(pos.row, pos.col, accessibleTypes);
    }
}
