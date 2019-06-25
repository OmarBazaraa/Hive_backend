package communicators.frontend;

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
 * This {@code FrontendCommunicator} class is the connection between our Hive Warehouse System's
 * backend and frontend.
 * <p>
 * It contains useful functions for sending and receiving information between the backend and the frontend.
 */
public class FrontendCommunicator {

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

    /**
     * Whether ACK is received on the last update message.
     */
    private boolean receivedAck = false;

    /**
     * The updates of the current time step to be sent to the frontend in the next UPDATE message.
     */
    private JSONArray actions, logs, statistics;

    /**
     * The controls to be sent to the frontend in the next CONTROL message.
     */
    private JSONArray activatedAgents, deactivatedAgents, blockedAgents;

    /**
     * Object used to lock threads from modifying
     * the UPDATE message-related variables of this {@code FrontendCommunicator} simultaneously.
     */
    private final Object lock1 = new Object();

    /**
     * Object used to lock threads from modifying
     * the CONTROL message-related variables of this {@code FrontendCommunicator} simultaneously.
     */
    private final Object lock2 = new Object();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code FrontendCommunicator} object.
     *
     * @param port the port number.
     */
    public FrontendCommunicator(int port, CommunicationListener l) {
        // Protected constructor to ensure a singleton object.
        server = Service.ignite();
        server.port(port);
        server.webSocket("/", new WebSocketHandler());

        // Set the communication listener
        listener = l;

        // Clear queues
        clearUpdateStates();
        clearControlStates();
    }

    /**
     * Starts and initializes this {@code FrontendCommunicator} object.
     */
    public void start() {
        server.init();
    }

    /**
     * Closes and terminates this {@code FrontendCommunicator} object.
     */
    public void close() {
        server.stop();
    }

    /**
     * Opens a {@code Session} with the frontend.
     *
     * @param sess the frontend {@code Session} to open.
     */
    private synchronized void openSession(Session sess) {
        session = sess;
    }

    /**
     * Closes the {@code Session} with the frontend.
     *
     * @param sess the frontend {@code Session} to close.
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
        try {
            session.getRemote().sendString(msg.toString());

            // DEBUG
            System.out.println("FrontendCommunicator :: Sending to frontend ...");
            System.out.println(msg.toString(4));
            System.out.println();
        } catch (IOException ex) {
            listener.onStop();
            System.err.println(ex.getMessage());
        }
    }

    // ===============================================================================================
    //
    // Frontend -> Backend
    //

    /**
     * Processes the incoming messages from the frontend.
     * <p>
     * This function is to be called from communicator threads not the main thread.
     *
     * @param msg the raw message as received from the frontend.
     */
    private void process(String msg) {
        // Try to process the incoming JSON message
        try {
            process(new JSONObject(msg));
        }
        // Handle invalid message format
        catch (JSONException ex) {
            sendErr(FrontendConstants.ERR_MSG_FORMAT, "Invalid message format.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        // Handle data inconsistency exceptions
        catch (DataException ex) {
            sendErr(ex.getErrorCode(), ex.getMessage(), ex.getErrorArgs());
            System.err.println(ex.getMessage());
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
    private void process(JSONObject msg) throws JSONException, DataException {
        // Get message type
        int type = msg.getInt(FrontendConstants.KEY_TYPE);
        JSONObject data = msg.optJSONObject(FrontendConstants.KEY_DATA);

        // Switch on different message types from the frontend
        switch (type) {
            case FrontendConstants.TYPE_START:
                processStartMsg(data);
                break;
            case FrontendConstants.TYPE_STOP:
                processStopMsg(data);
                break;
            case FrontendConstants.TYPE_PAUSE:
                processPauseMsg(data);
                break;
            case FrontendConstants.TYPE_RESUME:
                processResumeMsg(data);
                break;
            case FrontendConstants.TYPE_ORDER:
                processOrderMsg(data);
                break;
            case FrontendConstants.TYPE_CONTROL:
                processControlMsg(data);
                break;
            case FrontendConstants.TYPE_ACK_UPDATE:
                processAckMsg(data);
                break;
            default:
                throw new DataException("Invalid message type.", FrontendConstants.ERR_MSG_FORMAT);
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
    private void processStartMsg(JSONObject data) throws DataException {
        if (listener.getState() != ServerState.IDLE) {
            throw new DataException("Received START message while the server is not in IDLE state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        try {
            // Decode initial warehouse configurations
            int mode = data.getInt(FrontendConstants.KEY_MODE);
            JSONObject state = data.getJSONObject(FrontendConstants.KEY_STATE);

            RunningMode runningMode = (mode == FrontendConstants.TYPE_MODE_DEPLOY) ?
                    RunningMode.DEPLOYMENT : RunningMode.SIMULATION;

            synchronized (warehouse) {
                Decoder.decodeWarehouse(state, runningMode);
                listener.onStart(runningMode);
            }

            // Send start acknowledgement
            sendMsg(FrontendConstants.TYPE_ACK_START, FrontendConstants.TYPE_OK, 0, "");
            setLastStepStatus(true);
        } catch (JSONException ex) {
            sendMsg(FrontendConstants.TYPE_ACK_START, FrontendConstants.TYPE_ERROR,
                    FrontendConstants.ERR_MSG_FORMAT, "Invalid START message format.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        } catch (DataException ex) {
            sendMsg(FrontendConstants.TYPE_ACK_START, FrontendConstants.TYPE_ERROR,
                    ex.getErrorCode(), ex.getMessage(), ex.getErrorArgs());
            System.err.println(ex.getMessage());
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
    private void processStopMsg(JSONObject data) throws DataException {
        listener.onStop();
    }

    /**
     * Processes the PAUSE message from the frontend.
     * <p>
     * PAUSE message is used to pause the dynamics of the {@code Warehouse}
     * till RESUME is received.
     *
     * @param data the received JSON data part of the message.
     */
    private void processPauseMsg(JSONObject data) throws DataException {
        if (listener.getState() != ServerState.RUNNING) {
            throw new DataException("Received PAUSE message while the server is not in RUNNING state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        listener.onPause();
    }

    /**
     * Processes the RESUME message from the frontend.
     * <p>
     * RESUME message is used to resume the dynamics of the {@code Warehouse}
     * from the same state before receiving PAUSE.
     *
     * @param data the received JSON data part of the message.
     */
    private void processResumeMsg(JSONObject data) throws DataException {
        if (listener.getState() != ServerState.PAUSE) {
            throw new DataException("Received RESUME message while the server is not in PAUSE state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        listener.onResume();
        sendMsg(FrontendConstants.TYPE_ACK_RESUME, FrontendConstants.TYPE_OK, 0, "");
    }

    /**
     * Processes the ORDER message from the frontend.
     * <p>
     * ORDER message is used to add a new {@code Order} to the {@code Warehouse}.
     *
     * @param data the received JSON data part of the message.
     */
    private void processOrderMsg(JSONObject data) throws DataException {
        if (listener.getState() != ServerState.RUNNING) {
            throw new DataException("Received ORDER message while the server is not in RUNNING state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        try {
            // Decode incoming order
            synchronized (warehouse) {
                Order order = Decoder.decodeOrder(data);
                listener.onOrderIssued(order);
            }

            // Send order acknowledgement
            sendMsg(FrontendConstants.TYPE_ACK_ORDER, FrontendConstants.TYPE_OK, 0, "");
        } catch (JSONException ex) {
            sendMsg(FrontendConstants.TYPE_ACK_START, FrontendConstants.TYPE_ERROR,
                    FrontendConstants.ERR_MSG_FORMAT, "Invalid ORDER message format.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        } catch (DataException ex) {
            sendMsg(FrontendConstants.TYPE_ACK_ORDER, FrontendConstants.TYPE_ERROR,
                    ex.getErrorCode(), ex.getMessage(), ex.getErrorArgs());
            System.err.println(ex.getMessage());
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
    private void processControlMsg(JSONObject data) throws DataException {
        if (listener.getState() != ServerState.RUNNING) {
            throw new DataException("Received CONTROL message while the server is not in RUNNING state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        int id = data.getInt(FrontendConstants.KEY_ID);
        int type = data.getInt(FrontendConstants.KEY_TYPE);
        Agent agent;

        synchronized (warehouse) {
            agent = warehouse.getAgentById(id);
        }

        if (agent == null) {
            throw new DataException("Control message with invalid agent id: " + id + ".",
                    FrontendConstants.ERR_INVALID_ARGS);
        }

        switch (type) {
            case FrontendConstants.TYPE_CONTROL_ACTIVATE:
                listener.onAgentActivated(agent);
                break;
            case FrontendConstants.TYPE_CONTROL_DEACTIVATE:
                listener.onAgentDeactivated(agent);
                break;
            default:
                throw new DataException("Control message with invalid type: " + type + ".",
                        FrontendConstants.ERR_INVALID_ARGS);
        }
    }

    /**
     * Processes the ACK_UPDATE message from the frontend.
     * <p>
     * ACK_UPDATE message is used to acknowledge the communicator of that last update message
     * has been received successfully.
     *
     * @param data the received JSON data part of the message.
     */
    private void processAckMsg(JSONObject data) throws DataException {
        if (listener.getState() == ServerState.IDLE) {
            throw new DataException("Received ACK message while the server is in IDLE state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        if (isLastStepCompleted()) {
            throw new DataException("Received multiple ACK messages.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        // Update ACK flag
        setLastStepStatus(true);

        // DEBUG
        System.out.println("FrontendCommunicator :: Received actions DONE.");
        System.out.println();
    }

    /**
     * Checks whether the last time step has been completed by the frontend or not.
     *
     * @return {@code true} if completed; {@code false} otherwise.
     */
    public boolean isLastStepCompleted() {
        synchronized (lock1) {
            return receivedAck;
        }
    }

    /**
     * Sets the last time step as being completed by the frontend.
     *
     * @param completed {@code true} to set the last step as completed; {@code false} otherwise.
     */
    private void setLastStepStatus(boolean completed) {
        synchronized (lock1) {
            receivedAck = completed;
        }
    }

    // ===============================================================================================
    //
    // Backend -> Frontend
    //

    /**
     * Sends a message to the frontend.
     *
     * @param type      the type of message.
     * @param status    the status of the message. Either OK or ERROR.
     * @param errCode   the error code in case of ERROR; 0 otherwise.
     * @param errReason the string message explaining the reason of the error if any.
     * @param errArgs   the error arguments if any.
     */
    public void sendMsg(int type, int status, int errCode, String errReason, Object... errArgs) {
        send(Encoder.encodeAckMsg(type, status, errCode, errReason, errArgs));
    }

    /**
     * Sends an error message to the frontend.
     *
     * @param errCode   the error code in case;
     * @param errReason the string message explaining the reason of the error if any.
     * @param errArgs   the error arguments if any.
     */
    public void sendErr(int errCode, String errReason, Object... errArgs) {
        sendMsg(FrontendConstants.TYPE_MSG, FrontendConstants.TYPE_ERROR, errCode, errReason, errArgs);
    }

    /**
     * Clears the update states JSON arrays of the current time step.
     */
    public void clearUpdateStates() {
        synchronized (lock1) {
            actions = new JSONArray();
            logs = new JSONArray();
            statistics = new JSONArray();
        }
    }

    /**
     * Enqueues an {@code AgentAction} to be sent in the next update message.
     *
     * @param agent  the updated {@code Agent}.
     * @param action the performed action.
     */
    public void enqueueAgentAction(Agent agent, AgentAction action) {
        synchronized (lock1) {
            actions.put(Encoder.encodeAgentAction(agent, action));
        }
    }

    /**
     * Enqueues a log about a newly assigned {@code Task} to be sent in the next update message.
     *
     * @param task  the newly assigned {@code Task}.
     * @param order the associated {@code Order}.
     */
    public void enqueueTaskAssignedLog(Task task, Order order) {
        synchronized (lock1) {
            logs.put(Encoder.encodeTaskAssignedLog(task, order));
        }
    }

    /**
     * Enqueues a log about a newly completed {@code Task} to be sent in the next update message.
     *
     * @param task  the newly assigned {@code Task}.
     * @param order the associated {@code Order}.
     * @param items the map of add/removed items by the completed {@code Task}.
     */
    public void enqueueTaskCompletedLog(Task task, Order order, Map<Item, Integer> items) {
        synchronized (lock1) {
            logs.put(Encoder.encodeTaskCompletedLog(task, order, items));
        }
    }

    /**
     * Enqueues a log about a newly fulfilled {@code Order} to be sent in the next update message.
     *
     * @param order the newly issued {@code Order}.
     */
    public void enqueueOrderFulfilledLog(Order order) {
        synchronized (lock1) {
            logs.put(Encoder.encodeOrderLog(FrontendConstants.TYPE_LOG_ORDER_FULFILLED, order));
        }
    }

    /**
     * Enqueues a new statistic to be sent in the next update message.
     *
     * @param key   the statistic type.
     * @param value the value of the statistic.
     */
    public void enqueueStatistics(int key, double value) {
        synchronized (lock1) {
            actions.put(Encoder.encodeStatistics(key, value));
        }
    }

    /**
     * Sends the current update message to the frontend.
     */
    public void flushUpdateMsg() {
        synchronized (lock1) {
            if (actions.isEmpty() && logs.isEmpty() && statistics.isEmpty()) {
                return;
            }

            send(Encoder.encodeUpdateMsg(warehouse.getTime(), actions, logs, statistics));
            clearUpdateStates();
            setLastStepStatus(false);
        }
    }

    /**
     * Clears the control states JSON arrays.
     */
    public void clearControlStates() {
        synchronized (lock2) {
            activatedAgents = new JSONArray();
            deactivatedAgents = new JSONArray();
            blockedAgents = new JSONArray();
        }
    }

    /**
     * Enqueues an activated {@code Agent} to be sent in the next control message.
     *
     * @param agent the activated {@code Agent}.
     */
    public void enqueueActivatedAgent(Agent agent) {
        synchronized (lock2) {
            activatedAgents.put(agent.getId());
        }
    }

    /**
     * Enqueues an deactivated {@code Agent} to be sent in the next control message.
     *
     * @param agent the deactivated {@code Agent}.
     */
    public void enqueueDeactivatedAgent(Agent agent) {
        synchronized (lock2) {
            deactivatedAgents.put(agent.getId());
        }
    }

    /**
     * Enqueues a blocked {@code Agent} to be sent in the next control message.
     *
     * @param agent the blocked {@code Agent}.
     */
    public void enqueueBlockedAgent(Agent agent) {
        synchronized (lock2) {
            blockedAgents.put(agent.getId());
        }
    }

    /**
     * Sends the current control message to the frontend.
     */
    public void flushControlMsg() {
        synchronized (lock2) {
            if (activatedAgents.isEmpty() && deactivatedAgents.isEmpty() && blockedAgents.isEmpty()) {
                return;
            }

            send(Encoder.encodeControlMsg(activatedAgents, deactivatedAgents, blockedAgents));
            clearControlStates();
        }
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    @WebSocket
    public class WebSocketHandler {

        @OnWebSocketConnect
        public void onConnect(Session client) {
            // TODO: force only one the frontend client
            openSession(client);

            // DEBUG
            System.out.println();
            System.out.println("FrontendCommunicator :: Frontend connected!");
            System.out.println();
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            // TODO: force only one the frontend client
            closeSession(client);

            // DEBUG
            System.out.println();
            System.out.println("FrontendCommunicator :: Frontend connection closed with status code: " + statusCode);
            System.out.println();
        }

        @OnWebSocketMessage
        public void onMessage(Session client, String message) {
            process(message);
        }
    }
}
