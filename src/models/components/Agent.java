package models.components;

import models.components.base.HiveObject;
import models.components.base.SrcHiveObject;
import models.map.GuideGrid;
import models.map.MapCell;
import models.map.MapGrid;
import models.map.GuideCell;
import models.map.base.Position;
import utils.Constants.*;
import utils.Utility;


/**
 * This {@code Agent} class is a model for robot agent in our Hive System.
 */
public class Agent extends SrcHiveObject {

    //
    // Member Variables
    //

    /**
     * The current status of this agent.
     */
    private AgentStatus status = AgentStatus.IDLE;

    /**
     * The assigned task of this agent.
     */
    private Task task;

    /**
     * The last time this agent has performed a bringBlank.
     * Needed by the planner algorithm.
     */
    private int lastActionTime;

    /**
     * The last time this agent tried to be bring a blank to a higher priority agent.
     * Needed by the planner algorithm.
     */
    private int lastBringBlankTime;

    // Skip for now
    private int capacity;
    private int chargeMaxCap;
    private int chargeLevel;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new agent robot.
     *
     * @param id  the id of the agent.
     * @param row the row position of the agent.
     * @param col the column position of the agent.
     */
    public Agent(int id, int row, int col) {
        super(id, row, col);
    }

    /**
     * Returns the current status of this agent.
     *
     * @return an {@code AgentStatus} value representing the current status of the agent.
     */
    public AgentStatus getStatus() {
        return this.status;
    }

    /**
     * Sets a new status to this agent.
     *
     * @param status the new status to set.
     */
    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    /**
     * Checks whether this agent is currently active or not.
     *
     * @return {@code true} if this agent is active, {@code false} otherwise.
     */
    public boolean isActive() {
        return (status == AgentStatus.ACTIVE || status == AgentStatus.ACTIVE_LOADED);
    }

    /**
     * Checks whether this agent is currently loaded by a rack or not.
     *
     * @return {@code true} if this agent is loaded, {@code false} otherwise.
     */
    public boolean isLoaded() {
        return (status == AgentStatus.ACTIVE_LOADED);
    }

    /**
     * Returns the assigned task of this agent.
     *
     * @return a {@code Task} object representing the assigned task of this agent;
     * {@code null} if no current assigned task.
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * Assigns a new task to this agent.
     *
     * @param task the new task to assign.
     */
    public void setTask(Task task) {
        this.task = task;
        this.status = AgentStatus.ACTIVE;
    }

    /**
     * Clears the currently assigned task of this agent.
     */
    public void clearTask() {
        this.task = null;
        this.status = AgentStatus.IDLE;
    }

    /**
     * Returns the last time this agent has performed an action.
     */
    public int getLastActionTime() {
        return this.lastActionTime;
    }

    /**
     * Sets the last time this agent has performed an action.
     *
     * @param time the time step of the last action.
     */
    public void setLastActionTime(int time) throws Exception {
        if (lastActionTime > time) {
            throw new Exception("The given time is smaller than the time of the last action performed by the agent!");
        }

        this.lastActionTime = time;
    }

    /**
     * Returns the last time this agent tried to be bring a blank position
     * to a higher priority agent.
     */
    public int getLastBringBlankTime() {
        return this.lastBringBlankTime;
    }

    /**
     * Sets the last time this agent tried to be bring a blank position
     * to a higher priority agent.
     *
     * @param time the time step of the last trial.
     */
    public void setLastBringBlankTime(int time) throws Exception {
        if (lastActionTime > time) {
            throw new Exception("The given time is smaller than the time of the last bring blank trial performed by the agent!");
        }

        this.lastBringBlankTime = time;
    }

    /**
     * Returns the priority of this agent.
     * Higher value indicates higher priority.
     *
     * @return an integer value representing the priority of this agent.
     */
    public int getPriority() {
        return (task != null ? task.getPriority() : Integer.MIN_VALUE);
    }

    /**
     * Returns the guide map to reach the target of the assigned task.
     *
     * @return the {@code GuideGrid} to reach the target.
     */
    public GuideGrid getGuideMap() {
        return (task != null ? task.getGuideMap() : null);
    }

    /**
     * Returns the estimated number of steps to finish the currently assigned task.
     *
     * @return an integer representing the estimated number of step to finish the assigned task.
     */
    public int getEstimatedSteps() {
        return (task != null ? task.getEstimatedDistance() : 0);
    }

    /**
     * Returns the next required action to be done by this agent
     * in order to bringBlank one step forward to complete this task.
     *
     * @return {@code AgentAction} to be done the next time step.
     */
    public AgentAction getNextAction() {
        return (task != null ? task.getNextAction() : AgentAction.NOTHING);
    }

    /**
     * Executes the given action by this agent.
     *
     * @param action the action to be executed.
     * @param map    the map's grid of the warehouse where the agent is.
     * @param time   the current time step.
     */
    public void executeAction(AgentAction action, MapGrid map, int time) throws Exception {
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
        setLastActionTime(time);
        task.updateStatus(action);
    }

    /**
     * Moves this agent in the given direction.
     *
     * @param dir the direction to bringBlank.
     * @param map the map's grid of the warehouse where the agent is.
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
        if (nxtCell.type == CellType.RACK && isLoaded()) {
            throw new Exception("Moving into a rack while the robot is currently loaded!");
        }

        // Move agent
        curCell.setSrcObject(null);
        nxtCell.setSrcObject(this);
        setPosition(nxt);
    }

    /**
     * Loads the rack above this agent.
     *
     * @param map the map's grid of the warehouse where the agent is.
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
     * @param map the map's grid of the warehouse where the agent is.
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
     * @param map the map's grid of the warehouse where the agent is.
     *
     * @return {@code true} if the rack is valid, {@code false} otherwise.
     */
    private boolean checkRackValidity(MapGrid map) {
        MapCell cell = map.get(getPosition());

        if (cell.type != CellType.RACK) {
            return false;
        }

        Rack rack = (Rack) cell.dstObj;

        if (!rack.equals(task.getRack())) {
            return false;
        }

        return true;
    }

    /**
     * Waits until the items of the task are taken at the gate.
     *
     * @param map the map's grid of the warehouse where the agent is.
     */
    public void waitOnGate(MapGrid map) throws Exception {
        // TODO:
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
    public int compareTo(HiveObject obj) {
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
