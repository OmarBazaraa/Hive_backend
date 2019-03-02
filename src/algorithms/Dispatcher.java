package algorithms;

import models.components.*;
import utils.Pair;

import java.util.*;


/**
 * This {@code Dispatcher} class contains some static method for order dispatching algorithms.
 */
public class Dispatcher {


    /**
     * Dispatches the given order into a set of specific tasks assigned to a set of agents.
     * <p>
     * TODO: ...
     * This algorithm is considered as complex as the multi-agent path planning algorithm,
     * and maybe more. So I'm currently solving this algorithm in a naive way.
     * The algorithm needed to be completed, revised and optimized.
     *
     * @param order       the order needed to be dispatched.
     * @param readyAgents the set of ready agents.
     *
     * @return a set of {@code Task} objects corresponding to the given order, or a subset of the given order.
     */
    public static List<Task> dispatch(Order order, Set<Agent> readyAgents) {
        // If there is no free agent then we cannot dispatch the order right now
        if (readyAgents.isEmpty()) {
            return null;
        }

        // Get order needed items and quantities
        Map<Item, Integer> neededItems = order.getItems();

        //
        Set<Agent> usedAgents = new HashSet<>();
        Map<Item, Integer> takenItems = new HashMap<>();

        List<Task> tasks = new ArrayList<>();

        //
        // Iterate over every needed item
        //
        for (Map.Entry<Item, Integer> pair : neededItems.entrySet()) {
            // Get current needed item
            Item item = pair.getKey();
            int neededQuantity = pair.getValue() - takenItems.getOrDefault(item, 0);

            List<Rack> racks = chooseRacks(item.getRacks(), neededQuantity);
        }

        return tasks;
    }

    private static List<Rack> chooseRacks(Map<Rack, Integer> racks, int neededQuantity) {
        List<Rack> ret = new ArrayList<>();
        Queue<Pair<Integer, Rack>> q = new PriorityQueue<>(Collections.reverseOrder());

        for (Map.Entry<Rack, Integer> pair : racks.entrySet()) {
            Rack rack = pair.getKey();
            int availableQuantity = pair.getValue();

            if (availableQuantity > neededQuantity) {
                ret.add(rack);
                return ret;
            }

            q.add(new Pair<>(availableQuantity, rack));
        }

        while (!q.isEmpty() && neededQuantity > 0) {
            Pair<Integer, Rack> pair = q.poll();
            Rack rack = pair.y;
            int availableQuantity = pair.x;

            ret.add(rack);
            neededQuantity -= availableQuantity;
        }

        return ret;
    }


    private static void assignTasks() {

    }
}
