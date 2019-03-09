package models.maps;

import utils.Dimensions;
import utils.Position;
import utils.Constants;
import utils.Constants.*;


/**
 * This {@code Grid} class represents a grid of 2D {@link Cell} objects.
 * <p>
 * This class is the parent class of {@link MapGrid} and {@link GuideGrid}.
 */
public class Grid<T extends Cell> {

    //
    // Member Variables
    //

    /**
     * The number of rows in this {@code Grid}.
     */
    protected int rows;

    /**
     * The number of columns in this {@code Grid}.
     */
    protected int cols;

    /**
     * The total number of cells in this {@code Grid}.
     */
    protected int size;

    /**
     * The 2D array of {@code Cell} objects forming this {@code Grid}.
     */
    protected T[][] grid;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Grid} object with the given 2D {@code Cell} array.
     *
     * @param grid a 2D array of {@code Cell} objects.
     */
    public Grid(T[][] grid) {
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.size = rows * cols;
        this.grid = grid;
    }

    /**
     * Returns the number of rows of this {@code Grid}.
     *
     * @return an integer representing the number of rows in the grid.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns in this {@code Grid}.
     *
     * @return an integer representing the number of columns in the grid.
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns the size of this {@code Grid}, that is the the total number of cells.
     *
     * @return an integer representing the total number of cell in the grid.
     */
    public int getCellsCount() {
        return size;
    }

    /**
     * Returns the dimensions of this {@code Grid}.
     *
     * @return a {@code Dimensions} object representing the dimensions of the grid.
     */
    public Dimensions getDimensions() {
        return (new Dimensions(rows, cols));
    }

    /**
     * Returns a {@code Cell} given its position in this {@code Grid}.
     *
     * @param row the row position of the cell to return.
     * @param col the column position of the cell to return.
     *
     * @return the {@code Cell} in the given position.
     */
    public T get(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns a {@code Cell} given its position in this {@code Grid}.
     *
     * @param pos the {@code Position} of the cell to return.
     *
     * @return the {@code Cell} in the given position.
     */
    public T get(Position pos) {
        return get(pos.row, pos.col);
    }

    /**
     * Returns a {@code Cell} given its id in this {@code Grid}.
     *
     * @param id the id of the cell to return.
     *
     * @return the {@code Cell} of the given id.
     */
    public T get(int id) {
        Position pos = toCellPos(id);
        return get(pos.row, pos.col);
    }

    /**
     * Converts a cell position to its corresponding unique id in this {@code Grid}.
     *
     * @param row the row position of the cell to convert.
     * @param col the column position of the cell to convert.
     *
     * @return a single unique integer id corresponding to the given cell position.
     */
    public int toCellId(int row, int col) {
        return row * cols + col;
    }

    /**
     * Converts a cell position to its corresponding unique id in this {@code Grid}.
     *
     * @param pos the {@code Position} of the cell to convert.
     *
     * @return a single unique integer id corresponding to the given cell position.
     */
    public int toCellId(Position pos) {
        return toCellId(pos.row, pos.col);
    }

    /**
     * Converts a cell id to its corresponding unique position in this {@code Grid}.
     *
     * @param id the id of the cell to convert.
     *
     * @return a {@code Position} object corresponding to the given cell id.
     */
    public Position toCellPos(int id) {
        int row = id / cols;
        int col = id % cols;
        return new Position(row, col);
    }

    /**
     * Checks whether a cell is inside the boundaries of this {@code Grid} or not.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     *
     * @return {@code true} if the cell is inside the grid; {@code false} otherwise.
     */
    public boolean isInBound(int row, int col) {
        return 1 <= row && row <= rows && 1 <= col && col <= cols;
    }

    /**
     * Checks whether a cell is inside the boundaries of this {@code Grid} or not.
     *
     * @param pos the {@code Position} of the cell to check.
     *
     * @return {@code true} if the cell is inside the grid; {@code false} otherwise.
     */
    public boolean isInBound(Position pos) {
        return isInBound(pos.row, pos.col);
    }

    /**
     * Checks whether a cell is inside the boundaries of this {@code Grid} or not.
     *
     * @param id the id of the cell to check.
     *
     * @return {@code true} if the cell is inside the grid; {@code false} otherwise.
     */
    public boolean isInBound(int id) {
        return 0 <= id && id < size;
    }

    /**
     * Calculates the next cell position if moving along the given direction
     * from a given current cell.
     * (i.e. position(current) + dir = position(next)).
     *
     * @param row the row position of the current cell.
     * @param col the row position of the current cell.
     * @param dir the {@code Direction} to move along.
     *
     * @return the {@code Position} corresponding to the next cell.
     */
    public Position next(int row, int col, Direction dir) {
        int i = dir.ordinal();
        row += Constants.DIR_ROW[i];
        col += Constants.DIR_COL[i];
        return new Position(row, col);
    }

    /**
     * Calculates the next cell position if moving along the given direction
     * from a given current cell.
     * (i.e. position(current) + dir = position(next)).
     *
     * @param pos the {@code Position} of the current cell .
     * @param dir the {@code Direction} to move along.
     *
     * @return the {@code Position} corresponding to the next cell.
     */
    public Position next(Position pos, Direction dir) {
        return next(pos.row, pos.col, dir);
    }

    /**
     * Calculates the next cell id if moving along the given direction
     * from a given current cell.
     * (i.e. position(current) + dir = position(next)).
     *
     * @param id  the current cell id.
     * @param dir the {@code Direction} to move along.
     *
     * @return the id corresponding to the next cell.
     */
    public int next(int id, Direction dir) {
        int i = dir.ordinal();
        id += Constants.DIR_ROW[i] * cols;
        id += Constants.DIR_COL[i];
        return id;
    }

    /**
     * Calculates the previous cell position if moving backward along the given direction
     * from a given current cell.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param row the row position of the current cell.
     * @param col the row position of the current cell.
     * @param dir the {@code Direction} to move along.
     *
     * @return the {@code Position} corresponding to the previous cell.
     */
    public Position previous(int row, int col, Direction dir) {
        int i = dir.ordinal();
        row -= Constants.DIR_ROW[i];
        col -= Constants.DIR_COL[i];
        return new Position(row, col);
    }

    /**
     * Calculates the previous cell position if moving backward along the given direction
     * from a given current cell.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param pos the {@code Position} of the current cell .
     * @param dir the {@code Direction} to move along.
     *
     * @return the {@code Position} corresponding to the previous cell.
     */
    public Position previous(Position pos, Direction dir) {
        return previous(pos.row, pos.col, dir);
    }

    /**
     * Calculates the previous cell id if moving backward along the given direction
     * from a given current cell.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param id  the current cell id.
     * @param dir the {@code Direction} to move along.
     *
     * @return the id corresponding to the previous cell.
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