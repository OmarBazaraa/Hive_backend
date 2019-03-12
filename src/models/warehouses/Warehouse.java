package models.warehouses;

import algorithms.Dispatcher;
import algorithms.Planner;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.maps.MapGrid;
import models.orders.Order;

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
     * The map of all agents in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Agent> agents = new HashMap<>();

    /**
     * The queue of all currently active agents, sorted by their priority.
     */
    private Queue<Agent> activeAgents = new PriorityQueue<>();

    /**
     * The queue of all currently idle agents.
     */
    private Queue<Agent> readyAgents = new LinkedList<>();

    /**
     * The map of all items in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Item> items = new HashMap<>();

    /**
     * The map of all racks in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Rack> racks = new HashMap<>();

    /**
     * The map of all gates in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Gate> gates = new HashMap<>();

    /**
     * The map of all charging stations in this {@code Warehouse}, indexed by their id.
     */
    private Map<Integer, Station> stations = new HashMap<>();

    /**
     * The queue of pending (not fully dispatched) orders.
     */
    private Queue<Order> pendingOrders = new LinkedList<>();

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

    /**
     * Returns the map grid of the singleton {@code Warehouse} object.
     *
     * TODO: prevent editing the map from outside the warehouse
     *
     * @return the {@code MapGrid} of the {@code Warehouse}.
     */
    public static MapGrid getMap() {
        return sWarehouse.map;
    }

    /**
     * Returns the current time step in the singleton {@code Warehouse} object.
     *
     * @return the current time step of the {@code Warehouse}.
     */
    public static int getTime() {
        return sWarehouse.time;
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
     * Configures the specifications of this {@code Warehouse}'s space and components.
     *
     * @param data the un-parsed {@code Warehouse} data.
     */
    public void configure(List<Object> data) {
        parseWarehouse(data);
        init();
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

    /**
     * Adds a new {@code Order} to this {@code Warehouse} to be delivered.
     *
     * @param data the un-parsed {@code Order} data.
     */
    public void addOrder(List<Object> data) throws Exception {
        Order order = parseOrder(data);

        if (order.isFeasible()) {
            order.activate();
            order.setOnFulfillListener(this);
            pendingOrders.add(order);
        } else {
            // TODO: add log message
        }
    }

    /**
     * The callback function to be invoked when an {@code Order} is fulfilled.
     *
     * @param order the fulfilled {@code Order}.
     */
    @Override
    public void onOrderFulfill(Order order) {
        // TODO: send feed back to the front-end.
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Dispatches the current pending orders of this {@code Warehouse}.
     *
     * TODO: check agent to rack reachability
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
     * Moves the active agents one step towards their targets.
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

    /**
     * Initializes and pre-computes some required values.
     */
    private void init() {
        // Compute guide map for every rack
        for (Rack rack : racks.values()) {
            rack.computeGuideMap(map);
        }

        // Compute guide map for every gate
        for (Gate gate : gates.values()) {
            gate.computeGuideMap(map);
        }

        // Compute guide map for every station
        for (Station station : stations.values()) {
            station.computeGuideMap(map);
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
