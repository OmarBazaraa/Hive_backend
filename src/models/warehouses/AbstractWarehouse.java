package models.warehouses;

import algorithms.dispatcher.Dispatcher;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
import models.maps.GridCell;
import models.maps.utils.Dimensions;
import models.maps.utils.Position;
import models.tasks.Task;
import models.tasks.orders.Order;

import utils.Constants.*;

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
     * The number of rows of this {@code Warehouse}.
     */
    protected int rows;

    /**
     * The number of columns of this {@code Warehouse}.
     */
    protected int cols;

    /**
     * The map grid of this {@code Warehouse}.
     */
    protected GridCell[][] grid;

    /**
     * The map of all agents in this {@code Warehouse}, indexed by their id.
     */
    protected Map<Integer, Agent> agents = new HashMap<>();

    /**
     * The set of all currently active agents, sorted by their priority.
     */
    protected TreeSet<Agent> activeAgents = new TreeSet<>(Collections.reverseOrder());

    /**
     * The set of all currently idle agents.
     */
    protected Set<Agent> readyAgents = new HashSet<>();

    /**
     * The set of currently blocked agents.
     */
    protected Queue<Agent> blockedAgents = new LinkedList<>();

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
     * Configures and allocates a new empty grid for the {@code Warehouse}
     * with the given dimensions and clears the previous state of
     * the {@code Warehouse} completely.
     *
     * @param rows the number of rows of this {@code Warehouse}.
     * @param cols the number of columns of this {@code Warehouse}.
     */
    public void configure(int rows, int cols) {
        this.clear();
        this.rows = rows;
        this.cols = cols;
        this.grid = GridCell.allocate2D(rows, cols);
    }

    /**
     * Clears the {@code Warehouse} and removes all its components.
     */
    public void clear() {
        time = 0;
        rows = cols;
        grid = null;
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
     * Retreats the blocked agents to their normal state if possible.
     */
    abstract protected void retreatBlockedAgents();

    /**
     * Moves the active agents one step towards their targets.
     */
    abstract protected void advanceActiveAgents();


    // ===============================================================================================
    //
    // Callback Methods
    //

    /**
     * A callback function to be invoked when an {@code Agent} get blocked.
     *
     * @param agent the blocked {@code Agent}.
     */
    public void onAgentBlocked(Agent agent) {
        readyAgents.remove(agent);
        activeAgents.remove(agent);
        blockedAgents.add(agent);
    }

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
     * Returns the number of rows of this {@code Warehouse}.
     *
     * @return an integer representing the number of rows in the {@code Warehouse} grid.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns in this {@code Warehouse}.
     *
     * @return an integer representing the number of columns in the {@code Warehouse} grid.
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns the dimensions of this {@code Warehouse}.
     *
     * @return a {@code Dimensions} object representing the dimensions of the {@code Warehouse}.
     */
    public Dimensions getDimensions() {
        return new Dimensions(rows, cols);
    }

    /**
     * Checks whether a cell is inside the boundaries of this {@code Warehouse} or not.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     *
     * @return {@code true} if the cell is inside the grid; {@code false} otherwise.
     */
    public boolean isInBound(int row, int col) {
        return 0 <= row && row < rows && 0 <= col && col < cols;
    }

    /**
     * Checks whether a cell is outside the boundaries of this {@code Warehouse} or not.
     *
     * @param row the row position of the cell to check.
     * @param col the column position of the cell to check.
     *
     * @return {@code true} if the cell is outside the grid; {@code false} otherwise.
     */
    public boolean isOutBound(int row, int col) {
        return 0 > row || row >= rows || 0 > col || col >= cols;
    }

    /**
     * Returns a {@code GridCell} given its position in this {@code Warehouse}.
     *
     * @param row the row position of the cell to return.
     * @param col the column position of the cell to return.
     *
     * @return the {@code GridCell} in the given position.
     */
    public GridCell get(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns a {@code GridCell} given its position in this {@code Warehouse}.
     *
     * @param pos the {@code Position} of the cell to return.
     *
     * @return the {@code GridCell} in the given position.
     */
    public GridCell get(Position pos) {
        return grid[pos.row][pos.col];
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
     * Returns the list of all {@code Agent} objects in this {@code Warehouse}.
     *
     * @return a collection of all agents.
     */
    public Collection<Agent> getAgentList() {
        return agents.values();
    }

    /**
     * Adds a new {@code Agent} object to the {@code Warehouse}.
     *
     * @param agent the new {@code Agent} to add.
     * @param row   the row position of the cell to add into.
     * @param col   the column position of the cell to add into.
     */
    public void addAgent(Agent agent, int row, int col) {
        agent.setPosition(row, col);
        grid[row][col].setAgent(agent);
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
     * @param row  the row position of the cell to add into.
     * @param col  the column position of the cell to add into.
     */
    public void addRack(Rack rack, int row, int col) {
        rack.setPosition(row, col);
        grid[row][col].setFacility(CellType.RACK, rack);
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
     * @param row  the row position of the cell to add into.
     * @param col  the column position of the cell to add into.
     */
    public void addGate(Gate gate, int row, int col) {
        gate.setPosition(row, col);
        grid[row][col].setFacility(CellType.GATE, gate);
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
     * @param row     the row position of the cell to add into.
     * @param col     the column position of the cell to add into.
     */
    public void addStation(Station station, int row, int col) {
        station.setPosition(row, col);
        grid[row][col].setFacility(CellType.STATION, station);
        stations.put(station.getId(), station);
    }

    /**
     * Adds a new obstacle object to the {@code Warehouse}.
     *
     * @param row the row position of the cell to add into.
     * @param col the column position of the cell to add into.
     */
    public void addObstacle(int row, int col) {
        grid[row][col].setFacility(CellType.OBSTACLE, null);
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

    /**
     * Adds and activates a new {@code Task} to this {@code Warehouse} after being dispatched
     * by {@link Dispatcher}.
     * <p>
     * This function should only be called from the {@link Dispatcher}.
     *
     * @param task the {@code Task} to add to the system.
     */
    abstract public void addTask(Task task);

    // ===============================================================================================
    //
    // Helper Methods
    //

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

    /**
     * Returns a string representation of this {@code Warehouse}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Warehouse}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Warehouse ").append(getDimensions());
        builder.append(" @time: ").append(time).append("\n");

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                builder.append(grid[i][j].toShape());
            }
            builder.append('\n');
        }

        return builder.toString();
    }
}
