package models.tasks;

import models.agents.Agent;
import models.facilities.Rack;
import models.orders.Order;


/**
 * Interface definition for all {@link Task} assignable classes.
 * <p>
 * A {@code TaskAssignable} class is a class that can be assigned a {@link Task}.
 * <p>
 * This interface is to be implemented by {@link Agent}, {@link Rack}, {@link Order}.
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
    void assignTask(Task task) throws Exception;

    /**
     * Called when a {@code Task} has been completed.
     *
     * @param task the completed {@code Task}.
     */
    void onTaskComplete(Task task) throws Exception;
}
