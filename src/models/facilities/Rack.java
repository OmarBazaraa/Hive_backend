package models.facilities;

import models.agents.Agent;
import models.items.Item;
import models.items.QuantityAddable;
import models.items.QuantityReservable;

import utils.Constants;
import utils.Utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This {@code Rack} class is a one of the {@link Facility} components
 * in our Hive Warehouse System.
 * <p>
 * A rack component is a container located in the {@link models.warehouses.Warehouse Warehouse} grid
 * where {@link Item Items} are stored.
 * A rack can possibly contain different items with different quantities.
 * <p>
 * A rack by itself is a static {@link Facility} component,
 * but it is different from other facilities in the point that it can be loaded and be moved around by
 * an {@link Agent}.
 *
 * @see models.HiveObject HiveObject
 * @see models.agents.Agent Agent
 * @see models.facilities.Facility Facility
 * @see models.facilities.Gate Gate
 * @see models.facilities.Station Station
 * @see models.tasks.Task Task
 */
public class Rack extends Facility implements QuantityAddable<Item>, QuantityReservable<Item> {

    //
    // Member Variables
    //

    /**
     * The maximum storing weight of the {@code Rack}.
     */
    private int capacity = Constants.RACK_DEFAULT_STORE_CAPACITY;

    /**
     * The weight of the {@code Rack} itself when being empty.
     */
    private int containerWeight = Constants.RACK_DEFAULT_CONTAINER_WEIGHT;

    /**
     * The total stored weight of all the items in the {@code Rack}.
     */
    private int storedWeight;

    /**
     * The map of available items this {@code Rack} is storing.<p>
     * The key is an {@code Item}.<p>
     * The mapped value represents the available quantity of this {@code Item}.
     */
    private Map<Item, Integer> items = new HashMap<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Rack} object.
     */
    public Rack() {
        super();
    }

    /**
     * Constructs a new {@code Rack} object.
     *
     * @param id              the id of the {@code Rack}.
     * @param capacity        the maximum storing weight of the {@code Rack}.
     * @param containerWeight the weight of the {@code Rack}'s container.
     */
    public Rack(int id, int capacity, int containerWeight) {
        super(id);
        this.capacity = capacity;
        this.containerWeight = containerWeight;
    }

    /**
     * Returns the maximum storing weight of this {@code Rack}.
     *
     * @return the storing capacity of the {@code Rack}.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the weight of the {@code Rack} itself when being empty.
     *
     * @return the weight of the {@code Rack}'s container.
     */
    public int getContainerWeight() {
        return containerWeight;
    }

    /**
     * Returns the stored weight in this {@code Rack}.
     *
     * @return the current stored weight of this {@code Rack}.
     */
    public int getStoredWeight() {
        return storedWeight;
    }

    /**
     * Returns the total weight of this {@code Rack}.
     * <p>
     * That is, the weight of the container plus the total weight of the stored units.
     *
     * @return the current total weight of this {@code Rack}.
     */
    public int getTotalWeight() {
        return containerWeight + storedWeight;
    }

    /**
     * Returns the current available number of an {@code Item} units
     * in this {@code Rack}.
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
     * Adds or removes some units of an {@code Item} into/from this {@code Rack}.
     * <p>
     * This function is used to add extra units of the given {@code Item} if the given
     * quantity is positive,
     * and used to remove existing units if the given quantity is negative.
     * <p>
     * This function should be called with appropriate parameters so that:
     * <ol>
     * <li>the total capacity of this {@code Rack} does not exceed the maximum limit</li>
     * <li>no {@code Item} has negative number of units</li>
     * </ol>
     *
     * @param item     the {@code Item} to be updated.
     * @param quantity the quantity to be updated with.
     */
    @Override
    public void add(Item item, int quantity) {
        item.add(this, quantity);
        QuantityAddable.update(items, item, quantity);
        storedWeight += quantity * item.getWeight();
    }

    /**
     * Reserves some number of units of the given {@code Item} in this {@code Rack}.
     * Reservation can be confirmed or undone by passing negative quantities.
     * <p>
     * This function should be called after ensuring that reservation is possible.
     * <p>
     * This function should only be called once per {@code Task} activation.
     *
     * @param item     the {@code Item} to reserve.
     * @param quantity the quantity to reserve.
     */
    @Override
    public void reserve(Item item, int quantity) {
        item.reserve(this, quantity);
        QuantityAddable.update(items, item, -quantity);
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
     * Binds this {@code Rack} with the given {@code Agent}.
     * <p>
     * It is preferable to allocate the {@code Rack} before binding it to an {@code Agent}.
     * <p>
     * This function should be called after checking that it is currently possible to bind
     * the given {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @param agent the {@code Agent} to bind.
     */
    @Override
    public void bind(Agent agent) {
        super.bind(agent);
        agent.loadRack(this);
    }

    /**
     * Unbinds the bound {@code Agent} from this {@code Rack}.
     * <p>
     * This function should be called after checking that it is currently possible to unbind
     * the bound {@code Agent}; otherwise un-expected behaviour could occur.
     */
    @Override
    public void unbind() {
        boundAgent.offloadRack(this);
        super.unbind();
    }

    /**
     * Returns a string representation of this {@code Rack}.
     * In general, the toString method returns a string that "textually represents" this object.
     *
     * @return a string representation of this {@code Rack}.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Rack: {");
        builder.append(" id: ").append(id).append(",");
        builder.append(" pos: ").append(getPosition()).append(", ");
        builder.append(" capacity: ").append(capacity).append(",");
        builder.append(" weight: ").append(containerWeight).append(",");
        builder.append(" stored_weight: ").append(storedWeight).append(",");
        builder.append(" items: ").append(Utility.stringifyItemQuantities(items));
        builder.append(" }");

        return builder.toString();
    }
}
