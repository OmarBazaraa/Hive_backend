package models.agents;

import models.facilities.Facility;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;


/**
 * Interface definition for all {@link Agent} bindable classes.
 * <p>
 * An {@code AgentBindable} object is an object that can be bound to an {@link Agent}.
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
     * Returns the {@code Agent} currently bound this object.
     *
     * @return the bound {@code Agent} if exists; {@code null} otherwise.
     */
    Agent getBoundAgent();

    /**
     * Checks whether this object is currently bound with an {@code Agent} or not.
     *
     * @return {@code true} if this object is bound; {@code false} otherwise.
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
     */
    void bind(Agent agent);

    /**
     * Checks whether its currently possible to unbind the bound {@code Agent} from this object.
     * <p>
     * This function should be called only when an {@code Agent} is already bound to this object.
     *
     * @return {@code true} if it is possible to unbind; {@code false} otherwise.
     */
    boolean canUnbind();

    /**
     * Unbinds the bound {@code Agent} from this object.
     * <p>
     * This function should be called after checking that it is currently possible to unbind
     * the bound {@code Agent}; otherwise un-expected behaviour could occur.
     */
    void unbind();
}
