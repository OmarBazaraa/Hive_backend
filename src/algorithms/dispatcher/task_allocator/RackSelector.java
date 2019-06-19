package algorithms.dispatcher.task_allocator;

import models.facilities.Rack;
import models.items.Item;
import models.maps.utils.Position;
import models.tasks.orders.Order;

import java.util.*;


public class RackSelector {
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
    public static int exchangeRacks(Map<Rack, Integer> rackSet, Map<Rack, Integer> selectedRacks,
                                    Map<Rack, Integer> tmpCandidateRacks,
                                    Map<Item, Integer> selectedRacksItemsQs, Order order, int estCost) {
        for (var rackEntry : rackSet.entrySet()) {
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
    public static int removeRedundantRack(Map<Rack, Integer> src, Map<Rack, Integer> dest,
                                          Map<Item, Integer> srcRacksQs, Order order, boolean updateMaps) {
        int savedCost = 0;
        var rackEntriesIterator = src.entrySet().iterator();
        while (rackEntriesIterator.hasNext()) {
            var rackEntry = rackEntriesIterator.next();
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
    public static Map<Item, Integer> rackOrderItemsSupply(Rack rack, Iterator<Map.Entry<Item, Integer>> orderItems) {
        Map<Item, Integer> ret = new HashMap<>();

        while (orderItems.hasNext()) {
            var itemEntry = orderItems.next();
            ret.put(itemEntry.getKey(), Math.min(itemEntry.getValue(), rack.get(itemEntry.getKey())));
        }

        return ret;
    }

    /**
     * Get the maximum provided quantities of order items that can be taken out of each candidate rack.
     *
     * @param rack       @{code Rack} rack in question.
     * @param orderItems Iterator to the list of the orders @{code Item}s.
     * @return int[] the maximum provided quantities of order items that can be taken out of each candidate rack.
     */
    public static Map<Item, Integer> rackMaxOrderItemsSupply(Rack rack, Iterator<Map.Entry<Item, Integer>> orderItems) {
        Map<Item, Integer> ret = new HashMap<>();

        while (orderItems.hasNext()) {
            var itemEntry = orderItems.next();
            ret.put(itemEntry.getKey(), rack.get(itemEntry.getKey()));
        }

        return ret;
    }

    /**
     * Get all the candidate racks for the specific items.
     *
     * @param itemsIterator iterator to the {@code item}s of the order.
     * @param gPos          {@code Position} gate position.
     * @return a map of all candidate racks and their round trip costs.
     */
    public static Map<Rack, Integer> getCandidateRacks(Iterator<Map.Entry<Item, Integer>> itemsIterator, Position gPos) {
        Map<Rack, Integer> ret = new HashMap<>();

        // Get all candidate racks
        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next().getKey();

            for (var racks : item) {
                ret.putIfAbsent(racks.getKey(), 0);
            }
        }

        // Calculate the round trip cost
        for (Rack rack : ret.keySet()) {
            ret.put(rack, rack.getDistanceTo(gPos));
        }

        return ret;
    }
}
