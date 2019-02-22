package utils;

import utils.Constants.*;

public class Utility {

    public static Position nextPosition(Position pos, Direction dir) {
        int r = pos.r, c = pos.c;

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
}
