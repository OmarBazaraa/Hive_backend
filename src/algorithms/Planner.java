package algorithms;

import models.components.Agent;
import models.map.GuideGrid;
import models.map.MapCell;
import models.map.MapGrid;
import models.map.GuideCell;
import utils.Constants.*;
import models.map.base.Dimensions;
import models.map.base.Position;
import utils.Utility;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * This {@code Planner} class contains some static method for multi-agent path planning algorithms.
 */
public class Planner {

    /**
     * Performs one step forward in the path planning algorithm.
     * <p>
     * This function is to be called for all active agents in descending order of their priorities.
     *
     * @param agent the agent to bringBlank.
     * @param map   the map's grid of the warehouse.
     * @param time  the current time step.
     */
    public static void step(Agent agent, MapGrid map, int time) throws Exception {
        // Return if the agent already performed a bringBlank in this time step
        // This can only happen when a higher priority agent displaces some lower priority agents
        // so it can bringBlank in its desired location
        if (agent.getLastActionTime() >= time) {
            return;
        }

        // Get the required action of the agent
        AgentAction action = agent.getNextAction();

        // Try to perform the required action
        if (action == AgentAction.MOVE) {
            move(agent, map, time);
        } else {
            agent.executeAction(action, map, time);
        }
    }

    public static boolean move(Agent agent, MapGrid map, int time) throws Exception {
        // Get current agent and guide map
        Position cur = agent.getPosition();
        GuideGrid guide = agent.getGuideMap();

        // List of directions occupied by agents
        List<Direction> dirs = new ArrayList<>();

        //
        // Iterate over all directions
        //
        for (Direction dir : Direction.values()) {
            // Get next position
            Position nxt = map.next(cur, dir);

            // Skip moving away from the target
            if (guide.getDistance(nxt) >= guide.getDistance(cur)) {
                continue;
            }

            // Move the agent if accessible cell, otherwise add the current direction to the list
            if (map.get(nxt).hasAgent()) {
                dirs.add(dir);
            } else {
                agent.executeAction(Utility.dirToAction(dir), map, time);
                return true;
            }
        }

        //
        // No empty direction is found, try to bring blanks
        //
        for (Direction dir : dirs) {
            // Get next position
            Position nxt = map.next(cur, dir);
            MapCell cell = map.get(nxt);

            // Get agent at the next cell
            Agent nxtAgent = (Agent) cell.srcObj;

            // Try to move the agent at the next cell to bring blank to the main agent
            if (bringBlank(nxtAgent, agent, map, time)) {
                agent.executeAction(Utility.dirToAction(dir), map, time);
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to bringBlank the given agent by bring a blank position
     *
     * @param agent
     * @param mainAgent
     * @param map
     * @param time
     *
     * @return {@code true} if the function manged to bring blank agent, {@code false} otherwise.
     */
    public static boolean bringBlank(Agent agent, Agent mainAgent, MapGrid map, int time) throws Exception {
        // Return if this agent has higher priority than the agent needed to be moved
        if (agent.compareTo(mainAgent) >= 0) {
            return false;
        }

        // Return if this agent has already been displaced by a higher priority agent
        if (agent.getLastActionTime() >= time) {
            return false;
        }

        // Return if we already tried to displace this agent, to avoid infinite loop
        if (agent.getLastBringBlankTime() >= time) {
            return false;
        }

        // Set bring blank time
        agent.setLastBringBlankTime(time);

        //
        Position pos = agent.getPosition();
        GuideGrid guide = agent.getGuideMap();

        for (Direction dir : Direction.values()) {
            Position nxt = map.next(pos, dir);

            if (!map.isInBound(nxt)) {
                continue;
            }

            if (guide.getDistance(nxt) >= guide.getDistance(pos)) {
                continue;
            }

            MapCell cell = map.get(nxt);

            if (cell.type == CellType.EMPTY || cell.type == CellType.RACK) {
                agent.executeAction(Utility.dirToAction(dir), map, time);
                return true;
            }
        }

        for (Direction dir : Direction.values()) {
            Position nxt = map.next(pos, dir);

            if (!map.isInBound(nxt)) {
                continue;
            }

            // TODO: try moving agents out of its path
            if (guide.getDistance(nxt) < guide.getDistance(pos)) {
                continue;
            }

            MapCell cell = map.get(nxt);

            if (cell.hasAgent()) {
                Agent nextAgent = (Agent) cell.srcObj;

                if (bringBlank(nextAgent, mainAgent, map, time)) {
                    agent.executeAction(Utility.dirToAction(dir), map, time);
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
     * @param map                 the grid map to compute upon.
     * @param dst                 the destination position.
     * @param accessibleCellTypes the accessible {@code CellType} to set.
     *
     * @return a 2D {@code GuideCell} array representing the guide map to reach the destination.
     */
    public static GuideCell[][] bfs(MapGrid map, Position dst, CellType... accessibleCellTypes) {
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

                // Continue if not accessible cell
                if (!map.isAccessible(prv, accessibleCellTypes)) {
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
