package models;

import utils.Constants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Map {

    //
    // Constants & Enums
    //
    public enum CellType {
        EMPTY,
        EMPTY_CORNER,
        EMPTY_TUNNEL,
        OBSTACLE,
        ITEM,
        AGENT,
        UNKNOWN
    }

    // ===============================================================================================
    //
    // Member Variables
    //

    private int rows;
    private int cols;
    private int agentsCount;
    private int itemsCount;
    private CellType[][] grid;

    // ===============================================================================================
    //
    // Static Functions
    //

    public static Map createMapFromFile(String fileName) throws FileNotFoundException {
        Map ret = new Map();
        ret.readFromFile(fileName);
        return ret;
    }

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Map() {
        this.rows = 0;
        this.cols = 0;
        this.agentsCount = 0;
        this.itemsCount = 0;
    }

    public Map(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.agentsCount = 0;
        this.itemsCount = 0;
    }

    public void readFromFile(String fileName) throws FileNotFoundException {
        // Open the map file
        Scanner reader = new Scanner(new FileReader(fileName));

        // Read map dimensions
        rows = reader.nextInt();
        cols = reader.nextInt();

        // Allocate map
        grid = new CellType[rows + 2][cols + 2];

        // Read the grid
        for (int i = 1; i <= rows; ++i) {
            String row = reader.next();

            for (int j = 1; j <= cols; ++j) {
                grid[i][j] = toGridCell(row.charAt(j));

                switch (grid[i][j]) {
                    case ITEM:
                        itemsCount++;
                        break;
                    case AGENT:
                        agentsCount++;
                        break;
                }
            }
        }

        // Read map items specs
        for (int i = 0; i < itemsCount; ++i) {
            // TODO
        }

        // Read map agents specs
        for (int i = 0; i < agentsCount; ++i) {
            // TODO
        }

        // Close the map file
        reader.close();
    }

    // ===============================================================================================
    //
    // Public Getters & Setters
    //

    public int getItemsCount() {
        return this.itemsCount;
    }

    public int getAgentsCount() {
        return this.agentsCount;
    }

    // ===============================================================================================
    //
    // Helper Private Member Functions
    //

    private CellType toGridCell(char cell) {
        if (Constants.STR_CELL_EMPTY.indexOf(cell) != -1) {
            return CellType.EMPTY;
        }

        if (Constants.STR_CELL_OBSTACLE.indexOf(cell) != -1) {
            return CellType.OBSTACLE;
        }

        if (Constants.STR_CELL_ITEM.indexOf(cell) != -1) {
            return CellType.ITEM;
        }

        if (Constants.STR_CELL_AGENT.indexOf(cell) != -1) {
            return CellType.AGENT;
        }

        return CellType.UNKNOWN;
    }
}
