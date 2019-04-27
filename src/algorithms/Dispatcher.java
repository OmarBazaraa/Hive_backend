package algorithms;

import javafx.geometry.Pos;
import models.agents.Agent;
import models.facilities.Rack;
import models.items.Item;
import models.maps.GuideGrid;
import models.maps.utils.Position;
import models.tasks.Order;
import models.tasks.Task;
import models.warehouses.Warehouse;
import utils.Pair;

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
    public static void dispatch(Order order, Set<Agent> readyAgents) throws Exception {
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

            // Create task and add it to the warehouse
            Task task = new Task(order, rack, agent);
            Warehouse.getInstance().addTask(task);
        }
    }

    /**
     * Finds the best suitable {@code Agent} for the given {@code Task}.
     *
     * @param readyAgent the set of all idle agents.
     * @param rack       the assigned {@code Rack}.
     * @param order      the needed {@code Order}.
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
     * WIP
     * TODO @Samir55 complete the Docs here upon function implementation and optimization completion.
     * TODO @Samir55 Selects the best {@code Rack} according to a specific heuristic search.
     * TODO @Samir55 Polish, Improve & Choose better variable names.
     *
     * @param gatePos         todo
     * @param racks           todo
     * @param items           todo
     * @param itemsQuantities todo
     * @return rack the most suitable {@code Rack}s for fulfilling the order found in the list.
     */
    private static List<Integer> selectRacks(Position gatePos, Map<Integer, Rack> racks, List<Item> items, int[] itemsQuantities) {
        // TODO @Samir55, Either remove useless racks or prevent sending them at the first place.
        // Initialization
        List<Integer> selRacks = new ArrayList<>();
        List<Integer> selRacksCost = new ArrayList<>();
        int[] selRacksQuantities = new int[items.size()];
        Double cost = 0.0;

        List<Integer> candiRacks = new ArrayList<>();
        List<Integer> uselessRacks = new ArrayList<>();
        List<Integer> tripCost = new ArrayList<>();

        for (Map.Entry<Integer, Rack> rack : racks.entrySet())
            candiRacks.add(rack.getKey());

        // Calculate the round trip cost using Manhattan distance between each rack and the Gate position
        for (int i = 0; i < racks.size(); i++) {
            Position rPos = racks.get(candiRacks.get(i)).getPosition();
            tripCost.add(2 * (Math.abs(rPos.row - gatePos.row) + Math.abs(rPos.col - gatePos.col)));
        }

        // Get the number of quantities left
        Integer q = 0;
        for (int i = 0; i < items.size(); i++)
            q += itemsQuantities[i];

        int[] quantities = itemsQuantities.clone();

        while (q > 0 && candiRacks.size() > 0) {

            // Calculate the maximum quantity of order items that can be taken out of each candidate rack
            int k = candiRacks.size();
            int[][] p = new int[k][items.size()];
            int[] b = new int[k];
            double[] costRate = new double[k];

            int argMin = -1;
            double lrate = 1e9;

            for (int i = 0; i < k; i++) {
                Rack rack = racks.get(candiRacks.get(i));

                for (int j = 0; j < items.size(); j++) {
                    p[i][j] = Math.min(quantities[j], rack.get(items.get(j)));
                    b[i] += p[i][j];
                }

                // Useless rack, mark to be removed.
                if (b[i] == 0) {
                    uselessRacks.add(candiRacks.get(i));
                }

                // Calculate the rate of this rack
                costRate[i] = 1. * tripCost.get(i) / b[i];

                if (Double.compare(costRate[i], lrate) < 0) {
                    lrate = costRate[i];
                    argMin = i;
                }
            }

            // Select the rack possessing the least cost rate.
            selRacks.add(candiRacks.get(argMin));
            selRacksCost.add(tripCost.get(argMin));

            cost += tripCost.get(argMin);

            // Remove from the candidate racks list.
            candiRacks.remove(argMin);
            tripCost.remove(argMin);

            // Update the left quantities of the orders and the total selected racks quantities.
            q = 0;
            for (int j = 0; j < items.size(); j++) {
                quantities[j] -= p[argMin][j];
                q += quantities[j];

//                selRacksQuantities[j] += p[argMin][j];
                selRacksQuantities[j] += racks.get(candiRacks.get(argMin)).get(items.get(j));
            }

            // Prepare the new candiRacks list.
            List<Integer> newTripCost = new ArrayList<>();
            List<Integer> newCandiRacks = new ArrayList<>();
            int l = 0;
            for (int i = 0; i < candiRacks.size(); i++) {
                if (uselessRacks.get(l).equals(candiRacks.get(i)))
                    l++;
                else {
                    newCandiRacks.add(candiRacks.get(i));
                    newTripCost.add(tripCost.get(i));
                }
            }
            candiRacks = newCandiRacks;
            tripCost = newTripCost;
        }

        // Stage 2. Delete Redundant racks from the final set.
        List<Integer> finalSet = new ArrayList<>(); // TODO @Samir55 Polishing required here.
        List<Integer> finalSetCost = new ArrayList<>();

        for (int i = 0; i < selRacks.size(); i++) {
            if (!redundantRack(racks.get(selRacks.get(i)), items, selRacksQuantities, itemsQuantities)) {
                finalSet.add(selRacks.get(i));
                finalSetCost.add(selRacksCost.get(i));
            }
        }

        // Stage 3. Improve the final rack list; Exchange strategy.

        return finalSet;
    }

    /**
     * WIP
     * <p>
     * Helper Function to the main task creation algorithm.
     * Check whether the rack is redundant to the the current selected racks list.
     *
     * @param rack                {@code Rack} rack in question.
     * @param items               List of the orders @{code Item}s.
     * @param currRacksQuantities List of the total quantities of order items found in the current selected @{code Rack}s.
     * @param orderQuantities     List of the needed orders @{code Item}s quantities.
     * @return boolean The rack in question is redundant or not.
     */
    private static boolean redundantRack(Rack rack, List<Item> items, int[] currRacksQuantities, int[] orderQuantities) {
        boolean redundant = true;

        for (int j = 0; redundant && j < orderQuantities.length; j++) {
            if (currRacksQuantities[j] - rack.get(items.get(j)) < orderQuantities[j]) // FIXME @Samir55
                redundant = false;
        }

        return redundant;
    }
}
