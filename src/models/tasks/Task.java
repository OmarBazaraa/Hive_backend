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
        SELECT_GATE,    // Select one of the associated gates as the current target
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
     * The current target {@code Gate} by this {@code Task}.
     */
    private Gate gate;

    /**
     * The number of assigned orders by this {@code Task} that are still running.
     */
    private int runningOrdersCount = 0;

    /**
     * The list of associated orders in which this {@code Task} is a part of.
     */
    private HashMap<Gate, Queue<Order>> orders = new HashMap<>();

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
        actions.add(new Pair<>(TaskAction.SELECT_GATE, null));      // Select a target gate
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
     * Returns the number of assigned orders to this {@code Task} that are still running.
     * That is, the number of added orders that has not been partially completed
     * by this {@code Task}.
     *
     * @return the number of running orders.
     */
    public int getRunningOrdersCount() {
        return runningOrdersCount;
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
        order.assignTask(this);

        Queue<Order> queue = orders.get(order.getDeliveryGate());

        if (queue == null) {
            queue = new LinkedList<>();
            queue.add(order);
            orders.put(order.getDeliveryGate(), queue);
        } else {
            queue.add(order);
        }

        runningOrdersCount++;

        // Check if currently the task is returning the rack back
        if (actions.size() == 1) {
            actions.addFirst(new Pair<>(TaskAction.SELECT_GATE, null));
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
        // Select a new target gate
        if (actions.getFirst().key == TaskAction.SELECT_GATE) {
            selectGate();
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
     */
    public void completeActiveOrder() {
        Queue<Order> gateOrders = orders.get(gate);

        Order order = gateOrders.remove();
        order.onTaskComplete(this);

        if (gateOrders.isEmpty()) {
            actions.addFirst(new Pair<>(TaskAction.UNBIND, gate));
            orders.remove(gate);
            gate = null;
        } else {
            actions.addFirst(new Pair<>(TaskAction.BIND, gate));
        }
    }

    /**
     * Selects the next target {@code Gate} to deliver its orders.
     */
    private void selectGate() {
        if (orders.isEmpty()) {
            actions.removeFirst();
            return;
        }

        int dis = Integer.MAX_VALUE;

        for (Gate g : orders.keySet()) {
            int d = g.getDistanceTo(agent);

            if (dis > d) {
                dis = d;
                gate = g;
            }
        }

        actions.addFirst(new Pair<>(TaskAction.BIND, gate));
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
            actions.removeFirst();

            if (!facility.isBound()) {
                facility.bind(agent);
            }

            if (facility instanceof Gate) {
                completeActiveOrder();
            }

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
            actions.removeFirst();
            facility.unbind();
            return true;
        } else {
            return agent.reach(facility);
        }
    }
}
