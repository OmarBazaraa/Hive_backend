package models.warehouses;

import algorithms.dispatcher.Dispatcher;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.facilities.Station;
import models.items.Item;
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
     * The {@code Agent} region that each cell belongs to.
     */
    private int[][] agentRegion;

    /**
     * The maximum load capacity in an {@code Agent} region.
     */
    private Map<Integer, Integer> agentRegionLoadCap = new HashMap<>();

    /**
     * The {@code Gate} region that each cell belongs to.
     */
    private int[][] gateRegion;

    /**
     * The {@code Item} quantities in a {@code Gate} region.
     */
    private Map<Integer, Map<Item, Integer>> gateRegionItems = new HashMap<>();

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

        agentRegion = null;
        agentRegionLoadCap.clear();

        gateRegion = null;
        gateRegionItems.clear();
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
        int agentRegionId = 0;
        agentRegion = new int[rows][cols];

        int gateRegionId = 0;
        gateRegion = new int[rows][cols];

        // Analyze
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                if (grid[i][j].hasAgent() && agentRegion[i][j] == 0) {
                    agentRegionId++;
                    agentRegionLoadCap.put(agentRegionId, floodAgentRegion(i, j, agentRegionId));
                }

                if (grid[i][j].getType() == CellType.GATE && gateRegion[i][j] == 0) {
                    gateRegionId++;
                    floodGateRegion(i, j, gateRegionId);
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

                if (gateRegion[i][j] == 0) {
                    throw new DataException("No gate is reachable to rack-" + rack.getId() + ".",
                            Constants.ERR_RACK_NO_GATE_REACHABLE, rack.getId());
                }

                int maxWeight = rack.getContainerWeight() + rack.getCapacity();
                int maxLoadCap = agentRegionLoadCap.getOrDefault(agentRegion[i][j], -1);

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
        return gateRegion[rack.getRow()][rack.getCol()] == gateRegion[gate.getRow()][gate.getCol()];
    }

    /**
     * Scans the {@code Agent} region starting at the given location, and
     * updates the internal variables in accordance.
     *
     * @param row      the row position of the cell.
     * @param col      the column position of the cell.
     * @param regionId the current region id to assign.
     *
     * @return the maximum load capacity in the scanned region.
     */
    private int floodAgentRegion(int row, int col, int regionId) {
        if (agentRegion[row][col] != 0) {
            return -1;
        }

        if (grid[row][col].getType() == CellType.OBSTACLE) {
            return -1;
        }

        agentRegion[row][col] = regionId;

        int ret = (grid[row][col].hasAgent() ? grid[row][col].getAgent().getLoadCapacity() : -1);

        for (int d : Constants.DIRECTIONS) {
            int r = row + Constants.DIR_ROW[d];
            int c = col + Constants.DIR_COL[d];

            if (isInBound(r, c)) {
                ret = Math.max(ret, floodAgentRegion(r, c, regionId));
            }
        }

        return ret;
    }

    /**
     * Scans the {@code Gate} region starting at the given location, and
     * updates the internal variables in accordance.
     *
     * @param row      the row position of the cell.
     * @param col      the column position of the cell.
     * @param regionId the current region id to assign.
     */
    private void floodGateRegion(int row, int col, int regionId) {
        if (gateRegion[row][col] != 0) {
            return;
        }

        CellType type = grid[row][col].getType();

        if (type == CellType.OBSTACLE) {
            return;
        }

        gateRegion[row][col] = regionId;

        if (type == CellType.RACK) {
            return;
        }

        for (int d : Constants.DIRECTIONS) {
            int r = row + Constants.DIR_ROW[d];
            int c = col + Constants.DIR_COL[d];

            if (isInBound(r, c)) {
                floodGateRegion(r, c, regionId);
            }
        }
    }
}
