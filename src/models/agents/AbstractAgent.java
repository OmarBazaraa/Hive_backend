package models.agents;

import models.Entity;
import models.HiveObject;
import models.facilities.Facility;
import models.facilities.Rack;
import models.maps.utils.Pose;
import models.tasks.Task;
import models.tasks.TaskAssignable;

import utils.Constants;
import utils.Constants.*;

import java.util.LinkedList;
import java.util.Queue;


/**
 * This {@code AbstractAgent} class is the base class of all the agent robots
 * in our Hive Warehouse System.
 *
 * @see Agent
 */
abstract public class AbstractAgent extends HiveObject implements TaskAssignable {

    //
    // Member Variables
    //

    /**
     * The current direction this {@code Agent} is heading to.
     */
    protected Direction direction = Constants.AGENT_DEFAULT_DIRECTION;

    /**
     * The flag indicating whether this {@code Agent} is currently deactivated or not.
     */
    protected boolean deactivated = false;

    /**
     * The flag indicating whether the last {@code AgentAction} got blocked or not.
     */
    protected boolean blocked = false;

    /**
     * The flag indicating whether this {@code Agent} is currently loaded by a {@code Rack}.
     */
    protected boolean loaded = false;

    /**
     * The maximum weight the {@code Agent} can load.
     */
    protected int loadCapacity = Constants.AGENT_DEFAULT_LOAD_CAPACITY;

    /**
     * The current battery percentage of this {@code Agent}.
     */
    protected int chargePercentage = Constants.AGENT_DEFAULT_CHARGE_PERCENTAGE;

    /**
     * The ip address of this {@code Agent} needed for communication.
     */
    protected String ip;

    /**
     * The port number of this {@code Agent} needed for communication.
     */
    protected String port;

    /**
     * The queue of assigned tasks for this {@code Agent}.
     */
    protected Queue<Task> tasks = new LinkedList<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Agent} robot.
     */
    public AbstractAgent() {
        super();
    }

    /**
     * Constructs a new {@code Agent} robot.
     *
     * @param id      the id of the {@code Agent}.
     * @param loadCap the maximum weight the {@code Agent} can load.
     */
    public AbstractAgent(int id, int loadCap) {
        super(id);
        this.loadCapacity = loadCap;
        this.direction = direction;
    }


    // ===============================================================================================
    //
    // Getters & Setters
    //

    /**
     * Returns the current direction this {@code Agent} is heading to.
     *
     * @return the {@code Direction} of this {@code Agent}.
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the direction of this {@code Agent}.
     *
     * @param dir the new {@code Direction} to set.
     */
    public void setDirection(Direction dir) {
        direction = dir;
    }

    /**
     * Returns the current pose of this {@code Agent}.
     *
     * @return the {@code Pose} of this {@code Agent}.
     */
    public Pose getPose() {
        return new Pose(row, col, direction);
    }

    /**
     * Sets the pose of this {@code Agent}.
     *
     * @param pose the new {@code Pose} to set.
     */
    public void setPose(Pose pose) {
        row = pose.row;
        col = pose.col;
        direction = pose.dir;
    }

    /**
     * Returns whether this {@code Agent} is currently deactivated or not.
     *
     * @return {@code true} if this {@code Agent} is currently deactivated; {@code false} otherwise.
     */
    public boolean isDeactivated() {
        return deactivated;
    }

    /**
     * Returns whether this {@code Agent} is currently blocked or not.
     *
     * @return {@code true} if this {@code Agent} is currently blocked; {@code false} otherwise.
     */
    public boolean isBlocked() {
        return blocked;
    }

    /**
     * Checks whether this {@code Agent} is currently loaded by a {@code Rack} or not.
     *
     * @return {@code true} if this {@code Agent} is loaded; {@code false} otherwise.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Returns the maximum weight this {@code Agent} can load.
     *
     * @return the load capacity of this {@code Agent}.
     */
    public int getLoadCapacity() {
        return loadCapacity;
    }

    /**
     * Returns the current battery percentage of this {@code Agent}.
     *
     * @return the charge percentage of this {@code Agent}.
     */
    public int getChargePercentage() {
        return chargePercentage;
    }

    /**
     * Returns the ip address of this {@code Agent} needed for communication.
     *
     * @return the ip address of this {@code Agent}.
     */
    public String getIpAddress() {
        return ip;
    }

    /**
     * Sets the ip address of this {@code Agent} needed for communication.
     *
     * @param ipAddr the new ip address to set.
     */
    public void setIpAddress(String ipAddr) {
        ip = ipAddr;
    }

    /**
     * Returns the port number of this {@code Agent} needed for communication.
     *
     * @return the port number of this {@code Agent}.
     */
    public String getPortNumber() {
        return port;
    }

    /**
     * Sets the port number of this {@code Agent} needed for communication.
     *
     * @param portNum the new port number to set.
     */
    public void setPortNumber(String portNum) {
        port = portNum;
    }

    // ===============================================================================================
    //
    // Task-Related Methods
    //

    /**
     * Returns the priority of this {@code Agent}.
     * Smaller value indicates higher priority.
     *
     * @return the priority of this {@code Agent}.
     */
    public int getPriority() {
        Task task = getActiveTask();
        return (task != null ? task.getPriority() : Short.MAX_VALUE);
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
     * @param task the new {@code Task} to assign.
     */
    @Override
    public void assignTask(Task task) {
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
    }

    // ===============================================================================================
    //
    // Control-Related Methods
    //

    /**
     * Activates this {@code Agent}.
     */
    abstract public void activate();

    /**
     * Deactivates this {@code Agent}.
     */
    abstract public void deactivate();

    /**
     * Sudden blocks this {@code Agent} and all the affected agents
     * from completing their last actions.
     */
    abstract public void block();

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
    abstract public void plan(Facility dst);

    /**
     * Drops and cancels the current plan of this {@code Agent}.
     */
    abstract public void dropPlan();

    /**
     * Executes the next required action as specified by the currently
     * active assigned {@code Task}.
     */
    abstract public void executeAction() throws Exception;

    /**
     * Moves a single step to reach the given {@code Facility}.
     *
     * @param dst the target to reach.
     */
    abstract public void reach(Facility dst);

    /**
     * Moves this {@code Agent} according to the given action.
     * <p>
     * The allowed actions are only:
     * {@code AgentAction.ROTATE_RIGHT}, {@code AgentAction.ROTATE_LEFT}, and
     * {@code AgentAction.MOVE}.
     *
     * @param action the {@code AgentAction} to move with.
     */
    abstract public void move(AgentAction action);

    /**
     * Retreats from the last action done and returns back to a normal state.
     */
    abstract public void retreat();

    /**
     * Loads and lifts the given {@code Rack} above this {@code Agent}.
     *
     * @param rack the {@code Rack} to load.
     */
    abstract public void loadRack(Rack rack);

    /**
     * Offloads and releases the {@code Rack} currently loaded by this {@code Agent}.
     *
     * @param rack the {@code Rack} to offload.
     */
    abstract public void offloadRack(Rack rack);

    // ===============================================================================================
    //
    // Helper Methods
    //

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
        int cmp = (rhs.getPriority() - getPriority());
        if (cmp == 0) {
            return id - rhs.id;
        }
        return cmp;
    }

    /**
     * Returns a string representation of this {@code Agent}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Agent}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Agent: {");
        builder.append(" id: ").append(id).append(",");
        builder.append(" pos: ").append(getPosition()).append(", ");
        builder.append(" load_capacity: ").append(loadCapacity).append(",");
        builder.append(" direction: ").append(direction);
        builder.append(" }");

        return builder.toString();
    }
}
