package models.warehouses;

import algorithms.Dispatcher;
import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.maps.TimeGrid;
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
     * Initializes the {@code Warehouse}, and performs any needed pre-computations.
     */
    @Override
    public void init() {
        // Initializes the time map
        timeMap = new TimeGrid(map.getRows(), map.getCols());

        // Initializes the guide maps
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
    public void addTask(Task task) {
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
    protected void dispatchPendingOrders() {
        // Get the initial size of the queue
        int size = pendingOrders.size();

        //
        // Iterate over every pending order and tries to dispatch it
        //
        for (int i = 0; i < size && !readyAgents.isEmpty(); ++i) {
            // Get the current order
            Order order = pendingOrders.remove();

            // Try dispatching the current order if its time comes
            if (time >= order.getStartTime()) {
                Dispatcher.dispatch(order, readyAgents);
            }

            // Re-add the order to the end of the queue if still pending
            if (order.isPending()) {
                pendingOrders.add(order);
            }
        }
    }

    /**
     * Moves the active agents one step towards their targets.
     */
    @Override
    protected void moveActiveAgents() {
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
