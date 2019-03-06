package algorithms;

import models.components.Agent;
import models.map.Cell;
import models.map.Grid;
import models.map.GuideCell;
import utils.Constants.*;
import models.map.base.Dimensions;
import models.map.base.Position;

import java.util.LinkedList;
import java.util.Queue;


/**
 * This {@code Planner} class contains some static method for multi-agent path planning algorithms.
 */
public class Planner {

    /**
     *
     */
    public static void step(Agent agent, Grid map, int time) throws Exception {
        if (agent.getLastActionTime() >= time) {
            return;
        }

        AgentAction action = agent.getNextAction();

        if (action != AgentAction.MOVE) {
            agent.executeAction(action, time);
            return;
        }

        move(agent, agent, map, time);
    }

    public static boolean move(Agent agent, Agent neededAgent, Grid map, int time) throws Exception {
        // TODO: Avoid infinite loop by checking for lastGetBlankTime

        if (agent.getLastActionTime() >= time) {
            return false;
        }

        if (neededAgent.compareTo(agent) < 0) {
            return false;
        }

        Position pos = agent.getPosition();
        GuideCell guide = agent.getGuideAt(pos.row, pos.col);

        for (Direction dir : Direction.values()) {
            Position nxt = map.next(pos, dir);

            if (!map.isInBound(nxt)) {
                continue;
            }

            if (guide.distance != agent.getGuideAt(nxt.row, nxt.col).distance + 1) {
                continue;
            }

            Cell cell = map.get(nxt);

            if (cell.type == CellType.EMPTY || cell.type == CellType.RACK) {
                agent.move(map, dir, time);
                return true;
            }
        }

        for (Direction dir : Direction.values()) {
            Position nxt = map.next(pos, dir);

            if (!map.isInBound(nxt)) {
                continue;
            }

            // TODO: try moving agents out of its path
            if (guide.distance != agent.getGuideAt(nxt.row, nxt.col).distance + 1) {
                continue;
            }

            Cell cell = map.get(nxt);

            if (cell.type == CellType.AGENT) {
                Agent nextAgent = (Agent) cell.srcObj;

                if (move(nextAgent, neededAgent, map, time)) {
                    agent.move(map, dir, time);
                    return true;
                }
            }
        }

        return false;
    }

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

                // Continue if occupied cell
                if (!map.isEmpty(prv)) {
                    continue;
                }

                // Continue if already visited
                if (ret[prv.row][prv.col].isReachable()) {
                    continue;
                }

                // Add expanded cell to the queue and update its guide values
                q.add(prv);
                ret[prv.row][prv.col].set(dis + 1, dir);
            }
        }

        return ret;
    }
}
