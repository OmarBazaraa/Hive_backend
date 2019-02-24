package models.map;

import models.hive.HiveObject;
import utils.Constants.*;


/**
 * This {@code GuideCell} class represents a cell in the guide map associated with
 * a target {@code DstHiveObject}.
 */
public class GuideCell {

    /**
     * The distance to reach the associated target {@code DstHiveObject}.
     */
    public int distance;

    /**
     * The direction to reach the associated target {@code DstHiveObject}.
     */
    public Direction direction;

    /**
     * Constructs a new guide cell.
     */
    public GuideCell() {
        this.distance = Integer.MAX_VALUE;
        this.direction = Direction.STILL;
    }

    /**
     * Constructs a new guide cell.
     *
     * @param distance  the distance to reach the target.
     * @param direction the direction to reach the target.
     */
    public GuideCell(int distance, Direction direction) {
        this.distance = distance;
        this.direction = direction;
    }

    /**
     * Checks whether the target is unreachable from this cell and vice versa.
     *
     * @return {@code true} if the target is unreachable, {@code false} otherwise.
     */
    public boolean isUnreachable() {
        return this.distance == Integer.MAX_VALUE;
    }
}
