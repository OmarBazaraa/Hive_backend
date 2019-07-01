package communicators.hardware;

import communicators.CommunicationListener;

import models.agents.Agent;
import models.warehouses.Warehouse;

import utils.Constants.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import spark.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;


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
     * The map of currently connected agents.
     * This maps an {@code Agent} to its corresponding {@code Session}.
     */
    private ConcurrentHashMap<Agent, Session> agentToSessionMap = new ConcurrentHashMap<>();

    /**
     * The map of all registered agents to connect with.
     * This maps an IP address to its corresponding {@code Agent}.
     */
    private ConcurrentHashMap<InetAddress, Agent> ipToAgentMap = new ConcurrentHashMap<>();

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
    private ConcurrentHashMap<Agent, AgentAction> pendingActionMap = new ConcurrentHashMap<>();

    /**
     * The map of received DONE messages.
     */
    private ConcurrentHashMap<Agent, AgentAction> receivedDoneMap = new ConcurrentHashMap<>();

    /**
     * The logging file to write any logs from the hardware robots.
     */
    private FileWriter logger;

    //
    // TODO: just for debugging, to be removed
    //
    private ConcurrentHashMap<Integer, Agent> idToAgentMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Session, Agent> sessionToAgentMap = new ConcurrentHashMap<>();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code HardwareCommunicator} object.
     *
     * @param port the port number to listen on.
     */
    public HardwareCommunicator(int port, CommunicationListener l) {
        // Protected constructor to ensure a singleton object.
        server = Service.ignite();
        server.port(port);
        server.webSocketIdleTimeoutMillis(HardwareConstants.TIMEOUT_INTERVAL);
        server.webSocket("/", new WebSocketDebuggingHandler());     // TODO: to be removed

        // Set the communication listener
        listener = l;

        // Open the logger file
        try {
            logger = new FileWriter(HardwareConstants.LOG_FILE);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
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
        agentToSessionMap.clear();
        ipToAgentMap.clear();
        pendingActionMap.clear();
        receivedDoneMap.clear();

        // TODO: to be removed
        idToAgentMap.clear();
        sessionToAgentMap.clear();

        server.stop();
    }

    /**
     * Registers a new {@code Agent} to listen for in this {@code HardwareCommunicator} object.
     *
     * @param agent the {@code Agent} to register
     */
    public void registerAgent(Agent agent) {
        ipToAgentMap.put(agent.getIpAddress(), agent);
        idToAgentMap.put(agent.getId(), agent);         // TODO: to be removed
    }

    /**
     * Configures the connection with all the registered agents.
     */
    public void configure() {
        // Sleep for some duration
        try {
            Thread.sleep(HardwareConstants.CONFIG_INTERVAL);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }

        // Check for un-connected agents
        for (Agent agent : ipToAgentMap.values()) {
            if (!agentToSessionMap.containsKey(agent)) {
                listener.onAgentDeactivate(agent);
            }
        }
    }

    /**
     * Opens a {@code Session} with a hardware robot.
     *
     * @param agent the connected {@code Agent}.
     * @param sess  the {@code Session} to open.
     */
    private void openSession(Agent agent, Session sess) {
        agentToSessionMap.put(agent, sess);
        listener.onAgentActivate(agent);
    }

    /**
     * Closes the {@code Session} with a hardware robot.
     *
     * @param agent the disconnected {@code Agent}.
     * @param sess  the {@code Session} to close.
     */
    private void closeSession(Agent agent, Session sess) {
        agentToSessionMap.remove(agent);
        listener.onAgentDeactivate(agent);
    }

    /**
     * Sends the give bytes array message to the given {@code Agent}.
     *
     * @param agent the {@code Agent} to send the message to.
     * @param msg   the message to sent.
     */
    private void send(Agent agent, byte[] msg) {
        Session session = agentToSessionMap.get(agent);

        if (session == null) {
            return;
        }

        try {
            session.getRemote().sendBytes(ByteBuffer.wrap(msg));

            // DEBUG
            System.out.println("HardwareCommunicator :: Sending to agent-" + agent.getId() + ": " + bytesToStr(msg) + " ...");
            System.out.println();
        } catch (IOException ex) {
            listener.onAgentDeactivate(agent);
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
    // Hardware -> Backend
    //

    /**
     * Processes the incoming messages from the hardware.
     * <p>
     * This function is to be called from communicator threads not the main thread.
     *
     * @param agent the {@code Agent} sending this message.
     * @param msg   the raw message as received from this {@code Agent}.
     */
    private void process(Agent agent, byte[] msg) {
        try {
            int type = msg[0];

            switch (type) {
                case HardwareConstants.TYPE_DONE:
                    handleDoneMsg(agent);
                    break;
                case HardwareConstants.TYPE_BATTERY:
                    int level = msg[1];
                    handleBatteryMsg(agent, level);
                    break;
                case HardwareConstants.TYPE_BLOCKED:
                    boolean blocked = (msg[1] == 1);
                    handleControlMsg(agent, blocked);
                    break;
            }
        } catch (Exception ex) {
            System.err.println("HardwareCommunicator :: Invalid message format from agent-" + agent.getId() + ": " + bytesToStr(msg));
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Handles the incoming battery update message from the given {@code Agent}.
     *
     * @param agent the {@code Agent} sending this message.
     * @param level the new battery level.
     */
    private void handleBatteryMsg(Agent agent, int level) {
        // DEBUG
        System.out.println("HardwareCommunicator :: Agent-" + agent.getId() + " battery level updated to level: " + level + ".");
        System.out.println();

        listener.onAgentBatteryLevelChange(agent, level);
    }

    /**
     * Handles the incoming control message from the given {@code Agent}.
     *
     *
     * @param agent   the {@code Agent} sending this message.
     * @param blocked {@code true} if this {@code Agent} get blocked; {@code false} if get unblocked.
     */
    private void handleControlMsg(Agent agent, boolean blocked) {
        if (blocked) {
            // DEBUG
            System.out.println("HardwareCommunicator :: Received BLOCKED from agent-" + agent.getId() + ".");
            System.out.println();

            listener.onAgentDeactivate(agent);
        } else {
            // DEBUG
            System.out.println("HardwareCommunicator :: Received UNBLOCKED from agent-" + agent.getId() + ".");
            System.out.println();

            listener.onAgentActivate(agent);
        }
    }

    /**
     * Handles the incoming DONE message from the given {@code Agent}.
     * <p>
     * Called at any time from Spark threads.
     *
     * @param agent the {@code Agent} sending this message.
     */
    private void handleDoneMsg(Agent agent) {
        receivedDoneMap.put(agent, AgentAction.NOTHING);
        pendingActionMap.remove(agent);

        if (pendingActionMap.isEmpty()) {
            listener.onActionsDone();
        }

        // DEBUG
        System.out.println("HardwareCommunicator :: Received action DONE from agent-" + agent.getId() + ".");
        System.out.println();
    }

    // ===============================================================================================
    //
    // Backend -> Hardware
    //

    /**
     * Pauses all the connected agents.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from Spark threads.
     */
    public void pause() {
        for (var pair : pendingActionMap.entrySet()) {
            Agent agent = pair.getKey();
            send(agent, new byte[]{HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_STOP});
        }
    }

    /**
     * Resumes the last action by all the paused agents.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from Spark threads.
     */
    public void resume() {
        for (var pair : pendingActionMap.entrySet()) {
            Agent agent = pair.getKey();
            AgentAction action = pair.getValue();

            if (receivedDoneMap.containsKey(agent)) {
                continue;
            }

            send(agent, encodeAgentAction(action));
        }
    }

    /**
     * Sends an immediate stop action to the given {@code Agent}.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from Spark threads.
     *
     * @param agent the {@code Agent} to send the action to.
     */
    public void sendAgentStop(Agent agent) {
        pendingActionMap.remove(agent);
        byte[] msg = {HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_STOP};
        send(agent, msg);
    }

    /**
     * Sends an action to the given {@code Agent}.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from the main thread.
     *
     * @param agent  the {@code Agent} to send the action to.
     * @param action the action to send.
     */
    public void sendAgentAction(Agent agent, AgentAction action) {
        byte[] msg = encodeAgentAction(action);

        if (msg == null) {
            return;
        }

        pendingActionMap.put(agent, action);
        receivedDoneMap.remove(agent);
        send(agent, msg);
    }

    /**
     * Sends a recover action to the given {@code Agent}.
     * <p>
     * Called with exclusive access to the {@code Warehouse} object from the main thread.
     *
     * @param agent  the {@code Agent} to send the action to.
     * @param action the action to send.
     */
    public void sendAgentRecoverAction(Agent agent, AgentAction action) {
        if (receivedDoneMap.containsKey(agent)) {
            return;
        }

        byte[] msg = encodeAgentAction(action);

        if (msg == null) {
            return;
        }

        pendingActionMap.put(agent, action);
        send(agent, msg);
    }

    /**
     * Sends a light instruction to the given {@code Agent}.
     *
     * @param agent the {@code Agent} to send the instruction to.
     * @param light the light bulb to control; RED or BLUE.
     * @param mode  the mode of the light; OFF, ON, or FLASHING.
     */
    public void sendAgentLightCommand(Agent agent, int light, int mode) {
        byte[] msg = {HardwareConstants.TYPE_LIGHTS, (byte) light, (byte) mode};
        send(agent, msg);
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Encodes the given {@code AgentAction} to be sent to the hardware robots.
     *
     * @param action the {@code AgentAction} to encode.
     *
     * @return the encoded {@code AgentAction}
     */
    private byte[] encodeAgentAction(AgentAction action) {
        switch (action) {
            case MOVE:
                return new byte[]{HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_MOVE};
            case ROTATE_RIGHT:
                return new byte[]{HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_ROTATE_RIGHT};
            case ROTATE_LEFT:
                return new byte[]{HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_ROTATE_LEFT};
            case RETREAT:
                return new byte[]{HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_RETREAT};
            case LOAD:
                return new byte[]{HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_LOAD};
            case OFFLOAD:
                return new byte[]{HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_OFFLOAD};
        }

        return null;
    }

    /**
     * Converts the given {@code byte} array into a string.
     *
     * @param buf the array of bytes to convert.
     *
     * @return the corresponding string.
     */
    private String bytesToStr(byte[] buf) {
        StringBuilder builder = new StringBuilder();

        builder.append("[");

        for (int i = 0; i < buf.length; ++i) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(buf[i]);
        }

        builder.append("]");

        return builder.toString();
    }

    // ===============================================================================================
    //
    // Sub-classes & Interface
    //

    @WebSocket
    public class WebSocketHandler {

        @OnWebSocketConnect
        public void onConnect(Session client) {
            InetAddress addr = client.getRemoteAddress().getAddress();
            Agent agent = ipToAgentMap.get(addr);

            if (agent != null) {
                // DEBUG
                System.out.println("HardwareCommunicator :: Agent-" + agent.getId() + " connected!");
                System.out.println();

                openSession(agent, client);
            } else {
                // DEBUG
                System.out.println("HardwareCommunicator :: Unknown session connected!");
                System.out.println();
            }
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            InetAddress addr = client.getRemoteAddress().getAddress();
            Agent agent = ipToAgentMap.get(addr);

            if (agent != null) {
                // DEBUG
                System.out.println("HardwareCommunicator :: Agent-" + agent.getId() + " connection closed with status code: " + statusCode + ", reason: " + reason);
                System.out.println();

                closeSession(agent, client);
            } else {
                // DEBUG
                System.out.println("HardwareCommunicator :: Unknown session closed!");
                System.out.println();
            }
        }

        @OnWebSocketError
        public void onError(Session client, Throwable error) {
            onClose(client, -1, error.getMessage());
        }

        @OnWebSocketMessage
        public void onMessage(Session client, String msg) {
            InetAddress addr = client.getRemoteAddress().getAddress();
            Agent agent = ipToAgentMap.get(addr);

            if (agent != null) {
                try {
                    logger.write("Agent-" + agent.getId() + ": " + msg + "\n");
                    logger.flush();
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }

        @OnWebSocketMessage
        public void onMessage(Session client, byte[] buffer, int offset, int length) {
            InetAddress addr = client.getRemoteAddress().getAddress();
            Agent agent = ipToAgentMap.get(addr);

            if (agent == null) {
                // DEBUG
                System.out.println("HardwareCommunicator :: Received message from unknown source!");
                System.out.println();
                return;
            }

            byte[] msg = new byte[length];

            for (int i = 0; i < length; ++i) {
                msg[i] = buffer[offset + i];
            }

            System.out.println(">> from Agent-" + agent.getId() + ": " + bytesToStr(msg));  // TODO: to be removed
            System.out.flush();

            process(agent, msg);
        }
    }

    //
    // TODO: just for debugging, to be removed
    //
    @WebSocket
    public class WebSocketDebuggingHandler {

        public void onConnect(Session client, int id) {
            Agent agent = idToAgentMap.get(id);

            if (agent != null) {
                // DEBUG
                System.out.println("HardwareCommunicator :: Agent-" + agent.getId() + " connected!");
                System.out.println();

                sessionToAgentMap.put(client, agent);
                openSession(agent, client);
            } else {
                // DEBUG
                System.out.println("HardwareCommunicator :: Unknown session connected!");
                System.out.println();
            }
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            Agent agent = sessionToAgentMap.get(client);

            if (agent != null) {
                // DEBUG
                System.out.println("HardwareCommunicator :: Agent-" + agent.getId() + " connection closed with status code: " + statusCode + ", reason: " + reason);
                System.out.println();

                sessionToAgentMap.remove(client);
                closeSession(agent, client);
            } else {
                // DEBUG
                System.out.println();
                System.out.println("HardwareCommunicator :: Unknown session closed!");
            }
        }

        @OnWebSocketError
        public void onError(Session client, Throwable error) {
            onClose(client, -1, error.getMessage());
        }

        @OnWebSocketMessage
        public void onMessage(Session client, byte[] buffer, int offset, int length) {
            if (buffer[offset] == 3) {
                onConnect(client, buffer[offset + 1]);
                return;
            }

            Agent agent = sessionToAgentMap.get(client);

            if (agent == null) {
                // DEBUG
                System.out.println("HardwareCommunicator :: Received message from unknown source!");
                System.out.println();
                return;
            }

            byte[] msg = new byte[length];

            for (int i = 0; i < length; ++i) {
                msg[i] = buffer[offset + i];
            }

            System.out.println(">> from Agent-" + agent.getId() + ": " + bytesToStr(msg));  // TODO: to be removed
            System.out.flush();

            process(agent, msg);
        }
    }
}
