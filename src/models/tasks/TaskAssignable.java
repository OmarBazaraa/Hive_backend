package models.tasks;

import models.agents.Agent;
import models.tasks.orders.Order;


/**
 * Interface definition for all {@link Task} assignable classes.
 * <p>
 * A {@code TaskAssignable} object is an object that can be assigned a {@link Task}.
 * <p>
 * This interface is to be implemented by {@link Agent}, {@link Order}.
 *
 * @see Task
 * @see Order
 */
public interface TaskAssignable {

    /**
     * Assigns a new {@code Task} to this object.
     *
     * @param task the new {@code Task} to assign.
     */
    void assignTask(Task task);

    /**
     * Called when a {@code Task} has been completed.
     *
     * @param task the completed {@code Task}.
     */
    void onTaskComplete(Task task);
}
