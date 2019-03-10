package models.tasks;


/**
 * Interface definition for all {@link Task} assignable classes.
 * <p>
 * A {@code TaskAssignable} class is a class that can be assigned a {@link Task}.
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
