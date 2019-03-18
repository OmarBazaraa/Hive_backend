package server;

import models.agents.Agent;
import models.tasks.Order;
import models.tasks.Task;
import models.warehouses.Warehouse;

import utils.Constants;
import utils.Constants.*;
import utils.Utility;

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
    // Member Variables
    //

    enum ServerStates {
        IDLE,
        RUNNING,
        WAITING_ACK,
        PAUSED,
        EXIT
    }

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
     * The last sent message to the frontend
     */
    private JSONObject sentMsg = getEmptySendMessage();

    // ===============================================================================================
    //
    // Static Methods
    //

    /**
     * The only instance of this {@code Server} class.
     */
    private static Server sServer =
            new Server(Constants.SERVER_PATH, Constants.SERVER_PORT);

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
    public void start() {
        Spark.init();
    }

    /**
     * Closes and terminates this {@code Server} object.
     */
    public void close() {
        Spark.stop();
    }

    /**
     * Checks whether this {@code Server} is still running and did not receive
     * an EXIT message or not.
     *
     * @return {@code true} if this {@code Server} did not receive an EXIT message; {@code false} otherwise.
     */
    public boolean isRunning() {
        return (currentState != ServerStates.EXIT);
    }

    /**
     * Checks whether this {@code Server} object is currently connected with the
     * frontend or not.
     *
     * @return {@code true} if connected; {@code false} otherwise.
     */
    public boolean isConnected() {
        return (session != null && session.isOpen());
    }

    /**
     *
     */
    public void run() throws Exception {
        if (currentState == ServerStates.EXIT) {
            return;
        }

        switch (currentState) {
            case IDLE:
                processIdleState();
                break;
            case PAUSED:
                processPausedState();
                break;
            case RUNNING:
                processRunningState();
                break;
            case WAITING_ACK:
                processWaitingAckState();
                break;
        }
    }

    public void sendAction(Agent agent, AgentAction action) {
        JSONObject data = sentMsg.getJSONObject(Constants.MSG_KEY_DATA);
        JSONArray actions = data.getJSONArray(Constants.MSG_KEY_ACTIONS);

        JSONObject actionObj = new JSONObject()
                .put(Constants.MSG_KEY_TYPE, Utility.actionToServerType(action))
                .put(Constants.MSG_KEY_AGENT_ID, agent.getId());

        actions.put(actionObj);
    }

    public void sendLog(Task task) {
        JSONObject data = sentMsg.getJSONObject(Constants.MSG_KEY_DATA);
        JSONArray logs = data.getJSONArray(Constants.MSG_KEY_ACTIONS);

        JSONObject logObj = new JSONObject();
        // TODO: add log items

        logs.put(logObj);
    }

    public void sendLog(Order order) {
        // TODO:
    }

    public void sendStatistics(Map<String, Integer> statistics) {
        JSONObject data = sentMsg.getJSONObject(Constants.MSG_KEY_DATA);
        data.put(Constants.MSG_KEY_STATISTICS, statistics);
    }

    public void flushSendBuffer() throws IOException {
        session.getRemote().sendString(sentMsg.toString());
    }

    /**
     * Initializes sent message.
     */
    private JSONObject getEmptySendMessage() {
        // Create data object
        JSONObject data = new JSONObject();
        data.put(Constants.MSG_KEY_TIME_STEP, warehouse.getTime());
        data.put(Constants.MSG_KEY_ACTIONS, new JSONArray());
        data.put(Constants.MSG_KEY_LOGS, new JSONArray());
        data.put(Constants.MSG_KEY_STATISTICS, new JSONObject());

        // Create main object
        JSONObject ret = new JSONObject();
        ret.put(Constants.MSG_KEY_TYPE, Constants.MSG_TYPE_UPDATE);
        ret.put(Constants.MSG_KEY_DATA, data);

        return ret;
    }

    /**
     *
     */
    private void processIdleState() throws Exception {
        // Get the first message or wait if no one is available
        JSONObject obj = receivedQueue.take();

        // Get message type and data
        int type = obj.optInt(Constants.MSG_KEY_TYPE);
        JSONObject data = obj.optJSONObject(Constants.MSG_KEY_DATA);

        // Skip un-related messages
        if (type != Constants.MSG_TYPE_CONFIG) {
            return;
        }

        // Process configuration message
        processConfigMsg(data);
    }

    /**
     *
     */
    private void processPausedState() throws Exception {
        // Get the first message or wait if no one is available
        JSONObject obj = receivedQueue.take();

        // Get message type and data
        int type = obj.optInt(Constants.MSG_KEY_TYPE);
        JSONObject data = obj.optJSONObject(Constants.MSG_KEY_DATA);

        // Handle valid messages
        switch (type) {
            case Constants.MSG_TYPE_RUN:
                currentState = ServerStates.RUNNING;
                break;
            case Constants.MSG_TYPE_DEPLOY:
                // TODO: handle later
                break;
            case Constants.MSG_TYPE_ORDER:
                processOrderMsg(data);
                break;
            case Constants.MSG_TYPE_CONFIG:
                processConfigMsg(data);
                break;
        }
    }

    /**
     *
     */
    private void processRunningState() throws Exception {
        warehouse.run();
        flushSendBuffer();
        currentState = ServerStates.WAITING_ACK;
    }

    /**
     *
     */
    private void processWaitingAckState() throws Exception {
        // Get the first message or wait if no one is available
        JSONObject obj = receivedQueue.take();

        // Get message type and data
        int type = obj.optInt(Constants.MSG_KEY_TYPE);
        JSONObject data = obj.optJSONObject(Constants.MSG_KEY_DATA);

        // Handle valid messages
        switch (type) {
            case Constants.MSG_TYPE_ACK:
                sentMsg = getEmptySendMessage();
                currentState = ServerStates.RUNNING;
                break;
            case Constants.MSG_TYPE_PAUSE:
                currentState = ServerStates.PAUSED;
                break;
            case Constants.MSG_TYPE_STOP:
                currentState = ServerStates.IDLE;
                break;
            case Constants.MSG_TYPE_ORDER:
                processOrderMsg(data);
                break;
            case Constants.MSG_TYPE_CONFIG:
                processConfigMsg(data);
                break;
        }
    }

    /**
     *
     * @param msg
     */
    private void processConfigMsg(JSONObject msg) throws Exception {
        warehouse.configure(msg);
        currentState = ServerStates.PAUSED;
    }

    /**
     *
     * @param msg
     */
    private void processOrderMsg(JSONObject msg) throws Exception {
        warehouse.addOrder(msg);
    }

    /**
     * Adds and inserts a new message from the frontend to the messages queue.
     * <p>
     * This function consumes messages of the following types:
     * <ul>
     *     <li>EXIT</li>
     * </ul>
     *
     * @param message the message to add.
     */
    private synchronized void addMessage(String message) throws Exception {
        // Create JSON object
        JSONObject obj = new JSONObject(message);

        // Get message type
        int type = obj.optInt(Constants.MSG_KEY_TYPE);

        // Consume EXIT message
        if (type == Constants.MSG_TYPE_EXIT) {
            currentState = ServerStates.EXIT;
        }

        // Add message to the queue
        receivedQueue.add(obj);
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
            addMessage(message);
        }
    }
}
