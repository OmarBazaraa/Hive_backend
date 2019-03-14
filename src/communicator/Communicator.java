package communicator;

import models.warehouses.Warehouse;
import utils.Constants;

import org.json.JSONObject;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import spark.Spark;


/**
 * This {@code Communicator} class is the connection between our Hive Warehouse System's
 * backend and frontend.
 * <p>
 * It contains useful functions for sending and receiving information between the backend and the frontend.
 */
public class Communicator {

    //
    // Member Variables
    //

    /**
     * The {@code Session} with the frontend.
     */
    private Session session;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * The only instance of this {@code Communicator} class.
     */
    private static Communicator sCommunicator =
            new Communicator(Constants.SERVER_PATH, Constants.SERVER_PORT);

    /**
     * Returns the only available instance of this {@code Communicator} class.
     *
     * @return the only available {@code Communicator} object.
     */
    public static Communicator getInstance() {
        return sCommunicator;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Communicator} object.
     *
     * @param path the endpoint path of the communicator.
     * @param port the port number.
     */
    private Communicator(String path, int port) {
        // Private constructor to ensure a singleton object.
        Spark.port(port);
        Spark.webSocket(path, new WebSocketHandler());
        Spark.init();
    }

    /**
     * Closes and terminates this {@code Communicator} object.
     */
    public synchronized void close() {
        Spark.stop();
    }

    /**
     * Checks whether this {@code Communicator} object is currently connected with the
     * frontend or not.
     *
     * @return {@code true} if connected; {@code false} otherwise.
     */
    public synchronized boolean isConnected() {
        return (session != null && session.isOpen());
    }

    /**
     * Sets the frontend {@code Session}.
     *
     * @param sess the frontend {@code Session} to set.
     */
    private synchronized void setSession(Session sess) {
        session = sess;
    }

    /**
     * Parses and dispatches a received message from the frontend client.
     *
     * @param message the message to parse.
     */
    private synchronized void dispatchMessage(String message) throws Exception {
        JSONObject msg = new JSONObject(message);

        int type = msg.optInt(Constants.MSG_KEY_TYPE);
        JSONObject data = msg.optJSONObject(Constants.MSG_KEY_DATA);

        switch (type) {
            case Constants.MSG_TYPE_CONFIG:
                Warehouse.getInstance().configure(data);
                break;
            case Constants.MSG_TYPE_ORDER:

                break;
            default:
                System.out.println("Invalid message type!");
        }
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    @WebSocket
    public class WebSocketHandler {

        @OnWebSocketConnect
        public void onConnect(Session client) throws Exception {
            if (isConnected()) {
                // TODO: find a better way of identifying the frontend
                client.disconnect();
                System.out.println("Multiple client connection!");
                return;
            }

            setSession(client);
            System.out.println("Frontend connected!");
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            System.out.println("Frontend connection closed with status code: " + statusCode);
        }

        @OnWebSocketMessage
        public void onMessage(Session client, String message) throws Exception {
            dispatchMessage(message);
        }
    }
}
