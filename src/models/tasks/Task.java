package models.tasks;

import models.agents.Agent;
import models.facilities.Facility;
import models.items.Item;
import models.facilities.Gate;
import models.facilities.Rack;
import models.tasks.orders.Order;

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
public class Task extends AbstractTask {

    //
    // Enums
    //

    /**
     * Different actions to be done by a {@code Task} during its lifecycle.
     */
    public enum TaskAction {
        BIND,           // Go and bind with a facility
        UNBIND,         // Go and unbind with a facility
        SELECT_ORDER,   // Select one of the associated orders to deliver
    }

    // ===============================================================================================
    //
    // Member Variables
    //

    /**
     * The {@code Agent} assigned for this {@code Task}.
     */
    private Agent agent;

    /**
     * The {@code Rack} needed to be delivered.
     */
    private Rack rack;

    /**
     * The currently active {@code Order} by this {@code Task}.
     */
    private Order activeOrder;

    /**
     * The list of associated orders in which this {@code Task} is a part of.
     */
    private LinkedList<Order> orders = new LinkedList<>();

    /**
     * The queue of actions to be done by the assigned {@code Agent} to complete this {@code Task}.
     */
    private Deque<Pair<TaskAction, Facility>> actions = new LinkedList<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Task} object.
     *
     * @param agent the assigned {@code Agent}.
     * @param rack  the assigned {@code Rack}.
     */
    public Task(Agent agent, Rack rack) {
        super();
        this.agent = agent;
        this.rack = rack;

        // Add initial basic actions
        actions.add(new Pair<>(TaskAction.BIND, rack));             // Go and load the rack
        actions.add(new Pair<>(TaskAction.SELECT_ORDER, null));     // Select an order to deliver
        actions.add(new Pair<>(TaskAction.UNBIND, rack));           // Go and offload the rack back
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
     * Returns the assigned {@code Rack} with this {@code Task}.
     *
     * @return the assigned {@code Rack}.
     */
    public Rack getRack() {
        return rack;
    }

    /**
     * Returns the currently active {@code Order} by this {@code Task}.
     *
     * @return the currently active {@code Order}.
     */
    public Order getActiveOrder() {
        return activeOrder;
    }

    /**
     * Returns the number of assigned orders to this {@code Task} that are still running.
     * That is, the number of added orders that has not been partially completed
     * by this {@code Task}.
     *
     * @return the number of running orders.
     */
    public int getRunningOrdersCount() {
        return orders.size() + (activeOrder != null ? 1 : 0);
    }

    /**
     * Adds a new {@code Order} to be partially fulfilled by this {@code Task}.
     * <p>
     * The newly added {@code Order} will start being partially fulfilled by
     * this {@code Task} when either it is added when the {@code Task} is currently
     * running (i.e. before calling {@link Task#terminate()}), or after activating
     * the {@code Task} (i.e. after calling {@link Task#activate()}).
     *
     * @param order the new {@code Order} to add.
     */
    public void addOrder(Order order) {
        orders.addLast(order);
        order.assignTask(this);

        // Check if currently the task is returning the rack back
        if (actions.size() == 1) {
            actions.addFirst(new Pair<>(TaskAction.SELECT_ORDER, null));
        }
    }

    /**
     * Activates this {@code Task} and allocates its required resources.
     * <p>
     * This function should be called only once per {@code Task} object.
     */
    @Override
    public void activate() {
        rack.allocate(agent);
        agent.assignTask(this);
        super.activate();
    }

    /**
     * Terminates this {@code Task} after completion.
     * <p>
     * A callback function to be invoked when this {@code Task} has been completed.
     * Used to clear and finalize allocated resources.
     */
    @Override
    protected void terminate() {
        // TODO: add task statistics finalization
        rack.deallocate();
        agent.onTaskComplete(this);
        super.terminate();
    }

    /**
     * Executes the next required action to be done to complete this {@code Task}.
     * <p>
     * This function should be called only when this {@code Task} is active.
     *
     * @return {@code true} if this {@code Task} manged to execute the action successfully; {@code false} otherwise.
     */
    public boolean executeAction() {
        // Select a new order to deliver
        if (actions.getFirst().key == TaskAction.SELECT_ORDER) {
            selectOrder();
        }

        TaskAction action = actions.element().key;
        Facility facility = actions.element().val;

        boolean ret = false;

        // Bind action
        if (action == TaskAction.BIND) {
            ret |= executeBind(facility);
        }
        // Unbind action
        if (action == TaskAction.UNBIND) {
            ret |= executeUnbind(facility);
        }

        // Check if all actions are completed
        if (actions.isEmpty()) {
            terminate();
        }

        return ret;
    }

    /**
     * Called when the delivery of {@code Rack} to the active {@code Gate} has been completed.
     * That is, when the currently active {@code Order} has been partially
     * fulfilled by this {@code Task}.
     * <p>
     * This function should be called from the {@code Gate} after collecting/refilling
     * the needed items.
     */
    public void deliveryCompleted() {
        activeOrder.onTaskComplete(this);
        activeOrder = null;
    }

    /**
     * Selects the next {@code Order} to be delivered by this {@code Task}.
     */
    private void selectOrder() {
        if (orders.isEmpty()) {
            actions.removeFirst();
            return;
        }

        activeOrder = orders.removeFirst();
        actions.addFirst(new Pair<>(TaskAction.UNBIND, activeOrder.getDeliveryGate()));
        actions.addFirst(new Pair<>(TaskAction.BIND, activeOrder.getDeliveryGate()));
    }

    /**
     * Reaches and binds with the given {@code Facility}.
     *
     * @param facility the {@code Facility} to bind with.
     *
     * @return {@code true} if manged to execute the action successfully; {@code false} otherwise.
     */
    private boolean executeBind(Facility facility) {
        if (facility.canBind(agent)) {
            facility.bind(agent);
            actions.removeFirst();
            return true;
        } else {
            return agent.reach(facility);
        }
    }

    /**
     * Reaches and unbinds from the given {@code Facility}.
     *
     * @param facility the {@code Facility} to unbind from.
     *
     * @return {@code true} if manged to execute the action successfully; {@code false} otherwise.
     */
    private boolean executeUnbind(Facility facility) {
        if (facility.canUnbind()) {
            facility.unbind();
            actions.removeFirst();
            return true;
        } else {
            return agent.reach(facility);
        }
    }
}
