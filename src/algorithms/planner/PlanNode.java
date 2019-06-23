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
     * Array holding the action leading to any state.
     */
    private static AgentAction[][][] par;

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
        int dirs = 4;

        par = new AgentAction[rows][cols][dirs];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                for (int k = 0; k < dirs; ++k) {
                    par[i][j][k] = AgentAction.NOTHING;
                }
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
     * The {@code Direction} of the {@code Agent} in the cell.
     */
    public Direction dir;

    /**
     * The time at which the {@code Agent} will be in this state.
     */
    public long time;

    /**
     * The action leading to this state.
     */
    public AgentAction action;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code PlanNode} object.
     *
     * @param row    the row position of the {@code Agent}.
     * @param col    the row position of the {@code Agent}.
     * @param dir    the {@code Direction} of the {@code Agent}.
     * @param time   the time at which the {@code Agent} will be in this state.
     */
    public PlanNode(int row, int col, Direction dir, long time) {
        this(row, col, dir, time, AgentAction.MOVE);
    }

    /**
     * Constructs a new {@code PlanNode} object.
     *
     * @param row    the row position of the {@code Agent}.
     * @param col    the row position of the {@code Agent}.
     * @param dir    the {@code Direction} of the {@code Agent}.
     * @param time   the time at which the {@code Agent} will be in this state.
     * @param action the action leading to this state.
     */
    private PlanNode(int row, int col, Direction dir, long time, AgentAction action) {
        this.row = row;
        this.col = col;
        this.dir = dir;
        this.time = time;
        this.action = action;
    }

    /**
     * Checks whether this state node is the initial state node.
     * That is, whether it has the same position and direction of the source {@code Agent}.
     *
     * @return {@code true} if it is an initial node; {@code false} otherwise.
     */
    public boolean isInitial() {
        return source.isCoincide(row, col) && dir == source.getDirection();
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

        // Get the agent that is scheduled to be in this state
        Agent a = cell.getScheduledAt(time);

        // If there is an agent with higher priority then skip this state as well
        if (a != null && a.compareTo(source) > 0) {
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
        return (par[row][col][dir.ordinal()] != AgentAction.NOTHING);
    }

    /**
     * Marks this state node as visited.
     */
    public void visit() {
        par[row][col][dir.ordinal()] = action;
    }

    /**
     * Calculates the next state if applying the given action.
     * <p>
     * The allowed actions are only:
     * {@code AgentAction.ROTATE_RIGHT}, {@code AgentAction.ROTATE_LEFT}, and
     * {@code AgentAction.MOVE}.
     *
     * @param action the action to apply.
     *
     * @return the next state {@code PlanNode}.
     */
    public PlanNode next(AgentAction action) {
        if (action == AgentAction.MOVE) {
            int i = dir.ordinal();
            int r = row + Constants.DIR_ROW[i];
            int c = col + Constants.DIR_COL[i];

            return new PlanNode(r, c, dir, time + 1, action);
        } else {
            return new PlanNode(row, col, Utility.nextDir(dir, action), time + 1, action);
        }
    }

    /**
     * Calculates the previous state if applying the best calculated action
     * of the planning algorithm in reverse manner.
     *
     * @return the previous state {@code PlanNode}.
     */
    public PlanNode previous() {
        PlanNode ret;

        if (action == AgentAction.MOVE) {
            int i = dir.ordinal();
            int r = row - Constants.DIR_ROW[i];
            int c = col - Constants.DIR_COL[i];

            ret = new PlanNode(r, c, dir, time - 1, action);
        } else {
            ret = new PlanNode(row, col, Utility.prevDir(dir, action), time - 1, action);
        }

        ret.action = par[ret.row][ret.col][ret.dir.ordinal()];

        return ret;
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
        long lhs = time + heuristic();
        long rhs = obj.time + obj.heuristic();

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
        builder.append(" pos: ").append("(").append(row).append(", ").append(col).append(")").append(", ");
        builder.append(" dir: ").append(dir).append(", ");
        builder.append(" time: ").append(time).append(", ");
        builder.append(" action: ").append(action);
        builder.append(" }");

        return builder.toString();
    }
}
