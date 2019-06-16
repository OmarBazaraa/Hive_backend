package models.facilities;

import algorithms.planner.Planner;

import models.HiveObject;
import models.agents.Agent;
import models.agents.AgentAllocatable;
import models.agents.AgentBindable;
import models.maps.GuideGrid;
import models.maps.MapGrid;


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
    protected GuideGrid guideMap;

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
     * Returns a guide map to reach this {@code Facility}.
     *
     * @return the {@code GuideGrid} of this {@code Facility}.
     */
    public GuideGrid getGuideMap() {
        return guideMap;
    }

    /**
     * Computes a guide map to reach this {@code Facility}.
     *
     * @param map the {@code MapGrid} of the {@code Warehouse} where this {@code Facility} is located in.
     */
    public void computeGuideMap(MapGrid map) {
        guideMap = Planner.computeGuideMap(map, getPosition());
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
