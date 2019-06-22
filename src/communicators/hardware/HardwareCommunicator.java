package communicators.hardware;

import communicators.CommunicationListener;
import communicators.exceptions.DataException;
import communicators.frontend.utils.Decoder;
import communicators.frontend.utils.Encoder;

import models.agents.Agent;
import models.items.Item;
import models.tasks.Task;
import models.tasks.orders.Order;
import models.warehouses.Warehouse;

import utils.Constants.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import spark.Service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;


/**
 * This {@code HardwareCommunicator} class is the connection between our Hive Warehouse System's
 * backend and the hardware robots.
 * <p>
 * It contains useful functions for sending and receiving information between the backend and the hardware.
 */
public class HardwareCommunicator {

    //
    // Member Variables
    //

    /**
     * The spark web socket server.
     */
    private Service server;

    /**
     * The {@code Session} with the frontend.
     */
    private Session session;

    /**
     * The communication listener.
     */
    private CommunicationListener listener;

    /**
     * The {@code Warehouse} object.
     */
    private final Warehouse warehouse = Warehouse.getInstance();


    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code HardwareCommunicator} object.
     *
     * @param port the port number.
     */
    public HardwareCommunicator(int port, CommunicationListener l) {
        // Protected constructor to ensure a singleton object.
        server = Service.ignite();
        server.port(port);
        server.webSocket("/", new WebSocketHandler());

        // Set the communication listener
        listener = l;
    }

    /**
     * Starts and initializes this {@code HardwareCommunicator} object.
     */
    public void start() {
        server.init();
    }

    /**
     * Closes and terminates this {@code HardwareCommunicator} object.
     */
    public void close() {
        server.stop();
    }

    /**
     * Opens a {@code Session} with a hardware robot.
     *
     * @param sess the {@code Session} to open.
     */
    private synchronized void openSession(Session sess) {
        session = sess;
    }

    /**
     * Closes the {@code Session} with a hardware robot.
     *
     * @param sess the {@code Session} to close.
     */
    private synchronized void closeSession(Session sess) {
        session = null;
        listener.onStop();
    }

    /**
     * Sends the give JSON message to the frontend.
     *
     * @param msg the message to sent.
     */
    private void send(JSONObject msg) {

    }

    // ===============================================================================================
    //
    // Hardware -> Backend
    //

    /**
     * Processes the incoming messages from the frontend.
     * <p>
     * This function is to be called from communicator threads not the main thread.
     *
     * @param msg the raw message as received from the frontend.
     */
    private void process(String msg) {

    }

    // ===============================================================================================
    //
    // Backend -> Frontend
    //



    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    @WebSocket
    public class WebSocketHandler {

        @OnWebSocketConnect
        public void onConnect(Session client) {
            openSession(client);

            // DEBUG
            System.out.println();
            System.out.println("Hardware robot connected!");
            System.out.println();
            System.out.println("Hardware communicator thread count: " + server.activeThreadCount());
            System.out.println();
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            closeSession(client);

            // DEBUG
            System.out.println();
            System.out.println("Hardware robot connection closed with status code: " + statusCode);
            System.out.println();
        }

        @OnWebSocketMessage
        public void onMessage(Session client, String message) {
            process(message);
        }
    }
}
