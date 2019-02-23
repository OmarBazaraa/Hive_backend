package algorithms;

import models.Agent;
import models.Grid;
import models.Task;
import utils.Constants.*;
import utils.Pair;
import utils.Position;
import utils.Utility;

import java.util.*;

public class Planner {

    private Grid map;
    private Queue<Agent> pendingAgents;
    private Queue<Agent> activeAgents;
    private Queue<Agent> solvedAgents;

    private Map<Pair<Integer, Integer>, Map<Integer, Queue<Direction>>> alternatePaths;

    public Planner(Grid map) {
        this.map = map;

        this.pendingAgents = new PriorityQueue<>();
        this.activeAgents = new PriorityQueue<>();
        this.solvedAgents = new PriorityQueue<>();
        this.alternatePaths = new HashMap<>();
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


    private Queue<Direction> constructPath(int src, int dst, Direction[] par) {
        Queue<Direction> ret = new LinkedList<>();

        while (dst != src) {
            ret.add(par[dst]);
            dst = map.previousId(dst, par[dst]);
        }

        return ret;
    }

    private Queue<Direction> computeAlternatePath(int src, int dst, int skip) {
        Queue<Integer> q = new LinkedList<>();

        int size = map.getCellsCount();

        boolean vis[] = new boolean[size];
        Direction par[] = new Direction[size];

        q.add(src);
        vis[src] = true;

        while (!q.isEmpty()) {
            int cur = q.poll();

            for (Direction dir : Direction.values()) {
                int nxt = map.nextId(cur, dir);

                if (nxt == skip || !map.isFree(nxt)) {
                    continue;
                }

                vis[nxt] = true;
                par[nxt] = dir;
                q.add(nxt);

                if (nxt == dst) {
                    break;
                }
            }
        }

        if (!vis[dst]) {
            return null;
        }

        return constructPath(src, dst, par);
    }

    private Queue<Direction> getAlternatePath(int src, int dst, int skip) {
        if (src > dst) {
            int t = dst;
            dst = src;
            src = t;
        }

        Pair<Integer, Integer> pair = new Pair<>(src, dst);

        Map<Integer, Queue<Direction>> paths = alternatePaths.get(pair);

        if (paths == null) {
            paths = new HashMap<>();
            alternatePaths.put(pair, paths);
        }

        if (!paths.containsKey(skip)) {
            paths.put(skip, computeAlternatePath(src, dst, skip));
        }

        return paths.get(skip);
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

            for (Direction dir : Direction.values()) {
                Position nxt = Utility.nextPosition(cur, dir);

                if (map.isValid(nxt.r, nxt.c)) {
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
