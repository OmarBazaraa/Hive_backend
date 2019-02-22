package models;

import algorithms.Planner;
import utils.Pair;

import java.util.*;

public class Warehouse {

    //
    // Member Variables
    //

    private Grid map;
    private Map<Integer, Agent> agents;
    private Map<Integer, Rack> racks;
    private Map<Integer, List<Rack>> items;

    private Queue<Instruction> pendingOrders;

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
        setupMap(reader);
        setupAgents(reader);
        setupRacks(reader);

        planner = new Planner(map, agents);
    }

    public void addOrder(int agentId, int rackId) {

    }

    public void addOrderBulk(List<Pair> orders) {

    }

    public void print() {
        System.out.println(map);
    }

    // ===============================================================================================
    //
    // Helper Private Member Functions
    //

    private void setupMap(Scanner reader) throws Exception {
        map = Grid.create(reader);
    }

    private void setupAgents(Scanner reader) {
        agents = new HashMap<>();

        int cnt = reader.nextInt();

        while (cnt-- > 0) {
            Agent agent = Agent.create(reader);
            agents.put(agent.getId(), agent);
            map.bind(agent);
        }
    }

    private void setupRacks(Scanner reader) {
        racks = new HashMap<>();
        items = new HashMap<>();

        int cnt = reader.nextInt();

        while (cnt-- > 0) {
            Rack rack = Rack.create(reader);
            racks.put(rack.getId(), rack);
            map.bind(rack);
            setupItem(rack.getItemId(), rack);
        }
    }

    private void setupItem(int itemId, Rack rack) {
        if (items.containsKey(itemId)) {
            items.get(itemId).add(rack);
        } else {
            List<Rack> list = new ArrayList<>();
            list.add(rack);
            items.put(itemId, list);
        }
    }
}
