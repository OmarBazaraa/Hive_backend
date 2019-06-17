package utils;

import models.facilities.Rack;
import models.items.Item;
import models.maps.utils.Position;

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
     * @param map     the items map to stringify.
     *
     * @return the visual representation of the map.
     */
    public static String stringifyItemQuantities(Map<Item, Integer> map) {
        StringBuilder builder = new StringBuilder();

        boolean first = true;

        builder.append("{");

        for (Map.Entry<Item, Integer> pair : map.entrySet()) {
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
     * @param map     the racks map to stringify.
     *
     * @return the visual representation of the map.
     */
    public static String stringifyRackQuantities(Map<Rack, Integer> map) {
        StringBuilder builder = new StringBuilder();

        boolean first = true;

        builder.append("{");

        for (Map.Entry<Rack, Integer> pair : map.entrySet()) {
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
     * Converts the given {@code Direction} into its visual representative character.
     *
     * @param dir the {@code Direction} to convert.
     *
     * @return the symbol representing the given {@code Direction}.
     */
    public static char dirToShape(Direction dir) {
        switch (dir) {
            case RIGHT:
                return Constants.SHAPE_DIR_RIGHT;
            case UP:
                return Constants.SHAPE_DIR_UP;
            case LEFT:
                return Constants.SHAPE_DIR_LEFT;
            case DOWN:
                return Constants.SHAPE_DIR_DOWN;
        }
        return Constants.SHAPE_DIR_UNKNOWN;
    }

    /**
     * Calculates the remainder of {@code a} after dividing by {@code m}.
     *
     * @param a
     * @param m
     *
     * @return the remainder.
     */
    public static int remainder(int a, int m) {
        a %= m;
        if (a < 0) {
            return a + m;
        }
        if (a >= m) {
            return a - m;
        }
        return a;
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
    public static Position nextPos(int row, int col, Direction dir) {
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
     * @param pos the {@code Position} of the current cell.
     * @param dir the {@code Direction} to move along.
     *
     * @return the {@code Position} corresponding to the next cell.
     */
    public static Position nextPos(Position pos, Direction dir) {
        return nextPos(pos.row, pos.col, dir);
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
    public static Position prevPos(int row, int col, Direction dir) {
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
     * @param pos the {@code Position} of the current cell.
     * @param dir the {@code Direction} to move along.
     *
     * @return the {@code Position} corresponding to the previous cell.
     */
    public static Position prevPos(Position pos, Direction dir) {
        return prevPos(pos.row, pos.col, dir);
    }

    /**
     * Returns the reverse (opposite) direction of the given one.
     *
     * @param dir the direction to get its reverse.
     *
     * @return the reverse direction.
     */
    public static Direction getReverseDir(Direction dir) {
        int i = dir.ordinal();
        Direction[] dirs = Direction.values();
        return dirs[(i + 2) % 4];
    }

    /**
     * Calculates the next direction if rotating with the given rotation action.
     * <p>
     * The allowed actions are only:
     * {@code AgentAction.ROTATE_RIGHT} and {@code AgentAction.ROTATE_LEFT}.
     *
     * @param dir    the current {@code Direction}.
     * @param action the rotation action to apply.
     *
     * @return
     */
    public static Direction nextDir(Direction dir, AgentAction action) {
        int i = dir.ordinal();

        if (action == AgentAction.ROTATE_RIGHT) {
            i--;
            if (i < 0) {
                i = 3;
            }
        } else {
            i++;
            if (i > 3) {
                i = 0;
            }
        }

        return Direction.values()[i];
    }

    /**
     * Calculates the previous direction if rotating with the reverse of
     * the given rotation action.
     * <p>
     * The allowed actions are only:
     * {@code AgentAction.ROTATE_RIGHT} and {@code AgentAction.ROTATE_LEFT}.
     *
     * @param dir    the current {@code Direction}.
     * @param action the rotation action to apply.
     *
     * @return
     */
    public static Direction prevDir(Direction dir, AgentAction action) {
        int i = dir.ordinal();

        if (action == AgentAction.ROTATE_LEFT) {
            i--;
            if (i < 0) {
                i = 3;
            }
        } else {
            i++;
            if (i > 3) {
                i = 0;
            }
        }

        return Direction.values()[i];
    }

    /**
     * Returns the reverse rotation action of the given one.
     * <p>
     * The allowed actions are only:
     * {@code AgentAction.ROTATE_RIGHT} and {@code AgentAction.ROTATE_LEFT}.
     *
     * @param action the rotation action.
     *
     * @return the reverse rotation action.
     */
    public static AgentAction getReverseRotation(AgentAction action) {
        if (action == AgentAction.ROTATE_RIGHT) {
            return AgentAction.ROTATE_LEFT;
        } else {
            return AgentAction.ROTATE_RIGHT;
        }
    }
}
