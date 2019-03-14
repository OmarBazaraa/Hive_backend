package models.maps;

import models.maps.utils.Position;

import utils.Constants;
import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;


/**
 * This {@code MapGrid} class represents the map grid of our Hive Warehouse System.
 *
 * @see Grid
 * @see GuideGrid
 */
public class MapGrid extends Grid<MapCell> {

    //
    // Static Methods
    //

    /**
     * Creates a new {@code MapGrid} object from JSON data.
     *
     * TODO: add checks and throw exceptions
     *
     * @param data the un-parsed JSON data.
     *
     * @return an {@code MapGrid} object.
     */
    public static MapGrid create(JSONObject data) throws Exception {
        int h = data.getInt(Constants.MSG_KEY_HEIGHT);
        int w = data.getInt(Constants.MSG_KEY_WIDTH);

        MapCell[][] grid = new MapCell[h][w];

        JSONArray gridJSON = data.getJSONArray(Constants.MSG_KEY_GRID);

        if (gridJSON.length() != h) {
            throw new Exception("Invalid grid dimensions");
        }

        for (int i = 0; i < h; ++i) {
            JSONArray rowJSON = gridJSON.getJSONArray(i);

            if (rowJSON.length() != w) {
                throw new Exception("Invalid grid dimensions");
            }

            for (int j = 0; j < w; ++j) {
                grid[i][j] = MapCell.create(rowJSON.getJSONObject(j));
            }
        }

        return new MapGrid(grid);
    }

    // ===============================================================================================
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

    /**
     * Returns a list of statically empty directions surrounding a given {@code Position}.
     * <p>
     * The directions are sorted so that a {@code Direction} leading to an empty cell
     * cell comes before a {@code Direction} leading to a cell with an {@code Agent}.
     *
     * @param row the row position of the cell.
     * @param col the column position of the cell.
     *
     * @return a list of guide directions.
     */
    public List<Direction> getEmptyDirections(int row, int col) {
        // Create empty list of direction
        LinkedList<Direction> ret = new LinkedList<>();

        //
        // Iterate over all direction and select the ones that lead to an empty cell
        //
        for (Direction dir : Direction.values()) {
            // Get next position
            Position nxt = next(row, col, dir);

            // Skip if not empty
            if (!isEmpty(nxt)) {
                continue;
            }

            // Add current direction to the corresponding list depending
            // whether there is an agent or not
            if (get(nxt).hasAgent()) {
                ret.addLast(dir);
            } else {
                ret.addFirst(dir);
            }
        }

        return ret;
    }

    /**
     * Returns a list of statically empty directions surrounding a given {@code Position}.
     * <p>
     * The directions are sorted so that a {@code Direction} leading to an empty cell
     * cell comes before a {@code Direction} leading to a cell with an {@code Agent}.
     *
     * @param pos the {@code Position} of the cell.
     *
     * @return a list of guide directions.
     */
    public List<Direction> getEmptyDirections(Position pos) {
        return getEmptyDirections(pos.row, pos.col);
    }
}
