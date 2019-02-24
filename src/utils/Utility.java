package utils;

import utils.Constants.*;

public class Utility {

    public static Position nextPosition(Position pos, Direction dir) {
        int r = pos.row, c = pos.col;

        switch (dir) {
            case UP:
                return new Position(r - 1, c);
            case RIGHT:
                return new Position(r, c + 1);
            case DOWN:
                return new Position(r + 1, c);
            case LEFT:
                return new Position(r, c - 1);
        }

        return pos;
    }

    public static Position previousPosition(Position pos, Direction dir) {
        int r = pos.row, c = pos.col;

        switch (dir) {
            case UP:
                return new Position(r + 1, c);
            case RIGHT:
                return new Position(r, c - 1);
            case DOWN:
                return new Position(r - 1, c);
            case LEFT:
                return new Position(r, c + 1);
        }

        return pos;
    }
}
