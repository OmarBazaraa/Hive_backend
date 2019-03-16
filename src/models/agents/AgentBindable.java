package models.agents;

import models.facilities.Facility;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;


/**
 * Interface definition for all {@link Agent} bindable classes.
 * <p>
 * An {@code AgentBindable} class is a class that can be bound to an {@link Agent}.
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
public interface AgentBindable {

    /**
     * Returns the {@code Agent} currently allocating this object.
     *
     * @return the allocating {@code Agent} if exists; {@code null} otherwise.
     *
     * @see AgentBindable#isAllocated()
     * @see AgentBindable#allocate(Agent)
     * @see AgentBindable#deallocate()
     */
    Agent getAllocatingAgent();

    /**
     * Checks whether this object is currently allocated by an {@code Agent} or not.
     *
     * @return {@code true} if this object is allocated; {@code false} otherwise.
     *
     * @see AgentBindable#getAllocatingAgent()
     * @see AgentBindable#allocate(Agent)
     * @see AgentBindable#deallocate()
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
     * @see AgentBindable#getAllocatingAgent()
     * @see AgentBindable#isAllocated()
     * @see AgentBindable#deallocate()
     */
    void allocate(Agent agent) throws Exception;

    /**
     * De-allocates and releases this object from the currently allocating {@code Agent}.
     *
     * @see AgentBindable#getAllocatingAgent()
     * @see AgentBindable#isAllocated()
     * @see AgentBindable#allocate(Agent)
     */
    void deallocate() throws Exception;

    /**
     * Checks whether this object is currently bound with an {@code Agent} or not.
     *
     * @return {@code true} if this object is bound; {@code false} otherwise.
     *
     * @see AgentBindable#canBind(Agent)
     * @see AgentBindable#bind(Agent)
     * @see AgentBindable#canUnbind()
     * @see AgentBindable#unbind()
     */
    boolean isBound();

    /**
     * Checks whether its currently possible to bind this object with the given {@code Agent}.
     * <p>
     * It is preferable to allocate the object before binding it to an {@code Agent}.
     *
     * @param agent the {@code Agent} to check.
     *
     * @return {@code true} if it is possible to bind; {@code false} otherwise.
     *
     * @see AgentBindable#isBound()
     * @see AgentBindable#bind(Agent)
     * @see AgentBindable#canUnbind()
     * @see AgentBindable#unbind()
     */
    boolean canBind(Agent agent);

    /**
     * Binds this object with the given {@code Agent}.
     * <p>
     * It is preferable to allocate the object before binding it to an {@code Agent}.
     * <p>
     * This function should be called after checking that it is currently possible to bind
     * the given {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @param agent the {@code Agent} to bind.
     *
     * @see AgentBindable#isBound()
     * @see AgentBindable#canBind(Agent)
     * @see AgentBindable#canUnbind()
     * @see AgentBindable#unbind()
     */
    void bind(Agent agent) throws Exception;

    /**
     * Checks whether its currently possible to unbind the bound {@code Agent} from this object.
     *
     * @return {@code true} if it is possible to unbind; {@code false} otherwise.
     *
     * @see AgentBindable#isBound()
     * @see AgentBindable#canBind(Agent)
     * @see AgentBindable#bind(Agent)
     * @see AgentBindable#unbind()
     */
    boolean canUnbind();

    /**
     * Unbinds the bound {@code Agent} from this object.
     * <p>
     * This function should be called after checking that it is currently possible to unbind
     * the bound {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @see AgentBindable#isBound()
     * @see AgentBindable#canBind(Agent)
     * @see AgentBindable#bind(Agent)
     * @see AgentBindable#canUnbind()
     */
    void unbind() throws Exception;
}
