package models.facilities;

import models.agents.Agent;

import server.Server;

import utils.Constants;
import utils.Constants.*;


/**
 * This {@code Station} class is a one of the {@link Facility} components
 * in our Hive Warehouse System.
 * <p>
 * A station is a location in the {@link models.warehouses.Warehouse Warehouse} grid
 * where an {@link models.agents.Agent Agent} goes to re-charge its batteries, be fixed, and others.
 *
 * @see models.HiveObject HiveObject
 * @see models.agents.Agent Agent
 * @see models.facilities.Facility Facility
 * @see models.facilities.Rack Rack
 * @see models.facilities.Gate Gate
 * @see models.tasks.Task Task
 */
public class Station extends Facility {

    /**
     * Constructs a new {@code Station} object.
     */
    public Station() {
        super();
    }

    /**
     * Constructs a new {@code Station} object.
     *
     * @param id the id of the {@code Station}.
     */
    public Station(int id) {
        super(id);
    }

    /**
     * Binds this {@code Station} with the given {@code Agent}.
     * <p>
     * It is preferable to allocate the {@code Station} before binding it to an {@code Agent}.
     * <p>
     * This function should be called after checking that it is currently possible to bind
     * the given {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @param agent the {@code Agent} to bind.
     *
     * @see Station#isBound()
     * @see Station#canBind(Agent)
     * @see Station#canUnbind()
     * @see Station#unbind()
     */
    @Override
    public void bind(Agent agent) {
        Server.getInstance().enqueueAgentAction(boundAgent, AgentAction.BIND);
        super.bind(agent);
    }

    /**
     * Unbinds the bound {@code Agent} from this {@code Station}.
     * <p>
     * This function should be called after checking that it is currently possible to unbind
     * the bound {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @see Station#isBound()
     * @see Station#canBind(Agent)
     * @see Station#bind(Agent)
     * @see Station#canUnbind()
     */
    @Override
    public void unbind() {
        Server.getInstance().enqueueAgentAction(boundAgent, AgentAction.UNBIND);
        super.unbind();
    }
}
