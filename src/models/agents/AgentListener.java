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
    void onAction(Agent agent, Constants.AgentAction action);

    /**
     * Called when the battery level of an {@code Agent} has changed.
     *
     * @param agent the {@code Agent}.
     * @param level the new battery level of this {@code Agent}.
     */
    void onBatteryLevelChange(Agent agent, int level);

    /**
     * Called when an {@code Agent} has been activated.
     *
     * @param agent the activated {@code Agent}.
     */
    void onActivate(Agent agent);

    /**
     * Called when an {@code Agent} has been deactivated.
     *
     * @param agent the deactivated {@code Agent}.
     */
    void onDeactivate(Agent agent);

    /**
     * Called when an {@code Agent} has been blocked.
     *
     * @param agent the blocked {@code Agent}.
     */
    void onBlock(Agent agent);
}
