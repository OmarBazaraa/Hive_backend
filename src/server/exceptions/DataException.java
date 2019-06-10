package server.exceptions;


/**
 * This {@code DataException} class is used for any data consistency exceptions
 * received from the frontend.
 */
public class DataException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public DataException(String message) {
        super(message);
    }
}
