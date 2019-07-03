package models.agents;

import models.Entity;
import models.HiveObject;
import models.facilities.Facility;
import models.facilities.Rack;
import models.maps.Pose;
import models.tasks.Task;
import models.tasks.TaskAssignable;
import models.warehouses.Warehouse;

import utils.Constants;
import utils.Utility;

import java.net.InetAddress;
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
    // Static Variables
    //

    /**
     * The {@code Warehouse} object.
     */
    protected static Warehouse sWarehouse = Warehouse.getInstance();

    // ===============================================================================================
    //
    // Member Variables
    //

    /**
     * The current direction this {@code Agent} is heading to.
     */
    protected int dir = Constants.AGENT_DEFAULT_DIRECTION;

    /**
     * The flag indicating whether this {@code Agent} is currently deactivated or not.
     */
    protected boolean deactivated = false;

    /**
     * The flag indicating whether the last {@code AgentAction} got blocked or not.
     */
    protected boolean blocked = false;

    /**
     * The flag indicating whether this {@code Agent} is currently locked by a {@code Facility}.
     */
    protected boolean locked = false;

    /**
     * The flag indicating whether this {@code Agent} is currently loaded by a {@code Rack}.
     */
    protected boolean loaded = false;

    /**
     * The maximum weight the {@code Agent} can load.
     */
    protected int loadCapacity = Constants.AGENT_DEFAULT_LOAD_CAPACITY;

    /**
     * The current battery level of this {@code Agent}.
     */
    protected int batteryLevel = Constants.AGENT_DEFAULT_BATTERY_LEVEL;

    /**
     * The ip address of this {@code Agent} needed for communication.
     */
    protected InetAddress ip;

    /**
     * The port number of this {@code Agent} needed for communication.
     */
    protected int port;

    /**
     * The listener object to this {@code Agent} events.
     */
    protected AgentListener listener;

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
    }

    // ===============================================================================================
    //
    // Getters & Setters
    //

    /**
     * Returns the current direction this {@code Agent} is heading to.
     *
     * @return the direction of this {@code Agent}.
     */
    public int getDirection() {
        return dir;
    }

    /**
     * Sets the direction of this {@code Agent}.
     *
     * @param d the new direction to set.
     */
    public void setDirection(int d) {
        dir = d;
    }

    /**
     * Returns the current pose of this {@code Agent}.
     *
     * @return the {@code Pose} of this {@code Agent}.
     */
    public Pose getPose() {
        return new Pose(row, col, dir);
    }

    /**
     * Sets the pose of this {@code Agent}.
     *
     * @param pose the new {@code Pose} to set.
     */
    public void setPose(Pose pose) {
        row = pose.row;
        col = pose.col;
        dir = pose.dir;
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
     * Checks whether this {@code Agent} is currently locked by a {@code Facility} or not.
     *
     * @return {@code true} if this {@code Agent} is locked; {@code false} otherwise.
     */
    public boolean isLocked() {
        return locked;
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
     * Returns the current battery level of this {@code Agent}.
     *
     * @return the charge level of this {@code Agent}.
     */
    public int getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * Sets the battery level of this {@code Agent}.
     *
     * @param level the new battery level to set.
     */
    public void setBatteryLevel(int level) {
        batteryLevel = level;

        if (listener != null) {
            listener.onBatteryLevelChange((Agent) this, level);
        }
    }

    /**
     * Returns the ip address of this {@code Agent} needed for communication.
     *
     * @return the ip address of this {@code Agent}.
     */
    public InetAddress getIpAddress() {
        return ip;
    }

    /**
     * Sets the ip address of this {@code Agent} needed for communication.
     *
     * @param ipAddr the new ip address to set.
     */
    public void setIpAddress(InetAddress ipAddr) {
        ip = ipAddr;
    }

    /**
     * Returns the port number of this {@code Agent} needed for communication.
     *
     * @return the port number of this {@code Agent}.
     */
    public int getPortNumber() {
        return port;
    }

    /**
     * Sets the port number of this {@code Agent} needed for communication.
     *
     * @param portNum the new port number to set.
     */
    public void setPortNumber(int portNum) {
        port = portNum;
    }

    /**
     * Registers a callback functions to be invoked when this {@code Agent} produces any events.
     *
     * @param l the callback to run; {@code null} to unregister any listeners.
     */
    public void setListener(AgentListener l) {
        listener = l;
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

        if (listener != null) {
            listener.onTaskAssign((Agent) this, task);
        }
    }

    /**
     * The callback function to be invoked when the currently active {@code Task} is completed.
     *
     * @param task the completed {@code Task}.
     */
    @Override
    public void onTaskComplete(Task task) {
        tasks.remove();

        if (listener != null) {
            listener.onTaskComplete((Agent) this, task);
        }
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

    /**
     * Recovers from the blockage and returns back to a normal state if possible.
     *
     * @return {@code true} if recovered successfully; {@code false} otherwise.
     */
    abstract public boolean recover();

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
    abstract public boolean executeAction() throws Exception;

    /**
     * Plans the sequence of actions to reach the given target {@code Facility}.
     * <p>
     * This function should be called with new destination only when the previous
     * plan has been reached.
     *
     * @param dst the target {@code Facility} to reach.
     */
    abstract protected void plan(Facility dst);

    /**
     * Drops and cancels the current plan of this {@code Agent}.
     */
    abstract protected void dropPlan();

    /**
     * Moves a single step to reach the given {@code Facility}.
     *
     * @param dst the target to reach.
     *
     * @return {@code true} if this {@code Agent} manged to move a step towards the target; {@code false} otherwise.
     */
    abstract public boolean reach(Facility dst);

    /**
     * Attempts to slide away from the current position of this {@code Agent} in order
     * to bring a blank cell to the given main {@code Agent}.
     *
     * @param mainAgent the main {@code Agent} issuing the slide.
     *
     * @return {@code true} if sliding is possible; {@code false} otherwise.
     */
    abstract protected boolean slide(Agent mainAgent);

    /**
     * Rotates this {@code Agent} to reach the given orientation.
     *
     * @param d the needed orientation.
     */
    abstract protected void rotate(int d);

    /**
     * Moves this {@code Agent} along its current direction.
     *
     * @param r the row position to move into.
     * @param c the column position to move into.
     */
    abstract protected void move(int r, int c);

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

    /**
     * Locks this {@code Agent} for the favor of the given {@code Facility}.
     *
     * @param facility the locking {@code Facility}.
     */
    abstract public void lock(Facility facility);

    /**
     * Unlocks the lock that the given {@code Facility} has locked this {@code Agent} by.
     *
     * @param facility the unlocking {@code Facility}.
     */
    abstract public void unlock(Facility facility);

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
        builder.append(" pos: ").append(getPosition()).append(",");
        builder.append(" dir: ").append(Utility.dirToShape(dir));
        builder.append(" load_capacity: ").append(loadCapacity).append(",");
        builder.append(" }");

        return builder.toString();
    }
}
