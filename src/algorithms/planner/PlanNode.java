package algorithms.planner;

import models.agents.Agent;
import models.facilities.Facility;
import models.facilities.Rack;
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
     * Array holding the direction leading to every state.
     */
    private static int[][][] par;

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

        source = src;
        target = dst;
        par = new int[rows][cols][Constants.DIR_COUNT];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                for (int d : Constants.DIRECTIONS) {
                    par[i][j][d] = -1;
                }
            }
        }
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
     * The direction of the {@code Agent} in the this simulation state.
     */
    public int dir;

    /**
     * The direction of the parent state leading to this state.
     */
    public int parDir;

    /**
     * The weight of the state. That is, the distance from the initial state to this {@code state}.
     */
    public int weight;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code PlanNode} object.
     *
     * @param row the row position of the {@code Agent}.
     * @param col the row position of the {@code Agent}.
     * @param dir the current direction of the {@code Agent}.
     */
    public PlanNode(int row, int col, int dir) {
        this(row, col, dir, Constants.DIR_RIGHT, 0);
    }

    /**
     * Constructs a new {@code PlanNode} object.
     *
     * @param row    the row position of the {@code Agent}.
     * @param col    the row position of the {@code Agent}.
     * @param dir    the direction of the {@code Agent}.
     * @param parDir the direction of the parent state.
     * @param weight the weight of the new state.
     */
    private PlanNode(int row, int col, int dir, int parDir, int weight) {
        this.row = row;
        this.col = col;
        this.dir = dir;
        this.parDir = parDir;
        this.weight = weight;
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

        // Skip if already visited or blocked cell
        if (isVisited() || cell.isBlocked()) {
            return false;
        }

        // Cannot pass on a rack cell if currently the agent is loading another one
        if (source.isLoaded() && cell.getType() == CellType.RACK) {
            Rack rack = (Rack) cell.getFacility();
            return rack.isBound();
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
        return (par[row][col][dir] != -1);
    }

    /**
     * Marks this state node as visited.
     */
    public void visit() {
        par[row][col][dir] = parDir;
    }

    /**
     * Calculates the next state if moving in the given direction.
     *
     * @param d the direction to move in.
     *
     * @return the next state {@code PlanNode}.
     */
    public PlanNode next(int d) {
        int r = row + Constants.DIR_ROW[d];
        int c = col + Constants.DIR_COL[d];
        return new PlanNode(r, c, d, dir, weight + Utility.getRotationsCount(d, dir) + 1);
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
        return new PlanNode(r, c, parDir, par[r][c][parDir], weight - Utility.getRotationsCount(parDir, dir) - 1);
    }

    /**
     * Updates the cost of this state by adding more weight if it holds
     * an idle agent.
     */
    public void updateWeight() {
        Agent blockingAgent = warehouse.get(row, col).getAgent();

        if (blockingAgent == null || blockingAgent.isActive()) {
            return;
        }

        weight += 1;
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
     * Calculates and return the total estimated cost to reach the target from the initial location.
     * That is, the f(s) function of the A* algorithm.
     * <p>
     * f(s) = g(s) + h(s), where:
     * f(s) is the total estimated cost to reach the target from the initial location,
     * g(s) the actual cost to reach the current state from the initial location, and
     * h(s) the estimated cost to reach the target from the current state.
     *
     * @return the total estimated cost.
     */
    public int totalCost() {
        return weight + heuristic();
    }

    /**
     * Compares whether some other object is less than, equal to, or greater than this one.
     *
     * @param obj the reference object with which to compare.
     *
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(PlanNode obj) {
        long lhs = totalCost();
        long rhs = obj.totalCost();

        if (lhs == rhs) {
            return 0;
        }

        return lhs < rhs ? -1 : 1;
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
        builder.append(" dir: ").append(dir);
        builder.append(" weight: ").append(weight).append(",");
        builder.append(" }");

        return builder.toString();
    }
}
