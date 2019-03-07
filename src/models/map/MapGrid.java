package models.map;

import models.map.base.Grid;
import models.map.base.Position;


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
     * Checks whether the given cell is empty.
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
     * Checks whether the given cell is empty.
     *
     * @param pos the position of the cell to check.
     *
     * @return {@code true} if the given cell is inside grid boundaries and empty, {@code false} otherwise.
     */
    public boolean isEmpty(Position pos) {
        return isEmpty(pos.row, pos.col);
    }

    /**
     * Checks whether the given cell is empty.
     *
     * @param id the id of the cell to check.
     *
     * @return {@code true} if the given cell is inside grid boundaries and empty, {@code false} otherwise.
     */
    public boolean isEmpty(int id) {
        Position pos = toCellPos(id);
        return isEmpty(pos.row, pos.col);
    }
}
