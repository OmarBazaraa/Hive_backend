package algorithms;

import models.agents.Agent;
import models.facilities.Rack;
import models.items.Item;
import models.maps.GuideGrid;
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
    public static void dispatch(Order order, Set<Agent> readyAgents, Queue<Agent> activeAgents) throws Exception {
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
     * Finds the best suitable {@code Agent} for the given {@code Task}.
     *
     * @param agents the set of all idle agents.
     * @param rack   the assigned {@code Rack}.
     * @param order  the needed {@code Order}.
     *
     * @return the best suitable {@code Agent} from the given set of agents.
     */
    private static Agent findAgent(Set<Agent> agents, Rack rack, Order order) {
        // If the rack is already allocated to an agent, then assign the task to that agent
        if (rack.isAllocated()) {
            return rack.getAllocatingAgent();
        }

        // Get the guide map of this rack
        GuideGrid guide = rack.getGuideMap();

        // Selected agent and its corresponding distance
        Agent ret = null;
        int distance = Integer.MAX_VALUE;

        // Find the nearest agent
        for (Agent agent : agents) {
            int dis = guide.getDistance(agent.getPosition());

            // Select the current agent if it is nearer to the rack
            if (distance > dis) {
                distance = dis;
                ret = agent;
            }
        }

        // Return the selected agent
        return ret;
    }
}
