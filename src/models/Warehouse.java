package models;

import algorithms.Planner;
import models.hive.Agent;
import models.components.Order;
import models.hive.ChargingStation;
import models.hive.Gate;
import models.hive.Rack;
import models.components.Task;
import models.map.Grid;
import models.map.Position;

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
     * Map of all agents in the warehouse indexed by their id.
     */
    private Map<Integer, Agent> agents;

    /**
     * Map of all racks in the warehouse indexed by their id.
     */
    private Map<Integer, Rack> racks;

    /**
     * Map of all gates in the warehouse indexed by their id.
     */
    private Map<Integer, Gate> gates;

    /**
     * Map of all charging spots in the warehouse indexed by their id.
     */
    private Map<Integer, ChargingStation> chargingSpots;



    private Map<Integer, List<Rack>> items;

    private Queue<Task> pendingOrders;

    private Planner planner;

    // ===============================================================================================
    //
    // Static Functions
    //

    public static Warehouse create(Scanner reader) throws Exception {
        Warehouse ret = new Warehouse();
        ret.setup(reader);
        return ret;
    }

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Warehouse() {

    }

    public void setup(Scanner reader) throws Exception {

    }

    public void run() {

    }

    public void addTask(int agentId, int rackId) {

    }

    public void addOrder(Order order) {

    }

    public void addOrderBulk(List<Position> orders) {

    }

    public boolean isActive() {
        return false;
    }

    public void print() {
        System.out.println(map);
    }
}
