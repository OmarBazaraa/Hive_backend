package algorithms;

import models.maps.utils.Position;

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
     * The target position in the search tree.
     */
    private static Position target;

    /**
     * Initializes the planning state.
     */
    public static void initializes(int rows, int cols, int dirs, Position dst) {
        par = new AgentAction[rows][cols][dirs];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                for (int k = 0; k < dirs; ++k) {
                    par[i][j][k] = AgentAction.NOTHING;
                }
            }
        }

        target = dst;
    }

    // ===============================================================================================
    //
    // Member Variables
    //

    /**
     * The {@code Position} of the {@code Agent} in the grid.
     */
    public Position pos;

    /**
     * The {@code Direction} of the {@code Agent} in the cell.
     */
    public Direction dir;

    /**
     * The action leading to this state.
     */
    public AgentAction action;

    /**
     * The time at which the {@code Agent} will be in this state.
     */
    public long time;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code PlanNode} object.
     *
     * @param pos    the {@code Position} of the {@code Agent} in the grid.
     * @param dir    the {@code Direction} of the {@code Agent} in the cell.
     * @param action the action leading to this state.
     * @param time   the time at which the {@code Agent} will be in this state.
     */
    public PlanNode(Position pos, Direction dir, AgentAction action, long time) {
        this.pos = pos;
        this.dir = dir;
        this.action = action;
        this.time = time;
    }

    /**
     * Checks whether this state node has been visited before or not.
     *
     * @return {@code true} if already visited; {@code false} otherwise.
     */
    public boolean isVisited() {
        return (par[pos.row][pos.col][dir.ordinal()] != AgentAction.NOTHING);
    }

    /**
     * Marks this state node as visited.
     */
    public void visit() {
        par[pos.row][pos.col][dir.ordinal()] = action;
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
            return new PlanNode(Utility.nextPos(pos, dir), dir, action, time + 1);
        } else {
            return new PlanNode(pos, Utility.nextDir(dir, action), action, time + 1);
        }
    }

    /**
     * Calculates the previous state if applying the given action in reverse manner.
     * <p>
     * The allowed actions are only:
     * {@code AgentAction.ROTATE_RIGHT}, {@code AgentAction.ROTATE_LEFT}, and
     * {@code AgentAction.MOVE}.
     *
     * @param action the action to apply in reverse.
     *
     * @return the previous state {@code PlanNode}.
     */
    public PlanNode previous(AgentAction action) {
        if (action == AgentAction.MOVE) {
            return new PlanNode(Utility.prevPos(pos, dir), dir, action, time - 1);
        } else {
            return new PlanNode(pos, Utility.prevDir(dir, action), action, time - 1);
        }
    }

    /**
     * Calculates the heuristic score to reach the target state.
     *
     * @return the heuristic score.
     */
    public int heuristic() {
        // Return the manhattan distance to the target
        return pos.distanceTo(target);
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
}
