package models.tasks;

import models.Entity;


/**
 * This {@code AbstractTask} class is the base class of all the tasks, instructions, and orders
 * in our Hive Warehouse System.
 *
 * @see Task
 * @see Order
 */
abstract public class AbstractTask extends Entity {

    //
    // Member Variables
    //

    /**
     * The listener to be invoked when this {@code AbstractTask} has been fulfilled.
     */
    protected OnFulFillListener fulFillListener;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * The number of {@code AbstractTask} in the system so far.
     */
    protected static int sCount = 0;

    /**
     * Returns the first available id for the next {@code AbstractTask} and increments.
     *
     * @return the first available id.
     */
    protected static int getNextId() {
        return sCount++;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code AbstractTask} with an incremental id.
     */
    public AbstractTask() {
        super(getNextId());
    }

    /**
     * Constructs a new {@code AbstractTask} with the given id.
     *
     * @param id the id of the {@code AbstractTask}.
     */
    public AbstractTask(int id) {
        super(id);
    }

    /**
     * Returns the priority of this {@code AbstractTask}.
     * Greater value indicates higher priority.
     *
     * @return the priority of this {@code AbstractTask}.
     */
    public int getPriority() {
        return -id;
    }

    /**
     * Sets the listener to be invoked when this {@code AbstractTask} is fulfilled.
     * <p>
     * To remove the previously assigned listener, just pass {@code null}.
     *
     * @param listener the {@code OnFulFillListener} object.
     */
    public void setOnFulfillListener(OnFulFillListener listener) {
        fulFillListener = listener;
    }

    /**
     * Checks whether this {@code AbstractTask} is currently active or not.
     *
     * @return {@code true} if this {@code AbstractTask} is active; {@code false} otherwise.
     */
    abstract public boolean isActive();

    /**
     * Checks whether this {@code AbstractTask} is fulfilled or not.
     *
     * @return {@code true} if this {@code AbstractTask} is fulfilled; {@code false} otherwise.
     */
    abstract public boolean isFulfilled();

    /**
     * Activates this {@code AbstractTask} and allocates its required resources.
     * <p>
     * This function should be called only once per {@code Task} object.
     */
    abstract public void activate() throws Exception;

    /**
     * Terminates this {@code AbstractTask} after completion.
     * <p>
     * A callback function to be invoked when this {@code AbstractTask} has been completed.
     * Used to clear and finalize allocated resources.
     */
    protected void terminate() {
        if (fulFillListener != null) {
            fulFillListener.onOrderFulfill(this);
        }
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    /**
     * Interface definition for a callback to be invoked when an {@link AbstractTask} object is fulfilled.
     */
    public interface OnFulFillListener {

        /**
         * Called when an {@code AbstractTask} has been fulfilled.
         *
         * @param task the fulfilled {@code AbstractTask}.
         */
        void onOrderFulfill(AbstractTask task);
    }
}
