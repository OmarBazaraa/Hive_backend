package models.facilities;

import models.agents.Agent;
import models.agents.AgentBindable;
import models.items.Item;
import models.items.QuantityAddable;
import models.items.QuantityReservable;
import models.tasks.Task;
import models.tasks.TaskAssignable;
import models.warehouses.Warehouse;

import utils.Constants;
import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;

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
 * but it is different from other facilities in that it can be loaded and be moved around by
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
     * The storing capacity (in weight units) of this {@code Rack}.
     */
    private int capacity = Constants.RACK_DEFAULT_STORE_CAPACITY;

    /**
     * The total stored weight of all the items in this {@code Rack}.
     */
    private int weight;

    /**
     * The map of all items this {@code Rack} is storing.<p>
     * The key is an {@code Item}.<p>
     * The mapped value represents the quantity of this {@code Item}.
     */
    private Map<Item, Integer> items = new HashMap<>();

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * Creates a new {@code Rack} object from JSON data.
     *
     * TODO: add checks and throw exceptions
     *
     * @param data the un-parsed JSON data.
     * @param row  the row position of the {@code MapCell} to create.
     * @param col  the column position of the {@code MapCell} to create.
     *
     * @return an {@code Rack} object.
     */
    public static Rack create(JSONObject data, int row, int col) throws Exception {
        Rack ret = new Rack();

        ret.setCapacity(data.getInt(Constants.MSG_KEY_CAPACITY));
        ret.setPosition(row, col);

        JSONArray itemsJSON = data.getJSONArray(Constants.MSG_KEY_ITEMS);

        for (int i = 0; i < itemsJSON.length(); ++i) {
            JSONObject itemJSON = itemsJSON.getJSONObject(i);

            int itemId = itemJSON.getInt(Constants.MSG_KEY_ID);
            int quantity = itemJSON.getInt(Constants.MSG_KEY_QUANTITY);
            Item item = Warehouse.getInstance().getItemById(itemId);

            if (quantity < 0) {
                throw new Exception("Invalid quantity to add to the rack!");
            }

            if (item == null) {
                throw new Exception("Invalid item to add to the rack!");
            }

            ret.add(item, quantity);
        }

        return ret;
    }

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
     * @param id  the id of the {@code Rack}.
     */
    public Rack(int id) {
        super(id);
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
        QuantityAddable.update(items, item, quantity);
        updateWeight(quantity * item.getWeight());
        item.add(this, quantity);
    }

    /**
     * Clears and empties this {@code Rack} from all its available items.
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
            QuantityAddable.update(items, pair.getKey(), -pair.getValue());
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
     * Binds this {@code Rack} with the given {@code Agent}.
     * <p>
     * It is preferable to allocate the {@code Rack} before binding it to an {@code Agent}.
     * <p>
     * This function should be called after checking that it is currently possible to bind
     * the given {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @param agent the {@code Agent} to bind.
     *
     * @see Rack#isBound()
     * @see Rack#canBind(Agent)
     * @see Rack#canUnbind()
     * @see Rack#unbind()
     */
    @Override
    public void bind(Agent agent) throws Exception {
        agent.loadRack(this);
        super.bind(agent);
    }

    /**
     * Unbinds the bound {@code Agent} from this {@code Rack}.
     * <p>
     * This function should be called after checking that it is currently possible to unbind
     * the bound {@code Agent}; otherwise un-expected behaviour could occur.
     *
     * @see Rack#isBound()
     * @see Rack#canBind(Agent)
     * @see Rack#bind(Agent)
     * @see Rack#canUnbind()
     */
    @Override
    public void unbind() throws Exception {
        boundAgent.offloadRack(this);
        super.unbind();
    }
}
