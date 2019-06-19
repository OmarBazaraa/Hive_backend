package server;

import models.agents.Agent;
import models.items.Item;
import models.tasks.orders.Order;
import models.tasks.Task;
import models.warehouses.Warehouse;

import server.exceptions.DataException;
import server.utils.ServerConstants;
import server.utils.ServerConstants.*;
import server.utils.ServerDecoder;
import server.utils.ServerEncoder;

import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import spark.Spark;

import java.io.IOException;
import java.util.Map;


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
     * Whether ACK is received on the last update message.
     */
    private boolean receivedAck = false;

    /**
     * The updates of the current time step to be sent to the frontend in the next UPDATE message.
     */
    private JSONArray actions, logs, statistics;

    /**
     * The controls to be sent to the frontend in the next CONTROL message
     */
    private JSONArray activatedAgents, deactivatedAgents, blockedAgents;

    /**
     * Object used to lock thread from updating the {@code Warehouse} simultaneously.
     */
    private final Object lock = new Object();

    // ===============================================================================================
    //
    // Static Variables & Methods
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
    protected Server(String path, int port) {
        // Protected constructor to ensure a singleton object.
        Spark.port(port);
        Spark.webSocket(path, new WebSocketHandler());

        clearUpdateStates();
        clearControlStates();
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
     * Opens a {@code Session} with the frontend.
     * <p>
     * TODO: force only one the frontend client
     *
     * @param sess the frontend {@code Session} to open.
     */
    private synchronized void openSession(Session sess) {
        session = sess;
        currentState = ServerStates.IDLE;
    }

    /**
     * Closes the {@code Session} with the frontend.
     * <p>
     * TODO: force only one the frontend client
     *
     * @param sess the frontend {@code Session} to close.
     */
    private synchronized void closeSession(Session sess) {
        session = null;
        currentState = ServerStates.IDLE;
    }

    /**
     * Sends the give JSON message to the frontend.
     *
     * @param msg the message to sent.
     */
    private void send(JSONObject msg) {
        // DEBUG
        System.out.println("Sending ...");
        System.out.println(msg.toString(4));
        System.out.println();

        try {
            session.getRemote().sendString(msg.toString());
        } catch (IOException ex) {
            currentState = ServerStates.IDLE;
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Checks whether this {@code Server} object is currently connected with the
     * frontend.
     *
     * @return {@code true} if connected; {@code false} otherwise.
     */
    public synchronized boolean isConnected() {
        return (session != null && session.isOpen());
    }

    /**
     * Checks whether this {@code Server} is still running and did not process
     * an EXIT message.
     *
     * @return {@code true} if this {@code Server} did not process an EXIT message; {@code false} otherwise.
     */
    public synchronized boolean isRunning() {
        return (currentState != ServerStates.EXIT);
    }

    // ===============================================================================================
    //
    // Running Methods
    //

    /**
     * Performs a single run step in the {@code Warehouse}.
     * <p>
     * This function is to be called from the main thread.
     */
    public synchronized void run() {
        // Must be in RUNNING state
        if (currentState != ServerStates.RUNNING) {
            return;
        }

        // Check if last update message has been acknowledged
        if (receivedAck) {
            clearUpdateStates();

            try {
                if (warehouse.run()) {
                    sendUpdateMsg();        // Send updates only in the case of actual change in the warehouse
                    receivedAck = false;    // Consume the ACK

                    // DEBUG
                    System.out.println(warehouse);
                }
            } catch (Exception ex) {
                sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR,
                        ServerConstants.ERR_SERVER, "Internal server error.");
                currentState = ServerStates.IDLE;
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // ===============================================================================================
    //
    // Frontend -> Backend
    //

    /**
     * Processes the incoming messages from the frontend.
     * <p>
     * This function is to be called from server threads no the main thread.
     *
     * @param msg the raw message as received from the frontend.
     */
    private synchronized void process(String msg) {
        try {
            process(new JSONObject(msg));
        }
        // Handle invalid message format
        catch (JSONException ex) {
            sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR,
                    ServerConstants.ERR_MSG_FORMAT, "Invalid message format.");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        // Handle data inconsistency exceptions
        catch (DataException ex) {
            sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR,
                    ex.getErrorCode(), ex.getMessage(), ex.getErrorArgs());
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        // Handle internal server exceptions
        catch (Exception ex) {
            sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR,
                    ServerConstants.ERR_SERVER, "Internal server error.");
            currentState = ServerStates.IDLE;
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Processes the incoming messages from the frontend.
     * <p>
     * Note that this function is not being called from the main thread.
     *
     * @param msg the message as from the frontend after JSON parsing.
     */
    private synchronized void process(JSONObject msg) throws Exception {
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
            case ServerConstants.TYPE_ACK_UPDATE:
                processUpdateAckMsg(data);
                break;
            case ServerConstants.TYPE_ORDER:
                processOrderMsg(data);
                break;
            case ServerConstants.TYPE_CONTROL:
                processControlMsg(data);
                break;
            default:
                throw new DataException("Invalid message type.", ServerConstants.ERR_MSG_FORMAT);
        }
    }

    /**
     * Processes the START message from the frontend.
     * <p>
     * START message is used to configure the {@code Warehouse} and starts
     * its dynamics.
     *
     * @param data the received JSON data part of the message.
     */
    private synchronized void processStartMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.IDLE) {
            throw new DataException("Received START message while the server is not in IDLE state.",
                    ServerConstants.ERR_MSG_UNEXPECTED);
        }

        try {
            int mode = data.getInt(ServerConstants.KEY_MODE);
            JSONObject state = data.getJSONObject(ServerConstants.KEY_STATE);

            ServerDecoder.decodeWarehouse(state);
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_OK, 0, "");
            currentState = ServerStates.RUNNING;
            receivedAck = true;

            // DEBUG
            warehouse.print();
        } catch (JSONException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR,
                    ServerConstants.ERR_MSG_FORMAT, "Invalid START message format.");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (DataException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR,
                    ex.getErrorCode(), ex.getMessage(), ex.getErrorArgs());
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Processes the STOP message from the frontend.
     * <p>
     * STOP message is used to stop the running of the {@code Warehouse}
     * and set the server back to its idle state.
     *
     * @param data the received JSON data part of the message.
     */
    private synchronized void processStopMsg(JSONObject data) throws Exception {
        currentState = ServerStates.IDLE;
    }

    /**
     * Processes the PAUSE message from the frontend.
     * <p>
     * PAUSE message is used to pause the dynamics of the {@code Warehouse}
     * till RESUME is received.
     *
     * @param data the received JSON data part of the message.
     */
    private synchronized void processPauseMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.RUNNING) {
            throw new DataException("Received PAUSE message while the server is not in RUNNING state.",
                    ServerConstants.ERR_MSG_UNEXPECTED);
        }

        currentState = ServerStates.PAUSE;
    }

    /**
     * Processes the RESUME message from the frontend.
     * <p>
     * RESUME message is used to resume the dynamics of the {@code Warehouse}
     * from the same state before receiving PAUSE.
     *
     * @param data the received JSON data part of the message.
     */
    private synchronized void processResumeMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.PAUSE) {
            throw new DataException("Received RESUME message while the server is not in PAUSE state.",
                    ServerConstants.ERR_MSG_UNEXPECTED);
        }

        sendAckMsg(ServerConstants.TYPE_ACK_RESUME, ServerConstants.TYPE_OK, 0, "");
        currentState = ServerStates.RUNNING;
    }

    /**
     * Processes the EXIT message from the frontend.
     * <p>
     * EXIT message is used to close the server completely.
     *
     * @param data the received JSON data part of the message.
     */
    private synchronized void processExistMsg(JSONObject data) throws Exception {
        currentState = ServerStates.EXIT;
    }

    /**
     * Processes the ACK_UPDATE message from the frontend.
     * <p>
     * ACK_UPDATE message is used to acknowledge the server of that last update message
     * has been received successfully.
     *
     * @param data the received JSON data part of the message.
     */
    private synchronized void processUpdateAckMsg(JSONObject data) throws Exception {
        if (currentState == ServerStates.IDLE) {
            throw new DataException("Received ACK message while the server is IDLE state.",
                    ServerConstants.ERR_MSG_UNEXPECTED);
        }

        if (receivedAck) {
            throw new DataException("Received multiple ACK messages.",
                    ServerConstants.ERR_MSG_UNEXPECTED);
        }

        receivedAck = true;

        // DEBUG
        System.out.println("Received ACK");
        System.out.println();
    }

    /**
     * Processes the ORDER message from the frontend.
     * <p>
     * ORDER message is used to add a new {@code Order} to the {@code Warehouse}.
     *
     * @param data the received JSON data part of the message.
     */
    private synchronized void processOrderMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.RUNNING) {
            throw new DataException("Received ORDER message while the server is not in RUNNING state.",
                    ServerConstants.ERR_MSG_UNEXPECTED);
        }

        try {
            Order order = ServerDecoder.decodeOrder(data);
            warehouse.addOrder(order);
            sendAckMsg(ServerConstants.TYPE_ACK_ORDER, ServerConstants.TYPE_OK, 0, "");

            // DEBUG
            System.out.println("Order received:");
            System.out.println("    > " + order);
            System.out.println();
        } catch (JSONException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR,
                    ServerConstants.ERR_MSG_FORMAT, "Invalid ORDER message format.");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (DataException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_ORDER, ServerConstants.TYPE_ERROR,
                    ex.getErrorCode(), ex.getMessage(), ex.getErrorArgs());
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Processes the CONTROL message from the frontend.
     * <p>
     * CONTROL message is used to control the agents of the {@code Warehouse}.
     *
     * @param data the received JSON data part of the message.
     */
    private synchronized void processControlMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.RUNNING) {
            throw new DataException("Received CONTROL message while the server is not in RUNNING state.",
                    ServerConstants.ERR_MSG_UNEXPECTED);
        }

        int type = data.getInt(ServerConstants.KEY_TYPE);
        int id = data.getInt(ServerConstants.KEY_ID);
        Agent agent = warehouse.getAgentById(id);

        if (agent == null) {
            throw new DataException("Control message with invalid agent id: " + id + ".",
                    ServerConstants.ERR_INVALID_ARGS);
        }

        clearControlStates();

        switch (type) {
            case ServerConstants.TYPE_CONTROL_ACTIVATE:
                agent.activate();

                // DEBUG
                System.out.println("Activating " + agent + ".");
                break;
            case ServerConstants.TYPE_CONTROL_DEACTIVATE:
                agent.deactivate();

                // DEBUG
                System.out.println("Deactivating " + agent + ".");
                break;
            default:
                throw new DataException("Control message with invalid type: " + type + ".",
                        ServerConstants.ERR_INVALID_ARGS);
        }

        sendControlMsg();
    }

    // ===============================================================================================
    //
    // Backend -> Frontend
    //

    /**
     * Clears the update states JSON arrays of the current time step.
     */
    public synchronized void clearUpdateStates() {
        actions = new JSONArray();
        logs = new JSONArray();
        statistics = new JSONArray();
    }

    /**
     * Sends an acknowledge message to the frontend.
     *
     * @param type      the type of acknowledgement.
     * @param status    the status of the acknowledgement. Either OK or ERROR.
     * @param errCode   the error code.
     * @param errReason the string message explaining the reason of the error.
     * @param errArgs   the error arguments.
     */
    private synchronized void sendAckMsg(int type, int status, int errCode, String errReason, Object... errArgs) {
        send(ServerEncoder.encodeAckMsg(type, status, errCode, errReason, errArgs));
    }

    /**
     * Enqueues an {@code AgentAction} to be sent in the next update message.
     *
     * @param agent  the updated {@code Agent}.
     * @param action the performed action.
     */
    public synchronized void enqueueAgentAction(Agent agent, AgentAction action) {
        actions.put(ServerEncoder.encodeAgentAction(agent, action));
    }

    /**
     * Enqueues a log about a newly assigned {@code Task} to be sent in the next update message.
     *
     * @param task  the newly assigned {@code Task}.
     * @param order the associated {@code Order}.
     */
    public synchronized void enqueueTaskAssignedLog(Task task, Order order) {
        logs.put(ServerEncoder.encodeTaskAssignedLog(task, order));
    }

    /**
     * Enqueues a log about a newly completed {@code Task} to be sent in the next update message.
     *
     * @param task  the newly assigned {@code Task}.
     * @param order the associated {@code Order}.
     * @param items the map of add/removed items by the completed {@code Task}.
     */
    public synchronized void enqueueTaskCompletedLog(Task task, Order order, Map<Item, Integer> items) {
        logs.put(ServerEncoder.encodeTaskCompletedLog(task, order, items));
    }

    /**
     * Enqueues a log about a newly fulfilled {@code Order} to be sent in the next update message.
     *
     * @param order the newly issued {@code Order}.
     */
    public synchronized void enqueueOrderFulfilledLog(Order order) {
        logs.put(ServerEncoder.encodeOrderLog(ServerConstants.TYPE_LOG_ORDER_FULFILLED, order));
    }

    /**
     * Enqueues a new statistic to be sent in the next update message.
     *
     * @param key   the statistic type.
     * @param value the value of the statistic.
     */
    public synchronized void enqueueStatistics(int key, double value) {
        actions.put(ServerEncoder.encodeStatistics(key, value));
    }

    /**
     * Sends the current update message to the frontend.
     */
    public synchronized void sendUpdateMsg() {
        send(ServerEncoder.encodeUpdateMsg(warehouse.getTime(), actions, logs, statistics));
    }

    /**
     * Clears the control states JSON arrays.
     */
    public synchronized void clearControlStates() {
        activatedAgents = new JSONArray();
        deactivatedAgents = new JSONArray();
        blockedAgents = new JSONArray();
    }

    /**
     * Enqueues an activated {@code Agent} to be sent in the next control message.
     *
     * @param agent the activated {@code Agent}.
     */
    public synchronized void enqueueActivatedAgent(Agent agent) {
        activatedAgents.put(agent.getId());
    }

    /**
     * Enqueues an deactivated {@code Agent} to be sent in the next control message.
     *
     * @param agent the deactivated {@code Agent}.
     */
    public synchronized void enqueueDeactivatedAgent(Agent agent) {
        deactivatedAgents.put(agent.getId());
    }

    /**
     * Enqueues a blocked {@code Agent} to be sent in the next control message.
     *
     * @param agent the blocked {@code Agent}.
     */
    public synchronized void enqueueBlockedAgent(Agent agent) {
        blockedAgents.put(agent.getId());
    }

    /**
     * Sends the current control message to the frontend.
     */
    public synchronized void sendControlMsg() {
        send(ServerEncoder.encodeControlMsg(activatedAgents, deactivatedAgents, blockedAgents));
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    @WebSocket
    public class WebSocketHandler {

        @OnWebSocketConnect
        public void onConnect(Session client) {
            openSession(client);

            System.out.println();
            System.out.println("Frontend connected!");
            System.out.println();
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            closeSession(client);

            System.out.println();
            System.out.println("Frontend connection closed with status code: " + statusCode);
            System.out.println();
        }

        @OnWebSocketMessage
        public void onMessage(Session client, String message) throws Exception {
            process(message);
        }
    }
}
