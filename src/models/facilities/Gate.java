package models.facilities;

import models.agents.Agent;
import models.tasks.Task;

import server.Server;

import utils.Constants;
import utils.Constants.*;


/**
 * This {@code Gate} class is a one of the {@link Facility} components
 * in our Hive Warehouse System.
 * <p>
 * A gate is a location in the {@link models.warehouses.Warehouse Warehouse} grid
 * where an {@link models.agents.Agent Agent} delivers {@link Rack} of {@link models.items.Item Items}.
 *
 * @see models.HiveObject HiveObject
 * @see models.agents.Agent Agent
 * @see models.facilities.Facility Facility
 * @see models.facilities.Rack Rack
 * @see models.facilities.Station Station
 * @see models.tasks.Task Task
 */
public class Gate extends Facility {

    /**
     * Constructs a new {@code Gate} object.
     */
    public Gate() {
        super();
    }

    /**
     * Constructs a new {@code Gate} object.
     *
     * @param id  the id of the {@code Gate}.
     */
    public Gate(int id) {
        super(id);
    }

    /**
     * Binds this {@code Gate} with the given {@code Agent}.
     * <p>
     * This function should be called after checking that it is currently possible to bind
     * the given {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @param agent the {@code Agent} to bind.
     */
    @Override
    public void bind(Agent agent) {
        super.bind(agent);
        // TODO: lock the agent
        Task task = agent.getActiveTask();
        Rack rack = task.getRack();
        rack.confirmReservation(task);

        Server.getInstance().enqueueAgentAction(boundAgent, AgentAction.BIND);
    }

    /**
     * Checks whether its currently possible to unbind the bound {@code Agent} from this {@code Gate}.
     *
     * @return {@code true} if it is possible to unbind; {@code false} otherwise.
     */
    @Override
    public boolean canUnbind() {
        // TODO: add extra check on time
        return (super.canUnbind());
    }

    /**
     * Unbinds the bound {@code Agent} from this {@code Gate}.
     * <p>
     * This function should be called after checking that it is currently possible to unbind
     * the bound {@code Agent}; otherwise un-expected behaviour could occur.
     */
    @Override
    public void unbind() {
        // TODO: unlock the bound agent
        Server.getInstance().enqueueAgentAction(boundAgent, AgentAction.UNBIND);
        super.unbind();
    }

    /**
     * Returns a string representation of this {@code Gate}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Gate}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder
                .append("Gate: {")
                .append(" id: ").append(id).append(",")
                .append(" pos: ").append("(").append(row).append("x").append(col).append(")")
                .append(" }");

        return builder.toString();
    }
}
