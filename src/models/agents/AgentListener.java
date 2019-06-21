package models.agents;

import utils.Constants;


/**
 * Interface definition for all {@code Agent} listeners for any occurring events
 * related to an {@code Agent}.
 */
public interface AgentListener {

    /**
     * Called when an {@code Agent} has performed an action.
     *
     * @param agent  the {@code Agent}.
     * @param action the action done by this {@code Agent}.
     */
    void onAgentActionExecuted(Agent agent, Constants.AgentAction action);

    /**
     * Called when an {@code Agent} has been activated.
     *
     * @param agent the activated {@code Agent}.
     */
    void onAgentActivated(Agent agent);

    /**
     * Called when an {@code Agent} has been deactivated.
     *
     * @param agent the deactivated {@code Agent}.
     */
    void onAgentDeactivated(Agent agent);

    /**
     * Called when an {@code Agent} has been blocked.
     *
     * @param agent the blocked {@code Agent}.
     */
    void onAgentBlocked(Agent agent);
}
