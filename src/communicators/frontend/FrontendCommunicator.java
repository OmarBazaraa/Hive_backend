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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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
     * The communication listener object.
     */
    private CommunicationListener listener;

    /**
     * The {@code Warehouse} object.
     */
    private final Warehouse warehouse = Warehouse.getInstance();

    /**
     * The map of pending actions.
     * That is, the actions that are waiting for DONE messages.
     */
    private ConcurrentHashMap<Integer, AgentAction> pendingActionMap = new ConcurrentHashMap<>();

    /**
     * The map of received DONE messages.
     */
    private ConcurrentHashMap<Integer, AgentAction> receivedDoneMap = new ConcurrentHashMap<>();

    /**
     * Object used to lock threads from sending using the same remote endpoint at the same time.
     */
    private Object lock = new Object();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code FrontendCommunicator} object.
     *
     * @param port the port number to listen on.
     */
    public FrontendCommunicator(int port, CommunicationListener l) {
        // Protected constructor to ensure a singleton object.
        server = Service.ignite();
        server.port(port);
        server.webSocket("/", new WebSocketHandler());

        // Set the communication listener
        listener = l;
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
            synchronized (lock) {
                session.getRemote().sendString(msg.toString());
            }

            // DEBUG
            System.out.println("FrontendCommunicator :: Sending to frontend: " + msg + " ...");
            System.out.println();
        } catch (IOException ex) {
            listener.onStop();
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Checks whether the last time step has been completed by all the agents or not.
     *
     * @return {@code true} if completed; {@code false} otherwise.
     */
    public boolean isLastStepCompleted() {
        return pendingActionMap.isEmpty();
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
                handleStartMsg(data);
                break;
            case FrontendConstants.TYPE_STOP:
                handleStopMsg(data);
                break;
            case FrontendConstants.TYPE_PAUSE:
                handlePauseMsg(data);
                break;
            case FrontendConstants.TYPE_RESUME:
                handleResumeMsg(data);
                break;
            case FrontendConstants.TYPE_ORDER:
                handleOrderMsg(data);
                break;
            case FrontendConstants.TYPE_CONTROL:
                handleControlMsg(data);
                break;
            case FrontendConstants.TYPE_DONE:
                handleDoneMsg(data);
                break;
            default:
                throw new DataException("Invalid message type.", FrontendConstants.ERR_MSG_FORMAT);
        }
    }

    /**
     * Handles the START message from the frontend.
     * <p>
     * START message is used to configure the {@code Warehouse} and starts
     * its dynamics.
     *
     * @param data the received JSON data part of the message.
     */
    private void handleStartMsg(JSONObject data) throws DataException {
        if (listener.getState() != ServerState.IDLE) {
            throw new DataException("Received START message while the server is not in IDLE state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        try {
            int mode = data.getInt(FrontendConstants.KEY_MODE);
            JSONObject state = data.getJSONObject(FrontendConstants.KEY_STATE);

            RunningMode runningMode = (mode == FrontendConstants.TYPE_MODE_DEPLOY) ?
                    RunningMode.DEPLOYMENT : RunningMode.SIMULATION;

            pendingActionMap.clear();
            receivedDoneMap.clear();

            synchronized (warehouse) {
                Decoder.decodeWarehouse(state, runningMode);
                listener.onStart(runningMode);
                sendMsg(FrontendConstants.TYPE_ACK_START, FrontendConstants.TYPE_OK, 0, "");
            }
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
     * Handles the STOP message from the frontend.
     * <p>
     * STOP message is used to stop the running of the {@code Warehouse}
     * and set the server back to its idle state.
     *
     * @param data the received JSON data part of the message.
     */
    private void handleStopMsg(JSONObject data) throws DataException {
        listener.onStop();
    }

    /**
     * Handles the PAUSE message from the frontend.
     * <p>
     * PAUSE message is used to pause the dynamics of the {@code Warehouse}
     * till RESUME is received.
     *
     * @param data the received JSON data part of the message.
     */
    private void handlePauseMsg(JSONObject data) throws DataException {
        if (listener.getState() != ServerState.RUNNING) {
            throw new DataException("Received PAUSE message while the server is not in RUNNING state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        listener.onPause();
    }

    /**
     * Handles the RESUME message from the frontend.
     * <p>
     * RESUME message is used to resume the dynamics of the {@code Warehouse}
     * from the same state before receiving PAUSE.
     *
     * @param data the received JSON data part of the message.
     */
    private void handleResumeMsg(JSONObject data) throws DataException {
        if (listener.getState() != ServerState.PAUSE) {
            throw new DataException("Received RESUME message while the server is not in PAUSE state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        listener.onResume();
        sendMsg(FrontendConstants.TYPE_ACK_RESUME, FrontendConstants.TYPE_OK, 0, "");
    }

    /**
     * Handles the ORDER message from the frontend.
     * <p>
     * ORDER message is used to add a new {@code Order} to the {@code Warehouse}.
     *
     * @param data the received JSON data part of the message.
     */
    private void handleOrderMsg(JSONObject data) throws DataException {
        if (listener.getState() == ServerState.IDLE) {
            throw new DataException("Received ORDER message while the server is in IDLE state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        try {
            synchronized (warehouse) {
                Order order = Decoder.decodeOrder(data);
                listener.onOrderIssue(order);
                sendMsg(FrontendConstants.TYPE_ACK_ORDER, FrontendConstants.TYPE_OK, 0, "");
            }
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
     * Handles the CONTROL message from the frontend.
     * <p>
     * CONTROL message is used to control the agents of the {@code Warehouse}.
     *
     * @param data the received JSON data part of the message.
     */
    private void handleControlMsg(JSONObject data) throws DataException {
        if (listener.getState() == ServerState.IDLE) {
            throw new DataException("Received CONTROL message while the server is in IDLE state.",
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
            case FrontendConstants.TYPE_AGENT_ACTIVATE:
                listener.onAgentActivate(agent);
                break;
            case FrontendConstants.TYPE_AGENT_DEACTIVATE:
                listener.onAgentDeactivate(agent);
                break;
            default:
                throw new DataException("Control message with invalid type: " + type + ".",
                        FrontendConstants.ERR_INVALID_ARGS);
        }
    }

    /**
     * Handles the DONE message from the frontend.
     * <p>
     * DONE message is used to acknowledge the communicator that last action of an {@code Agent}
     * has been completed successfully.
     * <p>
     * Called at any time from Spark threads.
     *
     * @param data the received JSON data part of the message.
     */
    private void handleDoneMsg(JSONObject data) throws DataException {
        if (listener.getState() == ServerState.IDLE) {
            throw new DataException("Received DONE message while the server is in IDLE state.",
                    FrontendConstants.ERR_MSG_UNEXPECTED);
        }

        int agentId = data.getInt(FrontendConstants.KEY_ID);
        receivedDoneMap.put(agentId, AgentAction.NOTHING);
        pendingActionMap.remove(agentId);

        if (pendingActionMap.isEmpty()) {
            listener.onActionsDone();
        }

        // DEBUG
        System.out.println("FrontendCommunicator :: Received action DONE from agent-" + agentId + ".");
        System.out.println();
    }

    // ===============================================================================================
    //
    // Backend -> Frontend
    //

    /**
     * Sends a control message for the given {@code Agent}.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from Spark threads.
     *
     * @param agent       the controlled {@code Agent}.
     * @param deactivated {@code true} to deactivate the {@code Agent}; {@code false} to activate it.
     */
    public void sendAgentControl(Agent agent, boolean deactivated) {
        send(Encoder.encodeAgentControl(agent, deactivated));
    }

    /**
     * Sends an immediate stop action for the given {@code Agent}.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from Spark threads.
     *
     * @param agent the {@code Agent} to send the action for.
     */
    public void sendAgentStop(Agent agent) {
        pendingActionMap.remove(agent.getId());
        send(Encoder.encodeAgentAction(agent, FrontendConstants.TYPE_AGENT_STOP));
    }

    /**
     * Sends an action for the given {@code Agent}.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from the main thread.
     *
     * @param agent  the {@code Agent} to send the action for.
     * @param action the action to send.
     */
    public void sendAgentAction(Agent agent, AgentAction action) {
        pendingActionMap.put(agent.getId(), action);
        receivedDoneMap.remove(agent.getId());
        send(Encoder.encodeAgentAction(agent, Encoder.encodeAgentActionType(action)));
    }

    /**
     * Sends a recover action for the given {@code Agent}.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from the main thread.
     *
     * @param agent  the {@code Agent} to send the action for.
     * @param action the action to send.
     */
    public void sendAgentRecoverAction(Agent agent, AgentAction action) {
        if (receivedDoneMap.containsKey(agent.getId())) {
            return;
        }

        pendingActionMap.put(agent.getId(), action);
        send(Encoder.encodeAgentAction(agent, Encoder.encodeAgentActionType(action)));
    }

    /**
     * Sends a log about a change in the battery level of an {@code Agent}.
     *
     * @param agent the updated {@code Agent}.
     */
    public void sendAgentBatteryUpdatedLog(Agent agent) {
        send(Encoder.encodeAgentBatteryUpdatedLog(agent));
    }

    /**
     * Sends a log about a newly assigned {@code Task}.
     *
     * @param order the associated {@code Order}.
     * @param task  the newly assigned {@code Task}.
     */
    public void sendTaskAssignedLog(Order order, Task task) {
        send(Encoder.encodeOrderTaskAssignedLog(order, task));
    }

    /**
     * Sends a log about a newly completed {@code Task}.
     *
     * @param order the associated {@code Order}.
     * @param task  the newly assigned {@code Task}.
     * @param items the map of add/removed items by the completed {@code Task}.
     */
    public void sendTaskCompletedLog(Order order, Task task, Map<Item, Integer> items) {
        send(Encoder.encodeOrderTaskCompletedLog(order, task, items));
    }

    /**
     * Sends a log about a newly fulfilled {@code Order}.
     *
     * @param order the newly issued {@code Order}.
     */
    public void sendOrderFulfilledLog(Order order) {
        send(Encoder.encodeOrderFulfilledLog(order));
    }

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
     * @param errCode   the error code in case.
     * @param errReason the string message explaining the reason of the error if any.
     * @param errArgs   the error arguments if any.
     */
    public void sendErr(int errCode, String errReason, Object... errArgs) {
        sendMsg(FrontendConstants.TYPE_MSG, FrontendConstants.TYPE_ERROR, errCode, errReason, errArgs);
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
            System.out.println("FrontendCommunicator :: Frontend connected!");
            System.out.println();
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            // TODO: force only one the frontend client
            closeSession(client);

            // DEBUG
            System.out.println("FrontendCommunicator :: Frontend connection closed with status code: " + statusCode);
            System.out.println();
        }

        @OnWebSocketMessage
        public void onMessage(Session client, String message) {
            System.out.println(">> from Frontend: " + message);  // TODO: to be removed
            System.out.flush();

            process(message);
        }
    }
}
