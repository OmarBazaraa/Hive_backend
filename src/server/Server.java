package server;

import models.agents.Agent;
import models.tasks.Order;
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
     * The updates states of the current time step to be sent to the frontend.
     */
    private JSONArray actions, logs, statistics;

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
     * Opens a {@code Session} with the frontend.
     *
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
     *
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
    private void send(JSONObject msg) throws IOException {
        // DEBUG
        System.out.println("Sending ...");
        System.out.println(msg.toString(4));

        session.getRemote().sendString(msg.toString());
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
    public synchronized void run() throws Exception {
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
                }
            }
            // Handle communication exceptions (probably the frontend is down)
            catch (IOException ex) {
                currentState = ServerStates.IDLE;
                System.out.println(ex.getMessage());
            }
            // Handle internal server exceptions
            catch (Exception ex) {
                sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR, "Internal server error.");
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
    private synchronized void process(String msg) throws Exception {
        try {
            process(new JSONObject(msg));
        }
        // Handle invalid message format
        catch (JSONException ex) {
            sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR, "Invalid message format.");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        // Handle data inconsistency exceptions
        catch (DataException ex) {
            sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR, ex.getMessage());
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        // Handle communication exceptions (probably the frontend is down)
        catch (IOException ex) {
            System.out.println(ex.getMessage());
            currentState = ServerStates.IDLE;
        }
        // Handle internal server exceptions
        catch (Exception ex) {
            sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR, "Internal server error.");
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
            default:
                throw new DataException("Invalid message type.");
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
            throw new DataException("Received START message while the server is not in IDLE state.");
        }

        try {
            ServerDecoder.decodeInitConfig(data);
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_OK, "");
            currentState = ServerStates.RUNNING;
            receivedAck = true;

            // DEBUG
            System.out.println(warehouse);
        } catch (JSONException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR, "Invalid START message format.");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (DataException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR, ex.getMessage());
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
        if (currentState == ServerStates.IDLE) {
            throw new DataException("Received STOP message while the server is in IDLE state.");
        }

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
            throw new DataException("Received PAUSE message while the server is not in RUNNING state.");
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
            throw new DataException("Received RESUME message while the server is not in PAUSE state.");
        }

        sendAckMsg(ServerConstants.TYPE_ACK_RESUME, ServerConstants.TYPE_OK, "");
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
            throw new DataException("Received ACK message while the server is IDLE state.");
        }

        if (receivedAck) {
            throw new DataException("Received multiple ACK messages.");
        }

        receivedAck = true;
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
            throw new DataException("Received ORDER message while the server is not in RUNNING state.");
        }

        try {
            Order order = ServerDecoder.decodeOrder(data);
            sendAckMsg(ServerConstants.TYPE_ACK_ORDER, ServerConstants.TYPE_OK, "");

            // DEBUG
            System.out.println("Order received:");
            System.out.println("    > " + order);
        } catch (JSONException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR, "Invalid ORDER message format.");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (DataException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_ORDER, ServerConstants.TYPE_ERROR, ex.getMessage());
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ===============================================================================================
    //
    // Backend -> Frontend
    //

    /**
     * Clears the update states JSON arrays of the current time step.
     */
    private synchronized void clearUpdateStates() {
        actions = new JSONArray();
        logs = new JSONArray();
        statistics = new JSONArray();
    }

    /**
     * Sends an acknowledge message to the frontend.
     *
     * @param type   the type of acknowledgement.
     * @param status the status of the acknowledgement.
     * @param msg    the piggybacked message if needed.
     */
    private synchronized void sendAckMsg(int type, int status, String msg) throws Exception {
        send(ServerEncoder.encodeAckMsg(type, status, msg));
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
     * @param task the newly assigned {@code Task}.
     */
    public synchronized void enqueueTaskAssignedLog(Task task) {
        logs.put(ServerEncoder.encodeTaskAssignedLog(task));
    }

    /**
     * Enqueues a log about a newly completed {@code Task} to be sent in the next update message.
     *
     * @param task the newly assigned {@code Task}.
     */
    public synchronized void enqueueTaskCompletedLog(Task task) {
        logs.put(ServerEncoder.encodeTaskCompletedLog(task));
    }

    /**
     * Enqueues a log about a newly issued {@code Order} to be sent in the next update message.
     *
     * @param order the newly issued {@code Order}.
     */
    public synchronized void enqueueOrderIssuedLog(Order order) {
        logs.put(ServerEncoder.encodeOrderLog(ServerConstants.TYPE_ORDER_ISSUED, order));
    }

    /**
     * Enqueues a log about a newly fulfilled {@code Order} to be sent in the next update message.
     *
     * @param order the newly issued {@code Order}.
     */
    public synchronized void enqueueOrderFulfilledLog(Order order) {
        logs.put(ServerEncoder.encodeOrderLog(ServerConstants.TYPE_ORDER_FULFILLED, order));
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
    public synchronized void sendUpdateMsg() throws Exception {
        send(ServerEncoder.encodeUpdateMsg(warehouse.getTime(), actions, logs, statistics));
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    @WebSocket
    public class WebSocketHandler {

        @OnWebSocketConnect
        public void onConnect(Session client) throws Exception {
            openSession(client);
            System.out.println("Frontend connected!");
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            closeSession(client);
            System.out.println("Frontend connection closed with status code: " + statusCode);
        }

        @OnWebSocketMessage
        public void onMessage(Session client, String message) throws Exception {
            process(message);
        }
    }
}
