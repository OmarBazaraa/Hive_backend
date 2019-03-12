package algorithms;

import models.agents.Agent;
import models.maps.GuideGrid;
import models.maps.MapCell;
import models.maps.MapGrid;
import models.maps.GuideCell;
import models.warehouses.Warehouse;
import utils.Constants.*;
import models.maps.utils.Dimensions;
import models.maps.utils.Position;
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
     * @param map   the maps's grid of the warehouse.
     */
    public static void step(Agent agent, MapGrid map) throws Exception {
        // Return if the agent already performed a move in this time step
        // This can only happen when a higher priority agent displaces some other lower priority agent
        // so that the higher one can move towards its desired location
        if (agent.getLastActionTime() >= Warehouse.getTime()) {
            return;
        }

        // Get the required action of the agent
        AgentAction action = agent.getNextAction();

        // Try to perform the required action
        if (action == AgentAction.MOVE) {
            move(agent, map);
        } else {
            agent.executeAction(action, map);
        }
    }

    public static boolean move(Agent agent, MapGrid map) throws Exception {
        // Get current agent and guide maps
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
                agent.executeAction(Utility.dirToAction(dir), map);
                return true;
            }
        }

        //
        // No empty direction is found, try to bring blank
        //
        for (Direction dir : dirs) {
            // Get next position
            Position nxt = map.next(cur, dir);
            MapCell cell = map.get(nxt);

            // Get agent at the next cell
            Agent nxtAgent = cell.getAgent();

            // Try to move the agent at the next cell to bring blank to the main agent
            if (bringBlank(nxtAgent, agent, map)) {
                agent.executeAction(Utility.dirToAction(dir), map);
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
     *
     * @return {@code true} if the function manged to bring blank agent; {@code false} otherwise.
     */
    public static boolean bringBlank(Agent agent, Agent mainAgent, MapGrid map) throws Exception {
        // Return if this agent has higher priority than the agent needed to be moved
        if (agent.compareTo(mainAgent) >= 0) {
            return false;
        }

        // Return if this agent has already been displaced by a higher priority agent
        if (agent.getLastActionTime() >= Warehouse.getTime()) {
            return false;
        }

        // Return if we already tried to displace this agent, to avoid infinite loop
        if (agent.getLastBringBlankTime() >= Warehouse.getTime()) {
            return false;
        }

        // Set bring blank time
        agent.updateLastBringBlankTime();

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

            if (cell.getType() == CellType.EMPTY || cell.getType() == CellType.RACK) {
                agent.executeAction(Utility.dirToAction(dir), map);
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
                Agent nextAgent = cell.getAgent();

                if (bringBlank(nextAgent, mainAgent, map)) {
                    agent.executeAction(Utility.dirToAction(dir), map);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Runs a BFS algorithms on the given grid to compute the guide maps
     * to the given destination position.
     *
     * @param map         the map grid to compute upon.
     * @param dst         the destination position.
     * @param accessTypes the accessible {@code CellType} to set.
     *
     * @return a 2D {@code GuideCell} array representing the guide map to reach the destination.
     */
    public static GuideCell[][] bfs(MapGrid map, Position dst, CellType... accessTypes) {
        // Initialize BFS algorithm requirements
        Dimensions dim = map.getDimensions();
        Queue<Position> q = new LinkedList<>();
        GuideCell[][] ret = GuideCell.allocate2D(dim.rows, dim.cols);

        // Add BFS base case
        q.add(dst);
        ret[dst.row][dst.col].setDistance(0);

        // Keep expanding all cell in the maps
        while (!q.isEmpty()) {
            // Get current node and its distance to the destination
            Position cur = q.poll();
            int dis = ret[cur.row][cur.col].getDistance();

            // Expanding in all directions
            for (Direction dir : Direction.values()) {
                // Get previous position
                Position prv = map.previous(cur, dir);

                // Continue if not accessible cell
                if (!map.isAccessible(prv, accessTypes)) {
                    continue;
                }

                // Continue if already visited
                if (ret[prv.row][prv.col].isReachable()) {
                    continue;
                }

                // Add expanded cell to the queue and update its guide values
                q.add(prv);
                ret[prv.row][prv.col].setDistance(dis + 1);
            }
        }

        return ret;
    }
}
