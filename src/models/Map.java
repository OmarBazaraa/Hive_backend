package models;


import java.io.FileReader;
import java.util.Scanner;

public class Map {

    //
    // Constants & Enums
    //

    // Cell types
    public enum CellType {
        EMPTY,
        EMPTY_CORNER,
        EMPTY_TUNNEL,
        OBSTACLE,
        RACK,
        AGENT,
        UNKNOWN
    }

    // Cell shapes
    public static final String CELL_SHAPE_EMPTY = ". ";
    public static final String CELL_SHAPE_OBSTACLE = "#";
    public static final String CELL_SHAPE_RACK = "$";
    public static final String CELL_SHAPE_AGENT = "@Xx";

    // ===============================================================================================
    //
    // Member Variables
    //

    private int mRows;
    private int mCols;
    private int mAgentsCount;
    private int mBoundedAgentsCount;
    private int mRacksCount;
    private CellType[][] mGrid;
    private Agent[][] mAgents;

    // ===============================================================================================
    //
    // Static Functions
    //

    public static Map createMapFromFile(String fileName) throws Exception {
        Map ret = new Map();
        ret.readFromFile(fileName);
        return ret;
    }

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Map() {
        this.mRows = 0;
        this.mCols = 0;
        this.mAgentsCount = 0;
        this.mRacksCount = 0;
    }

    public Map(int rows, int cols) {
        this.mRows = rows;
        this.mCols = cols;
        this.mAgentsCount = 0;
        this.mRacksCount = 0;
    }

    public void readFromFile(String fileName) throws Exception {
        // Open the map file
        Scanner reader = new Scanner(new FileReader(fileName));

        // Read map dimensions
        mRows = reader.nextInt();
        mCols = reader.nextInt();

        // Allocate map
        mGrid = new CellType[mRows + 2][mCols + 2];

        // Read the mGrid
        for (int i = 1; i <= mRows; ++i) {
            if (!reader.hasNext()) {
                throw new Exception("Map grid height mis-match");
            }

            String row = reader.next();

            if (row.length() != mCols) {
                throw new Exception("Map grid width mis-match");
            }

            for (int j = 1; j <= mCols; ++j) {
                mGrid[i][j] = toGridCellType(row.charAt(j));

                switch (mGrid[i][j]) {
                    case RACK:
                        mRacksCount++;
                        break;
                    case AGENT:
                        mAgentsCount++;
                        break;
                }
            }
        }

        // Read map items specs
        for (int i = 0; i < mRacksCount; ++i) {
            // TODO
        }

        // Read map agents specs
        for (int i = 0; i < mAgentsCount; ++i) {
            // TODO
        }

        // Close the map file
        reader.close();
    }

    public void bindAgent(Agent agent) {

    }

    public void bindRack(Rack rack) {

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= mRows; ++i) {
            for (int j = 1; j <= mCols; ++j) {
                builder.append(toGridCellShape(mGrid[i][j]));
            }

            builder.append('\n');
        }

        return builder.toString();
    }

    // ===============================================================================================
    //
    // Public Getters & Setters
    //

    public CellType get(int row, int col) {
        if (1 <= row && row <= mRows && 1 <= col && col <= mCols) {
            return mGrid[row][col];
        }

        return CellType.UNKNOWN;
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

    private CellType toGridCellType(char cell) {
        if (CELL_SHAPE_EMPTY.indexOf(cell) != -1) {
            return CellType.EMPTY;
        }
        if (CELL_SHAPE_OBSTACLE.indexOf(cell) != -1) {
            return CellType.OBSTACLE;
        }
        if (CELL_SHAPE_RACK.indexOf(cell) != -1) {
            return CellType.RACK;
        }
        if (CELL_SHAPE_AGENT.indexOf(cell) != -1) {
            return CellType.AGENT;
        }

        return CellType.UNKNOWN;
    }

    private char toGridCellShape(CellType type) {
        switch (type) {
            case EMPTY:
            case EMPTY_CORNER:
            case EMPTY_TUNNEL:
                return CELL_SHAPE_EMPTY.charAt(0);
            case OBSTACLE:
                return CELL_SHAPE_OBSTACLE.charAt(0);
            case RACK:
                return CELL_SHAPE_RACK.charAt(0);
            case AGENT:
                return CELL_SHAPE_AGENT.charAt(0);
            default:
                return '?';
        }
    }
}
