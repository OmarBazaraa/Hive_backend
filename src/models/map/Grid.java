package models.map;

import models.components.Agent;
import models.map.base.Dimensions;
import models.map.base.Position;
import utils.Constants;
import utils.Constants.*;


/**
 * This {@code Grid} class represents the map grid of our Hive System's warehouse.
 */
public class Grid {

    //
    // Member Variables
    //

    private int rows;
    private int cols;
    private int size;
    private Cell[][] grid;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new Hive map grid.
     *
     * @param grid the grid configurations.
     */
    public Grid(Cell[][] grid) {
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.size = rows * cols;
        this.grid = grid;
    }

    /**
     * Returns the number of rows in this grid.
     *
     * @return an integer representing the number of rows in this grid.
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Returns the number of columns in this grid.
     *
     * @return an integer representing the number of columns in this grid.
     */
    public int getCols() {
        return this.cols;
    }

    /**
     * Returns the total number of cells in this grid object.
     *
     * @return an integer representing the total number of cell in this grid.
     */
    public int getCellsCount() {
        return this.size;
    }

    /**
     * Returns the dimensions of this grid.
     *
     * @return a {@code Dimensions} object representing the size of this grid.
     */
    public Dimensions getDimensions() {
        return new Dimensions(rows, cols);
    }

    /**
     * Returns the grid cell given its position in this grid.
     *
     * @param row the row position of the grid cell.
     * @param col the column position of the grid cell.
     *
     * @return the {@code Cell} object in the given position, or {@code null} if position is out of bound.
     */
    public Cell get(int row, int col) {
        return isInBound(row, col) ? grid[row][col] : null;
    }

    /**
     * Returns the grid cell given its position in this grid.
     *
     * @param pos the position of the grid cell.
     *
     * @return the {@code Cell} object in the given position, or {@code null} if position is out of bound.
     */
    public Cell get(Position pos) {
        return get(pos.row, pos.col);
    }

    /**
     * Returns the grid cell given its id in this grid.
     *
     * @param id the id of the grid cell to return.
     *
     * @return the {@code Cell} object by its id, or {@code null} if position is out of bound.
     */
    public Cell get(int id) {
        Position pos = toCellPos(id);
        return get(pos.row, pos.col);
    }

    /**
     * Converts the given grid cell position to a unique id.
     *
     * @param row the row position of the grid cell to convert.
     * @param col the column position of the grid cell to convert.
     *
     * @return a single unique integer id corresponding to the given cell.
     */
    public int toCellId(int row, int col) {
        return row * cols + col;
    }

    /**
     * Converts the given grid cell position to a unique id.
     *
     * @param pos the position of the grid cell to convert.
     *
     * @return a single unique integer id corresponding to the given cell.
     */
    public int toCellId(Position pos) {
        return toCellId(pos.row, pos.col);
    }

    /**
     * Converts the given grid cell id to its position in the grid.
     *
     * @param id the id of the grid cell to convert.
     *
     * @return a {@code Position} object corresponding to the given cell id.
     */
    public Position toCellPos(int id) {
        int row = id / cols;
        int col = id % cols;
        return new Position(row, col);
    }

    /**
     * Checks whether the given cell is inside the boundaries of the grid.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     *
     * @return {@code true} if the given cell is in bound, {@code false} otherwise.
     */
    public boolean isInBound(int row, int col) {
        return 1 <= row && row <= rows && 1 <= col && col <= cols;
    }

    /**
     * Checks whether the given cell is inside the boundaries of the grid.
     *
     * @param pos the position of the cell to check.
     *
     * @return {@code true} if the given cell is in bound, {@code false} otherwise.
     */
    public boolean isInBound(Position pos) {
        return isInBound(pos.row, pos.col);
    }

    /**
     * Checks whether the given cell is inside the boundaries of the grid.
     *
     * @param id the id of the cell to check.
     *
     * @return {@code true} if the given cell is in bound, {@code false} otherwise.
     */
    public boolean isInBound(int id) {
        return 0 <= id && id < size;
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

    /**
     * Calculates the next cell position if moving in the given direction.
     * (i.e. position(current) + dir = position(next)).
     *
     * @param row the current cell row position.
     * @param col the current cell column position.
     * @param dir the direction to move along.
     *
     * @return a {@code Position} object corresponding to the next cell position.
     */
    public Position next(int row, int col, Direction dir) {
        int i = dir.ordinal();
        row += Constants.DIR_ROW[i];
        col += Constants.DIR_COL[i];
        return new Position(row, col);
    }

    /**
     * Calculates the next cell position if moving in the given direction.
     * (i.e. position(current) + dir = position(next)).
     *
     * @param pos the current cell position.
     * @param dir the direction to move along.
     *
     * @return a {@code Position} object corresponding to the next cell position.
     */
    public Position next(Position pos, Direction dir) {
        return next(pos.row, pos.col, dir);
    }

    /**
     * Calculates the next cell id if moving in the given direction.
     * (i.e. position(current) + dir = position(next)).
     *
     * @param id  the current cell id.
     * @param dir the direction to move along.
     *
     * @return the id of the next cell.
     */
    public int next(int id, Direction dir) {
        int i = dir.ordinal();
        id += Constants.DIR_ROW[i] * cols;
        id += Constants.DIR_COL[i];
        return id;
    }

    /**
     * Calculates the previous cell position if moving in the given direction in reverse manner.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param row the current cell row position.
     * @param col the current cell column position.
     * @param dir the direction to move along.
     *
     * @return a {@code Position} object corresponding to the previous cell position.
     */
    public Position previous(int row, int col, Direction dir) {
        int i = dir.ordinal();
        row -= Constants.DIR_ROW[i];
        col -= Constants.DIR_COL[i];
        return new Position(row, col);
    }

    /**
     * Calculates the previous cell position if moving in the given direction in reverse manner.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param pos the current cell position.
     * @param dir the direction to move along.
     *
     * @return a {@code Position} object corresponding to the previous cell position.
     */
    public Position previous(Position pos, Direction dir) {
        return previous(pos.row, pos.col, dir);
    }

    /**
     * Calculates the previous cell id if moving in the given direction in reverse manner.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param id  the current cell id.
     * @param dir the direction to move along.
     *
     * @return the id of the previous cell.
     */
    public int previous(int id, Direction dir) {
        int i = dir.ordinal();
        id -= Constants.DIR_ROW[i] * cols;
        id -= Constants.DIR_COL[i];
        return id;
    }

    /**
     * Returns a string representation of object.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of object.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= rows; ++i) {
            for (int j = 1; j <= cols; ++j) {
                builder.append(grid[i][j].toShape());
            }
            builder.append('\n');
        }

        return builder.toString();
    }
}