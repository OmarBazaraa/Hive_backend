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
        int rows = reader.nextInt();
        int cols = reader.nextInt();

        char[][] grid = new char[rows][cols];

        for (int i = 0; i < rows; ++i) {
            if (!reader.hasNext()) {
                throw new Exception("Grid dimensions mis-match!");
            }

            String row = reader.next();

            if (row.length() != cols) {
                throw new Exception("Grid dimensions mis-match!");
            }

            grid[i] = row.toCharArray();
        }

        map = Grid.createGrid(reader);
    }

    private void setupAgents(Scanner reader) throws Exception {
        agents = new HashMap<>();

        int cnt = reader.nextInt();

        while (cnt-- > 0) {
            int id = reader.nextInt();
            int r = reader.nextInt();
            int c = reader.nextInt();

            Agent agent = new Agent(id, r, c);
            agents.put(id, agent);
            map.bindAgent(agent);
        }
    }

    private void setupRack(Scanner reader) throws Exception {
        racks = new HashMap<>();
        items = new HashMap<>();

        int cnt = reader.nextInt();

        while (cnt-- > 0) {
            int id = reader.nextInt();
            int r = reader.nextInt();
            int c = reader.nextInt();
            int itemId = reader.nextInt();
            int itemCount = reader.nextInt();

            Rack rack = new Rack(id, r, c, itemId, itemCount);

            racks.put(id, rack);

            if (!items.containsKey(itemId)) {
                items.put(itemId, new ArrayList<>());
            }

            items.get(itemId).add(rack);

            map.bindRack(rack);
        }
    }
}
