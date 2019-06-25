package controller;

import communicators.CommunicationListener;
import communicators.frontend.FrontendCommunicator;
import communicators.frontend.FrontendConstants;
import communicators.hardware.HardwareCommunicator;

import models.agents.Agent;
import models.agents.AgentListener;
import models.items.Item;
import models.tasks.Task;
import models.tasks.orders.Order;
import models.tasks.orders.OrderListener;
import models.warehouses.Warehouse;

import utils.Constants;
import utils.Constants.*;

import java.util.Collection;
import java.util.Map;


public class Controller implements CommunicationListener, AgentListener, OrderListener {

    //
    // Member Variables
    //

    /**
     * The current state of this {@code Controller} object.
     */
    private ServerState currentState = ServerState.IDLE;

    /**
     * The running mode of this {@code Controller} object. Either simulation or deployment.
     */
    private RunningMode currentMode;

    /**
     * The {@code Warehouse} object.
     */
    private final Warehouse warehouse = Warehouse.getInstance();

    /**
     * The {@code FrontendCommunicator} object.
     */
    private FrontendCommunicator frontendComm;

    /**
     * The {@code HardwareCommunicator} object.
     */
    private HardwareCommunicator hardwareComm;

    /**
     * Object used to lock threads from modifying
     * the state/mode of this {@code Controller} simultaneously.
     */
    private final Object lock = new Object();

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Controller} object.
     */
    public Controller() {
        frontendComm = new FrontendCommunicator(Constants.FRONTEND_COMM_PORT, this);
        hardwareComm = new HardwareCommunicator(Constants.HARDWARE_COMM_PORT, this);
    }

    /**
     * Starts and initializes this {@code Controller} object.
     */
    public void start() {
        frontendComm.start();
        run();
    }

    /**
     * Keeps running this {@code Controller} object until an exit criterion is met.
     */
    private void run() {
        while (getState() != ServerState.EXIT) {

            // Must be in RUNNING state
            if (getState() != ServerState.RUNNING) {
                continue;
            }

            // Check if last time step has been completed
            if (!isLastStepCompleted()) {
                continue;
            }

            // Try running a single time step in the warehouse
            try {
                simulate();
            } catch (Exception ex) {
                setState(ServerState.IDLE);
                frontendComm.sendErr(FrontendConstants.ERR_SERVER, "Internal server error.");
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Simulates a single time step in the {@code Warehouse}.
     */
    private void simulate() {
        synchronized (warehouse) {
            if (warehouse.run()) {
                frontendComm.flushUpdateMsg();

                // DEBUG
                System.out.println(warehouse);
            }
        }
    }

    /**
     * Checks whether the last time step has been completed by
     * the frontend and the hardware.
     *
     * @return {@code true} if last time step has been completed; {@code false} otherwise.
     */
    private boolean isLastStepCompleted() {
        if (getMode() == RunningMode.DEPLOYMENT) {
            return frontendComm.isLastStepCompleted() && hardwareComm.isLastStepCompleted();
        } else {
            return frontendComm.isLastStepCompleted();
        }
    }

    // ===============================================================================================
    //
    // Communicator Listener Methods
    //

    /**
     * Returns the current state of this {@code Controller} object.
     *
     * @return the current {@code ServerState} of this {@code Controller}.
     */
    @Override
    public ServerState getState() {
        synchronized (lock) {
            return currentState;
        }
    }

    /**
     * Updates the current state of this {@code Controller} object.
     *
     * @param state the new {@code ServerState} to set.
     */
    private void setState(ServerState state) {
        synchronized (lock) {
            currentState = state;
        }
    }

    /**
     * Returns the current running mode of this {@code Controller} object.
     *
     * @return the current {@code RunningMode} of this {@code Controller}.
     */
    @Override
    public RunningMode getMode() {
        synchronized (lock) {
            return currentMode;
        }
    }

    /**
     * Updates the current running mode of this {@code Controller} object.
     *
     * @param mode the new {@code RunningMode} to set.
     */
    private void setMode(RunningMode mode) {
        synchronized (lock) {
            currentMode = mode;
        }
    }

    /**
     * Called when the frontend communicator receives a start message.
     *
     * @param mode the {@code RunningMode} of this new start.
     */
    @Override
    public void onStart(RunningMode mode) {
        setState(ServerState.RUNNING);
        setMode(mode);

        synchronized (warehouse) {
            Collection<Agent> agents = warehouse.getAgentList();

            // Register agents callback function
            for (Agent agent : agents) {
                agent.setListener(this);
            }

            // Initialize in case of deployment
            if (mode == RunningMode.DEPLOYMENT) {
                for (Agent agent : agents) {
                    hardwareComm.registerAgent(agent);
                }

                hardwareComm.start();
                hardwareComm.configure();
            }

            // DEBUG
            warehouse.print();
        }
    }

    /**
     * Called when the frontend communicator receives a stop message.
     */
    @Override
    public void onStop() {
        setState(ServerState.IDLE);

        if (getMode() == RunningMode.DEPLOYMENT) {
            hardwareComm.pause();
            hardwareComm.close();
        }
    }

    /**
     * Called when the frontend communicator receives a pause message.
     */
    @Override
    public void onPause() {
        setState(ServerState.PAUSE);

        if (getMode() == RunningMode.DEPLOYMENT) {
            hardwareComm.pause();
        }
    }

    /**
     * Called when the frontend communicator receives a resume message.
     */
    @Override
    public void onResume() {
        setState(ServerState.RUNNING);

        if (getMode() == RunningMode.DEPLOYMENT) {
            hardwareComm.resume();
        }
    }

    /**
     * Called when the frontend communicator receives a new {@code Order}.
     *
     * @param order the newly issued {@code order}.
     */
    @Override
    public void onOrderIssued(Order order) {
        synchronized (warehouse) {
            order.setListener(this);
            warehouse.addOrder(order);

            // DEBUG
            System.out.println("Order received ...");
            System.out.println(order);
            System.out.println();
        }
    }

    /**
     * Called when the frontend or the hardware communicators receives an {@code Agent} activation.
     *
     * @param agent the activated {@code Agent}.
     */
    @Override
    public void onAgentActivated(Agent agent) {
        synchronized (warehouse) {
            agent.activate();
            frontendComm.flushControlMsg();

            // DEBUG
            System.out.println("Activating " + agent + ".");
            System.out.println();
        }
    }

    /**
     * Called when the frontend or the hardware communicators receives an {@code Agent} deactivation.
     *
     * @param agent the deactivated {@code Agent}.
     */
    @Override
    public void onAgentDeactivated(Agent agent) {
        synchronized (warehouse) {
            agent.deactivate();
            frontendComm.flushControlMsg();

            // DEBUG
            System.out.println("Deactivating " + agent + ".");
            System.out.println();
        }
    }

    /**
     * Called when the hardware communicator receives a change in the battery level
     * of an {@code Agent}.
     *
     * @param agent the {@code Agent}.
     * @param level the new battery level of this {@code Agent}.
     */
    @Override
    public void onAgentBatteryLevelChanged(Agent agent, int level) {
        synchronized (warehouse) {
            agent.setBatteryLevel(level);
        }
    }

    // ===============================================================================================
    //
    // Agent Listener Methods
    //

    /**
     * Called when an {@code Agent} has performed an action.
     *
     * @param agent  the {@code Agent}.
     * @param action the action done by this {@code Agent}.
     */
    @Override
    public void onAction(Agent agent, AgentAction action) {
        frontendComm.enqueueAgentAction(agent, action);

        if (getMode() == RunningMode.DEPLOYMENT) {
            hardwareComm.sendAgentAction(agent, action);
        }
    }

    /**
     * Called when the battery level of an {@code Agent} has changed.
     *
     * @param agent the {@code Agent}.
     * @param level the new battery level of this {@code Agent}.
     */
    @Override
    public void onBatteryLevelChange(Agent agent, int level) {
        frontendComm.enqueueBatteryUpdatedLog(agent);
    }

    /**
     * Called when an {@code Agent} has been activated.
     *
     * @param agent the activated {@code Agent}.
     */
    @Override
    public void onActivate(Agent agent) {
        frontendComm.enqueueActivatedAgent(agent);
    }

    /**
     * Called when an {@code Agent} has been deactivated.
     *
     * @param agent the deactivated {@code Agent}.
     */
    @Override
    public void onDeactivate(Agent agent) {
        frontendComm.enqueueDeactivatedAgent(agent);
    }

    /**
     * Called when an {@code Agent} has been blocked.
     *
     * @param agent the blocked {@code Agent}.
     */
    @Override
    public void onBlock(Agent agent) {
        frontendComm.enqueueBlockedAgent(agent);

        if (getMode() == RunningMode.DEPLOYMENT) {
            hardwareComm.sendStop(agent);
        }
    }

    // ===============================================================================================
    //
    // Order Listener Methods
    //

    // All these functions are being called from the controller main thread (i.e. Main Thread)

    /**
     * Called when an {@code Order} has just been started.
     * That, is when it is assigned its first sub task.
     *
     * @param order the started {@code Order}.
     */
    @Override
    public void onStart(Order order) {

    }

    /**
     * Called when a {@code Task} has been assigned to an {@code Order}.
     *
     * @param order the {@code Order}.
     * @param task  the assigned {@code Task}.
     */
    @Override
    public void onTaskAssign(Order order, Task task) {
        frontendComm.enqueueTaskAssignedLog(task, order);
    }

    /**
     * Called when an assigned {@code Task} for an {@code Order} has been completed.
     *
     * @param order the {@code Order}.
     * @param task  the completed {@code Task}.
     * @param items the map of add/removed items by the completed {@code Task}.
     */
    @Override
    public void onTaskComplete(Order order, Task task, Map<Item, Integer> items) {
        frontendComm.enqueueTaskCompletedLog(task, order, items);
    }

    /**
     * Called when an {@code Order} has just been fulfilled.
     * That, is when its last assigned sub task has been completed.
     *
     * @param order the fulfilled {@code Order}.
     */
    @Override
    public void onFulfill(Order order) {
        frontendComm.enqueueOrderFulfilledLog(order);
    }

    /**
     * Called when an {@code Order} has been dismissed from the system.
     *
     * @param order the dismissed {@code Order}.
     */
    @Override
    public void onDismiss(Order order) {

    }
}
