package algorithms;

import models.map.Grid;
import models.map.GuideCell;
import utils.Constants.*;
import models.map.Dimensions;
import models.map.Position;

import java.util.LinkedList;
import java.util.Queue;


public class Planner {

    /**
     * Runs a BFS algorithms on the given grid to compute the guide map
     * to the given destination position.
     *
     * @param map the grid map to compute upon.
     * @param dst the destination position.
     *
     * @return a 2D {@code GuideCell} array representing the guide map to reach the destination.
     */
    public static GuideCell[][] bfs(Grid map, Position dst) {
        // Initialize BFS algorithm requirements
        Dimensions dim = map.getDimensions();
        Queue<Position> q = new LinkedList<>();
        GuideCell[][] ret = GuideCell.allocate2D(dim.rows, dim.cols);

        // Add BFS base case
        q.add(dst);
        ret[dst.row][dst.col].set(0, Direction.STILL);

        // Keep expanding all cell in the map
        while (!q.isEmpty()) {
            // Get current node and its distance to the destination
            Position cur = q.poll();
            int dis = ret[cur.row][cur.col].distance;

            // Expanding in all directions
            for (Direction dir : Direction.values()) {
                // Get previous position
                Position prv = map.previous(cur, dir);

                // Continue if already visited
                if (ret[prv.row][prv.col].isReachable()) {
                    continue;
                }

                // Add expanded cell to the queue and update its guide values
                q.add(prv);
                ret[prv.row][prv.col].set(dis, dir);
            }
        }

        return ret;
    }
}
