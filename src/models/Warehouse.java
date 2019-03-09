package models;

import algorithms.Dispatcher;
import algorithms.Planner;
import models.components.Item;
import models.agents.Agent;
import models.components.Order;
import models.facilities.Station;
import models.facilities.Gate;
import models.facilities.Rack;
import models.map.MapGrid;
import utils.Constants.*;

import java.util.*;


public class Warehouse implements Order.OnFulFillListener {

    //
    // Member Variables
    //

    /**
     * The current time step in this warehouse.
     */
    private int time;

    /**
     * Warehouse map grid.
     */
    private MapGrid map;

    /**
     * Map of all agents in the warehouse, indexed by their id.
     */
    private Map<Integer, Agent> agents = new HashMap<>();

    /**
     * Queue of all currently active agents, sorted by their priority.
     */
    private Queue<Agent> activeAgents = new PriorityQueue<>();

    /**
     * Queue of all currently idle agents.
     */
    private Queue<Agent> readyAgents = new LinkedList<>();

    /**
     * Map of all sell items in the warehouse, indexed by their id.
     */
    private Map<Integer, Item> items = new HashMap<>();

    /**
     * Map of all racks in the warehouse, indexed by their id.
     */
    private Map<Integer, Rack> racks = new HashMap<>();

    /**
     * Map of all gates in the warehouse, indexed by their id.
     */
    private Map<Integer, Gate> gates = new HashMap<>();

    /**
     * Map of all charging stations in the warehouse, indexed by their id.
     */
    private Map<Integer, Station> stations = new HashMap<>();

    /**
     * Queue of pending and/or partially pending orders.
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
     * Configures the specifications of the warehouse space and components.
     *
     * @param data the un-parsed warehouse object.
     */
    public void configure(List<Object> data) {
        parseWarehouse(data);
        init();
    }

    /**
     * Adds a new order to the warehouse.
     *
     * @param data the un-parsed order object.
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
     * The callback function to be called when an order has been fulfilled.
     *
     * @param order the fulfilled order.
     */
    @Override
    public void onFulfill(Order order) {
        // TODO: send feed back to the front-end.
    }

    /**
     * Performs a single time step in this warehouse.
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
        // Compute guide map for every rack
        for (Rack rack : racks.values()) {
            rack.computeGuideMap(map);
        }

        // Compute guide map for every gate
        for (Gate gate : gates.values()) {
            gate.computeGuideMap(map);
        }
    }

    /**
     * Dispatches the current pending orders of the warehouse.
     */
    private void dispatchPendingOrders() throws Exception {
        //
        // Iterate over every pending order and tries to dispatch it
        //
        int size = pendingOrders.size();

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
     */
    private void stepActiveAgents() throws Exception {
        // TODO: bringBlank agent off its destination position after finishing the task
        //
        // Iterate over all active agents
        //
        int size = activeAgents.size();

        for (int i = 0; i < size; ++i) {
            Agent agent = activeAgents.poll();
            Planner.step(agent, map, time);

            if (agent.getStatus() == AgentStatus.IDLE) {
                readyAgents.add(agent);
            }
        }
    }

    // ===============================================================================================
    //
    // Parsing Methods
    //

    /**
     * Parses the configurations of the warehouse space and components,
     * and updates the internal corresponding member variables.
     *
     * @param data the un-parsed warehouse object.
     */
    private void parseWarehouse(List<Object> data) {

    }

    /**
     * Parses the given robot agent data.
     *
     * @param data the un-parsed agent object.
     *
     * @return an {@code Agent} object corresponding to the given specs.
     */
    private Agent parseAgent(List<Object> data) {
        return null;
    }

    /**
     * Parses the given item data.
     *
     * @param data the un-parsed item object.
     *
     * @return an {@code Item} object corresponding to the given specs.
     */
    private Item parseItem(List<Object> data) {
        return null;
    }

    /**
     * Parses the given rack data.
     *
     * @param data the un-parsed rack object.
     *
     * @return an {@code Rack} object corresponding to the given specs.
     */
    private Rack parseRack(List<Object> data) {
        return null;
    }

    /**
     * Parses the given gate data.
     *
     * @param data the un-parsed gate object.
     *
     * @return an {@code Gate} object corresponding to the given specs.
     */
    private Gate parseGate(List<Object> data) {
        return null;
    }

    /**
     * Parses the given charging station data.
     *
     * @param data the un-parsed charging station object.
     *
     * @return an {@code Station} object corresponding to the given specs.
     */
    private Station parseChargeStation(List<Object> data) {
        return null;
    }

    /**
     * Parses the given order data.
     *
     * @param data the un-parsed order object.
     *
     * @return an {@code Order} object corresponding to the given specs.
     */
    private Order parseOrder(List<Object> data) {
        return null;
    }
}
