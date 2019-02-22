package algorithms;

import models.Agent;
import models.Grid;
import models.Task;
import utils.Constants.*;
import utils.Position;
import utils.Utility;

import java.util.*;

public class Planner {

    private Grid map;
    private Queue<Agent> pendingAgents;
    private Queue<Agent> activeAgents;

    public Planner(Grid map) {
        this.map = map;

        this.pendingAgents = new LinkedList<>();
        this.activeAgents = new LinkedList<>();
    }

    public void addTask(Task task) {
        Agent agent = task.agent;
        agent.assignTask(task);
        pendingAgents.add(agent);
    }

    public void plan() {
        int size = pendingAgents.size();

        for (int i = 0; i < size; ++i) {
            Agent agent = pendingAgents.poll();


        }
    }

    public void step() {

    }

    public boolean isActive() {
        return (pendingAgents.size() > 0 || activeAgents.size() > 0);
    }


    private void findPath(Agent agent) {
        Position src = agent.getPosition();
        Position dst = agent.getTargetPosition();

        Queue<Position> q = new LinkedList<>();

        q.add(src);

        int rows = map.getRows();
        int cols = map.getCols();

        boolean vis[][] = new boolean[rows + 2][cols + 2];

        vis[src.r][src.c] = true;

        while (!q.isEmpty()) {
            Position cur = q.poll();

            if (cur.equals(dst)) {
                // found the path
            }

            for (int i = 0; i < 4; ++i) {
                Direction dir = Direction.values()[i];
                Position nxt = Utility.nextPosition(cur, dir);

                if (map.valid(nxt.r, nxt.c)) {
                    Grid.Cell cell = map.get(nxt.r, nxt.c);

                    if (cell.type == CellType.EMPTY && !vis[nxt.r][nxt.c]) {
                        vis[nxt.r][nxt.c] = true;
                        q.add(nxt);
                    }
                }
            }
        }
    }
}
