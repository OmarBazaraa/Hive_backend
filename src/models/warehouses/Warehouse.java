package models.warehouses;

import algorithms.Dispatcher;
import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.tasks.Order;
import models.tasks.Task;

import java.util.*;


/**
 * This {@code Warehouse} class is considered the main controller of our Hive Warehouse System.
 * <p>
 * It contains required functions to simulate the process inside an automated smart warehouse.
 */
public class Warehouse extends AbstractWarehouse {

    //
    // Member Variables
    //

    /**
     * The queue of all currently active agents, sorted by their priority.
     */
    protected Queue<Agent> activeAgents = new PriorityQueue<>();

    /**
     * The set of all currently idle agents.
     */
    protected Set<Agent> readyAgents = new HashSet<>();

    /**
     * The queue of pending and not fully dispatched orders.
     */
    protected Queue<Order> pendingOrders = new LinkedList<>();

    // ===============================================================================================
    //
    // Static Methods
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
    private Warehouse() {
        // Private constructor to ensure a singleton object.
    }

    /**
     * Clears the {@code Warehouse} and removes all its components.
     */
    @Override
    public void clear() {
        super.clear();
        activeAgents.clear();
        readyAgents.clear();
        pendingOrders.clear();
    }

    /**
     * Initializes the {@code Warehouse}, and performs any needed pre-computations.
     */
    @Override
    public void init() {
        for (Rack rack : racks.values()) {
            rack.computeGuideMap(map);
        }
        for (Gate gate : gates.values()) {
            gate.computeGuideMap(map);
        }
        for (Station station : stations.values()) {
            station.computeGuideMap(map);
        }
    }

    /**
     * Adds a new {@code Agent} object to the {@code Warehouse}.
     *
     * @param agent the new {@code Agent} to add.
     */
    @Override
    public void addAgent(Agent agent) {
        super.addAgent(agent);
        readyAgents.add(agent);
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
        pendingOrders.add(order);
    }

    /**
     * Adds and activates a new {@code Task} to this {@code Warehouse} after being dispatched
     * by {@link Dispatcher}.
     * <p>
     * This function should only be called from the {@link Dispatcher}.
     *
     * @param task the {@code Task} to add to the system.
     */
    public void addTask(Task task) throws Exception {
        // Activate the task
        task.activate();

        // Update agents lists
        Agent agent = task.getAgent();

        if (readyAgents.contains(agent)) {
            readyAgents.remove(agent);
            activeAgents.add(agent);
        }
    }

    /**
     * Dispatches the current pending orders of this {@code Warehouse}.
     *
     * TODO: check agent to rack reach-ability
     */
    @Override
    protected void dispatchPendingOrders() throws Exception {
        // Get the initial size of the queue
        int size = pendingOrders.size();

        //
        // Iterate over every pending order and tries to dispatch it
        //
        for (int i = 0; i < size && !readyAgents.isEmpty(); ++i) {
            // Get the current order
            Order order = pendingOrders.poll();

            // Try dispatching the current order
            Dispatcher.dispatch(order, readyAgents);

            // Re-add the order to the queue if still pending
            if (order.isPending()) {
                pendingOrders.add(order);
            }
        }
    }

    /**
     * Moves the active agents one step towards their targets.
     */
    @Override
    protected void moveActiveAgents() throws Exception {
        // Get the initial size of the queue
        int size = activeAgents.size();

        // Create another queue of agents
        Queue<Agent> q = new PriorityQueue<>(size);

        //
        // Iterate over all active agents
        //
        for (int i = 0; i < size; ++i) {
            // Get current active agent
            Agent agent = activeAgents.remove();

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
