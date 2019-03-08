package models.map;

import utils.Position;
import utils.Constants.*;


/**
 * This {@code MapGrid} class represents the map grid of our Hive Warehouse System.
 */
public class MapGrid extends Grid<MapCell> {

    //
    // Member Methods
    //

    /**
     * Constructs a new {@code MapGrid} object with the given 2D {@code MapCell} array.
     *
     * @param grid a 2D array of {@code MapCell} objects.
     */
    public MapGrid(MapCell[][] grid) {
        super(grid);
    }

    /**
     * Checks whether a {@code MapCell} in this {@code MapGrid} is statically empty or not.
     * <p>
     * A cell is considered statically empty if it is empty or it is currently
     * occupied by an {@code Agent}.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     *
     * @return {@code true} if the cell exists and is empty; {@code false} otherwise.
     */
    public boolean isEmpty(int row, int col) {
        return isInBound(row, col) && grid[row][col].isEmpty();
    }

    /**
     * Checks whether a {@code MapCell} in this {@code MapGrid} is statically empty or not.
     * <p>
     * A cell is considered statically empty if it is empty or it is currently
     * occupied by an {@code Agent}.
     *
     * @param pos the {@code Position} of the cell to check.
     *
     * @return {@code true} if the cell exists and is empty; {@code false} otherwise.
     */
    public boolean isEmpty(Position pos) {
        return isEmpty(pos.row, pos.col);
    }

    /**
     * Checks whether a {@code MapCell} in this {@code MapGrid} is accessible or not.
     * <p>
     * A cell is considered accessible if it is empty, currently occupied by an {@code Agent},
     * or its type matches one of the given types.
     *
     * @param row         the row position of the cell to check.
     * @param col         the column position of the cell to check.
     * @param accessTypes a list of accessible cell types.
     *
     * @return {@code true} if the cell exists and is accessible; {@code false} otherwise.
     */
    public boolean isAccessible(int row, int col, CellType... accessTypes) {
        return isInBound(row, col) && grid[row][col].isAccessible(accessTypes);
    }

    /**
     * Checks whether a {@code MapCell} in this {@code MapGrid} is accessible or not.
     * <p>
     * A cell is considered accessible if it is empty, currently occupied by an {@code Agent},
     * or its type matches one of the given types.
     *
     * @param pos         the {@code Position} of the cell to check.
     * @param accessTypes a list of accessible cell types.
     *
     * @return {@code true} if the cell exists and is accessible; {@code false} otherwise.
     */
    public boolean isAccessible(Position pos, CellType... accessTypes) {
        return isAccessible(pos.row, pos.col, accessTypes);
    }
}
