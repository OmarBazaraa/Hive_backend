package algorithms.planner;

import models.agents.Agent;
import models.facilities.Facility;
import models.maps.GridCell;
import models.warehouses.Warehouse;

import utils.Constants;
import utils.Constants.*;
import utils.Utility;


/**
 * This {@code PlanNode} class represents a state node in the search tree of the
 * planning algorithm.
 */
public class PlanNode implements Comparable<PlanNode> {

    //
    // Static Variables & Methods
    //

    /**
     * Array holding the direction leading to any state.
     */
    private static int[][] par;

    /**
     * The {@code Warehouse} holding the map grid to plan into.
     */
    private static Warehouse warehouse = Warehouse.getInstance();

    /**
     * The source {@code Agent}.
     */
    private static Agent source;

    /**
     * The target {@code Facility} of the {@code Agent}.
     */
    private static Facility target;

    /**
     * Initializes the planning state.
     * <p>
     * This function should be called once before running the planning algorithm.
     *
     * @param src the source {@code Agent}.
     * @param dst the destination {@code Facility}.
     */
    public static void initializes(Agent src, Facility dst) {
        int rows = warehouse.getRows();
        int cols = warehouse.getCols();

        par = new int[rows][cols];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                par[i][j] = -1;
            }
        }

        source = src;
        target = dst;
    }

    // ===============================================================================================
    //
    // Member Variables
    //

    /**
     * The row position of the {@code Agent} in the this simulation state.
     */
    public int row;

    /**
     * The column position of the {@code Agent} in the this simulation state.
     */
    public int col;

    /**
     * The weight of the state. That is, the distance from the initial state to this {@code state}.
     */
    public int weight;

    /**
     * The direction leading to this state.
     */
    public int dir;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code PlanNode} object.
     *
     * @param row    the row position of the {@code Agent}.
     * @param col    the row position of the {@code Agent}.
     */
    public PlanNode(int row, int col) {
        this(row, col, 0, Constants.DIR_RIGHT);
    }

    /**
     * Constructs a new {@code PlanNode} object.
     *
     * @param row    the row position of the {@code Agent}.
     * @param col    the row position of the {@code Agent}.
     * @param weight the weight of the new state.
     * @param dir    the direction of the {@code Agent}.
     */
    private PlanNode(int row, int col, int weight, int dir) {
        this.row = row;
        this.col = col;
        this.weight = weight;
        this.dir = dir;
    }

    /**
     * Checks whether this state node is the initial state node.
     * That is, whether it has the same position and direction of the source {@code Agent}.
     *
     * @return {@code true} if it is an initial node; {@code false} otherwise.
     */
    public boolean isInitial() {
        return source.isCoincide(row, col);
    }

    /**
     * Checks whether this state node is the finial state node.
     * That is, whether it has the same position of the target {@code Facility}.
     *
     * @return {@code true} if it is a final node; {@code false} otherwise.
     */
    public boolean isFinal() {
        return target.isCoincide(row, col);
    }

    /**
     * Checks whether it is possible to further explore and expand this plan node or not.
     *
     * @return {@code true} if it is possible to explore; {@code false} otherwise.
     */
    public boolean canVisit() {
        // First of all return if out of warehouse boundaries
        if (warehouse.isOutBound(row, col)) {
            return false;
        }

        // Get this state cell
        GridCell cell = warehouse.get(row, col);

        // Skip if obstacle or already visited cell
        if (cell.isBlocked() || isVisited()) {
            return false;
        }

        // Get current agent in this state
        Agent a = cell.getAgent();

        // If that agent is currently locked or deactivated then skip this state as well
        if (a != null && a != source && (a.isLocked() || a.isDeactivated())) {
            return false;
        }

        // If there is a facility then we can only explore it
        // when it is either the source or target position
        if (cell.hasFacility()) {
            return source.isCoincide(row, col) || target.isCoincide(row, col);
        }

        // The state is empty so we can explore it
        return true;
    }

    /**
     * Checks whether this state node has been visited before or not.
     *
     * @return {@code true} if already visited; {@code false} otherwise.
     */
    public boolean isVisited() {
        return (par[row][col] != -1);
    }

    /**
     * Marks this state node as visited.
     */
    public void visit() {
        par[row][col] = dir;
    }

    /**
     * Calculates the next state if moving in the given direction.
     *
     * @param dir the direction to move in.
     *
     * @return the next state {@code PlanNode}.
     */
    public PlanNode next(int dir) {
        int r = row + Constants.DIR_ROW[dir];
        int c = col + Constants.DIR_COL[dir];
        return new PlanNode(r, c, weight + 1, dir);
    }

    /**
     * Calculates the previous state if moving in the best calculated direction
     * of the planning algorithm in reverse manner.
     *
     * @return the previous state {@code PlanNode}.
     */
    public PlanNode previous() {
        int r = row - Constants.DIR_ROW[dir];
        int c = col - Constants.DIR_COL[dir];
        return new PlanNode(r, c, weight - 1, par[r][c]);
    }

    /**
     * Calculates the heuristic score to reach the target state.
     *
     * @return the heuristic score.
     */
    public int heuristic() {
        return target.getDistanceTo(row, col);
    }

    /**
     * Compares whether some other object is less than, equal to, or greater than this one.
     *
     * @param obj the reference object with which to compare.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(PlanNode obj) {
        long lhs = weight + heuristic();
        long rhs = obj.weight + obj.heuristic();

        if (lhs == rhs) {
            return 0;
        }

        return lhs < rhs ? -1 : +1;
    }

    /**
     * Returns a string representation of this {@code PlanNode}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code PlanNode}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("PlanNode: {");
        builder.append(" pos: ").append("(").append(row).append(", ").append(col).append(")").append(",");
        builder.append(" dir: ").append(dir).append(",");
        builder.append(" weight: ").append(weight).append(",");
        builder.append(" leading_dir: ").append(dir);
        builder.append(" }");

        return builder.toString();
    }
}
