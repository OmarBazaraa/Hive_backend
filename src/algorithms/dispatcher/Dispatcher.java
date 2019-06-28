package algorithms.dispatcher;

import models.agents.Agent;
import models.facilities.Gate;
import models.facilities.Rack;
import models.tasks.Task;
import models.tasks.orders.Order;
import models.tasks.orders.RefillOrder;
import models.warehouses.Warehouse;

import java.util.*;


/**
 * This {@code Dispatcher} class contains some static method for order dispatching algorithms.
 */
public class Dispatcher {

    /**
     * Dispatches the given {@code Order} into a set of specific tasks assigned
     * to a set of agents and racks.
     *
     * @param order       the {@code Order} needed to be dispatched.
     * @param readyAgents the set of currently idle agents.
     */
    public static void dispatch(Order order, Set<Agent> readyAgents) {
        // Get the set of candidate racks to fulfill the order
        Set<Rack> candidateRacks = order.getCandidateRacks();

        // Whether the candidate racks have been filtered or not
        boolean filtered = false;

        //
        // Keep dispatching while the order is not fulfilled
        //
        while (order.isPending() && candidateRacks.size() > 0) {
            // Filter the candidate racks when currently no idle agents
            if (readyAgents.isEmpty() && !filtered) {
                candidateRacks.removeIf(rack -> !rack.isAllocated());
                filtered = true;
            }

            // Find a suitable rack
            Rack rack = selectRack(order, candidateRacks);

            // If no rack is capable of supplying the order then return
            if (rack == null) {
                return;
            }

            // Remove the selected rack from the set of candidates
            candidateRacks.remove(rack);

            // Find a suitable agent
            Agent agent = selectAgent(order, rack, readyAgents);

            // If no agent is capable of carrying this rack then continue
            if (agent == null) {
                continue;
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
                Warehouse.getInstance().activateTask(task);
            }
        }
    }

    /**
     * Selects a suitable {@code Rack} to partially fulfill the given {@code Order}.
     *
     * @param order          the {@code Order} to select a {@code Rack} for.
     * @param candidateRacks the set of candidate racks to select from.
     *
     * @return a suitable {@code Rack}.
     */
    private static Rack selectRack(Order order, Set<Rack> candidateRacks) {
        // Base case when only one candidate rack is available
        if (candidateRacks.size() == 1) {
            return candidateRacks.iterator().next();
        }

        // Initialize the best rack to null
        Rack bestRack = null;
        double bestRank = 1e9;

        // Get the delivery gate of the order
        Gate gate = order.getDeliveryGate();

        //
        // Select the rack with the minimum rank
        //
        for (Rack rack : candidateRacks) {
            // Calculate the maximum number of items the rack can supply the order with
            int totalItemSupply = order.getMaxRackSupply(rack);

            // Calculate the estimated number of steps complete the task with the current rack
            int estimatedSteps;

            if (rack.isAllocated()) {
                Task task = rack.getAllocatingAgent().getActiveTask();
                estimatedSteps = task.calculateEstimatedSteps(order);
            } else {
                estimatedSteps = rack.getDistanceTo(gate.getPosition());
            }

            // Calculate the rank of this rack (i.e. distance_travelled / rack_supplied_items_count)
            double rank = 1.0 * estimatedSteps / totalItemSupply;

            // Update the best rack
            if (Double.compare(rank, bestRank) < 0) {
                bestRack = rack;
                bestRank = rank;
            }
        }

        // Return the best ranked rack
        return bestRack;
    }

    /**
     * Selects a suitable {@code Agent} for carrying out the given {@code Rack}.
     *
     * @param order       the {@code Order} to select an {@code Agent} for.
     * @param rack        the {@code Rack} to carry on.
     * @param readyAgents the set of currently idle agents.
     *
     * @return a suitable {@code Agent}.
     */
    private static Agent selectAgent(Order order, Rack rack, Set<Agent> readyAgents) {
        // Compute maximum rack weight during the task
        int rackWeight = rack.getStoredWeight();

        if (order instanceof RefillOrder) {
            rackWeight += ((RefillOrder) order).getAddedWeight();
        }

        // If the rack is already allocated to an agent,
        // then assign the task to that agent
        if (rack.isAllocated()) {
            Agent agent = rack.getAllocatingAgent();

            if (agent.getLoadCapacity() >= rackWeight) {
                return agent;
            } else {
                return null;
            }
        }

        // Selected agent and its corresponding distance
        Agent ret = null;
        int distance = Integer.MAX_VALUE;

        //
        // Find the nearest agent to the rack
        //
        for (Agent agent : readyAgents) {
            // Skip agent if it cannot hold that rack
            if (agent.getLoadCapacity() < rackWeight) {
                continue;
            }

            // Calculate distance from the agent to the rack
            int dis = rack.getDistanceTo(agent);

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
