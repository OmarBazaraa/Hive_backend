package models;

import utils.Pair;

import java.util.Scanner;


public class Grid {

    //
    // Constants & Enums & Classes
    //

    // Cell types
    public enum CellType {
        EMPTY,
        OBSTACLE,
        RACK,
        AGENT,
        UNKNOWN
    }

    // Grid cell class
    public static class Cell {
        public CellType type;
        public Agent agent;
        public Rack rack;

        public Cell(CellType type) {
            this.type = type;
            this.agent = null;
            this.rack = null;
        }
    }

    // Cell shapes
    public static final char CELL_SHAPE_EMPTY = '.';
    public static final char CELL_SHAPE_OBSTACLE = '#';
    public static final char CELL_SHAPE_RACK = '$';
    public static final char CELL_SHAPE_AGENT = '@';

    // ===============================================================================================
    //
    // Member Variables
    //

    private int mRows;
    private int mCols;
    private int mAgentsCount;
    private int mRacksCount;
    private Cell[][] mGrid;

    // ===============================================================================================
    //
    // Static Functions
    //

    public static Grid create(Scanner reader) throws Exception {
        Grid ret = new Grid();
        ret.setup(reader);
        return ret;
    }

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Grid() {
        this.mRows = 0;
        this.mCols = 0;
        this.mAgentsCount = 0;
        this.mRacksCount = 0;
    }

    public Grid(char[][] grid) throws Exception {
        this.setup(grid);
    }

    public void setup(char[][] grid) throws Exception {
        // Get grid dimensions
        mRows = grid.length;
        mCols = grid[0].length;

        // Clear counts
        mAgentsCount = 0;
        mRacksCount = 0;

        // Allocate grid
        mGrid = new Cell[mRows + 2][mCols + 2];

        // Read grid cells
        for (int i = 1; i <= mRows; ++i) {
            for (int j = 1; j <= mCols; ++j) {
                mGrid[i][j] = toGridCell(grid[i - 1][j - 1]);

                if (mGrid[i][j].type == CellType.UNKNOWN) {
                    throw new Exception("Unknown grid cell type!");
                }
            }
        }
    }

    public void setup(Scanner reader) throws Exception {
        // Read grid dimensions
        mRows = reader.nextInt();
        mCols = reader.nextInt();

        // Clear counts
        mAgentsCount = 0;
        mRacksCount = 0;

        // Allocate grid
        mGrid = new Cell[mRows + 2][mCols + 2];

        // Read grid cells
        for (int i = 1; i <= mRows; ++i) {
            if (!reader.hasNext()) {
                throw new Exception("Grid dimensions mis-match!");
            }

            String row = reader.next();

            if (row.length() != mCols) {
                throw new Exception("Grid dimensions mis-match!");
            }

            for (int j = 1; j <= mCols; ++j) {
                mGrid[i][j] = toGridCell(row.charAt(j - 1));

                if (mGrid[i][j].type == CellType.UNKNOWN) {
                    throw new Exception("Unknown grid cell type!");
                }
            }
        }
    }

    public boolean bind(Agent agent) {
        Pair p = agent.getPosition();

        int r = p.r, c = p.c;

        if (!valid(r, c) || mGrid[r][c].type != CellType.AGENT) {
            return false;
        }

        if (mGrid[r][c].agent == null) {
            mAgentsCount++;
        }

        mGrid[r][c].agent = agent;

        return true;
    }

    public boolean bind(Rack rack) {
        Pair p = rack.getPosition();

        int r = p.r, c = p.c;

        if (!valid(r, c) || mGrid[r][c].type != CellType.RACK) {
            return false;
        }

        if (mGrid[r][c].rack == null) {
            mRacksCount++;
        }

        mGrid[r][c].rack = rack;

        return true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= mRows; ++i) {
            for (int j = 1; j <= mCols; ++j) {
                builder.append(toGridCellShape(mGrid[i][j].type));
            }

            builder.append('\n');
        }

        return builder.toString();
    }

    // ===============================================================================================
    //
    // Public Getters & Setters
    //

    public Cell get(int row, int col) {
        return valid(row, col) ? mGrid[row][col] : null;
    }

    public int getRacksCount() {
        return this.mRacksCount;
    }

    public int getAgentsCount() {
        return this.mAgentsCount;
    }

    // ===============================================================================================
    //
    // Helper Private Member Functions
    //

    private boolean valid(int r, int c) {
        return 1 <= r && r <= mRows && 1 <= c && c <= mCols;
    }

    private Cell toGridCell(char cell) {
        if (CELL_SHAPE_EMPTY == cell) {
            return new Cell(CellType.EMPTY);
        }
        if (CELL_SHAPE_OBSTACLE == cell) {
            return new Cell(CellType.OBSTACLE);
        }
        if (CELL_SHAPE_RACK == cell) {
            return new Cell(CellType.RACK);
        }
        if (CELL_SHAPE_AGENT == cell) {
            return new Cell(CellType.AGENT);
        }

        return new Cell(CellType.UNKNOWN);
    }

    private char toGridCellShape(CellType type) {
        switch (type) {
            case EMPTY:
                return CELL_SHAPE_EMPTY;
            case OBSTACLE:
                return CELL_SHAPE_OBSTACLE;
            case RACK:
                return CELL_SHAPE_RACK;
            case AGENT:
                return CELL_SHAPE_AGENT;
            default:
                return '?';
        }
    }
}
