package algorithms;

import models.Agent;
import models.Grid;
import models.Path;
import models.Task;
import utils.Constants.*;
import utils.Pair;

import java.util.*;

public class Planner {

    private Grid map;
    private Queue<Agent> pendingAgents;
    private Queue<Agent> activeAgents;
    private Queue<Agent> solvedAgents;

    private Map<Pair<Integer, Integer>, Map<Integer, List<Direction>>> alternatePaths;

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

            List<Direction> path = findPath(agent);

            if (path != null) {
                Path p = new Path();
                p.mainPath = path;
                agent.assignPath(p);
                activeAgents.add(agent);
            } else {
                pendingAgents.add(agent);
            }
        }
    }

    public void step() {

    }

    public boolean isActive() {
        return (pendingAgents.size() > 0 || activeAgents.size() > 0);
    }


    private List<Direction> constructPath(int src, int dst, Direction[] par) {
        List<Direction> ret = new ArrayList<>();

        while (dst != src) {
            ret.add(par[dst]);
            dst = map.previousId(dst, par[dst]);
        }

        Collections.reverse(ret);

        return ret;
    }

    private List<Direction> computeAlternatePath(int src, int dst, int skip) {
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

                if (!map.isFree(nxt)) {
                    continue;
                }

                if (vis[nxt] || nxt == skip) {
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

    private List<Direction> getAlternatePath(int src, int dst, int skip) {
        if (src > dst) {
            int t = dst;
            dst = src;
            src = t;
        }

        Pair<Integer, Integer> pair = new Pair<>(src, dst);

        Map<Integer, List<Direction>> paths = alternatePaths.get(pair);

        if (paths == null) {
            paths = new HashMap<>();
            alternatePaths.put(pair, paths);
        }

        if (!paths.containsKey(skip)) {
            paths.put(skip, computeAlternatePath(src, dst, skip));
        }

        return paths.get(skip);
    }

    private List<Direction> findPath(Agent agent) {
        int src = map.getCellId(agent.getPosition());
        int dst = map.getCellId(agent.getTargetPosition());

        int size = map.getCellsCount();

        boolean vis[] = new boolean[size];
        Direction par[] = new Direction[size];

        Queue<Pair<Integer, Direction>> q = new LinkedList<>();
        q.add(new Pair<>(src, Direction.STILL));
        vis[src] = true;


        while (!q.isEmpty()) {
            Pair<Integer, Direction> p = q.poll();
            Direction prvDir = p.y;
            int cur = p.x;
            int prv = map.previousId(cur, prvDir);


            for (Direction dir : Direction.values()) {
                int nxt = map.nextId(cur, dir);

                if (nxt == dst) {
                    vis[nxt] = true;
                    par[nxt] = dir;
                    return constructPath(src, cur, par);
                }

                if (!map.isFree(nxt)) {
                    continue;
                }

                if (vis[nxt]) {
                    continue;
                }

                List<Direction> alterPath = getAlternatePath(prv, nxt, cur);

                if (alterPath == null) {
                    continue;
                }

                vis[nxt] = true;
                par[nxt] = dir;
                q.add(new Pair<>(nxt, dir));
            }
        }

        return null;
    }
}
