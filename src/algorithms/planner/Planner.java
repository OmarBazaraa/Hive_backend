package algorithms.planner;

import models.agents.Agent;
import models.facilities.Facility;
import models.maps.GridCell;
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
     * Runs a BFS algorithms on the {@link Warehouse} grid to compute the
     * shortest distance guide map from every cell to the given destination position.
     *
     * @param row the row position of the destination.
     * @param col the column position of the destination.
     *
     * @return the computed guide map to reach the destination.
     */
    public static int[][] computeGuideMap(int row, int col) {
        // Global information about the warehouse
        Warehouse warehouse = Warehouse.getInstance();
        int rows = warehouse.getRows();
        int cols = warehouse.getCols();

        // Initialize BFS algorithm requirements
        int[][] ret = new int[rows][cols];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                ret[i][j] = Integer.MAX_VALUE;  // Set all distance initially to infinity
            }
        }

        // Create the planning queue and add the initial state
        Queue<Position> q = new LinkedList<>();
        q.add(new Position(row, col));
        ret[row][col] = 0;

        // Keep expanding all cells in the maps
        while (!q.isEmpty()) {
            // Get current node and its distance to the destination
            Position cur = q.poll();

            // Expanding in all directions
            for (Direction dir : Direction.values()) {
                // Get net position
                Position nxt = Utility.nextPos(cur, dir);

                // Skip if next position is out of bound
                if (!warehouse.isInBound(nxt.row, nxt.col)) {
                    continue;
                }

                // Get next cell
                GridCell cell = warehouse.get(nxt.row, nxt.col);

                // Skip if obstacle or already visited cell
                if (cell.isObstacle() || ret[nxt.row][nxt.col] < Integer.MAX_VALUE) {
                    continue;
                }

                // Set the guide value
                ret[nxt.row][nxt.col] = ret[cur.row][cur.col] + 1;

                // Add expanded cell to the queue only if it is not holding a facility
                if (!cell.hasFacility()) {
                    q.add(nxt);
                }
            }
        }

        return ret;
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
        long time = warehouse.getTime();

        // Agent current position information
        Position curPos = agent.getPosition();
        GridCell curCell = warehouse.get(curPos.row, curPos.col);

        // Rotation actions are easy
        if (action != AgentAction.MOVE) {
            curCell.clearScheduleAt(time);
            agent.move(action);
            return true;
        }

        // Agent next position information
        Position nxtPos = Utility.nextPos(curPos, agent.getDirection());
        GridCell nxtCell = warehouse.get(nxtPos.row, nxtPos.col);
        Agent a = nxtCell.getAgent();

        // Check if the can move to the next cell
        if (a == null || slide(a, agent)) {
            curCell.clearScheduleAt(time);
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
     * @param source the source {@code Agent} to plan for.
     * @param target the target {@code Facility} of the {@code Agent}.
     *
     * @return a sequence of {@code AgentAction}; or {@code null} if currently unreachable.
     */
    public static Stack<AgentAction> plan(Agent source, Facility target) {
        // Initialize planning algorithm
        Warehouse warehouse = Warehouse.getInstance();
        PlanNode.initializes(source, target);

        // Create the planning queue and add the initial state
        PriorityQueue<PlanNode> q = new PriorityQueue<>();
        q.add(new PlanNode(source.getRow(), source.getCol(), source.getDirection(), warehouse.getTime()));

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
                if (!nxt.canVisit()) {
                    continue;
                }

                // Check if target has been reached
                if (nxt.isFinal()) {
                    return constructPlan(source, nxt);
                }

                // Add state for further exploration
                q.add(nxt);
            }
        }

        // No path has been found
        return null;
    }

    /**
     * Constructs the sequence of actions leading to the target after
     * finishing the planning, and update the timeline map of the {@code Warehouse}
     * in accordance.
     *
     * @param source the source {@code Agent} to plan for.
     * @param node   the target state {@code PlanNode}.
     *
     * @return a sequence of {@code AgentAction} to reach the given state.
     */
    private static Stack<AgentAction> constructPlan(Agent source, PlanNode node) {
        Warehouse warehouse = Warehouse.getInstance();

        // Prepare the stack of actions
        Stack<AgentAction> ret = new Stack<>();

        //
        // Keep moving backward until reaching the position of the agent
        //
        while (true) {
            // Get the agent that is planned to be in this state
            GridCell cell = warehouse.get(node.row, node.col);
            Agent a = cell.getScheduledAt(node.time);

            // If there is an agent then it must be with lower priority
            // So drop its plan for now
            if (a != null) {
                a.dropPlan();
            }

            // Update timeline
            cell.setScheduleAt(node.time, source);

            // Stop when reaching the initial state of the agent
            if (node.isInitial()) {
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
        Warehouse warehouse = Warehouse.getInstance();

        // Create a node with the current state of the agent
        PlanNode node = new PlanNode(agent.getRow(), agent.getCol(), agent.getDirection(), warehouse.getTime());

        //
        // Keep dropping the plan one action at a time
        //
        while (true) {
            // Clear the time slot of the agent in the current state
            GridCell cell = warehouse.get(node.row, node.col);
            cell.clearScheduleAt(node.time);

            // Stop when no further actions in the plan
            if (actions.isEmpty()) {
                break;
            }

            // Go to the next state in the plan
            node = node.next(actions.pop());
        }
    }
}
