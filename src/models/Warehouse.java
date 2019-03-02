package models;

import models.components.Item;
import models.components.Agent;
import models.components.Order;
import models.components.Station;
import models.components.Gate;
import models.components.Rack;
import models.components.Task;
import models.map.Grid;

import java.util.*;


public class Warehouse {

    //
    // Member Variables
    //

    /**
     * Warehouse grid map.
     */
    private Grid map;

    /**
     * Map of all agents in the warehouse, indexed by their id.
     */
    private Map<Integer, Agent> agents = new HashMap<>();

    /**
     * Set of all currently active agents, sorted by their priority.
     */
    private Set<Agent> activeAgents = new TreeSet<>();

    /**
     * Set of all currently idle agents.
     */
    private Set<Agent> readyAgents = new HashSet<>();

    /**
     * Map of all sell items in the warehouse, indexed by their id.
     */
    private Map<Integer, Item> items = new HashMap<>();

    /**
     * Map of the currently allocated items in the warehouse.
     */
    private Map<Item, Integer> allocatedItems = new HashMap<>();

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
     * @param data the un parsed warehouse object.
     */
    public void configure(List<Object> data) {
        parseWarehouse(data);
        init();
    }

    /**
     * Adds a new order.
     *
     * @param data the un parsed order object.
     */
    public void addOrder(List<Object> data) {
        Order order = parseOrder(data);

        if (feasible(order)) {
            allocateOrderResources(order);
            pendingOrders.add(order);
        } else {
            // TODO: add log message
        }
    }

    /**
     * Performs a single time step in this warehouse.
     */
    public void run() {
        // Dispatch pending orders
        dispatchPendingOrders();

        // Move active agents one step toward their targets
        step();
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
     * Checks whether the given order is feasible of being fulfilled regarding
     * its items and quantities.
     *
     * @param order the order to check.
     *
     * @return {@code true} if the given order is feasible, {@code false} otherwise.
     */
    private boolean feasible(Order order) {
        // Get map of required items of this order
        Map<Item, Integer> items = order.getItems();

        //
        // Iterate over every item
        //
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            // Get needed item and its quantity
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Get currently allocated items of this type
            int allocated = allocatedItems.getOrDefault(item, 0);

            // If needed quantity is greater than the overall available quantity
            // then this order is infeasible
            if (allocated + quantity > item.getTotalQuantity()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Allocates the resources of the given order in terms of the needed quantities.
     *
     * @param order the order to check.
     */
    private void allocateOrderResources(Order order) {
        // Get map of required items of this order
        Map<Item, Integer> items = order.getItems();

        //
        // Allocate every needed item in the order
        //
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            Item item = pair.getKey();
            int quantity = pair.getValue();
            int allocated = allocatedItems.getOrDefault(item, 0);

            allocatedItems.put(item, quantity + allocated);
        }
    }

    /**
     * Dispatches the current pending orders of the warehouse.
     */
    private void dispatchPendingOrders() {


    }

    /**
     * Moves the active agents one step towards their targets.
     */
    private void step() {

    }

    // ===============================================================================================
    //
    // Parsing Methods
    //

    /**
     * Parses the configurations of the warehouse space and components,
     * and updates the internal corresponding member variables.
     *
     * @param data the un parsed warehouse object.
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
