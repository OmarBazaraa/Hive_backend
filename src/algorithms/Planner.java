package algorithms;

import models.Agent;
import models.Grid;
import models.Rack;

import java.util.*;

public class Planner {

    private Grid map;
    private Map<Integer, Agent> agents;

    public Planner(Grid map, Map<Integer, Agent> agents) {
        this.map = map;
        this.agents = agents;
    }
}
