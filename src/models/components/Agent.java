package models.components;

import models.components.base.SrcHiveObject;
import utils.Constants.*;


/**
 * This {@code Agent} class is a model for robot agent in our Hive System.
 */
public class Agent extends SrcHiveObject implements Comparable<Agent> {

    //
    // Member Variables
    //

    /**
     * The priority of this agent among other agents.
     */
    private int priority;

    /**
     * The current status of this agent.
     */
    private AgentStatus status = AgentStatus.READY;

    /**
     * The assigned task of this agent.
     */
    private Task task;

    // Skip for now
    private int capacity;
    private int chargeMaxCap;
    private int chargeLevel;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new agent robot.
     *
     * @param id  the id of the agent.
     * @param row the row position of the agent.
     * @param col the column position of the agent.
     */
    public Agent(int id, int row, int col) {
        super(id, row, col);
        this.priority = id; // TODO to be set dynamically
    }

    /**
     * Returns the priority of this agent.
     *
     * @return an integer value representing the priority of this agent.
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Sets a new priority to this agent.
     *
     * @param priority the new priority to set.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Returns the current status of this agent.
     *
     * @return an {@code AgentStatus} value representing the current status of the agent.
     */
    public AgentStatus getStatus() {
        return this.status;
    }

    /**
     * Sets a new status to this agent.
     *
     * @param status the new status to set.
     */
    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    /**
     * Returns the assigned task of this agent.
     *
     * @return a {@code Task} object representing the assigned task of this agent;
     * {@code null} if no current assigned task.
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * Assigns a new task to this agent.
     *
     * @param task the new task to assign.
     */
    public void setTask(Task task) {
        this.task = task;
        this.status = AgentStatus.ACTIVE;
    }

    /**
     * Compares whether some other object is less than, equal to, or greater than this one.
     *
     * @param rhs the reference object with which to compare.
     *
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Agent rhs) {
        int cmp = (priority - rhs.priority);
        if (cmp == 0) {
            return id - rhs.id;
        }
        return cmp;
    }
}
