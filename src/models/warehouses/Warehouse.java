package models.warehouses;

import algorithms.dispatcher.Dispatcher;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.tasks.orders.Order;
import models.tasks.Task;

import java.util.*;


/**
 * This {@code Warehouse} class is considered the main controller of our Hive Warehouse System.
 * <p>
 * It contains required functions to simulate the process inside an automated smart warehouse.
 */
public class Warehouse extends AbstractWarehouse {

    //
    // Static Variables & Methods
    //

    /**
     * The only instance of this {@code Warehouse} class.
     */
    private static Warehouse sWarehouse = new Warehouse();

    /**
     * Returns the only available instance of this {@code Warehouse} class.
     *
     * @return the only available {@code Warehouse} object.
     */
    public static Warehouse getInstance() {
        return sWarehouse;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Warehouse} object.
     */
    protected Warehouse() {
        // Protected constructor to ensure a singleton object.
    }

    /**
     * Initializes the {@code Warehouse}, and performs any needed pre-computations.
     */
    @Override
    public void init() {
        // Initializes the guide maps
        for (Rack rack : racks.values()) {
            rack.computeGuideMap();
        }
        for (Gate gate : gates.values()) {
            gate.computeGuideMap();
        }
        for (Station station : stations.values()) {
            station.computeGuideMap();
        }
    }

    /**
     * Adds a new {@code Order} to this {@code Warehouse} to be delivered.
     *
     * @param order the {@code Order} to be added.
     */
    @Override
    public void addOrder(Order order) {
        super.addOrder(order);
        order.activate();
    }

    /**
     * Adds and activates a new {@code Task} to this {@code Warehouse} after being dispatched
     * by {@link Dispatcher}.
     * <p>
     * This function should only be called from the {@link Dispatcher}.
     *
     * @param task the {@code Task} to add to the system.
     */
    @Override
    public void addTask(Task task) {
        // Activate the task
        task.activate();

        //
        // Update agents lists
        //
        Agent agent = task.getAgent();

        if (readyAgents.contains(agent)) {
            readyAgents.remove(agent);
            activeAgents.add(agent);
        }
    }

    /**
     * Performs and simulates a single time step in this {@code Warehouse}.
     *
     * @return {@code true} if a change has happen in the {@code Warehouse}; {@code false} otherwise.
     */
    @Override
    public boolean run() {
        // Skip if warehouse is currently idle
        if (pendingOrders.isEmpty() && activeAgents.isEmpty() && blockedAgents.isEmpty()) {
            return false;
        }

        // Do a single run and increment time
        dispatchPendingOrders();
        retreatBlockedAgents();
        advanceActiveAgents();
        time++;
        return true;
    }

    /**
     * Dispatches the current pending orders of this {@code Warehouse}.
     */
    @Override
    protected void dispatchPendingOrders() {
        // Skip if no pending orders
        if (pendingOrders.isEmpty() || agents.isEmpty()) {
            return;
        }

        // Get the initial size of the queue
        int size = pendingOrders.size();

        //
        // Iterate over every pending order and tries to dispatch it
        //
        for (int i = 0; i < size; ++i) {
            // Get the current order
            Order order = pendingOrders.remove();

            // Try dispatching the current order
            Dispatcher.dispatch(order, readyAgents);

            // Re-add the order to the end of the queue if still pending
            if (order.isPending()) {
                pendingOrders.add(order);
            }
        }
    }

    /**
     * Retreats the blocked agents to their normal state if possible.
     */
    @Override
    protected void retreatBlockedAgents() {
        // Get the initial size of the queue
        int size = blockedAgents.size();

        //
        // Iterate over every blocked agent and tries to retreat it
        //
        for (int i = 0; i < size; ++i) {
            // Get the current blocked agent
            Agent agent = blockedAgents.remove();

            // Try retreating the current agent
            agent.retreat();

            // Re-add the agent to the end of the queue if still blocked
            if (agent.isBlocked()) {
                blockedAgents.add(agent);
            }
        }
    }

    /**
     * Moves the active agents one step towards their targets.
     */
    @Override
    protected void advanceActiveAgents() {
        // Skip if no active agents
        if (activeAgents.isEmpty()) {
            return;
        }

        // Get the initial size of the queue
        int size = activeAgents.size();

        // Create another queue of agents
        TreeSet<Agent> q = new TreeSet<>(Collections.reverseOrder());

        //
        // Iterate over all active agents
        //
        for (int i = 0; i < size; ++i) {
            // Get current active agent
            Agent agent = activeAgents.pollFirst();

            // Try moving the current agent towards its target
            agent.executeAction();

            // Re-add agent to the active queue if still active, otherwise add it to the ready queue
            if (agent.isActive()) {
                q.add(agent);
            } else {
                readyAgents.add(agent);
            }
        }

        // Update active agents queue
        activeAgents = q;
    }
}
