package models;

import java.io.FileReader;
import java.util.*;
import java.util.Map;

public class Warehouse {

    private Grid map;
    private Map<Integer, Agent> agents;
    private Map<Integer, Rack> racks;
    private Map<Integer, List<Rack>> items;


    public Warehouse() {

    }

    public void setup(String fileName) throws Exception {
        // Open the map file
        Scanner reader = new Scanner(new FileReader(fileName));

        // Setup the map, agents, racks, and items
        setupMap(reader);
        setupAgents(reader);
        setupRack(reader);

        // Close the reader object
        reader.close();
    }

    private void setupMap(Scanner reader) throws Exception {
        map = Grid.create(reader);
    }

    private void setupAgents(Scanner reader) throws Exception {
        agents = new HashMap<>();

        int cnt = reader.nextInt();

        while (cnt-- > 0) {
            Agent agent = Agent.create(reader);
            agents.put(agent.getId(), agent);
            map.bind(agent);
        }
    }

    private void setupRack(Scanner reader) throws Exception {
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
