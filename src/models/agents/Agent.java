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

import java.util.LinkedList;
import java.util.List;
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

        // Mark the agent as blocked and drop any plans
        blocked = true;
        dropPlan();

        // Get last action
        lastAction = getLastAction();

        // Handle move and retreat actions
        if (lastAction == AgentAction.MOVE || lastAction == AgentAction.RETREAT) {
            // Get the previous cell
            int r = row - Constants.DIR_ROW[dir];
            int c = col - Constants.DIR_COL[dir];
            GridCell prvCell = sWarehouse.get(r, c);

            // Recursive block affected agents
            if (prvCell.hasAgent()) {
                prvCell.getAgent().block();
            }

            // Lock the previous cell
            prvCell.lock();
        }

        // Inform the warehouse
        sWarehouse.onAgentBlocked(this);
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
        // Handle move and retreat actions
        if (action == AgentAction.MOVE || action == AgentAction.RETREAT) {
            // Get the current and the previous cells
            int r = row - Constants.DIR_ROW[dir];
            int c = col - Constants.DIR_COL[dir];
            GridCell prvCell = sWarehouse.get(r, c);
            GridCell curCell = sWarehouse.get(row, col);

            // Continue the last move if the blockage has been cleared
            if (!curCell.isLocked()) {
                prvCell.unlock();
                return action;
            }

            // Otherwise, if the previous cell is unoccupied, try to retreat
            if (!prvCell.hasAgent() && action != AgentAction.RETREAT) {
                prvCell.unlock();
                prvCell.setAgent(this);
                curCell.setAgent(null);
                row = r;
                col = c;
                dir = Utility.getReverseDir(dir);
                return AgentAction.RETREAT;
            }

            // Cannot recover
            return AgentAction.NOTHING;
        }

        // Handle other actions that does not change the position of the agent
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

        // Rotate if the planned direction and the current directions are different
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
        if (cell.isLocked() || (blockingAgent != null && !blockingAgent.slide(this, d))) {
            dropPlan();

            // Tries another single trial with the new plan
            if (lastActionTime < sWarehouse.getTime()) {
                lastActionTime = sWarehouse.getTime();
                lastAction = AgentAction.NOTHING;
                reach(dst);
            }

            return false;
        }

        // Check if the next location is empty
        if (cell.hasAgent()) {
            return true; // TODO: need to be tested
        }

        // Apply action and set the new pose of the agent
        move(r, c);
        return true;
    }

    /**
     * Attempts to slide away from the current position of this {@code Agent} in order
     * to bring a blank cell to the given main {@code Agent}.
     *
     * @param mainAgent   the main {@code Agent} issuing the slide.
     * @param incomingDir the incoming direction of the parent agent issuing the slide.
     *
     * @return {@code true} if sliding is possible; {@code false} otherwise.
     */
    protected boolean slide(Agent mainAgent, int incomingDir) {
        // In the following cases sliding fails
        if (locked || blocked || slidingTime >= sWarehouse.getTime() || this == mainAgent) {
            return false;
        }

        // If the agent already performed an action this time step
        // or it is of higher priority than the main agent issuing the slide
        // then delay the sliding for the next time step
        if (isAlreadyMoved() || compareTo(mainAgent) > 0) {
            return true;
        }

        // Set the sliding time to avoid infinite recursion
        slidingTime = sWarehouse.getTime();

        // Get a list of candidate direction to slide
        List<Integer> dirs = getCandidateSlidingDirs(incomingDir);

        // Slide in the first direction that fit sliding
        for (int d : dirs) {
            int r = row + Constants.DIR_ROW[d];
            int c = col + Constants.DIR_COL[d];
            GridCell cell = sWarehouse.get(r, c);
            Agent blockingAgent = cell.getAgent();

            // If no agent blocking the slide movement then begin the slide
            if (blockingAgent == null) {
                if (d != dir) {
                    rotate(d);
                } else {
                    move(r, c);
                }
                return true;
            }

            // Otherwise, if there is another agent blocking the slide, try to slide it as well
            // before beginning to slide the current agent
            if (blockingAgent.slide(mainAgent, d)) {
                if (d != dir) {
                    rotate(d);
                } else if (!cell.hasAgent()) {
                    move(r, c);
                }
                return true;
            }
        }

        // Return false if cannot slide
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
     * Returns a list of candidate directions this {@code Agent} can slide into.
     * The returned list is sorted in a way to reduce the sliding cost as possible.
     *
     * @param incomingDir the incoming direction of the parent agent issuing the slide.
     *
     * @return a list of sorted direction.
     */
    private List<Integer> getCandidateSlidingDirs(int incomingDir) {
        // List 1 contains directions leading to empty cells
        // List 2 contains directions leading to occupied cells
        List<Integer> ret1 = new LinkedList<>();
        List<Integer> ret2 = new LinkedList<>();

        //
        // Construct an initial set of directions
        //
        int[] dirs;

        if (hasPlan()) {
            // If the sliding agent is currently active and has a plan
            // then favor its current planned direction
            int D = plan.peek();
            dirs = new int[]{D, (D + 1) & 3, (D - 1) & 3, (D + 2) & 3};
        } else {
            // Otherwise, favor perpendicular directions to the direction
            // of the parent agent issuing the slide command
            int D = incomingDir;
            dirs = new int[]{(D + 1) & 3, (D - 1) & 3, D, (D + 2) & 3};
        }

        //
        // Filter the initial set of directions
        //
        for (int d : dirs) {
            int r = row + Constants.DIR_ROW[d];
            int c = col + Constants.DIR_COL[d];

            // Skip directions leading outside the warehouse
            if (sWarehouse.isOutBound(r, c)) {
                continue;
            }

            GridCell cell = sWarehouse.get(r, c);

            // Skip directions leading to blocked cells
            if (cell.isBlocked()) {
                continue;
            }

            // If the cell contains an agent or a facility then add
            // the direction to list 2; otherwise, add it to list 1
            if (cell.hasAgent() || cell.hasFacility()) {
                ret2.add(d);
            } else {
                ret1.add(d);
            }
        }

        // Return an combined list of both list 1 and 2
        ret1.addAll(ret2);
        return ret1;
    }

    /**
     * Checks whether this {@code Agent} currently has a plan or not.
     *
     * @return {@code true} if it currently has a plan; {@code false} otherwise.
     */
    private boolean hasPlan() {
        return (plan != null && plan.size() > 0);
    }

    /**
     * Checks whether this {@code Agent} has already performed an action this time step
     * or not.
     *
     * @return {@code true} if it already performed an action; {@code false} otherwise.
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
