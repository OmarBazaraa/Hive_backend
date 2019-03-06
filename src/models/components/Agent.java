package models.components;

import models.components.base.HiveObject;
import models.components.base.SrcHiveObject;
import utils.Constants.*;
import utils.Constants;


/**
 * This {@code Agent} class is a model for robot agent in our Hive System.
 */
public class Agent extends SrcHiveObject {

    //
    // Member Variables
    //

    /**
     * The current status of this agent.
     */
    private AgentStatus status = AgentStatus.READY;

    /**
     * The assigned task of this agent.
     */
    private Task task;

    /**
     * The last time step this agent has performed a move.
     */
    private int lastMoveTime;

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
     * Returns the last time step this agent has performed a move.
     */
    public int getLastMoveTime() {
        return this.lastMoveTime;
    }

    /**
     * Returns the estimated number of steps to finish the currently assigned task.
     *
     * @return an integer representing the estimated number of step to finish the assigned task.
     */
    public int getEstimatedSteps() {
        if (task == null) {
            return 0;
        }

        return id;
    }

    /**
     * Returns the priority of this agent.
     * Higher value indicates higher priority.
     *
     * @return an integer value representing the priority of this agent.
     */
    public int getPriority() {
        switch (status) {
            case READY:
                return 0;
            case ACTIVE:
                return getEstimatedSteps();
            case CHARGING:
            case OUT_OF_SERVICE:
                return Integer.MAX_VALUE;
            default:
                return 0;
        }
    }

    /**
     * Compares whether some other object is less than, equal to, or greater than this one.
     *
     * @param obj the reference object with which to compare.
     *
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(HiveObject obj) {
        if (!(obj instanceof Agent)) {
            return id - obj.getId();
        }
        Agent rhs = (Agent) obj;
        int cmp = (getPriority() - rhs.getPriority());
        if (cmp == 0) {
            return id - rhs.id;
        }
        return cmp;
    }
}
