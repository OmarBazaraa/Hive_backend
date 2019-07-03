package models.agents;

import algorithms.planner.Planner;

import models.facilities.Facility;
import models.facilities.Rack;
import models.maps.GridCell;
import models.maps.Pose;
import models.maps.Position;
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
     * That is, a sequence of directions to move along to reach the target.
     */
    private Stack<Integer> plan;

    /**
     * The last action done by this {@code Agent} that we are still waiting
     * for its acknowledgement.
     */
    private AgentAction lastAction = AgentAction.NOTHING;

    /**
     * The last time this {@code Agent} has performed an action.
     */
    private long lastActionTime = -1;

    /**
     * The last this {@code Agent} has attempt to slide.
     */
    private long slidingTime = -1;

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
        Pose nxt = getPose().next(lastAction);
        GridCell cell = sWarehouse.get(nxt.row, nxt.col);
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
        GridCell cell = sWarehouse.get(row, col);
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
        sWarehouse.onAgentBlocked(this);

        // Mark the agent as blocked and drop any plans
        blocked = true;
        dropPlan();

        //
        // Handle different last actions
        //

        // Get last action
        AgentAction action = getLastAction();

        // Rotation right action
        if (action == AgentAction.ROTATE_RIGHT) {
            dir = Utility.rotateLeft(dir);
            return;
        }

        // Rotation left action
        if (action == AgentAction.ROTATE_LEFT) {
            dir = Utility.rotateRight(dir);
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
            GridCell curCell = sWarehouse.get(row, col);
            GridCell prvCell = sWarehouse.get(prv);

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

        // If no action was interrupted during the last blockage recovering is easy
        if (lastAction == AgentAction.NOTHING) {
            blocked = false;
            return true;
        }

        // Apply and get the recover action
        AgentAction recoverAction = recover(lastAction);

        // If cannot recover just return false
        if (recoverAction == AgentAction.NOTHING) {
            return false;
        }

        // Recovered successfully, inform the listeners
        blocked = false;

        if (recoverAction != lastAction) {
            setLastAction(recoverAction);
        } else {
            setLastRecoverAction(recoverAction);
        }

        return true;
    }

    /**
     * Recovers from the blockage and returns back to a normal state if possible.
     *
     * @param action the last action to recover from.
     *
     * @return the applied recover action; {@code AgentAction.NOTHING} if cannot recover.
     */
    private AgentAction recover(AgentAction action) {

        // Rotation right action
        if (action == AgentAction.ROTATE_RIGHT) {
            dir = Utility.rotateRight(dir);
            return AgentAction.ROTATE_RIGHT;
        }

        // Rotation left action
        if (action == AgentAction.ROTATE_LEFT) {
            dir = Utility.rotateLeft(dir);
            return AgentAction.ROTATE_LEFT;
        }

        // Retreat action
        if (action == AgentAction.RETREAT) {
            dir = Utility.getReverseDir(dir);
            return AgentAction.RETREAT;
        }

        // Move action
        if (action == AgentAction.MOVE) {
            // Get the current and the next cells
            Position nxt = Utility.nextPos(row, col, dir);
            GridCell curCell = sWarehouse.get(row, col);
            GridCell nxtCell = sWarehouse.get(nxt);

            // Check if there is an agent in the cell that this agent was suppose to go
            Agent a = nxtCell.getAgent();

            // Continue the last move if the next cell is empty
            if (a == null) {
                curCell.setAgent(null);
                nxtCell.setAgent(this);
                setPosition(nxt);
                return AgentAction.MOVE;
            }

            // If current cell is locked by a deactivated agent the we cannot recover
            if (curCell.isLocked()) {
                return AgentAction.NOTHING;
            }
            // Otherwise, we can retreat back
            else {
                dir = Utility.getReverseDir(dir);
                return AgentAction.RETREAT;
            }
        }

        // Handle other actions that does not change the pose of the agent
        // Just try redoing the last action
        return action;
    }

    // ===============================================================================================
    //
    // Action-Related Methods
    //

    /**
     * Executes the next required action as specified by the currently
     * active assigned {@code Task}.
     *
     * @return {@code true} if this {@code Agent} manged to execute the action successfully; {@code false} otherwise.
     */
    @Override
    public boolean executeAction() {
        // Return if already did an action this time step
        if (isAlreadyMoved()) {
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
     * Plans the sequence of actions to reach the given target {@code Facility}.
     * <p>
     * This function should be called with new destination only when the previous
     * plan has been reached.
     *
     * @param dst the target {@code Facility} to reach.
     */
    @Override
    protected void plan(Facility dst) {
        // Return if already planned
        if (plan != null && target != null && target.equals(dst)) {
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
    protected void dropPlan() {
        plan = null;
        target = null;
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

        // Return if no plan
        if (!hasPlan()) {
            return false;
        }

        // Get next action to apply
        int d = plan.peek();

        //
        // Handle rotation actions
        //
        if (d != dir) {
            rotate(d);
            return true;
        }

        //
        // Handle move action
        //

        // Get the current and the next cells
        int r = row + Constants.DIR_ROW[d];
        int c = col + Constants.DIR_COL[d];
        GridCell cell = sWarehouse.get(r, c);
        Agent blockingAgent = cell.getAgent();

        // Check if next cell is currently blocked by an agent
        if (cell.isLocked() || (blockingAgent != null && !blockingAgent.slide(this))) {
            dropPlan();
            return false;
        }

        // Check if the next location is empty
        if (cell.hasAgent()) {
            return false;
        }

        // Apply action and set the new pose of the agent
        move(r, c);
        return true;
    }

    /**
     * Attempts to slide away from the current position of this {@code Agent} in order
     * to bring a blank cell to the given main {@code Agent}.
     *
     * @param mainAgent the main {@code Agent} issuing the slide.
     *
     * @return {@code true} if sliding is possible; {@code false} otherwise.
     */
    protected boolean slide(Agent mainAgent) {
        if (locked || blocked || slidingTime >= sWarehouse.getTime()) {
            return false;
        }

        if (isAlreadyMoved() || compareTo(mainAgent) > 0) {
            return true;
        }

        slidingTime = sWarehouse.getTime();

        int D = (hasPlan() ? plan.peek() : dir);
        int[] dirs = {D, Utility.rotateRight(D), Utility.rotateLeft(D), Utility.getReverseDir(D)};

        for (int d : dirs) {
            int r = row + Constants.DIR_ROW[d];
            int c = col + Constants.DIR_COL[d];

            if (sWarehouse.isOutBound(r, c)) {
                continue;
            }

            GridCell cell = sWarehouse.get(r, c);
            Agent blockingAgent = cell.getAgent();

            if (cell.isBlocked() || cell.hasFacility()) {
                continue;
            }

            if (d != dir) {
                rotate(d);
                return true;
            }

            if (blockingAgent == null) {
                move(r, c);
                return true;
            }

            if (blockingAgent.slide(mainAgent)) {
                if (cell.hasAgent()) {
                    return true;
                } else {
                    move(r, c);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Rotates this {@code Agent} to reach the given orientation.
     *
     * @param d the needed orientation.
     */
    @Override
    protected void rotate(int d) {
        if (d == Utility.rotateRight(dir)) {
            dir = d;
            setLastAction(AgentAction.ROTATE_RIGHT);
        } else {
            dir = Utility.rotateLeft(dir);
            setLastAction(AgentAction.ROTATE_LEFT);
        }
    }

    /**
     * Moves this {@code Agent} along its current direction.
     *
     * @param r the row position to move into.
     * @param c the column position to move into.
     */
    protected void move(int r, int c) {
        if (hasPlan()) {
            if (dir == plan.peek()) {
                plan.pop();
            } else {
                dropPlan();
            }
        }

        sWarehouse.get(row, col).setAgent(null);
        row = r;
        col = c;
        sWarehouse.get(row, col).setAgent(this);
        setLastAction(AgentAction.MOVE);
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
     * Checks whether this {@code Agent} currently has a plan or not.
     *
     * @return {@code true} if it currently has a plan. {@code false} otherwise.
     */
    private boolean hasPlan() {
        return (plan != null && plan.size() > 0);
    }

    /**
     * Checks whether this {@code Agent} has already performed an action this time step
     * or not.
     *
     * @return {@code true} if it already performed an action. {@code false} otherwise.
     */
    private boolean isAlreadyMoved() {
        return lastActionTime >= sWarehouse.getTime();
    }

    /**
     * Returns the action that this {@code Agent} has performed the last time step.
     *
     * @return the last {@code AgentAction} performed.
     */
    private AgentAction getLastAction() {
        if (lastActionTime + 1 < sWarehouse.getTime()) {
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
    private void setLastAction(AgentAction action) {
        lastAction = action;
        lastActionTime = sWarehouse.getTime();

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
    private void setLastRecoverAction(AgentAction action) {
        lastAction = action;
        lastActionTime = sWarehouse.getTime();

        // Inform listener
        if (listener != null) {
            listener.onRecover(this, action);
        }
    }
}
