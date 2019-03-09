package models.warehouses;

import algorithms.Dispatcher;
import algorithms.Planner;

import models.agents.Agent;
import models.components.Item;
import models.components.Order;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.maps.MapGrid;

import java.util.*;


/**
 * This {@code Warehouse} class is considered the main controller of our Hive Warehouse System.
 * <p>
 * It contains required functions to simulate the process inside an automated smart warehouse.
 */
public class Warehouse implements Order.OnFulFillListener {

    //
    // Member Variables
    //

    /**
     * The current time step in this {@code Warehouse}.
     * Needed to simulation purposes.
     */
    private int time;

    /**
     * The map grid of this {@code Warehouse}.
     */
    private MapGrid map;

    /**
     * The map of all {@code Agent}s in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Agent> agents = new HashMap<>();

    /**
     * The queue of all currently active {@code Agent}s, sorted by their priority.
     */
    private Queue<Agent> activeAgents = new PriorityQueue<>();

    /**
     * The queue of all currently idle {@code Agent}s.
     */
    private Queue<Agent> readyAgents = new LinkedList<>();

    /**
     * The map of all {@code Item}s in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Item> items = new HashMap<>();

    /**
     * The map of all {@code Rack}s in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Rack> racks = new HashMap<>();

    /**
     * The map of all {@code Gate}s in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Gate> gates = new HashMap<>();

    /**
     * The map of all charging {@code Station}s in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Station> stations = new HashMap<>();

    /**
     * The queue of pending (not fully dispatched) {@code Order}s.
     */
    private Queue<Order> pendingOrders = new LinkedList<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Warehouse} object.
     */
    public Warehouse() {

    }

    /**
     * Configures the specifications of this {@code Warehouse}'s space and components.
     *
     * @param data the un-parsed {@code Warehouse} data.
     */
    public void configure(List<Object> data) {
        parseWarehouse(data);
        init();
    }

    /**
     * Adds a new {@code Order} to this {@code Warehouse} to be delivered.
     *
     * @param data the un-parsed {@code Order} data.
     */
    public void addOrder(List<Object> data) throws Exception {
        Order order = parseOrder(data);

        if (order.isFeasible()) {
            order.reserve();
            order.setOnFulfillListener(this);
            pendingOrders.add(order);
        } else {
            // TODO: add log message
        }
    }

    /**
     * The callback function to be called when an {@code Order} has been fulfilled.
     *
     * @param order the fulfilled {@code Order}.
     */
    @Override
    public void onOrderFulfill(Order order) {
        // TODO: send feed back to the front-end.
    }

    /**
     * Performs and simulates a single time step in this {@code Warehouse}.
     */
    public void run() throws Exception {
        // Dispatch pending orders
        dispatchPendingOrders();

        // Move active agents one step toward their targets
        stepActiveAgents();

        // Increment time step
        time++;
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Initializes and pre-computes some required values.
     */
    private void init() {
        // Compute guide maps for every rack
        for (Rack rack : racks.values()) {
            rack.computeGuideMap(map);
        }

        // Compute guide maps for every gate
        for (Gate gate : gates.values()) {
            gate.computeGuideMap(map);
        }
    }

    /**
     * Dispatches the current pending {@code Order}s of this {@code Warehouse}.
     */
    private void dispatchPendingOrders() throws Exception {
        // Get the initial size of the queue
        int size = pendingOrders.size();

        //
        // Iterate over every pending order and tries to dispatch it
        //
        for (int i = 0; i < size && !readyAgents.isEmpty(); ++i) {
            // Get the current order
            Order order = pendingOrders.poll();

            // Try dispatching the current order
            Dispatcher.dispatch(order, readyAgents, activeAgents);

            // Re-add the order to the queue if still pending
            if (order.isPending()) {
                pendingOrders.add(order);
            }
        }
    }

    /**
     * Moves the active {@code Agent}s one step towards their targets.
     *
     * TODO: move agent off its destination position after finishing the task
     */
    private void stepActiveAgents() throws Exception {
        // Get the initial size of the queue
        int size = activeAgents.size();

        //
        // Iterate over all active agents
        //
        for (int i = 0; i < size; ++i) {
            // Get current active agent
            Agent agent = activeAgents.poll();

            // Try moving the current agent towards its target
            Planner.step(agent, map, time);

            // Re-add agent to the active queue if still active, otherwise add it to the ready queue
            if (agent.isActive()) {
                // TODO: edit agent comparator function to be function of last action time step
                activeAgents.add(agent);
            } else {
                readyAgents.add(agent);
            }
        }
    }

    // ===============================================================================================
    //
    // Parsing Methods
    //

    /**
     * Parses the configurations of the {@code Warehouse} space and components,
     * and updates the internal corresponding member variables.
     *
     * @param data the un-parsed {@code Warehouse} data.
     */
    private void parseWarehouse(List<Object> data) {

    }

    /**
     * Parses the given data representing an {@code Agent}.
     *
     * @param data the un-parsed {@code Agent} object.
     *
     * @return an {@code Agent} object corresponding to the given specs.
     */
    private Agent parseAgent(List<Object> data) {
        return null;
    }

    /**
     * Parses the given data representing an {@code Item}.
     *
     * @param data the un-parsed {@code Item} data.
     *
     * @return an {@code Item} object corresponding to the given specs.
     */
    private Item parseItem(List<Object> data) {
        return null;
    }

    /**
     * Parses the given data representing a {@code Rack}.
     *
     * @param data the un-parsed {@code Rack} data.
     *
     * @return an {@code Rack} object corresponding to the given specs.
     */
    private Rack parseRack(List<Object> data) {
        return null;
    }

    /**
     * Parses the given data representing a {@code Gate}.
     *
     * @param data the un-parsed {@code Gate} data.
     *
     * @return an {@code Gate} object corresponding to the given specs.
     */
    private Gate parseGate(List<Object> data) {
        return null;
    }

    /**
     * Parses the given data representing a charging {@code Station}.
     *
     * @param data the un-parsed charging {@code Station} data.
     *
     * @return an {@code Station} object corresponding to the given specs.
     */
    private Station parseChargeStation(List<Object> data) {
        return null;
    }

    /**
     * Parses the given data representing an {@code Order}.
     *
     * @param data the un-parsed {@code Order} data.
     *
     * @return an {@code Order} object corresponding to the given specs.
     */
    private Order parseOrder(List<Object> data) {
        return null;
    }
}
