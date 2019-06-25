package communicators.hardware;

import communicators.CommunicationListener;

import models.agents.Agent;

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
     * The map of the last action done by the agents.
     * This is used to wait until all agents send ACK back on their last actions.
     */
    private ConcurrentHashMap<Agent, AgentAction> agentLastAction = new ConcurrentHashMap<>();

    /**
     * The communication listener.
     */
    private CommunicationListener listener;

    /**
     * The logging file to write any logs from the hardware robots.
     */
    private FileWriter logger;

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
        server.webSocketIdleTimeoutMillis(HardwareConstants.TIMEOUT_INTERVAL);
        server.webSocket("/", new WebSocketHandler());

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
        server.stop();

        agentToSessionMap.clear();
        ipToAgentMap.clear();
        agentLastAction.clear();
    }

    /**
     * Registers a new {@code Agent} to listen for in this {@code HardwareCommunicator} object.
     *
     * @param agent the {@code Agent} to register
     */
    public void registerAgent(Agent agent) {
        ipToAgentMap.put(agent.getIpAddress(), agent);
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
                listener.onAgentDeactivated(agent);
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
        listener.onAgentActivated(agent);
    }

    /**
     * Closes the {@code Session} with a hardware robot.
     *
     * @param agent the disconnected {@code Agent}.
     * @param sess  the {@code Session} to close.
     */
    private void closeSession(Agent agent, Session sess) {
        agentToSessionMap.remove(agent);
        agentLastAction.remove(agent);
        listener.onAgentDeactivated(agent);
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
            listener.onAgentDeactivated(agent);
            System.err.println(ex.getMessage());
        }
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
                    processAckMsg(agent);
                    break;
                case HardwareConstants.TYPE_BATTERY:
                    int level = msg[1];
                    processBatteryMsg(agent, level);
                    break;
                case HardwareConstants.TYPE_BLOCKED:
                    boolean blocked = (msg[1] == 1);
                    processControlMsg(agent, blocked);
                    break;
            }
        } catch (Exception ex) {
            System.err.println("HardwareCommunicator :: Invalid message format from agent-" + agent.getId() + ": " + bytesToStr(msg));
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Processes the incoming battery update message from the given {@code Agent}.
     *
     * @param agent the {@code Agent} sending this message.
     * @param level the new battery level.
     */
    private void processBatteryMsg(Agent agent, int level) {
        // DEBUG
        System.out.println("HardwareCommunicator :: Received BLOCKED from agent-" + agent.getId() + ".");
        System.out.println();

        listener.onAgentBatteryLevelChanged(agent, level);
    }

    /**
     * Processes the incoming control message from the given {@code Agent}.
     *
     * @param agent   the {@code Agent} sending this message.
     * @param blocked {@code true} if this {@code Agent} get blocked; {@code false} if get unblocked.
     */
    private void processControlMsg(Agent agent, boolean blocked) {
        if (blocked) {
            // DEBUG
            System.out.println("HardwareCommunicator :: Received BLOCKED from agent-" + agent.getId() + ".");
            System.out.println();

            listener.onAgentDeactivated(agent);
        } else {
            // DEBUG
            System.out.println("HardwareCommunicator :: Received UNBLOCKED from agent-" + agent.getId() + ".");
            System.out.println();

            listener.onAgentActivated(agent);
        }
    }

    /**
     * Processes the incoming ACK message from the given {@code Agent}.
     *
     * @param agent the {@code Agent} sending this message.
     */
    private void processAckMsg(Agent agent) {
        agentLastAction.remove(agent);

        // DEBUG
        System.out.println("HardwareCommunicator :: Received action DONE from agent-" + agent.getId() + ".");
        System.out.println();
    }

    /**
     * Checks whether the last time step has been completed by all the agents or not.
     *
     * @return {@code true} if completed; {@code false} otherwise.
     */
    public boolean isLastStepCompleted() {
        return agentLastAction.isEmpty();
    }

    // ===============================================================================================
    //
    // Backend -> Hardware
    //

    /**
     * Pauses all the connected agents.
     */
    public void pause() {
        for (var pair : agentLastAction.entrySet()) {
            Agent agent = pair.getKey();
            AgentAction action = pair.getValue();

            sendStop(agent);
        }
    }

    /**
     * Resumes the last action by all the paused agents.
     */
    public void resume() {
        for (var pair : agentLastAction.entrySet()) {
            Agent agent = pair.getKey();
            AgentAction action = pair.getValue();

            sendAgentAction(agent, action);
        }
    }

    /**
     * Sends a new instruction action to the given {@code Agent}.
     *
     * @param agent  the {@code Agent} to send the instruction to.
     * @param action the instruction to send.
     */
    public void sendAgentAction(Agent agent, AgentAction action) {
        byte[] msg = {HardwareConstants.TYPE_ACTION, 0};

        switch (action) {
            case MOVE:
                msg[1] = HardwareConstants.TYPE_MOVE;
                break;
            case ROTATE_RIGHT:
                msg[1] = HardwareConstants.TYPE_ROTATE_RIGHT;
                break;
            case ROTATE_LEFT:
                msg[1] = HardwareConstants.TYPE_ROTATE_LEFT;
                break;
            case RETREAT:
                msg[1] = HardwareConstants.TYPE_RETREAT;
                break;
            case LOAD:
                msg[1] = HardwareConstants.TYPE_LOAD;
                break;
            case OFFLOAD:
                msg[1] = HardwareConstants.TYPE_OFFLOAD;
                break;
            default:
                return;
        }

        send(agent, msg);
        agentLastAction.put(agent, action);
    }

    /**
     * Sends an immediate stop instruction to the given {@code Agent}.
     *
     * @param agent the {@code Agent} to send the instruction to.
     */
    public void sendStop(Agent agent) {
        byte[] msg = {HardwareConstants.TYPE_ACTION, HardwareConstants.TYPE_STOP};
        send(agent, msg);
        agentLastAction.remove(agent);
    }

    // ===============================================================================================
    //
    // Helper Methods
    //

    /**
     * Converts the given {@code byte} array into a string.
     *
     * @param buf the array of bytes to convert.
     *
     * @return the corresponding string.
     */
    private String bytesToStr(byte[] buf) {
        StringBuilder builder = new StringBuilder();

        builder.append("\"");

        for (int i = 0; i < buf.length; ++i) {
            builder.append(buf[i] + '0');
        }

        builder.append("\"");

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
                openSession(agent, client);

                // DEBUG
                System.out.println();
                System.out.println("HardwareCommunicator :: Agent-" + agent.getId() + " connected!");
                System.out.println();
            } else {
                // DEBUG
                System.out.println();
                System.out.println("HardwareCommunicator :: Unknown session connected!");
                System.out.println();
            }
        }

        @OnWebSocketClose
        public void onClose(Session client, int statusCode, String reason) {
            InetAddress addr = client.getRemoteAddress().getAddress();
            Agent agent = ipToAgentMap.get(addr);

            if (agent != null) {
                closeSession(agent, client);

                // DEBUG
                System.out.println();
                System.out.println("HardwareCommunicator :: Agent-" + agent.getId() + " connection closed with status code: " + statusCode + ", reason: " + reason);
                System.out.println();
            } else {
                // DEBUG
                System.out.println();
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

            byte[] msg = new byte[length];

            for (int i = 0; i < length; ++i) {
                msg[i] = buffer[offset + i];
            }

            if (agent != null) {
                process(agent, msg);
            } else {
                // DEBUG
                System.out.println();
                System.out.println("HardwareCommunicator :: Received message from unknown source!");
                System.out.println();
            }
        }
    }
}
