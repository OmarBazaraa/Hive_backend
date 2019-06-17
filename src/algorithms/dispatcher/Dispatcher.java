package algorithms.dispatcher;

import models.agents.Agent;
import models.facilities.Rack;
import models.items.Item;
import models.maps.GuideGrid;
import models.tasks.orders.Order;
import models.tasks.Task;
import models.tasks.orders.RefillOrder;
import models.warehouses.Warehouse;

import java.util.*;


/**
 * This {@code Dispatcher} class contains some static method for order dispatching algorithms.
 */
public class Dispatcher {

    /**
     * Dispatches the given {@code Order} into a set of specific tasks assigned
     * to a set of agents.
     *
     * @param order       the {@code Order} needed to be dispatched.
     * @param readyAgents the set of ready agents.
     */
    public static void dispatch(Order order, Set<Agent> readyAgents) {
        //
        // Keep dispatching while the order is still pending
        //
        while (order.isPending()) {
            // Select the most suitable racks
            // List<Rack> selectedRacks = RackSelector.selectRacks(order);

            // Assign agents to the selected racks
            // Map<HiveObject, HiveObject> assignment = AgentAssigner.assignAgents(order.getDeliveryGate().getPosition(), selectedRacks, readyAgents);

            // Get current rack having the item
            Rack rack = selectRack(readyAgents, order);

            // Return if no rack is found
            if (rack == null) {
                return;
            }

            // Find a suitable agent
            Agent agent = selectAgent(readyAgents, rack);

            // Return if no agent is found
            if (agent == null) {
                return;
            }

            //
            // Create task to partially fulfill the order
            //
            Task activeTask = agent.getActiveTask();

            // Check if the found agent is currently active
            if (activeTask != null) {
                activeTask.addOrder(order);
            } else {
                Task task = new Task(agent, rack);
                task.addOrder(order);
                Warehouse.getInstance().addTask(task);
            }
        }
    }

    /**
     * Selects a suitable {@code Rack} to partially fulfill the given {@code Order}.
     *
     * @param readyAgents the set of all idle agents.
     * @param order       the {@code Order} to select {@code Rack} for.
     *
     * @return a suitable {@code Rack}.
     */
    private static Rack selectRack(Set<Agent> readyAgents, Order order) {
        // Refill orders as assigned with a rack by default
        if (order instanceof RefillOrder) {
            return ((RefillOrder) order).getRefillRack();
        }

        if (readyAgents.size() > 0) {
            // There exits idle agents to carry on the rack
            // Select a rack storing one of the needed items of the order
            Item item = order.iterator().next().getKey();
            return item.iterator().next().getKey();
        } else {
            // There are no idle agents at the moment
            // Select an allocated rack storing one of the needed items of the order
            for (var i : order) {
                Item item = i.getKey();

                for (var r : item) {
                    Rack rack = r.getKey();

                    if (rack.isAllocated()) {
                        return rack;
                    }
                }
            }
        }

        // No rack found
        return null;
    }

    /**
     * Selects a suitable {@code Agent} for carrying out the given {@code Rack}.
     *
     * @param readyAgents the set of all idle agents.
     * @param rack        the {@code Rack} to.
     *
     * @return a suitable {@code Agent}.
     */
    private static Agent selectAgent(Set<Agent> readyAgents, Rack rack) {
        // If the rack is already allocated to an agent,
        // then assign the task to that agent
        if (rack.isAllocated()) {
            return rack.getAllocatingAgent();
        }

        // Get the guide map of this rack
        GuideGrid guide = rack.getGuideMap();

        // Selected agent and its corresponding distance
        Agent ret = null;
        int distance = Integer.MAX_VALUE;

        //
        // Find the nearest agent to the rack
        //
        for (Agent agent : readyAgents) {
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

        // Return the selected agent if reachable
        return (distance == Integer.MAX_VALUE) ? null : ret;
    }
}
