package models.warehouses;

import algorithms.dispatcher.Dispatcher;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.tasks.orders.Order;
import models.tasks.Task;

import utils.Constants;
import utils.Constants.*;
import utils.exceptions.DataException;

import java.util.*;


/**
 * This {@code Warehouse} class is considered the main controller of our Hive Warehouse System.
 * <p>
 * It contains required functions to simulate the process inside an automated smart warehouse.
 */
public class Warehouse extends AbstractWarehouse {

    //
    // Static Variables & Methods
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
    // Member Variables
    //

    /**
     * The region that each cell belongs to.
     */
    private int[][] region;

    /**
     * The maximum load capacity in a region.
     */
    private Map<Integer, Integer> regionMaxLoadCap = new HashMap<>();

    /**
     * The number of gates in a region.
     */
    private Map<Integer, Integer> regionGatesCount = new HashMap<>();


    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Warehouse} object.
     */
    protected Warehouse() {
        // Protected constructor to ensure a singleton object.
    }

    /**
     * Clears the {@code Warehouse} and removes all its components.
     */
    @Override
    public void clear() {
        super.clear();

        region = null;
        regionMaxLoadCap.clear();
        regionGatesCount.clear();
    }

    /**
     * Initializes and validates the {@code Warehouse}, and performs any needed pre-computations.
     */
    @Override
    public void init() throws DataException {
        //
        // Initialize the warehouse regions
        //
        analyzeRegions();

        //
        // Initialize the guide maps
        //
        for (Rack rack : racks.values()) {
            rack.computeGuideMap();
        }
        for (Gate gate : gates.values()) {
            gate.computeGuideMap();
        }
        for (Station station : stations.values()) {
            station.computeGuideMap();
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
    @Override
    public void addTask(Task task) {
        // Activate the task
        task.activate();

        //
        // Update agents lists
        //
        Agent agent = task.getAgent();

        if (readyAgents.contains(agent)) {
            readyAgents.remove(agent);
            activeAgents.add(agent);
        }
    }

    /**
     * Performs and simulates a single time step in this {@code Warehouse}.
     *
     * @return {@code true} if a change has happen in the {@code Warehouse}; {@code false} otherwise.
     */
    @Override
    public boolean run() {
        time++;
        dispatchPendingOrders();
        return recoverBlockedAgents() | advanceActiveAgents();
    }

    /**
     * Dispatches the current pending orders of this {@code Warehouse}.
     */
    @Override
    protected void dispatchPendingOrders() {
        // Skip if no pending orders
        if (pendingOrders.isEmpty() || agents.isEmpty()) {
            return;
        }

        // Get the initial size of the queue
        int size = pendingOrders.size();

        //
        // Iterate over every pending order and tries to dispatch it
        //
        for (int i = 0; i < size; ++i) {
            // Get the current order
            Order order = pendingOrders.remove();

            // Try dispatching the current order
            Dispatcher.dispatch(order, readyAgents);

            // Re-add the order to the end of the queue if still pending
            if (order.isPending()) {
                pendingOrders.add(order);
            }
        }
    }

    /**
     * Recovers the blocked agents to their normal state if possible.
     *
     * @return {@code true} if at least one {@code Agent} has recovered; {@code false} otherwise.
     */
    @Override
    protected boolean recoverBlockedAgents() {
        // Skip if no blocked agents
        if (blockedAgents.isEmpty()) {
            return false;
        }

        // Initialize return value to false
        boolean ret = false;

        // Get the initial size of the queue
        int size = blockedAgents.size();

        //
        // Iterate over every blocked agent and tries to recover it
        //
        for (int i = 0; i < size; ++i) {
            // Get the current blocked agent
            Agent agent = blockedAgents.remove();

            // Try retreating the current agent
            ret |= agent.recover();

            // Re-add the agent to the end of the queue if still blocked
            if (agent.isBlocked()) {
                blockedAgents.add(agent);
            } else if (agent.isActive()) {
                activeAgents.add(agent);
            } else {
                readyAgents.add(agent);
            }
        }

        // Return whether any agent has recovered
        return ret;
    }

    /**
     * Moves the active agents one step towards their targets.
     *
     * @return {@code true} if at least one {@code Agent} has advanced; {@code false} otherwise.
     */
    @Override
    protected boolean advanceActiveAgents() {
        // Skip if no active agents
        if (activeAgents.isEmpty()) {
            return false;
        }

        // Initialize return value to false
        boolean ret = false;

        // Get the initial size of the queue
        int size = activeAgents.size();

        // Create another queue of agents
        TreeSet<Agent> q = new TreeSet<>(Collections.reverseOrder());

        //
        // Iterate over all active agents
        //
        for (int i = 0; i < size; ++i) {
            // Get current active agent
            Agent agent = activeAgents.pollFirst();

            // Try moving the current agent towards its target
            ret |= agent.executeAction();

            // Re-add agent to the active queue if still active, otherwise add it to the ready queue
            if (agent.isActive()) {
                q.add(agent);
            } else {
                readyAgents.add(agent);
            }
        }

        // Update active agents queue
        activeAgents = q;

        // Return whether any agent has advanced
        return ret;
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Analyzes the different regions of the {@code Warehouse}.
     */
    private void analyzeRegions() throws DataException {
        int regionId = 0;
        region = new int[rows][cols];

        // Analyze
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                if (region[i][j] == 0) {
                    regionId++;
                    regionMaxLoadCap.put(regionId, floodRegion(i, j, regionId));
                }
            }
        }

        // Validate
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                if (grid[i][j].getType() != CellType.RACK) {
                    continue;
                }

                Rack rack = (Rack) grid[i][j].getFacility();

                if (regionGatesCount.getOrDefault(region[i][j], 0) <= 0) {
                    throw new DataException("No gate is reachable to rack-" + rack.getId() + ".",
                            Constants.ERR_RACK_NO_GATE_REACHABLE, rack.getId());
                }

                int maxWeight = rack.getContainerWeight() + rack.getCapacity();
                int maxLoadCap = regionMaxLoadCap.getOrDefault(region[i][j], -1);

                if (maxWeight > maxLoadCap) {
                    throw new DataException("No agent can load rack-" + rack.getId() + " in its full capacity.",
                            Constants.ERR_RACK_NO_AGENT_REACHABLE, rack.getId(), maxLoadCap);
                }
            }
        }
    }

    /**
     * Checks whether a {@code Rack} can reach a {@code Gate} or not.
     *
     * @param rack the {@code Rack} to check.
     * @param gate the {@code Gate} to check.
     *
     * @return {@code true} if the given {@code Rack} is reachable to the given {@code Gate}.
     */
    public boolean isReachable(Rack rack, Gate gate) {
        return region[rack.getRow()][rack.getCol()] == region[gate.getRow()][gate.getCol()];
    }

    /**
     * Scans the region starting at the given location, and
     * updates the internal variables in accordance.
     *
     * @param row      the row position of the cell.
     * @param col      the column position of the cell.
     * @param regionId the current region id to assign.
     *
     * @return the maximum load capacity in the scanned region.
     */
    private int floodRegion(int row, int col, int regionId) {
        if (region[row][col] != 0) {
            return -1;
        }

        CellType type = grid[row][col].getType();

        if (type == CellType.OBSTACLE) {
            return -1;
        }

        region[row][col] = regionId;

        if (type == CellType.GATE) {
            regionGatesCount.put(regionId, regionGatesCount.getOrDefault(regionId, 0) + 1);
        }

        int ret = (grid[row][col].hasAgent() ? grid[row][col].getAgent().getLoadCapacity() : -1);

        for (int d : Constants.DIRECTIONS) {
            int r = row + Constants.DIR_ROW[d];
            int c = col + Constants.DIR_COL[d];

            if (isInBound(r, c)) {
                ret = Math.max(ret, floodRegion(r, c, regionId));
            }
        }

        return ret;
    }
}
