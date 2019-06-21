package models.tasks.orders;

import models.tasks.Task;


/**
 * Interface definition for all {@code Order} listeners for any occurring events
 * related to an {@code Order}.
 */
public interface OrderListener {

    /**
     * Called when an {@code Order} has just been started.
     * That, is when it is assigned its first sub task.
     *
     * @param order the started {@code Order}.
     */
    void onOrderStarted(Order order);

    /**
     * Called when a {@code Task} has been assigned to an {@code Order}.
     *
     * @param order the {@code Order}.
     * @param task  the assigned {@code Task}.
     */
    void onOrderTaskAssigned(Order order, Task task);

    /**
     * Called when an assigned {@code Task} for an {@code Order} has been completed.
     *
     * @param order the {@code Order}.
     * @param task  the completed {@code Task}.
     */
    void onOrderTaskCompleted(Order order, Task task);

    /**
     * Called when an {@code Order} has just been fulfilled.
     * That, is when its last assigned sub task has been completed.
     *
     * @param order the fulfilled {@code Order}.
     */
    void onOrderFulfilled(Order order);

    /**
     * Called when an {@code Order} has been dismissed from the system.
     *
     * @param order the dismissed {@code Order}.
     */
    void onOrderDismissed(Order order);
}
