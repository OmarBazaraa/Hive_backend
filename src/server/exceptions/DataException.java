package server.exceptions;


/**
 * This {@code DataException} class is used for any data consistency exceptions
 * received from the frontend.
 */
public class DataException extends Exception {

    /**
     * The error code of this {@code DataException}.
     */
    private int errorCode;

    /**
     * The arguments associated to this {@code DataException}.
     * To be interpreted by the error code.
     */
    private Object[] errorArgs;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     * @param errorCode the error code of the exception.
     * @param args      the detailed arguments of the exception.
     */
    public DataException(String message, int errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.errorArgs = args;
    }

    /**
     * Return the error code of this {@code DataException}.
     *
     * @return the error code.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the arguments associated to this {@code DataException}.
     *
     * These arguments are to be interpreted using the error code
     * by {@link #getErrorCode()} method.
     *
     * @return the error arguments.
     */
    public Object[] getErrorArgs() {
        return errorArgs;
    }
}
