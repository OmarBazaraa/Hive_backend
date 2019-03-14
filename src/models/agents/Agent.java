package models.agents;

import models.Entity;
import models.HiveObject;
import models.facilities.Facility;
import models.facilities.Rack;
import models.maps.GuideGrid;
import models.maps.MapCell;
import models.maps.MapGrid;
import models.maps.utils.Position;
import models.tasks.Task;
import models.tasks.TaskAssignable;
import models.warehouses.Warehouse;

import utils.Constants;
import utils.Constants.*;

import org.json.JSONObject;


/**
 * This {@code Agent} class is the model class for robot agents in our Hive System.
 * <p>
 * An {@code Agent} is responsible for carrying out {@link Task Tasks} inside a {@link models.warehouses.Warehouse Warehouse}.
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
     * The current status of this {@code Agent}.
     */
    private AgentStatus status = AgentStatus.IDLE;

    /**
     * The assigned {@code Task} of this {@code Agent}.
     */
    private Task task;

    /**
     * The last time this {@code Agent} has performed an action.
     * Needed by the planner algorithm.
     */
    private long lastActionTime;

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
     * Returns the current status of this {@code Agent}.
     *
     * @return an {@code AgentStatus} of this {@code Agent}.
     */
    public AgentStatus getStatus() {
        return status;
    }

    /**
     * Checks whether this {@code Agent} is currently active or not.
     * <p>
     * An {@code Agent} is said to be active if it is currently performing a {@code Task}.
     *
     * @return {@code true} if this {@code Agent} is active; {@code false} otherwise.
     */
    public boolean isActive() {
        return (status == AgentStatus.ACTIVE || status == AgentStatus.ACTIVE_LOADED);
    }

    /**
     * Checks whether this {@code Agent} is currently loaded by a {@code Rack} or not.
     *
     * @return {@code true} if this {@code Agent} is loaded; {@code false} otherwise.
     */
    public boolean isLoaded() {
        return (status == AgentStatus.ACTIVE_LOADED);
    }

    /**
     * Assigns a new {@code Task} to this {@code Agent}.
     *
     * @param t the new {@code Task} to assign.
     */
    @Override
    public void assignTask(Task t) {
        task = t;
        status = AgentStatus.ACTIVE;
    }

    /**
     * The callback function to be invoked when the assigned {@code Task} is completed.
     *
     * @param t the completed {@code Task}.
     */
    @Override
    public void onTaskComplete(Task t) {
        if (task == t) {
            task = null;
            status = AgentStatus.IDLE;
        }
    }

    /**
     * Returns the priority of this {@code Agent}.
     * Higher value indicates higher priority.
     *
     * @return the priority of this {@code Agent}.
     */
    public int getPriority() {
        return (task != null ? task.getPriority() : Integer.MIN_VALUE);
    }

    /**
     * Returns the estimated number of steps to finish the currently assigned {@code Task}.
     *
     * @return the estimated number of steps.
     */
    public int getEstimatedSteps() {
        return (task != null ? task.getEstimatedDistance() : 0);
    }

    /**
     * Returns the guide map to reach the target of the assigned {@code Task}.
     *
     * @return a {@code GuideGrid} to reach the target.
     */
    public GuideGrid getGuideMap() {
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

        // Execute action depending on the assigned task
        if (task != null) {
            task.executeAction();
        }
    }

    /**
     * Binds this {@code Agent} with the given {@code Facility} available at the
     * same current position of this {@code Agent}.
     *
     * @param facility the {@code Facility} to bind with.
     *
     * @return {@code true} if managed to bind successfully; {@code false} otherwise.
     */
    public boolean bind(Facility facility) throws Exception {
        // Update action time
        updateLastActionTime();

        // Bind if possible
        if (facility.canBind(this)) {
            facility.bind(this);
            return true;
        }

        return false;
    }

    /**
     * Unbinds this {@code Agent} from the given {@code Facility} available at the
     * same current position of this {@code Agent}.
     *
     * @param facility the {@code Facility} to bind with.
     *
     * @return {@code true} if managed to unbind successfully; {@code false} otherwise.
     */
    public boolean unbind(Facility facility) throws Exception {
        // Update action time
        updateLastActionTime();

        // Unbind if possible
        if (facility.canUnbind(this)) {
            facility.unbind(this);
            return true;
        }

        return false;
    }

    /**
     * Moves this {@code Agent} in the given {@code Direction}.
     *
     * @param dir the {@code Direction} to move along.
     *
     * @return {@code true} if managed to move successfully; {@code false} otherwise.
     */
    public boolean move(Direction dir) throws Exception {
        // Get warehouse map
        MapGrid map = Warehouse.getInstance().getMap();

        // Get current position
        Position cur = getPosition();
        MapCell curCell = map.get(cur);

        // Get next position
        Position nxt = map.next(cur, dir);
        MapCell nxtCell = map.get(nxt);

        // Ensure no agents in the next position
        if (nxtCell.hasAgent()) {
            throw new Exception("Moving into a another robot!");
        }

        // Ensure no racks in the next position if this agent is currently loaded
        if (nxtCell.getType() == CellType.RACK && isLoaded()) {
            throw new Exception("Moving into a rack while the robot is currently loaded!");
        }

        // Move agent
        curCell.setAgent(null);
        nxtCell.setAgent(this);
        setPosition(nxt);
        return true;
    }

    /**
     * Loads the rack above this agent.
     *
     * @param rack the {@code Rack} to load.
     */
    public void loadRack(Rack rack) throws Exception {
        if (isLoaded()) {
            throw new Exception("Cannot load more than one rack at a time!");
        }

        // Load the rack
        rack.load();
        status = AgentStatus.ACTIVE_LOADED;
    }

    /**
     * Offloads the rack above this agent.
     *
     * @param rack the {@code Rack} to offload.
     */
    public void offloadRack(Rack rack) throws Exception {
        if (!isLoaded()) {
            throw new Exception("No rack is available to be offloaded!");
        }

        // Offload the rack
        rack.offload();
        status = AgentStatus.ACTIVE;
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
