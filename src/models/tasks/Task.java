package models.tasks;

import algorithms.Planner;
import models.agents.Agent;
import models.items.Item;
import models.facilities.Gate;
import models.facilities.Rack;
import models.items.QuantityAddable;
import models.maps.GuideGrid;
import models.orders.Order;

import models.Entity;
import models.warehouses.Warehouse;
import utils.Constants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This {@code Task} class represents the basic delivery task in our Hive Warehouse System.
 * <p>
 * A task represent the basic commands that an {@link Agent} can execute.
 *
 * @see Order
 * @see Item
 * @see Agent
 * @see Rack
 * @see Gate
 */
public class Task extends Entity implements QuantityAddable<Item> {

    //
    // Member Variables
    //

    /**
     * The {@code Order} in which this {@code Task} is a part of.
     */
    private Order order;

    /**
     * The {@code Rack} needed to be delivered.
     */
    private Rack rack;

    /**
     * The {@code Rack} to deliver the {@code Rack} at.
     */
    private Gate gate;

    /**
     * The {@code Agent} assigned for this {@code Task}.
     */
    private Agent agent;

    /**
     * The map of items this {@code Task} is needing.<p>
     * The key is an {@code Item}.<p>
     * The mapped value represents the needed quantity of this {@code Item}.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The current status of this {@code Task}.
     */
    private TaskStatus status = TaskStatus.INACTIVE;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * The number of tasks in the system so far.
     */
    private static int tasksCount = 0;

    /**
     * Returns the first available id for the next {@code Task} and increments.
     *
     * @return the first available id.
     */
    private static int getNextId() {
        return tasksCount++;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Task} object.
     */
    public Task(Order order, Rack rack, Agent agent) {
        super(getNextId());
        this.order = order;
        this.gate = order.getDeliveryGate();
        this.rack = rack;
        this.agent = agent;
    }

    /**
     * Returns the associated {@code Order} with this {@code Task}.
     *
     * @return the associated {@code Order}.
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Returns the {@code Gate} where this {@code Task} must be delivered.
     *
     * @return the delivery {@code Gate}.
     */
    public Gate getDeliveryGate() {
        return gate;
    }

    /**
     * Returns the assigned {@code Rack} with this {@code Task}.
     *
     * @return the assigned {@code Rack}.
     */
    public Rack getRack() {
        return rack;
    }

    /**
     * Returns the assigned {@code Agent} with this {@code Task}.
     *
     * @return the assigned {@code Agent}.
     */
    public Agent getAgent() {
        return agent;
    }

    /**
     * Returns the quantity of the an {@code Item} needed by this {@code Task}.
     *
     * @param item the needed {@code Item}.
     *
     * @return the pending quantity of the given {@code Item}.
     */
    @Override
    public int get(Item item) {
        return items.getOrDefault(item, 0);
    }

    /**
     * Updates the quantity of an {@code Item} in this {@code Order}.
     * <p>
     * This function is used to add extra units of the given {@code Item} if the given
     * quantity is positive,
     * and used to remove existing units if the given quantity is negative.
     *
     * TODO: prevent adding item after activation the order
     * TODO: prevent adding/removing from outside this class
     *
     * @param item     the {@code Item} to be updated.
     * @param quantity the quantity to be updated with.
     */
    @Override
    public void add(Item item, int quantity) throws Exception {
        QuantityAddable.update(items, item, quantity);
    }

    /**
     * Fills this {@code Task} with the maximum number of items needed by the
     * associated {@code Order} that are available in the assigned {@code Rack}.
     *
     * This is done by taking the intersection of items in both the associated {@code Order},
     * and the assigned {@code Rack}.
     *
     * TODO: call this automatically from the constructor
     */
    public void fillItems() {
        items.clear();

        for (Map.Entry<Item, Integer> pair : order) {
            Item item = pair.getKey();
            items.put(item, Math.min(rack.get(item), pair.getValue()));
        }
    }

    /**
     * Returns an {@code Iterator} to iterate over the needed items in this {@code Task}.
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
     * Returns the current status of this {@code Task}.
     *
     * @return the {@code TaskStatus} of this {@code Task}.
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Checks whether this {@code Task} is currently active or not.
     *
     * @return {@code true} if this {@code Task} is active; {@code false} otherwise.
     */
    public boolean isActive() {
        return (status != TaskStatus.INACTIVE && status != TaskStatus.COMPLETED);
    }

    /**
     * Checks whether this {@code Task} has been completed or not.
     *
     * @return {@code true} if this {@code Task} has been completed; {@code false} otherwise.
     */
    public boolean isComplete() {
        return (status == TaskStatus.COMPLETED);
    }

    /**
     * Activates this {@code Task} and allocates its required resources.
     */
    public void activate() throws Exception {
        // Skip re-activating already activated tasks
        if (status != TaskStatus.INACTIVE) {
            return;
        }

        // Allocate task resources
        rack.reserve(this);
        agent.assignTask(this);
        order.assignTask(this);

        // Activate the task
        status = TaskStatus.FETCHING;
    }

    /**
     * Terminates this {@code Task} after completion.
     * <p>
     * A callback function to be invoked when this {@code Task} has been completed.
     * Used to clear and finalize allocated resources.
     */
    private void terminate() {
        order.onTaskComplete(this);
        agent.onTaskComplete(this);
    }

    /**
     * Returns the priority of this {@code Task}.
     * Higher value indicates higher priority.
     *
     * TODO: add better heuristic to compute the priority
     *
     * @return the priority of this {@code Task}.
     */
    public int getPriority() {
        return -order.getId();
    }

    /**
     * Returns the estimated number of steps to finish this {@code Task}.
     *
     * TODO: implement if needed
     *
     * @return the estimated number of steps.
     */
    public int getEstimatedDistance() {
        return 0;
    }

    /**
     * Returns the guide map to reach the target of this {@code Task}.
     *
     * @return the {@code GuideGrid} to reach the target.
     */
    public GuideGrid getGuideMap() {
        if (status == TaskStatus.FETCHING || status == TaskStatus.RETURNING) {
            return rack.getGuideMap();
        }
        if (status == TaskStatus.DELIVERING) {
            return gate.getGuideMap();
        }

        return null;
    }

    /**
     * Executes the next required action to be done to complete this {@code Task}.
     */
    public void executeAction() throws Exception {
        // Moving to the rack
        if (status == TaskStatus.FETCHING) {
            if (rack.canBind(agent)) {
                rack.bind(agent);
                status = TaskStatus.DELIVERING;
            } else {
                Planner.route(agent, Warehouse.getInstance().getMap());
            }
        }
        // Delivering the rack to the gate
        else if (status == TaskStatus.DELIVERING) {
            if (gate.samePosition(agent)) {
                gate.bind(agent);
                status = TaskStatus.OFFLOADING;
            } else {
                Planner.route(agent, Warehouse.getInstance().getMap());
            }
        }
        // Wait until offloading the items at the gate
        else if (status == TaskStatus.OFFLOADING) {
            if (gate.canUnbind()) {
                gate.unbind();
                status = TaskStatus.RETURNING;
            }
        }
        // Returning the rack back to its position
        else if (status == TaskStatus.RETURNING) {
            if (rack.canUnbind()) {
                rack.unbind();
                status = TaskStatus.COMPLETED;
                terminate();
            } else {
                Planner.route(agent, Warehouse.getInstance().getMap());
            }
        }
    }
}
