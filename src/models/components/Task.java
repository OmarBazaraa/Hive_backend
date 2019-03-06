package models.components;

import models.components.base.HiveObject;
import utils.Constants.*;

import java.util.HashMap;
import java.util.Map;


/**
 * This {@code Task} class represents a basic task for our robot agents
 * in our Hive Warehousing System.
 */
public class Task extends HiveObject {

    //
    // Member Variables
    //

    /**
     * The order in which this task is a part of.
     */
    private Order order;

    /**
     * The rack needed to be delivered.
     */
    private Rack rack;

    /**
     * The robot agent assigned for this task.
     */
    private Agent agent;

    /**
     * The map of needed items to be picked from the below rack.
     */
    private Map<Item, Integer> items = new HashMap<>();

    /**
     * The current status of this task.
     */
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * Flag to indicate whether this task has been activated or not.
     */
    private boolean activated = false;

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
     * Constructs a new task.
     */
    public Task() {
        super(getTaskId());
    }

    /**
     * Returns the associated order with this task.
     *
     * @return the associated order.
     */
    public Order getOrder() {
        return this.order;
    }

    /**
     * Returns the delivery gate of this task.
     *
     * @return the delivery gate.
     */
    public Gate getDeliveryGate() {
        return (order != null ? order.getDeliveryGate() : null);
    }

    /**
     * Assigns the associated order of this task.
     *
     * @param order the associated order.
     */
    public void assignOrder(Order order) {
        this.order = order;
    }

    /**
     * Returns the assigned rack with this task.
     *
     * @return the assigned rack.
     */
    public Rack getRack() {
        return this.rack;
    }

    /**
     * Assigns a destination rack for this task.
     *
     * @param rack the assigned rack.
     */
    public void assignRack(Rack rack) {
        this.rack = rack;
    }

    /**
     * Returns the assigned agent with this task.
     *
     * @return the assigned agent.
     */
    public Agent getAgent() {
        return this.agent;
    }

    /**
     * Assigns an agent for this task.
     *
     * @param agent the assigned agent.
     */
    public void assignAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     * Assigns the terminals of this task.
     *
     * @param agent the assigned agent.
     * @param rack  the assigned rack.
     */
    public void assignTerminals(Agent agent, Rack rack) {
        assignAgent(agent);
        assignRack(rack);
    }

    /**
     * Returns the quantity of the given item needed in this task.
     *
     * @param item the item to get its quantity.
     *
     * @return the quantity of the given item.
     */
    public int getItemQuantity(Item item) {
        return this.items.getOrDefault(item, 0);
    }

    /**
     * Returns the map of items needed for fulfilling this task.
     *
     * @return the map of items of this task.
     */
    public Map<Item, Integer> getItems() {
        return this.items;
    }

    /**
     * Adds a new needed item for this task.
     *
     * @param item     the new item.
     * @param quantity the needed quantity.
     *
     * @throws Exception when passing non-positive quantity.
     */
    public void addItem(Item item, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Passing non-positive item quantity!");
        }

        this.items.put(item, quantity + items.getOrDefault(item, 0));
    }

    /**
     * Fills this task with the maximum number of items needed by the
     * associated order that are available in the assigned rack.
     */
    public void fillItems() throws Exception {
        // Task information must be complete
        if (order == null || rack == null) {
            throw new Exception("No order and/or rack is assigned yet to the task!");
        }

        Map<Item, Integer> m1, m2;

        // Assign the smaller set of items to 'm1'
        if (order.getItems().size() < rack.getItems().size()) {
            m1 = order.getItems();
            m2 = rack.getItems();
        } else {
            m1 = rack.getItems();
            m2 = order.getItems();
        }

        clearItems();

        // Get the intersection between the items of the order and the items in the rack
        for (Item item : m1.keySet()) {
            int quantity = Math.min(m1.getOrDefault(item, 0), m2.getOrDefault(item, 0));
            this.items.put(item, quantity);
        }
    }

    /**
     * Clears this task from all its assigned items.
     */
    public void clearItems() {
        this.items.clear();
    }

    /**
     * Returns the current status of this task.
     *
     * @return an {@code TaskStatus} value representing the current status of the task.
     */
    public TaskStatus getStatus() {
        return this.status;
    }

    /**
     * Sets a new status to this task.
     *
     * @param status the new status to set.
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Updates the status of this task using the last action done by the agent.
     *
     * @param action the last action done by the agent.
     */
    public void updateStatus(AgentAction action) throws Exception {
        if (status == TaskStatus.PENDING) {
            throw new Exception("Invalid action done by the agent!");
        }
        else if (status == TaskStatus.FETCH) {
            if (action == AgentAction.MOVE) {
                if (agent.getPosition() == rack.getPosition()) {
                    status = TaskStatus.PICK;
                }
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.PICK) {
            if (action == AgentAction.PICK) {
                status = TaskStatus.DELIVER;
            }
            else if (action == AgentAction.MOVE) {
                status = TaskStatus.FETCH;
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.DELIVER) {
            if (action == AgentAction.MOVE) {
                if (agent.getPosition() == getDeliveryGate().getPosition()) {
                    status = TaskStatus.WAIT;
                }
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.WAIT) {
            if (action == AgentAction.WAIT) {
                status = TaskStatus.RETURN;
            }
            else if (action == AgentAction.MOVE) {
                status = TaskStatus.DELIVER;
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.RETURN) {
            if (action == AgentAction.MOVE) {
                if (agent.getPosition() == rack.getPosition()) {
                    status = TaskStatus.RELEASE;
                }
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
        else if (status == TaskStatus.RELEASE) {
            if (action == AgentAction.RELEASE) {
                status = TaskStatus.DONE;
            }
            else if (action == AgentAction.MOVE) {
                status = TaskStatus.RETURN;
            }
            else if (action != AgentAction.NOTHING) {
                throw new Exception("Invalid action done by the agent!");
            }
        }
    }

    /**
     * Returns whether this task has been activated or not.
     *
     * @return {@code true} if this task has been activated, {@code false} otherwise.
     */
    public boolean isActivated() {
        return this.activated;
    }

    /**
     * Activates this task and allocates its assigned resources.
     */
    public void activate() throws Exception {
        // Task information must be complete
        if (order == null || rack == null || agent == null) {
            throw new Exception("No order, agent and/or order is assigned yet to the task!");
        }

        // Skip re-activating already activated tasks
        if (activated) {
            return;
        }

        // Set task as activated
        activated = true;
        status = TaskStatus.FETCH;

        //
        // Iterate over all the items of the given task
        //
        for (Map.Entry<Item, Integer> pair : items.entrySet()) {
            // Get the current item
            Item item = pair.getKey();
            int quantity = pair.getValue();

            // Remove the current item from the pending items of the associated order
            order.removeItem(item, quantity);

            // Reserve the current item if the associated rack to this task
            rack.removeItem(item, quantity);
        }

        // TODO: Activate the assigned agent

        // TODO: Allocate the assigned rack
    }

    /**
     * Returns the next required action to be done by the assigned agent
     * in order to move one step forward to accomplish this task.
     *
     * @return {@code AgentAction} to be done the next time step.
     */
    public AgentAction getNextAction() {
        if (status == TaskStatus.PENDING) {
            return AgentAction.NOTHING;
        }

        if (status == TaskStatus.FETCH) {
            return AgentAction.MOVE;
        }

        if (status == TaskStatus.PICK) {
            return AgentAction.PICK;
        }

        if (status == TaskStatus.DELIVER) {
            return AgentAction.MOVE;
        }

        if (status == TaskStatus.WAIT) {
            return AgentAction.WAIT;
        }

        if (status == TaskStatus.RETURN) {
            return AgentAction.MOVE;
        }

        if (status == TaskStatus.RELEASE) {
            return AgentAction.RELEASE;
        }

        return AgentAction.NOTHING;
    }

    /**
     * Returns the priority of this task.
     * Higher value indicates higher priority.
     *
     * @return an integer value representing the priority of this task.
     */
    public int getPriority() {
        return (order != null ? -order.getId() : Integer.MIN_VALUE);
    }

    /**
     * Returns the estimated number of time steps to finish the currently assigned task.
     *
     * @return an integer representing the estimated number of step to finish the assigned task.
     */
    public int getEstimatedDistance() {
        Rack rack = getRack();
        Gate gate = getDeliveryGate();

        int x = rack.getEstimatedDistance(agent.getPosition());
        int y = gate.getEstimatedDistance(rack.getPosition());

        return x + y * 2;
    }
}
