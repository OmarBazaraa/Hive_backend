package models.tasks;

import models.agents.Agent;
import models.items.Item;
import models.facilities.Gate;
import models.facilities.Rack;
import models.items.QuantityAddable;
import models.maps.GuideGrid;
import models.orders.Order;

import utils.Entity;
import utils.Constants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This {@code Task} class represents the basic delivery task in our Hive Warehouse System.
 * <p>
 * A task represent the basic commands that a robot {@link Agent} can execute.
 *
 * @see Order
 * @see Agent
 * @see Rack
 * @see Gate
 * @see Item
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
     * The map of {@code Item}s this {@code Task} is needing.<p>
     * The key is an {@code Item}.<p>
     * The mapped value represents the needed quantity of this {@code Item}.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The current status of this {@code Task}.
     */
    private TaskStatus status = TaskStatus.PENDING;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * The number of tasks in the system so far.
     */
    private static int tasksCount = 0;

    /**
     * Returns the next available id for the next task.
     *
     * @return the next available id.
     */
    private static int getTaskId() {
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
        super(getTaskId());
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
        return (status != TaskStatus.PENDING && status != TaskStatus.COMPLETED);
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
        if (status != TaskStatus.PENDING) {
            return;
        }

        // Allocate task resources
        order.assignTask(this);
        rack.assignTask(this);
        agent.assignTask(this);

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
        rack.onTaskComplete(this);
        agent.onTaskComplete(this);
    }

    /**
     * Returns the priority of this task.
     * Higher value indicates higher priority.
     *
     * @return an integer value representing the priority of this task.
     */
    public int getPriority() {
        // TODO: add better heuristic to compute the priority
        return (order != null ? -order.getId() : Integer.MIN_VALUE);
    }

    /**
     * Returns the guide maps to reach the target of this task.
     *
     * @return the {@code GuideGrid} to reach the target.
     */
    public GuideGrid getGuideMap() {
        if (status == TaskStatus.FETCHING || status == TaskStatus.LOADING) {
            return rack.getGuideMap();
        }
        if (status == TaskStatus.DELIVERING || status == TaskStatus.WAITING) {
            return gate.getGuideMap();
        }
        if (status == TaskStatus.RETURNING || status == TaskStatus.OFFLOADING) {
            return rack.getGuideMap();
        }

        return null;
    }

    /**
     * Returns the estimated number of time steps to finish this task.
     *
     * @return an integer representing the estimated number of step to finish the assigned task.
     */
    public int getEstimatedDistance() {
        // TODO:
        return 0;
    }

    /**
     * Updates the status of this {@code Task} using the last action done by the {@code Agent}.
     *
     * @param action the last action done by the {@code Agent}.
     */
    public void updateStatus(AgentAction action) throws Exception {
        if (status == TaskStatus.PENDING) {
            throw new Exception("Invalid action done by the agent!");
        }
        else if (status == TaskStatus.FETCHING) {
            if (action == AgentAction.MOVE) {
                if (agent.getPosition() == rack.getPosition()) {
                    status = TaskStatus.LOADING;
                }
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.LOADING) {
            if (action == AgentAction.LOAD) {
                status = TaskStatus.DELIVERING;
            }
            else if (action == AgentAction.MOVE) {
                status = TaskStatus.FETCHING;
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.DELIVERING) {
            if (action == AgentAction.MOVE) {
                if (agent.getPosition() == gate.getPosition()) {
                    status = TaskStatus.WAITING;
                }
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.WAITING) {
            if (action == AgentAction.WAIT) {
                status = TaskStatus.RETURNING;
            }
            else if (action == AgentAction.MOVE) {
                status = TaskStatus.DELIVERING;
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.RETURNING) {
            if (action == AgentAction.MOVE) {
                if (agent.getPosition() == rack.getPosition()) {
                    status = TaskStatus.OFFLOADING;
                }
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.OFFLOADING) {
            if (action == AgentAction.OFFLOAD) {
                status = TaskStatus.COMPLETED;
                terminate();
            }
            else if (action == AgentAction.MOVE) {
                status = TaskStatus.RETURNING;
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
    }

    /**
     * Returns the next required action to be done by the assigned agent
     * in order to bringBlank one step forward to complete this task.
     *
     * @return {@code AgentAction} to be done the next time step.
     */
    public AgentAction getNextAction() {
        if (status == TaskStatus.PENDING) {
            return AgentAction.NOTHING;
        }

        if (status == TaskStatus.FETCHING) {
            return AgentAction.MOVE;
        }

        if (status == TaskStatus.LOADING) {
            return AgentAction.LOAD;
        }

        if (status == TaskStatus.DELIVERING) {
            return AgentAction.MOVE;
        }

        if (status == TaskStatus.WAITING) {
            return AgentAction.WAIT;
        }

        if (status == TaskStatus.RETURNING) {
            return AgentAction.MOVE;
        }

        if (status == TaskStatus.OFFLOADING) {
            return AgentAction.OFFLOAD;
        }

        return AgentAction.NOTHING;
    }
}
