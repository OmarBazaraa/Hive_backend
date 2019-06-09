package server;

import models.agents.Agent;
import models.tasks.Order;
import models.tasks.Task;
import models.warehouses.Warehouse;

import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import spark.Spark;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * This {@code Server} class is the connection between our Hive Warehouse System's
 * backend and frontend.
 * <p>
 * It contains useful functions for sending and receiving information between the backend and the frontend.
 */
public class Server {

    //
    // Enums
    //

    /**
     * Different states of the {@code Server} during its lifecycle.
     */
    enum ServerStates {
        IDLE,
        RUNNING,
        PAUSED,
        EXIT
    }

    // ===============================================================================================
    //
    // Member Variables
    //

    /**
     * The {@code Session} with the frontend.
     */
    private Session session;

    /**
     * The {@code Warehouse} object.
     */
    private Warehouse warehouse = Warehouse.getInstance();

    /**
     * The current state of the server.
     */
    private ServerStates currentState = ServerStates.IDLE;

    /**
     * The queue of received messages from the frontend.
     */
    private BlockingQueue<JSONObject> receivedQueue = new LinkedBlockingQueue<>();

    /**
     * The updates states of the current time step to be sent to the frontend.
     */
    private JSONArray actions, logs, statistics;

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * The only instance of this {@code Server} class.
     */
    private static Server sServer =
            new Server(ServerConstants.SERVER_PATH, ServerConstants.SERVER_PORT);

    /**
     * Returns the only available instance of this {@code Server} class.
     *
     * @return the only available {@code Server} object.
     */
    public static Server getInstance() {
        return sServer;
    }

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Server} object.
     *
     * @param path the endpoint path of the communicator.
     * @param port the port number.
     */
    private Server(String path, int port) {
        // Private constructor to ensure a singleton object.
        Spark.port(port);
        Spark.webSocket(path, new WebSocketHandler());
    }

    /**
     * Starts and initializes this {@code Server} object.
     */
    public void start() throws Exception {
        Spark.init();

        while (currentState != ServerStates.EXIT) {
            run();
        }
    }

    /**
     * Closes and terminates this {@code Server} object.
     */
    public void close() throws Exception  {
        Spark.stop();
    }

    /**
     * Checks whether this {@code Server} is still running and did not receive
     * an EXIT message.
     *
     * @return {@code true} if this {@code Server} did not receive an EXIT message; {@code false} otherwise.
     */
    public boolean isRunning() {
        return (currentState != ServerStates.EXIT);
    }

    /**
     * Checks whether this {@code Server} object is currently connected with the
     * frontend.
     *
     * @return {@code true} if connected; {@code false} otherwise.
     */
    public boolean isConnected() {
        return (session != null && session.isOpen());
    }

    /**
     * Clears the update states JSON arrays of the current time step.
     */
    private void clearUpdateStates() {
        actions = new JSONArray();
        logs = new JSONArray();
        statistics = new JSONArray();
    }

    /**
     * Keeps the communication running between the backend and the frontend.
     */
    private void run() throws Exception {
        // Get the first message or wait if no one is available
        JSONObject msg = receivedQueue.take();

        // Get message type
        int type = msg.getInt(ServerConstants.KEY_TYPE);
        JSONObject data = msg.optJSONObject(ServerConstants.KEY_DATA);

        // Switch on different message types from the frontend
        switch (type) {
            case ServerConstants.TYPE_START:
                processStartMsg(data);
                break;
            case ServerConstants.TYPE_STOP:
                processStopMsg(data);
                break;
            case ServerConstants.TYPE_RESUME:
                processResumeMsg(data);
                break;
            case ServerConstants.TYPE_PAUSE:
                processPauseMsg(data);
                break;
            case ServerConstants.TYPE_EXIT:
                processExistMsg(data);
                break;
            case ServerConstants.TYPE_ACK:
                processAckMsg(data);
                break;
            case ServerConstants.TYPE_ORDER:
                processOrderMsg(data);
                break;
        }
    }

    private void processStartMsg(JSONObject data) throws Exception {
        if (currentState == ServerStates.IDLE) {
            warehouse.clear();
            ServerDecoder.decodeInitConfig(data);
            clearUpdateStates();
            warehouse.init();
            warehouse.run();
            currentState = ServerStates.RUNNING;
        }
    }

    private void processStopMsg(JSONObject data) throws Exception {
        currentState = ServerStates.IDLE;
    }

    private void processResumeMsg(JSONObject data) throws Exception {
        if (currentState == ServerStates.PAUSED) {
            currentState = ServerStates.RUNNING;
        }
    }

    private void processPauseMsg(JSONObject data) throws Exception {
        if (currentState == ServerStates.RUNNING) {
            currentState = ServerStates.PAUSED;
        }
    }

    private void processExistMsg(JSONObject data) throws Exception {
        currentState = ServerStates.EXIT;
    }

    private void processAckMsg(JSONObject data) throws Exception {
        if (currentState == ServerStates.RUNNING) {
            warehouse.run();
        }
    }

    private void processOrderMsg(JSONObject data) throws Exception {
        if (currentState == ServerStates.RUNNING) {
            ServerDecoder.decodeOrder(data);
        }
    }

    public void enqueueAgentAction(Agent agent, AgentAction action) {
        actions.put(ServerEncoder.encodeAgentAction(agent, action));
    }

    public void enqueueStatistics(int key, double value) {
        actions.put(ServerEncoder.encodeStatistics(key, value));
    }

    private void sendUpdateMsg() throws Exception {
        send(ServerEncoder.encodeUpdateMsg(warehouse.getTime(), actions, logs, statistics));
    }

    private void sendAckMsg(int type, int status, String msg) throws Exception {
        send(ServerEncoder.encodeAckMsg(type, status, msg));
    }

    private void send(JSONObject msg) throws Exception {
        session.getRemote().sendString(msg.toString());
    }

    /**
     * Sets the frontend {@code Session}.
     *
     * @param sess the frontend {@code Session} to set.
     */
    private synchronized void setSession(Session sess) {
        session = sess;
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    @WebSocket
    public class WebSocketHandler {

        @OnWebSocketConnect
        public void onConnect(Session client) throws Exception {
            // TODO: force only one client, the frontend
            setSession(client);
            System.out.println("Frontend connected!");
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            System.out.println("Frontend connection closed with status code: " + statusCode);
        }

        @OnWebSocketMessage
        public void onMessage(Session client, String message) throws Exception {
            receivedQueue.add(new JSONObject(message));
        }
    }
}
