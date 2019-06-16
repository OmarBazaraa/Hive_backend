package algorithms.planner;

import models.agents.Agent;
import models.maps.*;
import models.maps.utils.Dimensions;
import models.maps.utils.Position;

import models.warehouses.Warehouse;
import utils.Constants;
import utils.Constants.*;
import utils.Utility;

import java.util.*;


/**
 * This {@code Planner} class contains some static method for multi-agent path planning algorithms.
 */
public class Planner {

    /**
     * Runs a BFS algorithms on the given grid to compute the guide maps
     * to the given destination position.
     *
     * @param map         the map grid to compute upon.
     * @param dst         the destination position.
     *
     * @return the computed guide map to reach the destination.
     */
    public static GuideGrid computeGuideMap(MapGrid map, Position dst) {
        // Initialize BFS algorithm requirements
        GuideCell[][] ret = GuideCell.allocate2D(map.getRows(), map.getCols());

        // Create the planning queue and add the initial state
        Queue<Position> q = new LinkedList<>();
        q.add(dst);
        ret[dst.row][dst.col].setDistance(0);

        // Keep expanding all cells in the maps
        while (!q.isEmpty()) {
            // Get current node and its distance to the destination
            Position cur = q.poll();
            int dis = ret[cur.row][cur.col].getDistance();

            // Expanding in all directions
            for (Direction dir : Direction.values()) {
                // Get net position
                Position nxt = map.next(cur, dir);

                // Skip if out of bound or obstacle cell or already visited
                if (map.isObstacle(nxt) || ret[nxt.row][nxt.col].isReachable()) {
                    continue;
                }

                // Set the guide value
                ret[nxt.row][nxt.col].setDistance(dis + 1);

                // Add expanded cell to the queue only if it is not holding a facility
                if (!map.get(nxt).hasFacility()) {
                    q.add(nxt);
                }
            }
        }

        return new GuideGrid(ret);
    }


    /**
     * Routes the given {@code Agent} one step towards its target location
     * using the given pre-planned action.
     *
     * @param agent  the {@code Agent} to be routed.
     * @param action the {@code AgentAction} to apply.
     */
    public static boolean route(Agent agent, AgentAction action) {
        // Global information about the warehouse
        Warehouse warehouse = Warehouse.getInstance();
        MapGrid map = warehouse.getMap();
        TimeGrid timeMap = warehouse.getTimeMap();
        long time = warehouse.getTime();

        // Agent current position information
        Position curPos = agent.getPosition();
        MapCell curCell = map.get(curPos);

        // Rotation actions are easy
        if (action != AgentAction.MOVE) {
            timeMap.clearAt(curPos, time);
            agent.move(action);
            return true;
        }

        // Agent next position information
        Position nxtPos = Utility.nextPos(curPos, agent.getDirection());
        MapCell nxtCell = map.get(nxtPos);
        Agent a = nxtCell.getAgent();

        // Check if the can move to the next cell
        if (a == null || slide(a, agent)) {
            timeMap.clearAt(curPos, time);
            curCell.setAgent(null);
            nxtCell.setAgent(agent);
            agent.setPosition(nxtPos);
            agent.move(action);
            return true;
        }

        // Cannot move the agent
        agent.dropPlan();
        return false;
    }

    /**
     * Tries sliding the given {@code Agent} in order to bring a blank location
     * for the main {@code Agent} to move into.
     *
     * @param slidingAgent the {@code Agent} to slide away.
     * @param mainAgent    the main {@code Agent}.
     *
     * @return {@code true} if sliding is done successfully; {@code false} otherwise.
     */
    private static boolean slide(Agent slidingAgent, Agent mainAgent) {
        return false;
    }

    /**
     * Plans a sequence of actions to be done by the given {@code Agent} to reach
     * its target.
     *
     * @param agent the {@code Agent} to plan for.
     * @param dst   the target position of the {@code Agent}.
     *
     * @return a sequence of {@code AgentAction}; or {@code null} if currently unreachable.
     */
    public static Stack<AgentAction> plan(Agent agent, Position dst) {
        // Initialize planning algorithm
        Warehouse warehouse = Warehouse.getInstance();
        MapGrid map = warehouse.getMap();
        TimeGrid timeMap = warehouse.getTimeMap();
        PlanNode.initializes(map.getRows(), map.getCols(), 4, dst);

        // Create the planning queue and add the initial state
        PriorityQueue<PlanNode> q = new PriorityQueue<>();
        q.add(new PlanNode(
                agent.getPosition(),
                agent.getDirection(),
                AgentAction.MOVE,       // The initial action is not significant but it cannot be AgentAction.NOTHING
                warehouse.getTime()
        ));

        //
        // Keep exploring states until the target is found
        //
        while (!q.isEmpty()) {
            // Get the current best node in the queue
            PlanNode cur = q.remove();

            // Skip visited states
            if (cur.isVisited()) {
                continue;
            }

            // Mark current state as visited
            cur.visit();

            //
            // Try exploring further states by trying all possible actions
            //
            for (AgentAction action : Constants.MOVE_ACTIONS) {
                // Get next state after doing the current action
                PlanNode nxt = cur.next(action);

                // Skip invalid states
                if (!canExplore(nxt, agent, dst, map, timeMap)) {
                    continue;
                }

                // Check if target has been reached
                if (dst.equals(nxt.pos)) {
                    return constructPlan(agent, nxt);
                }

                // Add state for further exploration
                q.add(nxt);
            }
        }

        // No path has been found
        return null;
    }

    /**
     * Checks whether it is possible to further explore and expand this plan node or not.
     *
     * @param nxt     the {@code PlanNode} to check.
     * @param agent   the associated {@code Agent}.
     * @param dst     the final destination of the {@code Agent}.
     * @param map     the grid map of the {@code Warehouse}.
     * @param timeMap the timeline map.
     *
     * @return {@code true} if it is possible to explore; {@code false} otherwise.
     */
    private static boolean canExplore(PlanNode nxt, Agent agent, Position dst, MapGrid map, TimeGrid timeMap) {
        // Skip out of bound or visited states
        if (!map.isInBound(nxt.pos) || nxt.isVisited()) {
            return false;
        }

        // Get the agent that is planned to be in this state
        Agent a = timeMap.getAgentAt(nxt.pos, nxt.time);

        // If there is an agent with higher priority then skip this state as well
        if (a != null && agent.getPriority() < a.getPriority()) {
            return false;
        }

        // Get cell
        MapCell cell = map.get(nxt.pos);

        // Skip obstacle cells
        if (cell.isObstacle()) {
            return false;
        }

        // If there is a facility then we can only explore it
        // when it is either the source or destination position
        if (cell.hasFacility()) {
            return agent.getPosition().equals(nxt.pos) || dst.equals(nxt.pos);
        }

        // The state is empty so we can explore it
        return true;
    }

    /**
     * Constructs the sequence of actions leading to the target after
     * finishing the planning, and update the timeline map of the {@code Warehouse}
     * in accordance.
     *
     * @param agent the {@code Agent} to plan for.
     * @param node  the target state {@code PlanNode}.
     *
     * @return a sequence of {@code AgentAction} to reach the given state.
     */
    private static Stack<AgentAction> constructPlan(Agent agent, PlanNode node) {
        TimeGrid timeMap = Warehouse.getInstance().getTimeMap();

        // Prepare the stack of actions
        Stack<AgentAction> ret = new Stack<>();

        //
        // Keep moving backward until reaching the position of the agent
        //
        while (true) {
            // Get the agent that is planned to be in this state
            Agent a = timeMap.getAgentAt(node.pos, node.time);

            // If there is an agent then it must be with lower priority
            // So drop its plan for now
            if (a != null) {
                a.dropPlan();
            }

            // Update timeline
            timeMap.setAgentAt(node.pos, node.time, agent);

            // Stop when reaching the initial position of the agent
            if (agent.getPosition().equals(node.pos) && agent.getDirection() == node.dir) {
                break;
            }

            // Add the action and proceed to previous state
            ret.add(node.action);
            node = node.previous();
        }

        return ret;
    }

    /**
     * Drops the current plan of the given {@code Agent} and updates the
     * timeline map of the {@code Warehouse} in accordance.
     *
     * @param agent   the {@code Agent} to drop its plan.
     * @param actions the plan of the agent.
     */
    public static void dropPlan(Agent agent, Stack<AgentAction> actions) {
        TimeGrid timeMap = Warehouse.getInstance().getTimeMap();

        // Create a node with the current state of the agent
        PlanNode node = new PlanNode(
                agent.getPosition(),
                agent.getDirection(),
                AgentAction.MOVE,
                Warehouse.getInstance().getTime()
        );

        //
        // Keep dropping the plan one action at a time
        //
        while (true) {
            // Clear the time slot of the agent in the current state
            timeMap.clearAt(node.pos, node.time);

            // Stop when no further actions in the plan
            if (actions.isEmpty()) {
                break;
            }

            // Go to the next state in the plan
            node = node.next(actions.pop());
        }
    }
}
