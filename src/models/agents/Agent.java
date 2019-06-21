package models.agents;

import algorithms.planner.Planner;

import communicators.frontend.FrontendCommunicator;

import models.facilities.Facility;
import models.facilities.Rack;
import models.maps.GridCell;
import models.maps.utils.Pose;
import models.maps.utils.Position;
import models.tasks.Task;
import models.warehouses.Warehouse;

import utils.Constants;
import utils.Constants.*;
import utils.Utility;

import java.util.Stack;


/**
 * This {@code Agent} class is the model class for robot agents in our Hive System.
 * <p>
 * An {@code Agent} is responsible for carrying out {@link Task Tasks} inside a {@link Warehouse}.
 *
 * @see models.HiveObject HiveObject
 * @see models.agents.AbstractAgent
 * @see models.facilities.Facility Facility
 * @see models.facilities.Rack Rack
 * @see models.facilities.Gate Gate
 * @see models.facilities.Station Station
 * @see models.tasks.Task Task
 */
public class Agent extends AbstractAgent {

    //
    // Member Variables
    //

    /**
     * The current destination of this {@code Agent}; or {@code null} if not available.
     * This variable is to be managed by the currently active assigned {@code Task}.
     */
    private Facility target;

    /**
     * The plan of this {@code Agent} to reach its destination.
     * That is, a sequence of actions to be done to reach the target.
     */
    private Stack<AgentAction> plan;

    /**
     * The last action done by this {@code Agent} that we are still waiting
     * for its acknowledgement.
     */
    private AgentAction lastAction = AgentAction.NOTHING;

    /**
     * The last time this {@code Agent} has performed an action.
     */
    private long lastActionTime = -1;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Agent} robot.
     */
    public Agent() {
        super();
    }

    /**
     * Constructs a new {@code Agent} robot.
     *
     * @param id      the id of the {@code Agent}.
     * @param loadCap the maximum weight the {@code Agent} can load.
     */
    public Agent(int id, int loadCap) {
        super(id, loadCap);
    }

    // ===============================================================================================
    //
    // Control-Related Methods
    //

    /**
     * Activates this {@code Agent}.
     */
    public void activate() {
        // Return if already activated
        if (!deactivated) {
            return;
        }

        // Inform the frontend
        FrontendCommunicator.getInstance().enqueueActivatedAgent(this);

        // Mark as activated
        deactivated = false;

        // Unlock the cell that the agent was supposed to be in
        Warehouse warehouse = Warehouse.getInstance();
        Pose nxt = getPose().next(lastAction);
        GridCell cell = warehouse.get(nxt.row, nxt.col);
        cell.setLock(false);
    }

    /**
     * Deactivates this {@code Agent}.
     */
    public void deactivate() {
        // Return if already deactivated
        if (deactivated) {
            return;
        }

        // Inform the frontend
        FrontendCommunicator.getInstance().enqueueDeactivatedAgent(this);

        // Mark as deactivated
        deactivated = true;

        // Lock the cell that the agent was supposed to be in
        Warehouse warehouse = Warehouse.getInstance();
        GridCell cell = warehouse.get(row, col);
        cell.setLock(true);

        // Recursive block affected agents
        block();
    }

    /**
     * Sudden blocks this {@code Agent} and all the affected agents
     * from completing their last actions.
     */
    @Override
    public void block() {
        // Return if already block to avoid infinite recursion
        if (blocked) {
            return;
        }

        // Inform the frontend
        FrontendCommunicator.getInstance().enqueueBlockedAgent(this);

        // Inform the warehouse
        Warehouse warehouse = Warehouse.getInstance();
        warehouse.onAgentBlocked(this);

        // Mark the agent as blocked and drop any plans
        blocked = true;
        dropPlan();

        //
        // Handle different last actions
        //

        // Get last action
        AgentAction action = getLastAction();

        // Actions that does not change the pose of the agent
        if (action == AgentAction.NOTHING || action == AgentAction.LOAD || action == AgentAction.OFFLOAD) {
            return;
        }

        // Move action
        if (action == AgentAction.MOVE) {
            // Get the cell that the agent supposed to go
            GridCell cur = warehouse.get(row, col);

            // Undo movement
            cur.setAgent(null);
            int i = direction.ordinal();
            row -= Constants.DIR_ROW[i];
            col -= Constants.DIR_COL[i];

            // Get the previous cell that the agent was in
            GridCell prv = warehouse.get(row, col);

            // Check if there are other agents that was going to the previous position of this agent
            Agent a = prv.getAgent();

            // Recursive block affected agents
            if (a != null) {
                a.block();
            }

            // Consider the agent in its previous position
            prv.setAgent(this);
        }
        // Rotation actions
        else {
            // Undo rotation
            direction = Utility.prevDir(direction, action);
        }
    }

    // ===============================================================================================
    //
    // Action-Related Methods
    //

    /**
     * Plans the sequence of actions to reach the given target {@code Facility}.
     * <p>
     * This function should be called with new destination only when the previous
     * plan has been reached.
     *
     * @param dst the target {@code Facility} to reach.
     */
    @Override
    public void plan(Facility dst) {
        // Return if already planned
        if (target != null && target.equals(dst)) {
            return;
        }

        // Set the destination and plan the path
        target = dst;
        plan = Planner.plan(this, dst);
    }

    /**
     * Drops and cancels the current plan of this {@code Agent}.
     */
    @Override
    public void dropPlan() {
        target = null;
        Planner.dropPlan(this, plan);
    }

    /**
     * Executes the next required action as specified by the currently
     * active assigned {@code Task}.
     */
    @Override
    public void executeAction() {
        // Return if already did an action this time step
        if (lastActionTime >= Warehouse.getInstance().getTime()) {
            return;
        }

        // Execute action depending on the currently active task
        Task task = getActiveTask();

        if (task != null) {
            task.executeAction();
        }
    }

    /**
     * Moves a single step to reach the given {@code Facility}.
     *
     * @param dst the target to reach.
     */
    @Override
    public void reach(Facility dst) {
        plan(dst);
        Planner.route(this, plan.pop());
    }

    /**
     * Moves this {@code Agent} according to the given action.
     * <p>
     * The allowed actions are only:
     * {@code AgentAction.ROTATE_RIGHT}, {@code AgentAction.ROTATE_LEFT}, and
     * {@code AgentAction.MOVE}.
     *
     * @param action the {@code AgentAction} to move with.
     */
    @Override
    public void move(AgentAction action) {
        if (action == AgentAction.MOVE) {
            // Setting the new position is done by the routing function
        } else {
            direction = Utility.nextDir(direction, action);
        }

        updateLastAction(action);
    }

    /**
     * Retreats from the last action done and returns back to a normal state.
     *
     * @return {@code true} if retreated successfully; {@code false} otherwise.
     */
    public boolean retreat() {
        // Cannot retreat if the agent is deactivate
        // Wait until it is activated again
        if (deactivated) {
            return false;
        }

        //
        // Handle different last actions
        //

        // Actions that does not change the pose of the agent
        if (lastAction == AgentAction.NOTHING || lastAction == AgentAction.LOAD || lastAction == AgentAction.OFFLOAD) {
            blocked = true;
            updateLastAction(lastAction);
            return true;
        }

        // Retreat action
        if (lastAction == AgentAction.RETREAT) {
            direction = Utility.getReverseDir(direction);
            blocked = true;
            updateLastAction(lastAction);
            return true;
        }

        // Rotation action
        if (lastAction == AgentAction.ROTATE_RIGHT || lastAction == AgentAction.ROTATE_LEFT) {
            direction = Utility.nextDir(direction, lastAction);
            blocked = true;
            updateLastAction(lastAction);
            return true;
        }

        // Move action
        if (lastAction == AgentAction.MOVE) {
            // Get the current and the next cells
            Warehouse warehouse = Warehouse.getInstance();
            Position nxt = Utility.nextPos(row, col, direction);
            GridCell curCell = warehouse.get(row, col);
            GridCell nxtCell = warehouse.get(nxt);

            // Check if there are other agents that was going to the previous position of this agent
            Agent a = nxtCell.getAgent();

            // Recursive block affected agents
            if (a != null) {
                a.retreat();
            }

            if (!nxtCell.hasAgent()) {
                curCell.setAgent(null);
                nxtCell.setAgent(this);
                setPosition(nxt);
            }

            if (!curCell.isBlocked()) {
                direction = Utility.getReverseDir(direction);
                blocked = true;
                updateLastAction(lastAction);
                return true;
            }
        }

        return false;
    }

    /**
     * Loads and lifts the given {@code Rack} above this {@code Agent}.
     *
     * @param rack the {@code Rack} to load.
     */
    @Override
    public void loadRack(Rack rack) {
        loaded = true;
        updateLastAction(AgentAction.LOAD);
    }

    /**
     * Offloads and releases the {@code Rack} currently loaded by this {@code Agent}.
     *
     * @param rack the {@code Rack} to offload.
     */
    @Override
    public void offloadRack(Rack rack) {
        loaded = false;
        updateLastAction(AgentAction.OFFLOAD);
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Returns the action that this {@code Agent} has performed the last time step.
     *
     * @return the last {@code AgentAction} performed.
     */
    public AgentAction getLastAction() {
        if (lastActionTime + 1 < Warehouse.getInstance().getTime()) {
            return AgentAction.NOTHING;
        } else {
            return lastAction;
        }
    }

    /**
     * Updates the last action this {@code Agent} has performed.
     */
    public void updateLastAction(AgentAction action) {
        lastAction = action;
        lastActionTime = Warehouse.getInstance().getTime();
        Server.getInstance().enqueueAgentAction(this, action);
    }
}
