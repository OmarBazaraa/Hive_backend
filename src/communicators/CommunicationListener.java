package communicators;

import models.agents.Agent;
import models.tasks.orders.Order;

import utils.Constants.*;


/**
 * Interface definition for communication listeners for any occurring events
 * related to frontend and hardware.
 */
public interface CommunicationListener {

    /**
     * Returns the current state of the listener object.
     *
     * @return the current {@code ServerState} of the listener.
     */
    ServerState getState();

    /**
     * Returns the current running mode of the listener object.
     *
     * @return the current {@code RunningMode} of the listener.
     */
    RunningMode getMode();

    /**
     * Called when the communicator receives a start message.
     *
     * @param mode the {@code RunningMode} of this new start.
     */
    void onStart(RunningMode mode);

    /**
     * Called when the communicator receives a stop message.
     */
    void onStop();

    /**
     * Called when the communicator receives a pause message.
     */
    void onPause();

    /**
     * Called when the communicator receives a resume message.
     */
    void onResume();

    /**
     * Called when the communicator receives DONE on all the sent actions.
     */
    void onActionsDone();

    /**
     * Called when the communicator receives a new {@code Order}.
     *
     * @param order the newly issued {@code order}.
     */
    void onOrderIssue(Order order);

    /**
     * Called when the communicator receives an {@code Agent} activation.
     *
     * @param agent the activated {@code Agent}.
     */
    void onAgentActivate(Agent agent);

    /**
     * Called when the communicator receives an {@code Agent} deactivation.
     *
     * @param agent the deactivated {@code Agent}.
     */
    void onAgentDeactivate(Agent agent);

    /**
     * Called when the communicator receives an {@code Agent} blockage.
     *
     * @param agent the blocked {@code Agent}.
     */
    void onAgentBlocked(Agent agent);

    /**
     * Called when the communicator receives an {@code Agent} blockage cleared.
     *
     * @param agent the unblocked {@code Agent}.
     */
    void onAgentBlockageCleared(Agent agent);

    /**
     * Called when the communicator receives a change in the battery level
     * of an {@code Agent}.
     *
     * @param agent the {@code Agent}.
     * @param level the new battery level of this {@code Agent}.
     */
    void onAgentBatteryLevelChange(Agent agent, int level);
}
