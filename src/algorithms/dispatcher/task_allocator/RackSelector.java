package algorithms.dispatcher.task_allocator;

import models.facilities.Rack;
import models.items.Item;
import models.maps.MapGrid;
import models.maps.utils.Position;
import models.tasks.Order;
import models.warehouses.Warehouse;
import utils.Constants.*;
import utils.Pair;

import java.util.*;


public class RackSelector {
    /**
     * Select the optimal racks fulfilling this order. Implementation is based on the
     * approach found in this paper "Optimal Selection Of Movable Shelves Under
     * Cargo-to-person Picking Mode".
     *
     * @param order {@code Order}       The order for which we select the most suitable racks.
     * @return list of the most suitable {@code Rack}s for fulfilling the order.
     */
    public static List<Rack> selectRacks(Order order) {
        // Get all candidate racks and their round trip costs
        Map<Rack, Integer> candidateRacks = getCandidateRacks(order.iterator(), order.getDeliveryGate().getPosition());

        // Create necessary maps
        Map<Rack, Integer> ignoredRacks = new HashMap<>();
        Map<Rack, Integer> selectedRacks = new HashMap<>();
        Map<Item, Integer> selectedRacksItemsQs = new HashMap<>();

        int estimatedCost = 0;
        int orderTotalQs = order.getPendingUnits();

        //
        // Stage 1: Find an initial solution
        //

        while (orderTotalQs > 0 && candidateRacks.size() > 0) {
            Rack bestRack = null;
            double bestRank = 1e9;
            Map<Item, Integer> bestRackItemsQs = null;

            for (Map.Entry<Rack, Integer> rackEntry : candidateRacks.entrySet()) {
                Rack rack = rackEntry.getKey();

                // Calculate the maximum needed quantity of order items that can be taken out of each candidate rack
                Map<Item, Integer> rackItemsQs = rackOrderItemsSupply(rack, order.iterator());

                int rackTotalItemSupply = rackItemsQs.values().stream().reduce(0, Integer::sum);

                // Ignore rack, doesn't offer new items to the current accepted racks set
                if (rackTotalItemSupply == 0) {
                    ignoredRacks.put(rack, rackEntry.getValue());
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

            // Accept the best rack and Remove from the candidate racks list
            estimatedCost += candidateRacks.get(bestRack);
            selectedRacks.put(bestRack, candidateRacks.get(bestRack));
            candidateRacks.remove(bestRack);

            // Update the left quantities of the orders items and the total selected racks quantities
            orderTotalQs = 0;
            for (Map.Entry<Item, Integer> itemEntry : order) {
                Item item = itemEntry.getKey();
                int q = itemEntry.getValue();
                int bestRackQ = bestRackItemsQs.get(item);

                orderTotalQs += (q - bestRackQ);
                selectedRacksItemsQs.put(item, selectedRacksItemsQs.getOrDefault(item, 0) + bestRackQ);
            }
        }

        //
        // Stage 2. Delete Redundant racks from the final set
        //

        removeRedundantRack(selectedRacks, candidateRacks, selectedRacksItemsQs, order, true);
        estimatedCost = 0;
        for (Rack rack : selectedRacks.keySet()) {
            estimatedCost += selectedRacks.get(rack);
        }

        //
        // Stage 3. Improve the final rack list; Exchange strategy
        //

        Map<Rack, Integer> tmpCandidateRacks = new HashMap<>();

        estimatedCost = exchangeRacks(ignoredRacks, selectedRacks, tmpCandidateRacks, selectedRacksItemsQs, order, estimatedCost);
        estimatedCost = exchangeRacks(candidateRacks, selectedRacks, tmpCandidateRacks, selectedRacksItemsQs, order, estimatedCost);

        return new ArrayList<>(selectedRacks.keySet());
    }

    /**
     * Apply Exchange step to the current accepted racks with the ignored and candidate racks
     * for better refinements if possible.
     *
     * @param rackSet              HashMap racks where we want to exchange each of its racks with ones in the accepted racks.
     * @param selectedRacks        HashMap racks the accepted racks needs refinements.
     * @param tmpCandidateRacks    HashMap racks where removed racks from the accepted racks are put due to exchange operation.
     * @param selectedRacksItemsQs Map of the total quantities of order items found in the current selected @{code Rack}s.
     * @param order                List of the orders @{code Item}s.
     * @param estCost              Estimated round trip costs of the current accepted racks.
     */
    private static int exchangeRacks(Map<Rack, Integer> rackSet, Map<Rack, Integer> selectedRacks,
                                     Map<Rack, Integer> tmpCandidateRacks,
                                     Map<Item, Integer> selectedRacksItemsQs, Order order, int estCost) {
        for (Map.Entry<Rack, Integer> rackEntry : rackSet.entrySet()) {
            Rack rack = rackEntry.getKey();
            Map<Item, Integer> q = new HashMap<>();

            for (Item item : selectedRacksItemsQs.keySet()) {
                q.put(item, selectedRacksItemsQs.get(item) + rack.get(item));
            }

            if (q.equals(selectedRacksItemsQs)) // Not making any differences
                continue;

            // Check whether it's worth updating
            selectedRacks.put(rack, rackEntry.getValue());
            int savedCost = removeRedundantRack(selectedRacks, tmpCandidateRacks, q, order, false);

            if (savedCost > rackEntry.getValue()) {
                removeRedundantRack(selectedRacks, tmpCandidateRacks, q, order, true);
                estCost = estCost + rackEntry.getValue() - savedCost;
            } else {
                selectedRacks.remove(rack);
            }

        }
        return estCost;
    }

    /**
     * Helper Function to the main task creation algorithm.
     * Calculate the cost of the redundant racks and Remove the redundant racks found in the src map
     * and put them in the dest map if allowed.
     *
     * @param src        HashMap racks map in question where we want to calculate the saved cost of the redundant.
     * @param dest       HashMap racks map where to out the removed racks if allowed.
     * @param order      {@code Order} the current order
     * @param srcRacksQs List of the total quantities of items found in the current selected src @{code Rack}s.
     * @param updateMaps boolean Remove the redundant racks from the src and put in the dest or not.
     * @return Integer the saved cost.
     */
    private static int removeRedundantRack(Map<Rack, Integer> src, Map<Rack, Integer> dest,
                                           Map<Item, Integer> srcRacksQs, Order order, boolean updateMaps) {
        int savedCost = 0;
        Iterator<Map.Entry<Rack, Integer>> rackEntriesIterator = src.entrySet().iterator();
        while (rackEntriesIterator.hasNext()) {
            Map.Entry<Rack, Integer> rackEntry = rackEntriesIterator.next();
            Rack rack = rackEntry.getKey();

            boolean redundant = true;

            Iterator<Item> it = srcRacksQs.keySet().iterator();
            while (redundant && it.hasNext()) {
                Item item = it.next();

                if (srcRacksQs.get(item) - rack.get(item) < order.get(item))
                    redundant = false;
            }

            if (redundant) {
                savedCost += rackEntry.getValue();

                it = srcRacksQs.keySet().iterator();
                while (it.hasNext()) {
                    Item item = it.next();

                    srcRacksQs.put(item, srcRacksQs.get(item) - rack.get(item));
                }

                if (updateMaps) {
                    dest.put(rack, rackEntry.getValue());
                    rackEntriesIterator.remove();
                }
            }
        }
        return savedCost;
    }

    /**
     * Get the maximum needed quantities of order items that can be taken out of each candidate rack.
     *
     * @param rack       @{code Rack} rack in question.
     * @param orderItems Iterator to the list of the orders @{code Item}s.
     * @return int[] the maximum needed quantities of order items that can be taken out of each candidate rack.
     */
    private static Map<Item, Integer> rackOrderItemsSupply(Rack rack, Iterator<Map.Entry<Item, Integer>> orderItems) {
        Map<Item, Integer> ret = new HashMap<>();

        while (orderItems.hasNext()) {
            Map.Entry<Item, Integer> itemEntry = orderItems.next();
            ret.put(itemEntry.getKey(), Math.min(itemEntry.getValue(), rack.get(itemEntry.getKey())));
        }

        return ret;
    }

    /**
     * Get the estimated round trip cost between a rack and a certain gate.
     *
     * @param rackPos {@code Position} the position of the rack in question.
     * @param gatePos {@code Position} the position of the gate in question.
     * @return the estimated round trip cost
     */
    private static Integer getRoundTripCost(Position rackPos, Position gatePos) {
        // Get warehouse map
        MapGrid map = Warehouse.getInstance().getMap();

        Queue<Pair<Position, Integer>> q = new LinkedList<>();
        Map<Position, Boolean> vis = new HashMap<>();
        q.add(new Pair<>(rackPos, 0));

        while (!q.isEmpty()) {
            Pair<Position, Integer> u = q.remove();

            vis.put(u.key, true);

            if (u.key.equals(gatePos)) {
                return u.val * 2;
            }

            // Get empty directions
            List<Direction> dirs = map.getEmptyDirections(u.key);

            for (Direction dir : dirs) {
                if (!vis.containsKey(map.next(u.key, dir))) {
                    q.add(new Pair<>(map.next(u.key, dir), u.val + 1));
                }
            }
        }
        return Integer.MAX_VALUE;
    }


    /**
     * Get all the candidate racks for the specific items.
     *
     * @param itemsIterator iterator to the {@code item}s of the order.
     * @param gPos          {@code Position} gate position.
     * @return a map of all candidate racks and their round trip costs.
     */
    private static Map<Rack, Integer> getCandidateRacks(Iterator<Map.Entry<Item, Integer>> itemsIterator, Position gPos) {
        Map<Rack, Integer> ret = new HashMap<>();

        // Get all candidate racks
        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next().getKey();

            for (Map.Entry<Rack, Integer> rack : item) {
                ret.putIfAbsent(rack.getKey(), 0);
            }
        }

        // Calculate the round trip cost
        for (Rack rack : ret.keySet()) {
            ret.put(rack, getRoundTripCost(rack.getPosition(), gPos));
        }

        return ret;
    }

}
