package models.agents;

import models.Entity;
import models.HiveObject;
import models.facilities.Facility;
import models.facilities.Rack;
import models.tasks.Task;
import models.tasks.TaskAssignable;
import models.warehouses.Warehouse;

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
     * The maximum weight the {@code Agent} can load.
     */
    protected int loadCapacity = Constants.AGENT_DEFAULT_LOAD_CAPACITY;

    /**
     * The current battery percentage of this {@code Agent}.
     */
    protected int chargePercentage = Constants.AGENT_DEFAULT_CHARGE_PERCENTAGE;

    /**
     * The current direction this {@code Agent} is heading to.
     */
    protected Direction direction = Constants.AGENT_DEFAULT_DIRECTION;

    /**
     * The flag indicating whether this {@code Agent} is currently loaded by a {@code Rack}.
     */
    protected boolean loaded = false;

    /**
     * The last time this {@code Agent} has performed an action.
     * Needed by the planner algorithm.
     */
    protected long lastActionTime = -1;

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
     * @param id           the id of the {@code Agent}.
     * @param loadCapacity the maximum weight the {@code Agent} can load.
     * @param direction    the current direction this {@code Agent} is heading to.
     */
    public AbstractAgent(int id, int loadCapacity, Direction direction) {
        super(id);
        this.loadCapacity = loadCapacity;
        this.direction = direction;
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
     * Returns the current direction this {@code Agent} is heading to.
     *
     * @return the direction of this {@code Agent}.
     */
    public Direction getDirection() {
        return direction;
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
     * Checks whether this {@code Agent} already done an action this time step or not.
     *
     * @return {@code true} if already done an action; {@code false} otherwise.
     */
    public boolean isAlreadyMoved() {
        return lastActionTime >= Warehouse.getInstance().getTime();
    }

    /**
     * Sets the last time this {@code Agent} has performed an action.
     */
    public void updateLastActionTime() {
        lastActionTime = Warehouse.getInstance().getTime();
    }

    /**
     * Returns the priority of this {@code Agent}.
     * Greater value indicates higher priority.
     *
     * @return the priority of this {@code Agent}.
     */
    public int getPriority() {
        Task task = getActiveTask();
        return (task != null ? task.getPriority() : Short.MIN_VALUE);
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
     * Moves this {@code Agent} forward in its current {@code Direction}.
     */
    abstract public void move();

    /**
     * Rotates this {@code Agent} according to the given rotation action.
     * <p>
     * The allowed actions are only:
     * {@code AgentAction.ROTATE_RIGHT}, {@code AgentAction.ROTATE_LEFT}.
     *
     * @param action the {@code AgentAction} to rotate with.
     */
    abstract public void rotate(AgentAction action);

    /**
     * Moves this {@code Agent} in the given {@code Direction}.
     *
     * @param dir the {@code Direction} to move along.
     */
    abstract public void move(Direction dir);

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
     * Compares whether some other object is less than, equal to, or greater than this one.
     *
     * @param obj the reference object with which to compare.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
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
