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
     * @param agent the {@code Agent} to be stepped.
     * @param map   the {@code MapGrid} of the {@code Warehouse}.
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
            route(agent, agent, map);
        } else {
            agent.executeAction(action, map);
        }
    }

    public static boolean route(Agent agent, Agent mainAgent, MapGrid map) throws Exception {
        // Return if this agent has higher priority than the agent needed to be moved
        if (agent.compareTo(mainAgent) > 0) {
            return false;
        }

        // Return if this agent has already been displaced by a higher priority agent,
        // or it has been tried to displace it
        long time = Warehouse.getTime();
        if (agent.getLastActionTime() >= time || agent.getLastBringBlankTime() >= time) {
            return false;
        }

        // Update bring blank time
        agent.updateLastBringBlankTime();

        // Get current agent and guide maps
        Position cur = agent.getPosition();
        GuideGrid guide = agent.getGuideMap();

        // Get list of valid directions
        List<Direction> dirs;

        if (guide != null) {
            dirs = guide.getGuideDirections(cur);
        } else {
            dirs = map.getEmptyDirections(cur);
        }

        //
        // Iterate over all guide directions
        //
        for (Direction dir : dirs) {
            // Get next position
            Position nxt = map.next(cur, dir);

            // Skip moving away from the target if this is our main agent
            if (agent == mainAgent) {
                // Guide map should never be null here
                if (guide != null && guide.getDistance(nxt) >= guide.getDistance(cur)) {
                    continue;
                }
            }

            // Get agent in the next cell
            Agent nxtAgent = map.get(nxt).getAgent();

            // Move the agent if free cell or we manged to get a blank in the next position
            if (nxtAgent == null || route(nxtAgent, mainAgent, map)) {
                agent.executeAction(Utility.dirToAction(dir), map);
                return true;
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
