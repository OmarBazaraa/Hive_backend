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
import utils.Constants.*;
import utils.Utility;


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

    /**
     * The last time this {@code Agent} tried to be bring a blank location
     * to a higher priority {@code Agent}.
     * Needed by the planner algorithm.
     */
    private long lastBringBlankTime;

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
     * Returns the next required action to be done by this {@code Agent}.
     * This is to be determined by the assigned {@code Task}.
     *
     * @return {@code AgentAction} to be done the next time step.
     */
    public AgentAction getNextAction() {
        return (task != null ? task.getNextAction() : AgentAction.NOTHING);
    }

    /**
     * Executes the given action.
     *
     * @param action the action to be executed.
     * @param map    the map grid of the {@code Warehouse} where this {@code Agent} is located.
     */
    public void executeAction(AgentAction action, MapGrid map) throws Exception {
        // Return if the action is nothing
        if (action == AgentAction.NOTHING) {
            return;
        }

        // Switch on different actions
        switch (action) {
            case MOVE_UP:
            case MOVE_RIGHT:
            case MOVE_DOWN:
            case MOVE_LEFT:
                move(Utility.actionToDir(action), map);
                break;
            case LOAD:
                loadRack(map);
                break;
            case OFFLOAD:
                offloadRack(map);
                break;
            case WAIT:
                waitOnGate(map);
                break;
            default:
                throw new Exception("Invalid action!");
        }

        // Set the last action time
        updateLastActionTime();
        task.updateStatus(action);
    }

    /**
     * Moves this agent in the given direction.
     *
     * @param dir the direction to move along.
     * @param map the maps's grid of the warehouse where the agent is.
     */
    public void move(Direction dir, MapGrid map) throws Exception {
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
    }

    /**
     * Loads the rack above this agent.
     *
     * @param map the maps's grid of the warehouse where the agent is.
     */
    public void loadRack(MapGrid map) throws Exception {
        if (isLoaded()) {
            throw new Exception("Cannot load more than one rack at a time!");
        }

        // Throw exception if invalid
        if (!checkRackValidity(map)) {
            throw new Exception("Loading invalid rack!");
        }

        // Load the rack
        task.getRack().load();
        status = AgentStatus.ACTIVE_LOADED;
    }

    /**
     * Offloads the rack above this agent.
     *
     * @param map the maps's grid of the warehouse where the agent is.
     */
    public void offloadRack(MapGrid map) throws Exception {
        if (!isLoaded()) {
            throw new Exception("No rack is available to be offloaded!");
        }

        // Throw exception if invalid
        if (!checkRackValidity(map)) {
            throw new Exception("Offloading invalid rack!");
        }

        // Offload the rack
        task.getRack().offload();
        status = AgentStatus.ACTIVE;
    }

    /**
     * Checks the validity of the rack during loading/offloading actions.
     * This methods checks the position of the rack and the identity of the assigned rack.
     *
     * @param map the maps's grid of the warehouse where the agent is.
     *
     * @return {@code true} if the rack is valid; {@code false} otherwise.
     */
    private boolean checkRackValidity(MapGrid map) {
        MapCell cell = map.get(getPosition());

        if (cell.getType() != CellType.RACK) {
            return false;
        }

        Rack rack = (Rack) cell.getFacility();

        if (!rack.equals(task.getRack())) {
            return false;
        }

        return true;
    }

    /**
     * Waits until the items of the task are taken at the gate.
     *
     * @param map the maps's grid of the warehouse where the agent is.
     */
    public void waitOnGate(MapGrid map) throws Exception {
        // TODO:
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

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
        lastActionTime = Warehouse.getTime();
    }

    /**
     * Returns the last time this {@code Agent} tried to be bring a blank location
     * to a higher priority {@code Agent}.
     */
    public long getLastBringBlankTime() {
        return lastBringBlankTime;
    }

    /**
     * Updates the last time this agent tried to be bring a blank position
     * to a higher priority agent.
     */
    public void updateLastBringBlankTime() {
        lastBringBlankTime = Warehouse.getTime();
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
