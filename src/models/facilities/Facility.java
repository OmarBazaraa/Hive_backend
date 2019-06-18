package models.facilities;

import algorithms.planner.Planner;

import models.HiveObject;
import models.agents.Agent;
import models.agents.AgentAllocatable;
import models.agents.AgentBindable;
import models.maps.utils.Position;


/**
 * This {@code Facility} class is the base class of all the facilities components
 * in our Hive Warehouse System.
 * <p>
 * A facilities component is an object in the {@link models.warehouses.Warehouse Warehouse} grid
 * that provide services and functions to either an {@link models.agents.Agent Agent} or to a client.
 * A facilities component is typically a static object with fixed location in the warehouse's grid.
 *
 * @see models.HiveObject HiveObject
 * @see models.agents.Agent Agent
 * @see models.facilities.Rack Rack
 * @see models.facilities.Gate Gate
 * @see models.facilities.Station Station
 */
abstract public class Facility extends HiveObject implements AgentBindable, AgentAllocatable {

    //
    // Member Variables
    //

    /**
     * The guide map grid of this {@code Facility}.
     */
    protected int[][] guideMap;

    /**
     * The {@code Agent} allocating this {@code Facility}.
     */
    protected Agent allocatingAgent;

    /**
     * The {@code Agent} currently bound to this {@code Facility}.
     */
    protected Agent boundAgent;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Facility} object.
     */
    public Facility() {
        super();
    }

    /**
     * Constructs a new {@code Facility} object with the given id.
     *
     * @param id the id of the {@code Facility}.
     */
    public Facility(int id) {
        super(id);
    }

    /**
     * Returns the least number of steps to reach this {@code Facility}
     * from the given position in the {@code Warehouse} or vice versa.
     *
     * @param row the row position of the destination.
     * @param col the column position of the destination.
     *
     * @return the shortest distance to the given position;
     *         or {@link Integer#MAX_VALUE} if unreachable.
     */
    public int getDistanceTo(int row, int col) {
        return guideMap[row][col];
    }

    /**
     * Returns the least number of steps to reach this {@code Facility}
     * from the given position in the {@code Warehouse} or vice versa.
     *
     * @param pos the {@code Position} of the destination.
     *
     * @return the shortest distance to the given position;
     *         or {@link Integer#MAX_VALUE} if unreachable.
     */
    public int getDistanceTo(Position pos) {
        return guideMap[pos.row][pos.col];
    }

    /**
     * Returns the least number of steps for the given {@code Agent}
     * to reach this {@code Facility} in the {@code Warehouse} or vice versa.
     *
     * @param agent the target {@code Agent}.
     *
     * @return the shortest distance to the given position;
     *         or {@link Integer#MAX_VALUE} if unreachable.
     */
    public int getDistanceTo(Agent agent) {
        return guideMap[agent.getRow()][agent.getCol()];
    }

    /**
     * Computes the guide map to reach this {@code Facility}.
     * That is, a map with the least number of steps to reach this {@code Facility}
     * from any other cell in the {@code Warehouse}.
     */
    public void computeGuideMap() {
        guideMap = Planner.computeGuideMap(row, col);
    }

    /**
     * Returns the {@code Agent} currently allocating this {@code Facility}.
     *
     * @return the allocating {@code Agent} if exists; {@code null} otherwise.
     */
    public Agent getAllocatingAgent() {
        return allocatingAgent;
    }

    /**
     * Checks whether this {@code Facility} is currently allocated by an {@code Agent} or not.
     *
     * @return {@code true} if this {@code Facility} is allocated; {@code false} otherwise.
     */
    public boolean isAllocated() {
        return (allocatingAgent != null);
    }

    /**
     * Allocates and reserves this {@code Facility} to the given {@code Agent}.
     * <p>
     * This function should be called after checking that this {@code Facility} is currently
     * un-allocated; otherwise un-expected behaviour could occur.
     *
     * @param agent the allocating {@code Agent}.
     */
    @Override
    public void allocate(Agent agent) {
        allocatingAgent = agent;
    }

    /**
     * De-allocates and releases this {@code Facility} from the currently allocating {@code Agent}.
     * <p>
     * This function should be called only when an {@code Agent} is already allocating this {@code Facility}.
     */
    public void deallocate() {
        allocatingAgent = null;
    }

    /**
     * Checks whether this {@code Facility} is currently bound with an {@code Agent} or not.
     *
     * @return {@code true} if this {@code Facility} is bound; {@code false} otherwise.
     */
    public boolean isBound() {
        return (boundAgent != null);
    }

    /**
     * Checks whether its currently possible to bind this {@code Facility} with the given {@code Agent}.
     * <p>
     * It is preferable to allocate the {@code Facility} before binding it to an {@code Agent}.
     *
     * @param agent the {@code Agent} to check.
     *
     * @return {@code true} if it is possible to bind; {@code false} otherwise.
     */
    @Override
    public boolean canBind(Agent agent) {
        return (agent.isCoincide(this));
    }

    /**
     * Binds this {@code Facility} with the given {@code Agent}.
     * <p>
     * It is preferable to allocate the {@code Facility} before binding it to an {@code Agent}.
     * <p>
     * This function should be called after checking that it is currently possible to bind
     * the given {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @param agent the {@code Agent} to bind.
     */
    @Override
    public void bind(Agent agent) {
        boundAgent = agent;
    }

    /**
     * Checks whether its currently possible to unbind the bound {@code Agent} from this {@code Facility}.
     * <p>
     * This function should be called only when an {@code Agent} is already bound to this {@code Facility}.
     *
     * @return {@code true} if it is possible to unbind; {@code false} otherwise.
     */
    @Override
    public boolean canUnbind() {
        return (boundAgent.isCoincide(this));
    }

    /**
     * Unbinds the bound {@code Agent} from this {@code Facility}.
     * <p>
     * This function should be called after checking that it is currently possible to unbind
     * the bound {@code Agent}; otherwise un-expected behaviour could occur.
     */
    @Override
    public void unbind() {
        boundAgent = null;
    }
}
