package algorithms.dispatcher;

import models.facilities.Rack;
import models.items.Item;
import models.maps.MapGrid;
import models.maps.utils.Position;
import models.warehouses.Warehouse;
import utils.Constants.*;
import utils.Pair;

import java.util.*;

public class RackSelector {
    /**
     * WIP
     * TODO @Samir55 complete the Docs here upon function implementation and optimization completion.
     * TODO @Samir55, Either remove useless racks or prevent sending them at the first place.
     * TODO @Samir55 Polish, Improve & Choose better variable names.
     *
     * @param gatePos         todo
     * @param racksList       todo
     * @param items           todo
     * @param itemsQuantities todo
     * @return rack the most suitable {@code Rack}s for fulfilling the order found in the list.
     */
    public static List<Rack> selectRacks(Position gatePos, List<Rack> racksList, List<Item> items, int[] itemsQuantities) {
        int itemsCount = items.size();

        Map<Rack, Integer> candidateRacks = new HashMap<>(); // Map of candidate racks and their round trip cost
        Map<Rack, Integer> ignoredRacks = new HashMap<>();
        Map<Rack, Integer> acceptedRacks = new HashMap<>();

        int[] acceptedRacksItemsQuantities = new int[itemsCount];
        int estCost = 0;

        // Calculate the round trip cost using Manhattan distance between each rack and the Gate position
        for (Rack rack : racksList) {
//            candidateRacks.put(rack, (Math.abs(rack.getPosition().row - gatePos.row) +
//                    Math.abs(rack.getPosition().col - gatePos.col)));

            candidateRacks.put(rack, getRoundTripCost(rack.getPosition(), gatePos));
        }

        int[] quantities = itemsQuantities.clone();
        int totalQuantities = Arrays.stream(quantities).sum();

        // Stage 1: Find an initial solution
        while (totalQuantities > 0 && candidateRacks.size() > 0) {
            Map<Rack, int[]> racksItemsQuantities = new HashMap<>(); // TODO @Samir55 CHANGE NAME HERE.

            Rack bestRack = null;
            double bestRate = 1e9;

            for (Map.Entry<Rack, Integer> rackEntry : candidateRacks.entrySet()) {
                Rack rack = rackEntry.getKey();

                // Calculate the maximum needed quantity of order items that can be taken out of each candidate rack
                racksItemsQuantities.put(rack, rackItemsSupply(rack, items, itemsQuantities));

                int rackTotalItemSupply = Arrays.stream(racksItemsQuantities.get(rack)).sum();

                // Ignore rack, doesn't offer new items to the current accepted racks set
                if (rackTotalItemSupply == 0) {
                    ignoredRacks.put(rack, rackEntry.getValue());
                    candidateRacks.remove(rack);
                    continue;
                }

                // Calculate the rate of this rack
                double rackCostRate = 1. * candidateRacks.get(rack) / rackTotalItemSupply;

                if (Double.compare(rackCostRate, bestRate) < 0) {
                    bestRate = rackCostRate;
                    bestRack = rack;
                }
            }

            // Accept the best rack.
            assert bestRack != null;
            int bestRackCost = candidateRacks.get(bestRack);
            acceptedRacks.put(bestRack, bestRackCost);
            estCost += bestRackCost;

            // Remove from the candidate racks list
            candidateRacks.remove(bestRack);

            // Update the left quantities of the orders items and the total selected racks quantities
            totalQuantities = 0;
            for (int j = 0; j < itemsCount; j++) {
                quantities[j] -= racksItemsQuantities.get(bestRack)[j];
                totalQuantities += quantities[j];
                acceptedRacksItemsQuantities[j] += bestRack.get(items.get(j));
            }
        }

        // Stage 2. Delete Redundant racks from the final set
        removeRedundantRack(acceptedRacks, candidateRacks, items, acceptedRacksItemsQuantities, itemsQuantities,
                true);
        estCost = 0;
        for (Rack rack : acceptedRacks.keySet()) {
            estCost += acceptedRacks.get(rack);
        }

        // Stage 3. Improve the final rack list; Exchange strategy
        Map<Rack, Integer> tmpCandidateRacks = new HashMap<>();

        estCost = exchangeRacks(ignoredRacks, acceptedRacks, tmpCandidateRacks, acceptedRacksItemsQuantities, items,
                itemsQuantities, estCost);
        estCost = exchangeRacks(candidateRacks, acceptedRacks, tmpCandidateRacks, acceptedRacksItemsQuantities, items,
                itemsQuantities, estCost);

        return new ArrayList<>(acceptedRacks.keySet());
    }

    /**
     * WIP
     * Apply Exchange step to the current accepted racks with the ignored and candidate racks for better refinements if
     * possible.
     *
     * @param rackSet                 HashMap racks where we want to exchange each of its racks with ones in the accepted racks.
     * @param acceptedRacks           HashMap racks the accepted racks needs refinements.
     * @param tmpCandidateRacks       HashMap racks where we put the removed racks from the accepted racks due to exchange operation.
     * @param accRacksItemsQuantities List of the total quantities of order items found in the current selected @{code Rack}s.
     * @param items                   List of the orders @{code Item}s.
     * @param orderQuantities         List of the needed orders @{code Item}s quantities.
     * @param estCost                 Estimated round trip costs of the current accepted racks.
     */
    private static int exchangeRacks(Map<Rack, Integer> rackSet, Map<Rack, Integer> acceptedRacks,
                                     Map<Rack, Integer> tmpCandidateRacks, int[] accRacksItemsQuantities,
                                     List<Item> items, int[] orderQuantities, int estCost) {
        for (Map.Entry<Rack, Integer> rackEntry : rackSet.entrySet()) {
            Rack rack = rackEntry.getKey();
            int[] q = accRacksItemsQuantities.clone();

            for (int j = 0; j < q.length; j++)
                q[j] += rack.get(items.get(j));

            if (Arrays.equals(q, accRacksItemsQuantities)) // Not making any differences
                continue;

            // Check whether it's worth updating
            acceptedRacks.put(rack, rackEntry.getValue());
            int savedCost = removeRedundantRack(acceptedRacks, tmpCandidateRacks, items, q, orderQuantities, false); // FIXME @Samir55

            if (savedCost > rackEntry.getValue()) {
                removeRedundantRack(acceptedRacks, tmpCandidateRacks, items, q, orderQuantities, true);
                estCost = estCost + rackEntry.getValue() - savedCost;
            } else {
                acceptedRacks.remove(rack);
            }

        }
        return estCost;
    }

    /**
     * WIP
     * Helper Function to the main task creation algorithm.
     * Calculate the cost of the redundant racks and Remove the redundant racks found in the src map
     * and put them in the dest map if allowed.
     *
     * @param src                 HashMap racks map in question where we want to calculate the saved cost of the redundant.
     * @param dest                HashMap racks map where to out the removed racks if allowed.
     * @param items               List of the orders @{code Item}s.
     * @param currRacksQuantities List of the total quantities of order items found in the current selected @{code Rack}s.
     * @param orderQuantities     List of the needed orders @{code Item}s quantities.
     * @param updateMaps          boolean Remove the redundant racks from the src and put in the dest or not.
     * @return Integer the saved cost.
     */
    private static int removeRedundantRack(Map<Rack, Integer> src, Map<Rack, Integer> dest, List<Item> items, int[] currRacksQuantities, int[] orderQuantities, boolean updateMaps) {
        int savedCost = 0;
        Iterator<Map.Entry<Rack, Integer>> rackEntriesIterator = src.entrySet().iterator();
        while (rackEntriesIterator.hasNext()) {
            Map.Entry<Rack, Integer> rackEntry = rackEntriesIterator.next();
            Rack rack = rackEntry.getKey();

            boolean redundant = true;

            for (int j = 0; redundant && j < orderQuantities.length; j++) {
                if (currRacksQuantities[j] - rack.get(items.get(j)) < orderQuantities[j])
                    redundant = false;
            }

            if (redundant) {
                savedCost += rackEntry.getValue();

                for (int j = 0; j < orderQuantities.length; j++) {
                    currRacksQuantities[j] -= rack.get(items.get(j));
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
     * @param rack            @{code Rack} rack in question.
     * @param items           List list of the orders @{code Item}s.
     * @param orderQuantities int[] array of the needed quantities.
     * @return int[] the maximum needed quantities of order items that can be taken out of each candidate rack.
     */
    private static int[] rackItemsSupply(Rack rack, List<Item> items, int[] orderQuantities) {
        int[] ret = new int[orderQuantities.length];

        for (int j = 0; j < items.size(); j++)
            ret[j] = Math.min(orderQuantities[j], rack.get(items.get(j)));

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
        q.add(new Pair<>(rackPos, 1));

        while (!q.isEmpty()) {
            Pair<Position, Integer> u = q.remove();

            if (u.key.equals(gatePos)) {
                return u.val * 2;
            }

            // Get empty directions
            List<Direction> dirs = map.getEmptyDirections(u.key);

            for (Direction dir : dirs) {
                q.add(new Pair<>(map.next(u.key, dir), u.val + 1));
            }
        }
        return Integer.MAX_VALUE;
    }

}
