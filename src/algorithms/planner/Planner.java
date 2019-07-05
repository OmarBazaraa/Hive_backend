package algorithms.planner;

import models.agents.Agent;
import models.facilities.Facility;
import models.maps.GridCell;
import models.maps.Position;
import models.warehouses.Warehouse;

import utils.Constants;

import java.util.*;


/**
 * This {@code Planner} class contains some static method for multi-agent path planning algorithms.
 */
public class Planner {

    //
    // Guide Map
    //

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
        // Initialize BFS algorithm requirements
        Warehouse warehouse = Warehouse.getInstance();
        int rows = warehouse.getRows();
        int cols = warehouse.getCols();
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

        //
        // Keep exploring all states in the warehouse
        //
        while (!q.isEmpty()) {
            // Get current node
            Position cur = q.poll();

            //
            // Expanding in all directions
            //
            for (int dir : Constants.DIRECTIONS) {
                // Get next position
                Position nxt = cur.next(dir);

                // Skip if next position is out of bound
                if (warehouse.isOutBound(nxt.row, nxt.col)) {
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

    // ===============================================================================================
    //
    // Planning Sequence of Actions
    //

    /**
     * Plans a sequence of actions to be done by the given {@code Agent} to reach
     * its target.
     *
     * @param source the source {@code Agent} to plan for.
     * @param target the target {@code Facility} of the {@code Agent}.
     *
     * @return a sequence of directions to move along; or {@code null} if currently unreachable.
     */
    public static Stack<Integer> plan(Agent source, Facility target) {
        // No plan can be found if the target facility is currently bound to another agent
        if (target.isBound() && target.getBoundAgent() != source) {
            return null;
        }

        // Initialize planning algorithm
        PlanNode.initializes(source, target);

        // Create the planning queue and add the initial state
        PriorityQueue<PlanNode> q = new PriorityQueue<>();
        q.add(new PlanNode(source.getRow(), source.getCol()));

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
            // Expanding in all directions
            //
            for (int dir : Constants.DIRECTIONS) {
                // Get next state after doing the current action
                PlanNode nxt = cur.next(dir);

                // Skip invalid states
                if (!nxt.canVisit()) {
                    continue;
                }

                // Check if target has been reached
                if (nxt.isFinal()) {
                    return constructPlan(nxt);
                }

                // Add state for further exploration
                nxt.updateWeight();
                q.add(nxt);
            }
        }

        // No path has been found
        return null;
    }

    /**
     * Constructs the sequence of actions leading to the target after
     * finishing the planning, and updates the timeline map of the {@code Warehouse}
     * in accordance.
     *
     * @param node the target state {@code PlanNode}.
     *
     * @return a sequence of directions to move along to reach the given state.
     */
    private static Stack<Integer> constructPlan(PlanNode node) {
        // Prepare the stack of actions
        Stack<Integer> ret = new Stack<>();

        // Keep moving backward until reaching the initial position of the agent
        while (!node.isInitial()) {
            ret.add(node.dir);
            node = node.previous();
        }

        // Return the sequence of direction leading to the target
        return ret;
    }
}
