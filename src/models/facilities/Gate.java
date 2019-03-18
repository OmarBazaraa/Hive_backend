package models.facilities;

import models.agents.Agent;
import models.tasks.Task;

import server.Server;

import utils.Constants;
import utils.Constants.*;

import org.json.JSONObject;


/**
 * This {@code Gate} class is a one of the {@link Facility} components
 * in our Hive Warehouse System.
 * <p>
 * A gate is a location in the {@link models.warehouses.Warehouse Warehouse} grid
 * where an {@link models.agents.Agent Agent} deliver {@link Rack} of {@link models.items.Item Items}.
 *
 * @see models.HiveObject HiveObject
 * @see models.agents.Agent Agent
 * @see models.facilities.Facility Facility
 * @see models.facilities.Rack Rack
 * @see models.facilities.Station Station
 * @see models.tasks.Task Task
 */
public class Gate extends Facility {

    //
    // Static Methods
    //

    /**
     * Creates a new {@code Gate} object from JSON data.
     *
     * @param data the un-parsed JSON data.
     * @param row  the row position of the {@code MapCell} to create.
     * @param col  the column position of the {@code MapCell} to create.
     *
     * @return an {@code Gate} object.
     */
    public static Gate create(JSONObject data, int row, int col) throws Exception {
        Gate ret = new Gate();
        ret.setPosition(row, col);
        return ret;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

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
     *
     * @see Gate#isBound()
     * @see Gate#canBind(Agent)
     * @see Gate#canUnbind()
     * @see Gate#unbind()
     */
    @Override
    public void bind(Agent agent) throws Exception {
        // Bind
        // TODO: lock the agent
        Task task = agent.getActiveTask();
        Rack rack = task.getRack();
        rack.confirmReservation(task);
        super.bind(agent);

        // Send binding to the front frontend
        Server.getInstance().sendAction(agent, AgentAction.BIND_GATE);
    }

    /**
     * Checks whether its currently possible to unbind the bound {@code Agent} from this {@code Gate}.
     *
     * @return {@code true} if it is possible to unbind; {@code false} otherwise.
     *
     * @see Facility#isBound()
     * @see Facility#canBind(Agent)
     * @see Facility#bind(Agent)
     * @see Facility#unbind()
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
     *
     * @see Gate#isBound()
     * @see Gate#canBind(Agent)
     * @see Gate#bind(Agent)
     * @see Gate#canUnbind()
     */
    @Override
    public void unbind() throws Exception {
        // Unbind
        // TODO: unlock the bound agent
        super.unbind();

        // Send unbinding to the front frontend
        Server.getInstance().sendAction(boundAgent, AgentAction.UNBIND_GATE);
    }
}
