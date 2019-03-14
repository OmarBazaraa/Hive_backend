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
     * Checks whether its currently possible to bind the given {@code Agent} to this object.
     *
     * @return {@code true} if it is possible to bind; {@code false} otherwise.
     */
    default boolean canBind(Agent agent) {
        return true;
    }

    /**
     * Binds the given {@code Agent} with this object.
     *
     * @param agent the {@code Agent} to bind.
     */
    void bind(Agent agent);

    /**
     * Checks whether its currently possible to unbind the given {@code Agent} from this object.
     *
     * @return {@code true} if it is possible to bind; {@code false} otherwise.
     */
    default boolean canUnbind(Agent agent) {
        return true;
    }

    /**
     * Unbinds the given {@code Agent} from this object.
     *
     * @param agent the {@code Agent} to unbind.
     */
    void unbind(Agent agent);
}
