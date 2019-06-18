package algorithms.dispatcher;

import models.agents.Agent;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.orders.Order;
import models.tasks.Task;
import models.tasks.orders.RefillOrder;
import models.warehouses.Warehouse;
import algorithms.dispatcher.task_allocator.RackSelector;

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
            List<Rack> selectedRacks = selectRacks(order, readyAgents);

            // Return if no rack is found
            if (selectedRacks.size() == 0) {
                return;
            }

            for (Rack rack : selectedRacks) {
                // Find a suitable agent
                Agent agent = selectAgent(readyAgents, order, rack);

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
    }

    /**
     * Select the optimal racks fulfilling this order. Implementation is based on the
     * approach found in this paper "Optimal Selection Of Movable Shelves Under
     * Cargo-to-person Picking Mode".
     *
     * @param order {@code Order}       The order for which we select the most suitable racks.
     * @return list of the most suitable {@code Rack}s for fulfilling the order.
     */
    public static List<Rack> selectRacks(Order order, Set<Agent> readyAgents) {
        // Refill orders as assigned with a rack by default
        if (order instanceof RefillOrder) {
            return new ArrayList<>(List.of(((RefillOrder) order).getRefillRack()));
        }

        // Get all candidate racks and their round trip costs
        Map<Rack, Integer> candidateRacks = RackSelector.getCandidateRacks(order.iterator(), order.getDeliveryGate().getPosition());

        // Create necessary maps
        Set<Agent> idleAgents = new HashSet<>();

        if (readyAgents != null) {
            idleAgents.addAll(readyAgents);
        }

        Map<Rack, Agent> ret = new HashMap<>();
        Map<Rack, Integer> selectedRacks = new HashMap<>();
        Map<Item, Integer> selectedRacksItemsQs = new HashMap<>();

        int estimatedCost = 0;
        int orderTotalQs = order.getPendingUnits();

        //
        // Stage 1: Find an initial solution
        //

        // TODO Add if looped twice without adding anything return empty list
        // TODO Put Constraint on the second stage
        while (orderTotalQs > 0 && candidateRacks.size() > 0) {
            Rack bestRack = null;
            double bestRank = 1e9;
            Map<Item, Integer> bestRackItemsQs = null;

            for (var rackEntry : candidateRacks.entrySet()) {
                Rack rack = rackEntry.getKey();

                // Calculate the maximum needed quantity of order items that can be taken out of each candidate rack
                Map<Item, Integer> rackItemsQs = RackSelector.rackOrderItemsSupply(rack, order.iterator());

                int rackTotalItemSupply = rackItemsQs.values().stream().reduce(0, Integer::sum);

                // Ignore rack, doesn't offer new items to the current accepted racks set
                if (rackTotalItemSupply == 0) {
                    candidateRacks.remove(rack);
                    continue;
                }

                // Calculate the rate of this rack
                double rackCostRate = 1. * candidateRacks.get(rack) / rackTotalItemSupply;

                if (Double.compare(rackCostRate, bestRank) < 0) {
                    bestRack = rack;
                    bestRank = rackCostRate;
                    bestRackItemsQs = rackItemsQs;
                }
            }

            assert bestRack != null;

            // Accept the best rack and Remove from the candidate racks list if it can be assigned to an agent
            int bestRackCost = candidateRacks.get(bestRack);
            candidateRacks.remove(bestRack);

            if (readyAgents != null) {
                Agent agent = selectAgent(idleAgents, order, bestRack);
                if (agent == null) {
                    continue;
                }
                ret.put(bestRack, agent);
                idleAgents.remove(agent);
            }

            estimatedCost += bestRackCost;
            selectedRacks.put(bestRack, bestRackCost);

            // Update the left quantities of the orders items and the total selected racks quantities
            orderTotalQs = 0;
            for (var itemEntry : order) {
                Item item = itemEntry.getKey();
                int q = itemEntry.getValue();
                int bestRackQ = bestRackItemsQs.get(item);

                orderTotalQs += (q - bestRackQ);
                selectedRacksItemsQs.put(item, selectedRacksItemsQs.getOrDefault(item, 0) + bestRackQ);
            }
        }

        //
        // Stage 2. Delete Redundant racks from the final set, Do this if the racks count is less than some threshold
        //
        if (selectedRacks.size() < 1000) {
            RackSelector.removeRedundantRack(selectedRacks, candidateRacks, selectedRacksItemsQs, order, true);
        }

        estimatedCost = 0;
        for (Rack rack : selectedRacks.keySet()) {
            estimatedCost += selectedRacks.get(rack);
        }

        return new ArrayList<>(selectedRacks.keySet());
    }

    /**
     * Selects a suitable {@code Agent} for carrying out the given {@code Rack}.
     *
     * @param readyAgents the set of all idle agents.
     * @param order       the {@code Order} to select an {@code Agent} for.
     * @param rack        the {@code Rack} to.
     *
     * @return a suitable {@code Agent}.
     */
    private static Agent selectAgent(Set<Agent> readyAgents, Order order, Rack rack) {
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
            int dis = rack.getDistanceTo(agent.getRow(), agent.getCol());

            // Select the current agent if it is nearer to the rack
            if (distance > dis) {
                distance = dis;
                ret = agent;
            }
        }

        // Return the selected agent if reachable
        return (distance == Integer.MAX_VALUE) ? null : ret;
    }

    /**
     * @param readyAgents the set of all idle agents.
     * @param order       the {@code Order} to select a {@code Rack} for.
     * @return a suitable {@code Rack}.
     * @deprecated Selects a suitable {@code Rack} to partially fulfill the given {@code Order}.
     */
    private static Rack selectRack(Set<Agent> readyAgents, Order order) { // TODO
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
}
