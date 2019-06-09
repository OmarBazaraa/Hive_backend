package models.agents;

import models.facilities.Rack;
import models.maps.GuideGrid;
import models.maps.MapCell;
import models.maps.MapGrid;
import models.maps.utils.Position;
import models.tasks.Task;
import models.warehouses.Warehouse;

import server.Server;

import utils.Constants;
import utils.Constants.Direction;
import utils.Utility;

import org.json.JSONObject;


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
        Agent ret = new Agent(id, Constants.AGENT_DEFAULT_LOAD_CAPACITY, Constants.AGENT_DEFAULT_DIRECTION);
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
     * Executes the next required action.
     */
    @Override
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

        // Send move to the frontend
        Server.getInstance().sendAction(this, Utility.dirToAction(dir));
    }

    /**
     * Loads and lifts the given {@code Rack} above this {@code Agent}.
     *
     * @param rack the {@code Rack} to load.
     */
    @Override
    public void loadRack(Rack rack) {
        // Enable loaded flag
        loaded = true;

        // Update action time
        updateLastActionTime();
    }

    /**
     * Offloads and releases the {@code Rack} currently loaded by this {@code Agent}.
     *
     * @param rack the {@code Rack} to offload.
     */
    @Override
    public void offloadRack(Rack rack) {
        // Disable loaded flag
        loaded = false;

        // Update action time
        updateLastActionTime();
    }
}
