package models.facilities;

import models.agents.Agent;
import models.items.Item;
import models.items.QuantityAddable;
import models.items.QuantityReservable;

import utils.Constants;

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
     * The mapped value represents the quantity of this {@code Item}.
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
        QuantityAddable.update(items, item, quantity);
        storedWeight += quantity * item.getWeight();
        item.add(this, quantity);
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
     * This functions removes/adds some items from/to the {@code Rack} without
     * actually changing its weight.
     * The items are physically removed/added when the reservation is confirmed.
     * <p>
     * Positive quantities means the items are to be taken from the {@code Rack}.
     * While negative quantities means the items are to be added into the {@code Rack}.
     * <p>
     * This function should only be called once per {@code Task} activation.
     *
     * @param container the {@code QuantityAddable} container.
     *
     * @see Rack#confirmReservation(QuantityAddable)
     */
    @Override
    public void reserve(QuantityAddable<Item> container) {
        for (Map.Entry<Item, Integer> pair : container) {
            // Get item and its quantity
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Remove the specified quantity for the map of available items
            QuantityAddable.update(items, item, -quantity);
        }
    }

    /**
     * Confirms the previously assigned reservations specified by the given
     * {@code QuantityAddable} container.
     * <p>
     * This function physically removes/adds some of the reserved items and
     * change the weight of the {@code Rack} in accordance.
     * <p>
     * Positive quantities means the items are to be taken from the {@code Rack}.
     * While negative quantities means the items are to be added into the {@code Rack}.
     * <p>
     * This function should be called after reserving a same or a super container first;
     * otherwise un-expected behaviour could occur.
     * <p>
     * This function should only be called once per {@code Task} termination.
     *
     * @param container the {@code QuantityAddable} container.
     *
     * @see Rack#reserve(QuantityAddable)
     */
    @Override
    public void confirmReservation(QuantityAddable<Item> container) {
        for (Map.Entry<Item, Integer> pair : container) {
            // Get item and its quantity
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Remove item units and confirm reservation
            storedWeight -= item.getWeight() * quantity;
            item.add(this, -quantity);
            item.confirmReservation(container);
        }
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

        builder
                .append("Rack: {")
                .append(" id: ").append(id).append(",")
                .append(" pos: ").append("(").append(row).append("x").append(col).append(")")
                .append(" capacity: ").append(capacity).append(",")
                .append(" container weight: ").append(containerWeight).append(",")
                .append(" stored weight: ").append(storedWeight).append(",")
                .append(" items: ").append(items.size())
                .append(" }");

        return builder.toString();
    }
}
