package algorithms.dispatcher;

import algorithms.dispatcher.task_allocator.RackSelector;
import algorithms.dispatcher.task_allocator.AgentAssigner;
import models.HiveObject;
import models.agents.Agent;
import models.facilities.Rack;
import models.items.Item;
import models.maps.GuideGrid;
import models.tasks.Order;
import models.tasks.Task;
import models.warehouses.Warehouse;

import java.util.*;


/**
 * This {@code Dispatcher} class contains some static method for order dispatching algorithms.
 */
public class Dispatcher {

    /**
     * Dispatches the given {@code Order} into a set of specific tasks assigned
     * to a set of agents.
     * <p>
     * TODO: support refill orders
     *
     * @param order       the {@code Order} needed to be dispatched.
     * @param readyAgents the set of ready agents.
     */
    public static void dispatch(Order order, Set<Agent> readyAgents) {
        //
        // Keep dispatching while the order is still pending and
        // there are still idle robots
        //
        while (order.isPending() && readyAgents.size() > 0) {
            // Select the most suitable racks
//            List<Rack> selectedRacks = RackSelector.selectRacks(order);

            // Assign agents to the selected racks
//            Map<HiveObject, HiveObject> assignment = AgentAssigner.assignAgents(order.getDeliveryGate().getPosition(), selectedRacks, readyAgents);

            // Get current needed item in the order
            Item item = order.iterator().next().getKey();

            // Get current rack having the item
            Rack rack = item.iterator().next().getKey();

            // Find a suitable agent
            Agent agent = findAgent(readyAgents, rack, order);

            // Return if no agent is found
            if (agent == null) {
                return;
            }

            // Plan the items to collect
            Map<Item, Integer> plannedItems = planCollectOrderItems(agent, rack, order);

            // Create task and add it to the warehouse
            Task task = new Task(rack, agent);
            task.addOrder(order, plannedItems);
            Warehouse.getInstance().addTask(task);
        }
    }

    /**
     * Finds the best suitable {@code Agent} for the given {@code Task}.
     *
     * @param readyAgent the set of all idle agents.
     * @param rack       the assigned {@code Rack}.
     * @param order      the needed {@code Order}.
     *
     * @return the best suitable {@code Agent} from the given set of agents.
     */
    private static Agent findAgent(Set<Agent> readyAgent, Rack rack, Order order) {
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
        for (Agent agent : readyAgent) {
            // Skip agent if it cannot hold that rack
            if (agent.getLoadCapacity() < rack.getTotalWeight()) {
                continue;
            }

            // Calculate distance from the agent to the rack
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

    /**
     * Plans the best collection of units to collect from the given {@code Rack}
     * based on the given {@code Order}.
     *
     * @param agent the selected {@code Agent}.
     * @param rack  the selected {@code Rack}.
     * @param order the needed {@code Order}.
     *
     * @return the best {@code Item} collection.
     */
    private static Map<Item, Integer> planCollectOrderItems(Agent agent, Rack rack, Order order) {
        Map<Item, Integer> ret = new HashMap<>();

        for (var pair : order) {
            Item neededItem = pair.getKey();
            int neededQuantity = pair.getValue();
            int availableQuantity = rack.get(neededItem);
            int plannedQuantity = Math.min(neededQuantity, availableQuantity);

            if (plannedQuantity != 0) {
                ret.put(neededItem, plannedQuantity);
            }
        }

        return ret;
    }

    /**
     * Plans the best collection of units to refill the given {@code Rack}
     * based on the given {@code Order}.
     *
     * @param agent the selected {@code Agent}.
     * @param rack  the selected {@code Rack}.
     * @param order the needed {@code Order}.
     *
     * @return the best {@code Item} collection.
     */
    private static Map<Item, Integer> planRefillOrderItems(Agent agent, Rack rack, Order order) {
        // TODO
        return null;
    }
}
