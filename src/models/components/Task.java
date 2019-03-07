package models.components;

import models.components.base.HiveObject;
import models.map.GuideCell;
import models.map.base.Position;
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
     * The gate to deliver the rack at.
     */
    private Gate gate;

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
        return this.gate;
    }

    /**
     * Assigns the associated order of this task.
     *
     * @param order the associated order.
     */
    public void assignOrder(Order order) {
        this.order = order;
        this.gate = order.getDeliveryGate();
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
     * Checks whether this task is currently active or not.
     *
     * @return {@code true} if this task is active, {@code false} otherwise.
     */
    public boolean isActive() {
        return this.status != TaskStatus.COMPLETE;
    }

    /**
     * Checks whether this task has been completed or not.
     *
     * @return {@code true} if this task has been completed, {@code false} otherwise.
     */
    public boolean isComplete() {
        return this.status == TaskStatus.COMPLETE;
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
                status = TaskStatus.COMPLETE;
                onComplete();
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
     * Activates this task and reserves its assigned resources.
     */
    public void activate() throws Exception {
        // Skip re-activating already activated tasks
        if (isActive()) {
            return;
        }

        //
        // Iterate over all the items of the given task and reserve them
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

        // Activate this task
        agent.setTask(this);
        rack.setTask(this);
        status = TaskStatus.FETCHING;
    }

    /**
     * A callback function to be invoked when this task has been completed.
     * Used to clear and finalize resources.
     */
    public void onComplete() {
        agent.clearTask();
        rack.clearTask();
        order.onTaskCompleted(this);
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
     * Returns the guide cell at the given position to reach the target of this task.
     *
     * @param row the row position of the needed guide cell.
     * @param row the column position of the needed guide cell.
     *
     * @return the {@code GuideCell} at the given position.
     */
    public GuideCell getGuideAt(int row, int col) {
        if (status == TaskStatus.FETCHING || status == TaskStatus.RETURNING) {
            return rack.getGuideAt(row, col);
        }

        if (status == TaskStatus.DELIVERING) {
            return gate.getGuideAt(row, col);
        }

        return new GuideCell(0, Direction.STILL);
    }

    /**
     * Returns the guide cell at the given position to reach the target of this task.
     *
     * @param pos the position of the needed guide cell.
     *
     * @return the {@code GuideCell} at the given position.
     */
    public GuideCell getGuideAt(Position pos) {
        return getGuideAt(pos.row, pos.col);
    }

    /**
     * Returns the estimated number of time steps to finish this task.
     *
     * @return an integer representing the estimated number of step to finish the assigned task.
     */
    public int getEstimatedDistance() {
        int x = rack.getEstimatedDistance(agent.getPosition());
        int y = gate.getEstimatedDistance(rack.getPosition());

        return x + y * 2;
    }

    /**
     * Returns the next required action to be done by the assigned agent
     * in order to move one step forward to complete this task.
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
