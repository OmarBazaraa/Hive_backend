package models.tasks;

import algorithms.Planner;
import models.agents.Agent;
import models.facilities.Facility;
import models.items.Item;
import models.facilities.Gate;
import models.facilities.Rack;
import models.items.QuantityAddable;
import models.maps.GuideGrid;
import models.warehouses.Warehouse;

import utils.Pair;

import java.util.*;


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
public class Task extends AbstractTask implements QuantityAddable<Item> {

    //
    // Enums
    //

    /**
     * Different actions to be done by a {@code Task} during its lifecycle.
     */
    public enum TaskAction {
        BIND,
        UNBIND
    }

    // ===============================================================================================
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
     * The {@code Gate} to deliver the {@code Rack} at.
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
     * The queue of actions to be done by the assigned {@code Agent} to complete this {@code Task}.
     */
    private Queue<Pair<TaskAction, Facility>> actions = new LinkedList<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Task} object.
     *
     * @param order the associated {@code Order}.
     * @param rack the assigned {@code Rack}.
     * @param agent the assigned {@code agent}.
     */
    public Task(Order order, Rack rack, Agent agent) {
        super();
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
     * <p>
     * This function should be called only once with positive quantities during
     * the construction of the {@code Task} object.
     *
     * @param item     the {@code Item} to be updated.
     * @param quantity the quantity to be updated with.
     */
    @Override
    public void add(Item item, int quantity) {
        QuantityAddable.update(items, item, quantity);
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
     * Fills this {@code Task} with the maximum number of items needed by the
     * associated {@code Order} that are available in the assigned {@code Rack}.
     *
     * This is done by taking the intersection of items in both the associated {@code Order},
     * and the assigned {@code Rack}.
     */
    private void fillItems() {
        items.clear();

        for (Map.Entry<Item, Integer> pair : order) {
            Item item = pair.getKey();
            items.put(item, Math.min(rack.get(item), pair.getValue()));
        }
    }

    /**
     * Activates this {@code Task} and allocates its required resources.
     * <p>
     * This function should be called only once per {@code Task} object.
     */
    @Override
    public void activate() {
        // Fill the items of the task
        fillItems();

        // Allocate task resources
        rack.reserve(this);
        agent.assignTask(this);
        order.assignTask(this);

        // Add actions
        actions.add(new Pair<>(TaskAction.BIND, rack));    // 1. Load the rack
        actions.add(new Pair<>(TaskAction.BIND, gate));    // 2. Bind with the gate
        actions.add(new Pair<>(TaskAction.UNBIND, gate));  // 3. Unbind with the gate
        actions.add(new Pair<>(TaskAction.UNBIND, rack));  // 4. Offload the rack

        // Activate the task
        super.activate();
    }

    /**
     * Terminates this {@code Task} after completion.
     * <p>
     * A callback function to be invoked when this {@code Task} has been completed.
     * Used to clear and finalize allocated resources.
     *
     * TODO: add task statistics finalization
     */
    @Override
    protected void terminate() {
        order.onTaskComplete(this);
        agent.onTaskComplete(this);
        super.terminate();
    }

    /**
     * Returns the guide map to reach the target of this {@code Task}.
     * <p>
     * This function should be called only when this {@code Task} is active.
     *
     * @return the {@code GuideGrid} to reach the target.
     */
    public GuideGrid getGuideMap() {
        Facility facility = actions.element().val;
        return facility.getGuideMap();
    }

    /**
     * Executes the next required action to be done to complete this {@code Task}.
     * <p>
     * This function should be called only when this {@code Task} is active.
     */
    public void executeAction() {
        TaskAction action = actions.element().key;
        Facility facility = actions.element().val;

        // Bind action
        if (action == TaskAction.BIND) {
            if (facility.canBind(agent)) {
                facility.bind(agent);
                actions.remove();
            } else {
                agent.reach(facility);
            }
        }

        // Unbind action
        if (action == TaskAction.UNBIND) {
            if (facility.canUnbind()) {
                facility.unbind();
                actions.remove();
            } else {
                agent.reach(facility);
            }
        }

        // Check if all actions are completed
        if (actions.isEmpty()) {
            status = TaskStatus.FULFILLED;
            terminate();
        }
    }
}
