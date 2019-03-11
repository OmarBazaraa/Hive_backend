package models.facilities;

import models.agents.Agent;
import models.items.Item;
import models.items.QuantityAddable;
import models.items.QuantityReservable;
import models.tasks.Task;

import models.tasks.TaskAssignable;
import utils.Constants;
import utils.Constants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This {@code Rack} class is a one of the {@link Facility} components
 * in our Hive Warehouse System.
 * <p>
 * A rack component is a container located in the warehouse's grid where items and goods
 * are stored.
 * A rack can possibly contain different items with different quantities.
 * <p>
 * A rack by itself is a static {@link Facility} component,
 * but it is different from other facilities in that it can be loaded and move around by
 * an {@link Agent}.
 *
 * @see Facility
 * @see Gate
 * @see Station
 * @see Agent
 */
public class Rack extends Facility implements QuantityAddable<Item>, QuantityReservable<Item>, TaskAssignable {

    //
    // Member Variables
    //

    /**
     * The storing capacity (in weight units) of this {@code Rack}.
     */
    private int capacity = Constants.RACK_DEFAULT_STORE_CAPACITY;

    /**
     * The total stored weight of all the {@code Item}s in this {@code Rack}.
     */
    private int weight;

    /**
     * The map of all {@code Item}s this {@code Rack} is storing.<p>
     * The key is an {@code Item}.<p>
     * The mapped value represents the quantity of this {@code Item}.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The current status of this {@code Rack}.
     */
    private RackStatus status = RackStatus.IDLE;

    /**
     * The assigned {@code Task} responsible of delivering this {@code Rack}.
     */
    private Task task;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Rack} object.
     *
     * @param id  the id of the {@code Rack}.
     * @param row the row position of the {@code Rack}.
     * @param col the column position of the {@code Rack}.
     */
    public Rack(int id, int row, int col) {
        super(id, row, col);
    }

    /**
     * Returns the storing capacity (in weight units) of this {@code Rack}.
     *
     * @return the maximum storing capacity of this rack.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the storing capacity (in weight units) of this {@code Rack}.
     *
     * @param cap the new maximum storing capacity.
     */
    public void setCapacity(int cap) throws Exception {
        if (cap < weight) {
            throw new Exception("Unable to reduce the size of the rack!");
        }

        capacity = cap;
    }

    /**
     * Returns the current stored weight in this {@code Rack}.
     *
     * @return the current stored weight.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Updates the weight of this {@code Rack} by the given amount.
     *
     * @param w the weight to be added/removed.
     */
    private void updateWeight(int w) throws Exception {
        weight += w;

        if (weight < 0) {
            throw new Exception("Invalid negative weight of the rack!");
        }

        if (weight > capacity) {
            throw new Exception("The weight of the rack exceeded maximum capacity!");
        }
    }

    /**
     * Updates the quantity of an {@code Item} in this {@code Rack}
     * without updating the weight of the rack.
     * <p>
     * This function is used to add extra units of the given {@code Item} if the given
     * quantity is positive,
     * and used to remove existing units if the given quantity is negative.
     *
     * @param item     the {@code Item} to be updated.
     * @param quantity the quantity to be updated with.
     */
    private void updateQuantity(Item item, int quantity) throws Exception {
        int total = quantity + items.getOrDefault(item, 0);

        if (total < 0) {
            throw new Exception("No enough items to be removed from the rack!");
        }

        if (total > 0) {
            items.put(item, total);
        } else {
            items.remove(item);
        }
    }

    /**
     * Returns the current quantity of an {@code Item} in this {@code Rack}.
     *
     * @param item the {@code Item} to get its quantity.
     *
     * @return the quantity of the given {@code Item}.
     */
    @Override
    public int get(Item item) {
        return items.getOrDefault(item, 0);
    }

    /**
     * Updates the quantity of an {@code Item} in this {@code Rack}.
     * <p>
     * This function is used to add extra units of the given {@code Item} if the given
     * quantity is positive,
     * and used to remove existing units if the given quantity is negative.
     *
     * @param item     the {@code Item} to be updated.
     * @param quantity the quantity to be updated with.
     */
    @Override
    public void add(Item item, int quantity) throws Exception {
        updateQuantity(item, quantity);
        updateWeight(quantity * item.getWeight());
        item.add(this, quantity);
    }

    /**
     * Clears and empties this {@code Rack} from all its available {@code Item}s.
     */
    public void clear() throws Exception {
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            add(pair.getKey(), -pair.getValue());
        }
    }

    /**
     * Returns an {@code Iterator} to iterate over the available items in this {@code Rack}.
     * <p>
     * Note that this iterator should be used in read-only operations;
     * otherwise undefined behaviour could arises.
     *
     * @return an {@code Iterator}.
     */
    @Override
    public Iterator<Map.Entry<Item, Integer>> iterator() {
        return items.entrySet().iterator();
    }

    /**
     * Reserves some units specified by the given {@code QuantityAddable} container.
     * <p>
     * This functions removes some items from the rack without actually reducing
     * the weight of this {@code Rack}.
     * The items are physically removed when the reservation is confirmed.
     *
     * @param container the {@code QuantityAddable} container.
     */
    @Override
    public void reserve(QuantityAddable<Item> container) throws Exception {
        for (Map.Entry<Item, Integer> pair : container) {
            updateQuantity(pair.getKey(), -pair.getValue());
        }
    }

    /**
     * Confirms the previously assigned reservations specified by the given
     * {@code QuantityAddable} container, and removes those reserved units from this object.
     * <p></p>
     * This function physically removes some of the reserved items and reduces the weight
     * of this {@code Rack}.
     *
     * @param container the {@code QuantityAddable} container.
     */
    @Override
    public void confirmReservation(QuantityAddable<Item> container) throws Exception {
        for (Map.Entry<Item, Integer> pair : container) {
            updateWeight(-pair.getKey().getWeight() * pair.getValue());
        }
    }

    /**
     * Returns the current status of this {@code Rack}.
     *
     * @return the {@code RackStatus} of this {@code Rack}.
     */
    public RackStatus getStatus() {
        return status;
    }

    /**
     * Assigns a new {@code Task} responsible of delivering this {@code Rack}.
     *
     * @param t the new {@code Task} to assign.
     */
    @Override
    public void assignTask(Task t) throws Exception {
        if (status != RackStatus.IDLE) {
            throw new Exception("The previously assigned task has not been completed yet!");
        }

        // Reserve task items
        reserve(t);

        // Assign task
        task = t;
        status = RackStatus.RESERVED;
    }

    /**
     * The callback function to be invoked when the assigned {@code Task} is completed.
     *
     * @param t the completed {@code Task}.
     */
    @Override
    public void onTaskComplete(Task t) {
        if (task == t) {
            task = null;
            status = RackStatus.IDLE;
        }
    }

    /**
     * Loads this {@code Rack} to be delivered by an {@code Agent}.
     */
    public void load() throws Exception {
        if (status != RackStatus.RESERVED) {
            throw new Exception("Loading un-reserved rack!");
        }

        status = RackStatus.LOADED;
    }

    /**
     * Offloads this {@code Rack} after being delivered by an {@code Agent}.
     */
    public void offload() throws Exception {
        if (status != RackStatus.LOADED) {
            throw new Exception("Offloading un-loaded rack!");
        }

        status = RackStatus.RESERVED;
    }
}
