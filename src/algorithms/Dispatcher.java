package algorithms;

import models.agents.Agent;
import models.facilities.Rack;
import models.items.Item;
import models.orders.Order;
import models.tasks.Task;

import java.util.*;


/**
 * This {@code Dispatcher} class contains some static method for order dispatching algorithms.
 */
public class Dispatcher {

    /**
     * Dispatches the given order into a set of specific tasks assigned to a set of agents.
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
            Item item = order.getFirst().getKey();

            // Get current rack having the item
            Rack rack = item.getFirst().getKey();

            // Find a suitable agent
            Agent agent = findAgent(readyAgents, rack, order);

            // Create task
            Task task = new Task(order, rack, agent);
            task.fillItems();
            task.activate();

            // Update agents queues
            readyAgents.remove(agent);
            activeAgents.add(agent);
        }
    }

    /**
     * Finds the best suitable {@code Agent} for the given task.
     *
     * @param agents the set of all idle agents.
     * @param rack   the rack.
     * @param order  the order.
     *
     * @return the best suitable {@code Agent} from the given set of agents.
     */
    private static Agent findAgent(Queue<Agent> agents, Rack rack, Order order) {
        return agents.iterator().next();
    }
}
