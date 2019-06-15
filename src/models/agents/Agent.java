package models.agents;

import algorithms.planner.Planner;

import models.facilities.Facility;
import models.facilities.Rack;
import models.maps.GuideGrid;
import models.maps.MapCell;
import models.maps.MapGrid;
import models.maps.utils.Position;
import models.tasks.Task;
import models.warehouses.Warehouse;

import server.Server;

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
     * @param id           the id of the {@code Agent}.
     * @param loadCapacity the maximum weight the {@code Agent} can load.
     * @param direction    the current direction this {@code Agent} is heading to.
     */
    public Agent(int id, int loadCapacity, Direction direction) {
        super(id, loadCapacity, direction);
    }

    /**
     * Returns the guide map to reach the target of the currently active {@code Task}.
     *
     * @return a {@code GuideGrid} to reach the target.
     */
    public GuideGrid getGuideMap() {
        Task task = getActiveTask();
        return (task != null ? task.getGuideMap() : null);
    }

    /**
     * Plans the sequence of actions to reach the given target {@code Facility}.
     * <p>
     * This function should be called with new destination only when the previous
     * plan has been reached.
     *
     * @param dst the target to reach.
     */
    public void plan(Facility dst) {
        // Return if already planned
        if (target != null && target.equals(dst)) {
            return;
        }

        // Set the destination and plan the path
        target = dst;
        plan = Planner.plan(this, dst.getPosition());
    }

    /**
     * Drops and cancels the current plan of this {@code Agent}.
     */
    public void dropPlan() {
        target = null;
        Planner.dropPlan(this, plan);
    }

    /**
     * Assigns a new {@code Task} to this {@code Agent}.
     * <p>
     * TODO: skip returning the rack back to its place when multiple tasks are assigned with the same rack
     *
     * @param task the new {@code Task} to assign.
     */
    @Override
    public void assignTask(Task task) {
        if (tasks.isEmpty()) {
            task.getRack().allocate(this);
        }

        tasks.add(task);
    }

    /**
     * The callback function to be invoked when the currently active {@code Task} is completed.
     *
     * @param task the completed {@code Task}.
     */
    @Override
    public void onTaskComplete(Task task) {
        tasks.remove();

        if (tasks.isEmpty()) {
            task.getRack().deallocate();
        }
    }

    /**
     * Executes the next required action as specified by the currently
     * active assigned {@code Task}.
     */
    @Override
    public void executeAction() {
        // Return if already did an action this time step
        if (isAlreadyMoved()) {
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
    public void move(AgentAction action) {
        if (action == AgentAction.MOVE) {
            // Setting the new position is done by the routing function
        } else {
            direction = Utility.nextDir(direction, action);
        }

        updateLastActionTime();
        Server.getInstance().enqueueAgentAction(this, action);
    }

    /**
     * Moves this {@code Agent} in the given {@code Direction}.
     *
     * @param dir the {@code Direction} to move along.
     */
    @Override
    public void move(Direction dir) {
        // Get warehouse map
        MapGrid map = Warehouse.getInstance().getMap();

        // Get current position
        Position cur = getPosition();
        MapCell curCell = map.get(cur);

        // Get next position
        Position nxt = map.next(cur, dir);
        MapCell nxtCell = map.get(nxt);

        // Move agent
        curCell.setAgent(null);
        nxtCell.setAgent(this);
        setPosition(nxt);

        // Update action time
        updateLastActionTime();
    }

    /**
     * Loads and lifts the given {@code Rack} above this {@code Agent}.
     *
     * @param rack the {@code Rack} to load.
     */
    @Override
    public void loadRack(Rack rack) {
        loaded = true;
        updateLastActionTime();
        Server.getInstance().enqueueAgentAction(this, AgentAction.LOAD);
    }

    /**
     * Offloads and releases the {@code Rack} currently loaded by this {@code Agent}.
     *
     * @param rack the {@code Rack} to offload.
     */
    @Override
    public void offloadRack(Rack rack) {
        loaded = false;
        updateLastActionTime();
        Server.getInstance().enqueueAgentAction(this, AgentAction.OFFLOAD);
    }
}
