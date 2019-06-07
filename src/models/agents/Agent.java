package models.agents;

import models.Entity;
import models.HiveObject;
import models.facilities.Rack;
import models.maps.GuideGrid;
import models.maps.MapCell;
import models.maps.MapGrid;
import models.maps.utils.Position;
import models.tasks.Task;
import models.tasks.TaskAssignable;
import models.warehouses.Warehouse;

import server.Server;

import utils.Constants;
import utils.Constants.*;
import utils.Utility;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;


/**
 * This {@code Agent} class is the model class for robot agents in our Hive System.
 * <p>
 * An {@code Agent} is responsible for carrying out {@link Task Tasks} inside a {@link Warehouse}.
 *
 * @see models.HiveObject HiveObject
 * @see models.facilities.Facility Facility
 * @see models.facilities.Rack Rack
 * @see models.facilities.Gate Gate
 * @see models.facilities.Station Station
 * @see models.tasks.Task Task
 */
public class Agent extends HiveObject implements TaskAssignable {

    //
    // Member Variables
    //

    // Skip for now
    private int capacity;
    private int chargeMaxCap;
    private int chargeLevel;

    /**
     * The queue of assigned tasks of this {@code Agent}.
     */
    private Queue<Task> tasks = new LinkedList<>();

    /**
     * The flag indicating whether this {@code Agent} is currently loaded by a {@code Rack}.
     */
    private boolean loaded = false;

    /**
     * The last time this {@code Agent} has performed an action.
     * Needed by the planner algorithm.
     */
    private long lastActionTime = -1;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * Creates a new {@code Agent} object from JSON data.
     *
     * @param data the un-parsed JSON data.
     * @param row  the row position of the {@code MapCell} to create.
     * @param col  the column position of the {@code MapCell} to create.
     *
     * @return an {@code Agent} object.
     */
    public static Agent create(JSONObject data, int row, int col) throws Exception {
        int id = data.getInt(Constants.MSG_KEY_ID);
        Agent ret = new Agent(id);
        ret.setPosition(row, col);
        return ret;
    }

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
     * @param id  the id of the {@code Agent}.
     */
    public Agent(int id) {
        super(id);
    }

    /**
     * Checks whether this {@code Agent} is currently active or not.
     * <p>
     * An {@code Agent} is said to be active if it is currently performing a {@code Task}.
     *
     * @return {@code true} if this {@code Agent} is active; {@code false} otherwise.
     */
    public boolean isActive() {
        return (tasks.size() > 0);
    }

    /**
     * Checks whether this {@code Agent} is currently loaded by a {@code Rack} or not.
     *
     * @return {@code true} if this {@code Agent} is loaded; {@code false} otherwise.
     */
    public boolean isLoaded() {
        return (loaded);
    }

    /**
     * Returns the active {@code Task} this {@code Agent} is currently executing.
     *
     * @return the currently active {@code Task} is exists; {@code null} otherwise.
     */
    public Task getActiveTask() {
        return tasks.peek();
    }

    /**
     * Assigns a new {@code Task} to this {@code Agent}.
     *
     * TODO: skip returning the rack back to its place when multiple tasks are assigned with the same rack
     *
     * @param task the new {@code Task} to assign.
     */
    @Override
    public void assignTask(Task task) throws Exception {
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
     * Returns the priority of this {@code Agent}.
     * Higher value indicates higher priority.
     *
     * @return the priority of this {@code Agent}.
     */
    public int getPriority() {
        Task task = getActiveTask();
        return (task != null ? task.getPriority() : Short.MIN_VALUE);
    }

    /**
     * Returns the estimated number of steps to finish the currently active {@code Task}.
     *
     * @return the estimated number of steps.
     */
    public int getEstimatedSteps() {
        Task task = getActiveTask();
        return (task != null ? task.getEstimatedDistance() : 0);
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
     * Executes the next required action.
     */
    public void executeAction() throws Exception {
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
     * Moves this {@code Agent} in the given {@code Direction}.
     *
     * @param dir the {@code Direction} to move along.
     */
    public void move(Direction dir) throws Exception {
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

        // Send move to the frontend
        Server.getInstance().sendAction(this, Utility.dirToAction(dir));
    }

    /**
     * Loads and lifts the given {@code Rack} above this {@code Agent}.
     *
     * @param rack the {@code Rack} to load.
     */
    public void loadRack(Rack rack) throws Exception {
        // Enable loaded flag
        loaded = true;

        // Update action time
        updateLastActionTime();
    }

    /**
     * Offloads and releases the {@code Rack} loaded by this {@code Agent}.
     *
     * @param rack the {@code Rack} to offload.
     */
    public void offloadRack(Rack rack) throws Exception {
        // Disable loaded flag
        loaded = false;

        // Update action time
        updateLastActionTime();
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Checks whether this {@code Agent} can execute an action this time step or not.
     *
     * @return {@code true} if it possible to execute an action; {@code false} otherwise.
     */
    public boolean canMove() {
        return lastActionTime < Warehouse.getInstance().getTime();
    }

    /**
     * Checks whether this {@code Agent} already done an action this time step or not.
     *
     * @return {@code true} if already done an action; {@code false} otherwise.
     */
    public boolean isAlreadyMoved() {
        return lastActionTime >= Warehouse.getInstance().getTime();
    }

    /**
     * Returns the last time this {@code Agent} has performed an action.
     */
    public long getLastActionTime() {
        return lastActionTime;
    }

    /**
     * Sets the last time this {@code Agent} has performed an action.
     */
    public void updateLastActionTime() {
        lastActionTime = Warehouse.getInstance().getTime();
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
    public int compareTo(Entity obj) {
        if (!(obj instanceof Agent)) {
            return id - obj.getId();
        }
        Agent rhs = (Agent) obj;
        int cmp = (getPriority() - rhs.getPriority());
        if (cmp == 0) {
            return id - rhs.id;
        }
        return cmp;
    }
}
