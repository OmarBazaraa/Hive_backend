package utils;

import models.facilities.Rack;
import models.items.Item;
import models.maps.Position;

import utils.Constants.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;


/**
 * This {@code Utility} class is a collection of a static utility functions
 * to be used across the entire project modules.
 */
public class Utility {

    /**
     * Reads the given file and returns its content.
     *
     * @param path the path of the file to read.
     *
     * @return the content of the file.
     */
    public static String readFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        String line;
        StringBuilder builder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return builder.toString();
    }

    /**
     * Converts the given item-quantity map into a visual representative string.
     *
     * @param map the items map to stringify.
     *
     * @return the visual representation of the map.
     */
    public static String stringifyItemQuantities(Map<Item, Integer> map) {
        StringBuilder builder = new StringBuilder();

        boolean first = true;

        builder.append("{");

        for (var pair : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }

            builder.append("item-").append(pair.getKey().getId()).append(": ").append(pair.getValue());
        }

        builder.append("}");

        return builder.toString();
    }

    /**
     * Converts the given rack-quantity map into a visual representative string.
     *
     * @param map the racks map to stringify.
     *
     * @return the visual representation of the map.
     */
    public static String stringifyRackQuantities(Map<Rack, Integer> map) {
        StringBuilder builder = new StringBuilder();

        boolean first = true;

        builder.append("{");

        for (var pair : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }

            builder.append("rack-").append(pair.getKey().getId()).append(": ").append(pair.getValue());
        }

        builder.append("}");

        return builder.toString();
    }

    /**
     * Converts the given direction into its visual representative character.
     *
     * @param dir the direction to convert.
     *
     * @return the symbol representing the given direction.
     */
    public static char dirToShape(int dir) {
        switch (dir) {
            case Constants.DIR_RIGHT:
                return Constants.SHAPE_DIR_RIGHT;
            case Constants.DIR_UP:
                return Constants.SHAPE_DIR_UP;
            case Constants.DIR_LEFT:
                return Constants.SHAPE_DIR_LEFT;
            case Constants.DIR_DOWN:
                return Constants.SHAPE_DIR_DOWN;
        }
        return Constants.SHAPE_DIR_UNKNOWN;
    }

    /**
     * Calculates the next cell position if moving along the given direction
     * from a given current cell.
     * (i.e. position(current) + dir = position(next)).
     *
     * @param row the row position of the current cell.
     * @param col the row position of the current cell.
     * @param dir the direction to move along.
     *
     * @return the {@code Position} corresponding to the next cell.
     */
    public static Position nextPos(int row, int col, int dir) {
        row += Constants.DIR_ROW[dir];
        col += Constants.DIR_COL[dir];
        return new Position(row, col);
    }

    /**
     * Calculates the previous cell position if moving backward along the given direction
     * from a given current cell.
     * (i.e. position(previous) + dir = position(current)).
     *
     * @param row the row position of the current cell.
     * @param col the row position of the current cell.
     * @param dir the direction to move along.
     *
     * @return the {@code Position} corresponding to the previous cell.
     */
    public static Position prevPos(int row, int col, int dir) {
        row -= Constants.DIR_ROW[dir];
        col -= Constants.DIR_COL[dir];
        return new Position(row, col);
    }

    /**
     * Calculates the next direction if rotating right.
     *
     * @param dir the current direction.
     *
     * @return the next direction.
     */
    public static int rotateRight(int dir) {
        return (dir - 1) & 3;
    }

    /**
     * Calculates the previous direction if rotating left
     *
     * @param dir the current direction.
     *
     * @return the next direction.
     */
    public static int rotateLeft(int dir) {
        return (dir + 1) & 3;
    }

    /**
     * Returns the reverse (opposite) direction of the given one.
     *
     * @param dir the direction to get its reverse.
     *
     * @return the reverse direction.
     */
    public static int getReverseDir(int dir) {
        return (dir + 2) & 3;
    }

    /**
     * Returns the minimum number of rotations to reach the second direction
     * from the given first direction.
     *
     * @param d1 the first source direction.
     * @param d2 the second target direction.
     *
     * @return the minimum number of rotations.
     */
    public static int getRotationsCount(int d1, int d2) {
        return Math.min(Math.abs(d1 - d2), 2);
    }
}
