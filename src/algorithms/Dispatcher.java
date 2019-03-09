package algorithms;

import models.agents.Agent;
import models.facilities.Rack;
import models.components.*;
import models.tasks.Task;
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
     * and maybe more. So I'm currently solving this algorithm in a greedy way.
     * The algorithm needs to be revised and optimized in later phases.
     *
     * @param order        the order needed to be dispatched.
     * @param readyAgents  the queue of ready agents.
     * @param activeAgents the queue of active agents.
     */
    public static void dispatch(Order order, Queue<Agent> readyAgents, Queue<Agent> activeAgents) throws Exception {
        //
        // Keep dispatching while the order is still pending and
        // there are still idle robots
        //
        while (order.isPending() && !readyAgents.isEmpty()) {
            // Get current needed item in the order
            Map.Entry<Item, Integer> pair = order.getFirstItem();
            Item item = pair.getKey();
            int neededQuantity = pair.getValue();

            // Sort the racks of the current item in decreasing order of the number of available quantities
            Queue<Pair<Integer, Rack>> racks = sortRacks(item.getRacks());

            //
            // Keep assigning tasks until fulfilling the current item
            //
            while (neededQuantity > 0 && !racks.isEmpty() && !readyAgents.isEmpty()) {
                // Get current rack
                int availableQuantity = racks.poll().key;
                Rack rack = racks.peek().val;

                // Assign a new task
                Task task = new Task();
                task.assignOrder(order);
                task.assignRack(rack);
                task.fillItems();

                // Assign an agent for this task
                Agent agent = findAgent(readyAgents, task);
                task.assignAgent(agent);
                readyAgents.remove(agent);
                activeAgents.add(agent);

                // Commit this task
                order.addTask(task);
                task.activate();
                neededQuantity -= availableQuantity;
            }
        }
    }

    /**
     * Finds the best suitable agent for the given task.
     * TODO: write algorithm
     *
     * @param agents the set of all idle agents.
     * @param task   the task.
     *
     * @return the best suitable agent from the given set of agents.
     */
    private static Agent findAgent(Queue<Agent> agents, Task task) {
        return agents.iterator().next();
    }


    private static Queue<Pair<Integer, Rack>> sortRacks(Map<Rack, Integer> racks) {
        Queue<Pair<Integer, Rack>> q = new PriorityQueue<>(Collections.reverseOrder());

        for (Map.Entry<Rack, Integer> pair : racks.entrySet()) {
            Rack rack = pair.getKey();
            int quantity = pair.getValue();

            q.add(new Pair<>(quantity, rack));
        }

        return q;
    }
}
