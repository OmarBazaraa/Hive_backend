package models.agents;

import algorithms.planner.Planner;

import models.facilities.Facility;
import models.facilities.Rack;
import models.maps.GridCell;
import models.maps.utils.Pose;
import models.maps.utils.Position;
import models.tasks.Task;
import models.warehouses.Warehouse;

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

        // Inform listener
        if (listener != null) {
            listener.onActivate(this);
        }

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

        // Inform listener
        if (listener != null) {
            listener.onDeactivate(this);
        }

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

        // Inform listener
        if (listener != null) {
            listener.onBlock(this);
        }

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

        // Rotation actions
        if (action == AgentAction.ROTATE_RIGHT || action == AgentAction.ROTATE_LEFT) {
            dir = Utility.prevDir(dir, action);
            return;
        }

        // Retreat action
        if (action == AgentAction.RETREAT) {
            dir = Utility.getReverseDir(dir);
            return;
        }

        // Move action
        if (action == AgentAction.MOVE) {
            // Get the current and the previous cells
            Position prv = getPosition().prev(dir);
            GridCell curCell = warehouse.get(row, col);
            GridCell prvCell = warehouse.get(prv);

            // Undo movement
            curCell.setAgent(null);

            //
            // Recursive block affected agents
            //
            Agent a = prvCell.getAgent();

            if (a != null) {
                a.block();
            }

            // Consider the agent in its previous position
            prvCell.setAgent(this);
            setPosition(prv.row, prv.col);
            return;
        }
    }

    /**
     * Recovers from the blockage and returns back to a normal state if possible.
     *
     * @return {@code true} if recovered successfully; {@code false} otherwise.
     */
    @Override
    public boolean recover() {
        // Cannot recover if the agent is deactivate
        // Wait until it is activated again
        if (deactivated) {
            return false;
        }

        //
        // Handle different last actions
        //

        // No last action
        if (lastAction == AgentAction.NOTHING) {
            blocked = false;
            return true;
        }

        // Rotation action
        if (lastAction == AgentAction.ROTATE_RIGHT || lastAction == AgentAction.ROTATE_LEFT) {
            blocked = false;
            dir = Utility.nextDir(dir, lastAction);
            setLastRecoverAction(lastAction);
            return true;
        }

        // Retreat action
        if (lastAction == AgentAction.RETREAT) {
            blocked = false;
            dir = Utility.getReverseDir(dir);
            setLastRecoverAction(lastAction);
            return true;
        }

        // Move action
        if (lastAction == AgentAction.MOVE) {
            // Get the current and the next cells
            Warehouse warehouse = Warehouse.getInstance();
            Position nxt = Utility.nextPos(row, col, dir);
            GridCell curCell = warehouse.get(row, col);
            GridCell nxtCell = warehouse.get(nxt);

            // Check if there is an agent in the cell that this agent was suppose to go
            Agent a = nxtCell.getAgent();

            // Continue the last move if the next cell is empty
            if (a == null) {
                blocked = false;
                curCell.setAgent(null);
                nxtCell.setAgent(this);
                setPosition(nxt);
                setLastRecoverAction(lastAction);
                return true;
            }

            // Retreat if the current cell is not locked by a deactivated agent
            if (!curCell.isLocked()) {
                blocked = false;
                dir = Utility.getReverseDir(dir);
                setLastRecoverAction(lastAction);
                return true;
            }

            // Cannot recover
            return false;
        }

        // Handle other actions that does not change the pose of the agent
        // Just try redoing the last action
        blocked = false;
        setLastRecoverAction(lastAction);
        return true;
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
        if (plan != null) {
            Planner.dropPlan(this, plan);
        }

        plan = null;
        target = null;
    }

    /**
     * Executes the next required action as specified by the currently
     * active assigned {@code Task}.
     *
     * @return {@code true} if this {@code Agent} manged to execute the action successfully; {@code false} otherwise.
     */
    @Override
    public boolean executeAction() {
        // Return if already did an action this time step
        if (lastActionTime >= Warehouse.getInstance().getTime()) {
            return false;
        }

        // Execute action depending on the currently active task
        Task task = getActiveTask();

        if (task == null) {
            return false;
        }

        return task.executeAction();
    }

    /**
     * Moves a single step to reach the given {@code Facility}.
     *
     * @param dst the target to reach.
     *
     * @return {@code true} if this {@code Agent} manged to move a step towards the target; {@code false} otherwise.
     */
    @Override
    public boolean reach(Facility dst) {
        // Plan what action to apply next
        plan(dst);

        if (plan == null || plan.isEmpty()) {
            return false;
        }

        AgentAction action = plan.pop();

        // Get the current and the next cells
        Warehouse warehouse = Warehouse.getInstance();
        Pose nxt = getPose().next(action);
        GridCell curCell = warehouse.get(row, col);
        GridCell nxtCell = warehouse.get(nxt.row, nxt.col);
        Agent blockingAgent = nxtCell.getAgent();
        long time = warehouse.getTime();

        // Check next cell
        if (nxtCell.isLocked() || blockingAgent != null && blockingAgent != this) {
            dropPlan();
            return false;
        }

        // Apply action and set the new pose of the agent
        curCell.clearScheduleAt(time);
        curCell.setAgent(null);
        nxtCell.setAgent(this);
        setPose(nxt);
        setLastAction(action);
        return true;
    }

    /**
     * Loads and lifts the given {@code Rack} above this {@code Agent}.
     *
     * @param rack the {@code Rack} to load.
     */
    @Override
    public void loadRack(Rack rack) {
        loaded = true;
        setLastAction(AgentAction.LOAD);
    }

    /**
     * Offloads and releases the {@code Rack} currently loaded by this {@code Agent}.
     *
     * @param rack the {@code Rack} to offload.
     */
    @Override
    public void offloadRack(Rack rack) {
        loaded = false;
        setLastAction(AgentAction.OFFLOAD);
    }

    /**
     * Locks this {@code Agent} for the favor of the given {@code Facility}.
     *
     * @param facility the locking {@code Facility}.
     */
    @Override
    public void lock(Facility facility) {
        locked = true;
        setLastAction(AgentAction.BIND);
    }

    /**
     * Unlocks the lock that the given {@code Facility} has locked this {@code Agent} by.
     *
     * @param facility the unlocking {@code Facility}.
     */
    @Override
    public void unlock(Facility facility) {
        locked = false;
        setLastAction(AgentAction.UNBIND);
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
     * Sets the last action this {@code Agent} has performed.
     *
     * @param action the action done by this {@code Agent}.
     */
    public void setLastAction(AgentAction action) {
        lastAction = action;
        lastActionTime = Warehouse.getInstance().getTime();

        // Inform listener
        if (listener != null) {
            listener.onAction(this, action);
        }
    }

    /**
     * Sets the last recover action this {@code Agent} has performed.
     *
     * @param action the action done by this {@code Agent}.
     */
    public void setLastRecoverAction(AgentAction action) {
        lastAction = action;
        lastActionTime = Warehouse.getInstance().getTime();

        // Inform listener
        if (listener != null) {
            listener.onRecover(this, action);
        }
    }
}
