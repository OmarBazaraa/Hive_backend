package models;

import algorithms.Planner;
import utils.Position;

import java.util.*;

public class Warehouse {

    //
    // Member Variables
    //

    private Grid map;

    private Map<Integer, Agent> agents;

    private Map<Integer, Rack> racks;
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
