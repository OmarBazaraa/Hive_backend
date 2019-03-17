package models.warehouses;

import algorithms.Dispatcher;
import algorithms.Planner;

import models.agents.Agent;
import models.facilities.Facility;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.maps.MapCell;
import models.maps.MapGrid;
import models.orders.Order;

import models.tasks.Task;
import utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private long time;

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
     * The set of all currently idle agents.
     */
    private Set<Agent> readyAgents = new HashSet<>();

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
    public void configure(JSONObject data) throws Exception {
        configureItems(data.getJSONArray(Constants.MSG_KEY_ITEMS));
        configureWarehouse(data.getJSONObject(Constants.MSG_KEY_MAP));
    }

    /**
     * Performs and simulates a single time step in this {@code Warehouse}.
     */
    public void run() throws Exception {
        dispatchPendingOrders();
        stepActiveAgents();
        time++;
    }

    /**
     * Adds a new {@code Order} to this {@code Warehouse} to be delivered.
     *
     * @param data the un-parsed {@code Order} data.
     */
    public void addOrder(JSONObject data) throws Exception {
        Order order = Order.create(data);

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

    /**
     * Adds and activates a new {@code Task} to this {@code Warehouse} after being dispatched
     * by {@link Dispatcher}.
     * <p></p>
     * This function should only be called from the {@link Dispatcher}.
     *
     * @param task the {@code Task} to add to the system.
     */
    public void addTask(Task task) throws Exception {
        // Activate the task
        task.fillItems();
        task.activate();

        // Update agents lists
        Agent agent = task.getAgent();
        if (readyAgents.contains(agent)) {
            readyAgents.remove(agent);
            activeAgents.add(agent);
        }
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Dispatches the current pending orders of this {@code Warehouse}.
     *
     * TODO: check agent to rack reach-ability
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
    private void stepActiveAgents() throws Exception {
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

    // ===============================================================================================
    //
    // Getter Methods
    //

    /**
     * Returns the current time step in this {@code Warehouse} object.
     *
     * @return the current time step of this {@code Warehouse}.
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns the map grid of this {@code Warehouse} object.
     *
     * TODO: prevent editing the map from outside the warehouse
     *
     * @return the {@code MapGrid} of this {@code Warehouse}.
     */
    public MapGrid getMap() {
        return map;
    }

    /**
     * Returns the {@code Agent} object with the given id.
     *
     * @param id the id of the needed {@code Agent}.
     *
     * @return the needed {@code Agent} if available; {@code null} otherwise.
     */
    public Agent getAgentById(int id) {
        return agents.get(id);
    }

    /**
     * Returns the {@code Rack} object with the given id.
     *
     * @param id the id of the needed {@code Rack}.
     *
     * @return the needed {@code Rack} if available; {@code null} otherwise.
     */
    public Rack getRackById(int id) {
        return racks.get(id);
    }

    /**
     * Returns the {@code Gate} object with the given id.
     *
     * @param id the id of the needed {@code Gate}.
     *
     * @return the needed {@code Gate} if available; {@code null} otherwise.
     */
    public Gate getGateById(int id) {
        return gates.get(id);
    }

    /**
     * Returns the {@code Station} object with the given id.
     *
     * @param id the id of the needed {@code Station}.
     *
     * @return the needed {@code Station} if available; {@code null} otherwise.
     */
    public Station getStationById(int id) {
        return stations.get(id);
    }

    /**
     * Returns the {@code Item} object with the given id.
     *
     * @param id the id of the needed {@code Item}.
     *
     * @return the needed {@code Item} if available; {@code null} otherwise.
     */
    public Item getItemById(int id) {
        return items.get(id);
    }

    // ===============================================================================================
    //
    // Parsing & Initializing Methods
    //

    /**
     * Configures the space and components of this {@code Warehouse},
     * and updates the internal corresponding member variables.
     *
     * @param data the un-parsed {@code Warehouse} data.
     */
    private void configureWarehouse(JSONObject data) throws Exception {
        map = MapGrid.create(data);

        for (int i = 0; i < map.getRows(); ++i) {
            for (int j = 0; j < map.getCols(); ++j) {
                MapCell cell = map.get(i, j);

                Agent agent = cell.getAgent();

                if (agent != null) {
                    agents.put(agent.getId(), agent);
                    readyAgents.add(agent);
                }

                Facility facility = cell.getFacility();

                if (facility != null) {
                    facility.computeGuideMap(map);

                    switch (cell.getType()) {
                        case RACK:
                            racks.put(facility.getId(), (Rack) facility);
                            break;
                        case GATE:
                            gates.put(facility.getId(), (Gate) facility);
                            break;
                        case STATION:
                            stations.put(facility.getId(), (Station) facility);
                            break;
                    }
                }
            }
        }
    }

    /**
     * Configures the items of this {@code Warehouse},
     * and updates the internal corresponding member variables.
     *
     * @param data the un-parsed {@code Items} data.
     */
    private void configureItems(JSONArray data) throws Exception {
        for (int i = 0; i < data.length(); ++i) {
            Item item = Item.create(data.getJSONObject(i));
            items.put(item.getId(), item);
        }
    }
}
