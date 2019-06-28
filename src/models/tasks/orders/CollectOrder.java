package models.tasks.orders;

import models.facilities.Gate;
import models.facilities.Rack;
import models.items.Item;
import models.tasks.Task;
import models.warehouses.Warehouse;

import utils.Utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This {@code CollectOrder} class represents an order of collect type in our Hive Warehousing System.
 * <p>
 * A collect order is an {@link Order} used to collect a set of {@link Item Items}
 * from the {@link Rack Racks} of the {@link Warehouse} and deliver them to a specified {@link Gate}.
 *
 * @see Task
 * @see Item
 * @see Gate
 * @see Rack
 * @see Order
 * @see RefillOrder
 */
public class CollectOrder extends Order {

    /**
     * Constructs a new {@code CollectOrder} object.
     *
     * @param id   the id of the {@code Order}.
     * @param gate the delivery {@code Gate} of the {@code Order}.
     */
    public CollectOrder(int id, Gate gate) {
        super(id, gate);
    }

    /**
     * Returns the set of candidate racks that can supply this {@code Order}.
     *
     * @return a set of all candidate racks.
     */
    @Override
    public Set<Rack> getCandidateRacks() {
        Set<Rack> ret = new HashSet<>();

        for (var i : this) {
            Item item = i.getKey();

            for (var r : item) {
                Rack rack = r.getKey();

                if (deliveryGate.getDistanceTo(rack.getPosition()) < Integer.MAX_VALUE) {
                    ret.add(r.getKey());
                }
            }
        }

        return ret;
    }

    /**
     * Plans the set of items to reserve for the favor of this {@code Order}
     * by the given {@code Task}.
     *
     * @param task the {@code Task} responsible for carrying out the reservation.
     */
    @Override
    protected void planItemsToReserve(Task task) {
        Map<Item, Integer> items = new HashMap<>();

        Rack rack = task.getRack();

        for (var pair : pendingItems.entrySet()) {
            Item item = pair.getKey();
            int neededQuantity = pair.getValue();
            int availableQuantity = rack.get(item);
            int plannedQuantity = Math.min(neededQuantity, availableQuantity);

            if (plannedQuantity != 0) {
                items.put(item, plannedQuantity);
            }
        }

        reservedItems.put(task, items);
    }

    /**
     * Returns a string representation of this {@code Order}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Order}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("CollectOrder: {");
        builder.append(" id: ").append(id).append(",");
        builder.append(" gate_id: ").append(deliveryGate.getId()).append(",");
        builder.append(" items: ").append(Utility.stringifyItemQuantities(pendingItems));
        builder.append(" }");

        return builder.toString();
    }
}
