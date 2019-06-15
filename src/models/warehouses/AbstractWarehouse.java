package models.warehouses;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.maps.MapCell;
import models.maps.MapGrid;
import models.maps.TimeGrid;
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
     * The timeline map of the {@code Warehouse}.
     */
    protected TimeGrid timeMap;

    /**
     * The map of all agents in this {@code Warehouse}, indexed by their id.
     */
    protected Map<Integer, Agent> agents = new HashMap<>();

    /**
     * The queue of all currently active agents, sorted by their priority.
     */
    protected Queue<Agent> activeAgents = new PriorityQueue<>();

    /**
     * The set of all currently idle agents.
     */
    protected Set<Agent> readyAgents = new HashSet<>();

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

    /**
     * The queue of pending and not fully dispatched orders.
     */
    protected Queue<Order> pendingOrders = new LinkedList<>();

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
        timeMap = null;
        agents.clear();
        activeAgents.clear();
        readyAgents.clear();
        racks.clear();
        gates.clear();
        stations.clear();
        items.clear();
        orders.clear();
        pendingOrders.clear();
    }

    /**
     * Initializes the {@code Warehouse}, and performs any needed pre-computations.
     */
    abstract public void init();

    /**
     * Performs and simulates a single time step in this {@code Warehouse}.
     *
     * @return {@code true} if a change has happen in the {@code Warehouse}; {@code false} otherwise.
     */
    abstract public boolean run();

    /**
     * Dispatches the current pending orders of this {@code Warehouse}.
     */
    abstract protected void dispatchPendingOrders();

    /**
     * Moves the active agents one step towards their targets.
     */
    abstract protected void advanceActiveAgents();

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
     * Returns the timeline map of this {@code Warehouse} object.
     *
     * @return the timeline map of this {@code Warehouse}.
     */
    public TimeGrid getTimeMap() {
        return timeMap;
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
        readyAgents.add(agent);
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
        pendingOrders.add(order);
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Returns a string representation of this {@code Warehouse}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Warehouse}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Warehouse of size: ")
                .append(map.getRows()).append("x").append(map.getCols())
                .append(" @time: ").append(time).append("\n")
                .append(map);

        return builder.toString();
    }

    /**
     * Prints a visual representation of this {@code Warehouse} along with
     * all its components.
     */
    public void print() {
        StringBuilder builder = new StringBuilder();

        // Print the grid
        builder.append(this).append("\n");

        // Print the list of agents
        builder.append("Agents: ").append(agents.size()).append("\n");
        for (Agent agent : agents.values()) {
            builder.append("    > ").append(agent).append("\n");
        }
        builder.append("\n");

        // Print the list of racks
        builder.append("Racks: ").append(racks.size()).append("\n");
        for (Rack rack : racks.values()) {
            builder.append("    > ").append(rack).append("\n");
        }
        builder.append("\n");

        // Print the list of gates
        builder.append("Gates: ").append(gates.size()).append("\n");
        for (Gate gate : gates.values()) {
            builder.append("    > ").append(gate).append("\n");
        }
        builder.append("\n");

        // Print the list of stations
        builder.append("Stations: ").append(stations.size()).append("\n");
        for (Station station : stations.values()) {
            builder.append("    > ").append(station).append("\n");
        }
        builder.append("\n");

        // Print the list of items
        builder.append("Items: ").append(items.size()).append("\n");
        for (Item item : items.values()) {
            builder.append("    > ").append(item).append("\n");
        }

        // Print to standard output
        System.out.println(builder);
    }
}
