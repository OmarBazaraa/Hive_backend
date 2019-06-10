package server;

import models.agents.Agent;
import models.warehouses.Warehouse;

import server.exceptions.DataException;

import utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import spark.Spark;

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
        PAUSE,
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
    private BlockingQueue<String> receivedQueue = new LinkedBlockingQueue<>();

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
     * Checks whether this {@code Server} object is currently connected with the
     * frontend.
     *
     * @return {@code true} if connected; {@code false} otherwise.
     */
    public boolean isConnected() {
        return (session != null && session.isOpen());
    }

    /**
     * Checks whether this {@code Server} is still running and did not process
     * an EXIT message.
     *
     * @return {@code true} if this {@code Server} did not process an EXIT message; {@code false} otherwise.
     */
    public boolean isRunning() {
        return (currentState != ServerStates.EXIT);
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
     * Sets the frontend {@code Session}.
     *
     * @param sess the frontend {@code Session} to set.
     */
    private synchronized void setSession(Session sess) {
        session = sess;
    }

    /**
     * Starts and initializes this {@code Server} object.
     */
    public void start() throws Exception {
        Spark.init();

        while (currentState != ServerStates.EXIT) {
            try {
                process();
            } catch (JSONException ex) {
                sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR, "Invalid message format.");
                currentState = ServerStates.IDLE;
            } catch (DataException ex) {
                sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR, ex.getMessage());
                currentState = ServerStates.IDLE;
            } catch (Exception ex) {
                sendAckMsg(ServerConstants.TYPE_MSG, ServerConstants.TYPE_ERROR, "Unknown error.");
                currentState = ServerStates.IDLE;
            }
        }
    }

    /**
     * Closes and terminates this {@code Server} object.
     */
    public void close() throws Exception {
        Spark.stop();
    }

    /**
     * Sends the give JSON message to the frontend.
     *
     * @param msg the message to sent.
     */
    private void send(JSONObject msg) throws Exception {
        session.getRemote().sendString(msg.toString());
    }

    // ===============================================================================================
    //
    // Frontend -> Backend
    //

    /**
     * Processes the first incoming message in the queue from the frontend.
     * <p>
     * If no incoming message yet, the thread gets blocked till a new message comes.
     */
    private void process() throws Exception {
        // Get the first message or wait if no one is available
        JSONObject msg = new JSONObject(receivedQueue.take());

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
            default:
                throw new DataException("Invalid message type.");
        }
    }

    /**
     * Processes the START message from the frontend.
     *
     * @param data the received JSON data part of the message.
     */
    private void processStartMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.IDLE) {
            throw new DataException("Received START message while the server is not in IDLE state.");
        }

        try {
            warehouse.clear();
            clearUpdateStates();
            ServerDecoder.decodeInitConfig(data);
            warehouse.init();
            warehouse.run();
            currentState = ServerStates.RUNNING;
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_OK, "");
        } catch (JSONException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR, "Invalid START message format.");
        } catch (DataException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR, ex.getMessage());
        }
    }

    /**
     * Processes the STOP message from the frontend.
     *
     * @param data the received JSON data part of the message.
     */
    private void processStopMsg(JSONObject data) throws Exception {
        if (currentState == ServerStates.IDLE) {
            throw new DataException("Received STOP message while the server is in IDLE state.");
        }

        currentState = ServerStates.IDLE;
    }

    /**
     * Processes the RESUME message from the frontend.
     *
     * @param data the received JSON data part of the message.
     */
    private void processResumeMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.PAUSE) {
            throw new DataException("Received RESUME message while the server is not in PAUSE state.");
        }

        currentState = ServerStates.RUNNING;
        sendAckMsg(ServerConstants.TYPE_ACK_RESUME, ServerConstants.TYPE_OK, "");
    }

    /**
     * Processes the PAUSE message from the frontend.
     *
     * @param data the received JSON data part of the message.
     */
    private void processPauseMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.RUNNING) {
            throw new DataException("Received PAUSE message while the server is not in RUNNING state.");
        }

        currentState = ServerStates.PAUSE;
    }

    /**
     * Processes the EXIT message from the frontend.
     *
     * @param data the received JSON data part of the message.
     */
    private void processExistMsg(JSONObject data) throws Exception {
        currentState = ServerStates.EXIT;
    }

    /**
     * Processes the ACK message from the frontend.
     *
     * @param data the received JSON data part of the message.
     */
    private void processAckMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.RUNNING) {
            throw new DataException("Received ACK message while the server is not in RUNNING state.");
        }

        warehouse.run();
    }

    /**
     * Processes the ORDER message from the frontend.
     *
     * @param data the received JSON data part of the message.
     */
    private void processOrderMsg(JSONObject data) throws Exception {
        if (currentState != ServerStates.RUNNING) {
            throw new DataException("Received ORDER message while the server is not in RUNNING state.");
        }

        try {
            ServerDecoder.decodeOrder(data);
            sendAckMsg(ServerConstants.TYPE_ACK_ORDER, ServerConstants.TYPE_OK, "");
        } catch (JSONException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_START, ServerConstants.TYPE_ERROR, "Invalid ORDER message format.");
        } catch (DataException ex) {
            sendAckMsg(ServerConstants.TYPE_ACK_ORDER, ServerConstants.TYPE_ERROR, ex.getMessage());
        }
    }

    // ===============================================================================================
    //
    // Backend -> Frontend
    //

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
            receivedQueue.add(message);
        }
    }
}
