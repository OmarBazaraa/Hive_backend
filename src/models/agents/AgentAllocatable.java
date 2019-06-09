package models.agents;

import models.facilities.Facility;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;


/**
 * Interface definition for all {@link Agent} allocatable classes.
 * <p>
 * An {@code AgentAllocatable} object is an object that can be allocated to an {@link Agent}.
 * <p>
 * This interface is to be implemented by all the {@link Facility} classes:
 * {@link Rack}, {@link Gate}, and {@link Station}.
 *
 * @see Agent
 * @see Facility
 * @see Rack
 * @see Gate
 * @see Station
 */
public interface AgentAllocatable {

    /**
     * Returns the {@code Agent} currently allocating this object.
     *
     * @return the allocating {@code Agent} if exists; {@code null} otherwise.
     *
     * @see AgentAllocatable#isAllocated()
     * @see AgentAllocatable#allocate(Agent)
     * @see AgentAllocatable#deallocate()
     */
    Agent getAllocatingAgent();

    /**
     * Checks whether this object is currently allocated by an {@code Agent} or not.
     *
     * @return {@code true} if this object is allocated; {@code false} otherwise.
     *
     * @see AgentAllocatable#getAllocatingAgent()
     * @see AgentAllocatable#allocate(Agent)
     * @see AgentAllocatable#deallocate()
     */
    boolean isAllocated();

    /**
     * Allocates and reserves this object to the given {@code Agent}.
     * <p>
     * This function should be called after checking that this object is currently
     * un-allocated; otherwise un-expected behaviour could occur.
     *
     * @param agent the allocating {@code Agent}.
     *
     * @see AgentAllocatable#getAllocatingAgent()
     * @see AgentAllocatable#isAllocated()
     * @see AgentAllocatable#deallocate()
     */
    void allocate(Agent agent) throws Exception;

    /**
     * De-allocates and releases this object from the currently allocating {@code Agent}.
     * <p>
     * This function should be called only when an {@code Agent} is already allocating this object.
     *
     * @see AgentAllocatable#getAllocatingAgent()
     * @see AgentAllocatable#isAllocated()
     * @see AgentAllocatable#allocate(Agent)
     */
    void deallocate() throws Exception;
}
