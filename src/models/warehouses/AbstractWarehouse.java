package models.warehouses;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.maps.MapCell;
import models.maps.MapGrid;
import models.tasks.Order;

import java.util.*;


/**
 * This {@code AbstractWarehouse} class is the base class of all the warehouses
 * in our Hive Warehouse System.
 *
 * @see Warehouse
 */
abstract public class AbstractWarehouse {

    //
    // Member Variables
    //

    /**
     * The current time step in this {@code Warehouse}.
     * Needed for simulation purposes.
     */
    protected long time;

    /**
     * The map grid of this {@code Warehouse}.
     */
    protected MapGrid map;

    /**
     * The map of all agents in this {@code Warehouse}, indexed by their id.
     */
    protected Map<Integer, Agent> agents = new HashMap<>();

    /**
     * The map of all racks in this {@code Warehouse}, indexed by their id.
     */
    protected Map<Integer, Rack> racks = new HashMap<>();

    /**
     * The map of all gates in this {@code Warehouse}, indexed by their id.
     */
    protected Map<Integer, Gate> gates = new HashMap<>();

    /**
     * The map of all charging stations in this {@code Warehouse}, indexed by their id.
     */
    protected Map<Integer, Station> stations = new HashMap<>();

    /**
     * The map of all items in this {@code Warehouse}, indexed by their id.
     */
    protected Map<Integer, Item> items = new HashMap<>();

    /**
     * The map of all orders in this {@code Warehouse}, indexed by their id.
     */
    protected Map<Integer, Order> orders = new HashMap<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Clears the {@code Warehouse} and removes all its components.
     */
    public void clear() {
        time = 0;
        map = null;
        agents.clear();
        racks.clear();
        gates.clear();
        stations.clear();
        items.clear();
        orders.clear();
    }

    /**
     * Initializes the {@code Warehouse}, and performs any needed pre-computations.
     */
    abstract public void init();

    /**
     * Performs and simulates a single time step in this {@code Warehouse}.
     */
    public void run() throws Exception {
        dispatchPendingOrders();
        moveActiveAgents();
        time++;
    }

    /**
     * Dispatches the current pending orders of this {@code Warehouse}.
     */
    abstract protected void dispatchPendingOrders() throws Exception;

    /**
     * Moves the active agents one step towards their targets.
     */
    abstract protected void moveActiveAgents() throws Exception;

    // ===============================================================================================
    //
    // Getters & Setters
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
     * @return the {@code MapGrid} of this {@code Warehouse}.
     */
    public MapGrid getMap() {
        return map;
    }

    /**
     * Updates the grid map of this {@code Warehouse} object,
     * and clears all previous components.
     *
     * @param grid the new map grid.
     */
    public void updateMap(MapCell[][] grid) {
        map = new MapGrid(grid);
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
     * Adds a new {@code Agent} object to the {@code Warehouse}.
     *
     * @param agent the new {@code Agent} to add.
     */
    public void addAgent(Agent agent) {
        agents.put(agent.getId(), agent);
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
     * Adds a new {@code Rack} object to the {@code Warehouse}.
     *
     * @param rack the new {@code Rack} to add.
     */
    public void addRack(Rack rack) {
        racks.put(rack.getId(), rack);
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
     * Adds a new {@code Gate} object to the {@code Warehouse}.
     *
     * @param gate the new {@code Gate} to add.
     */
    public void addGate(Gate gate) {
        gates.put(gate.getId(), gate);
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
     * Adds a new {@code Station} object to the {@code Warehouse}.
     *
     * @param station the new {@code Station} to add.
     */
    public void addStation(Station station) {
        stations.put(station.getId(), station);
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

    /**
     * Adds a new {@code Item} type to the {@code Warehouse}.
     *
     * @param item the new {@code Item} to add.
     */
    public void addItem(Item item) {
        items.put(item.getId(), item);
    }

    /**
     * Returns the {@code Order} with the given id.
     *
     * @param id the id of the needed {@code Order}.
     *
     * @return the needed {@code Order} if available; {@code null} otherwise.
     */
    public Order getOrderById(int id) {
        return orders.get(id);
    }

    /**
     * Adds a new {@code Order} to the {@code Warehouse} to be delivered.
     *
     * @param order the {@code Order} to be added.
     */
    public void addOrder(Order order) {
        orders.put(order.getId(), order);
    }
}
