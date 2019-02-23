package models;

import utils.Constants;
import utils.Constants.*;
import utils.Position;

import java.util.Scanner;

public class Agent implements Comparable<Agent> {

    //
    // Member Variables
    //

    private int id;
    private int priority;
    private int row, col;
    private AgentStatus status;
    private Task task;
    private Path path;

    // Skip for now
    private int capacity;
    private int chargeMaxCap;
    private int chargeLevel;

    // ===============================================================================================
    //
    // Static Functions
    //

    public static Agent create(Scanner reader) {
        Agent ret = new Agent();
        ret.setup(reader);
        return ret;
    }

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Agent() {

    }

    public Agent(int id, int row, int col) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.priority = id; // TODO to be set dynamically
        this.status = AgentStatus.READY;

        // Skip for now
        this.capacity = Constants.AGENT_DEFAULT_CAPACITY;
        this.chargeMaxCap = Constants.AGENT_DEFAULT_CHARGE_CAPACITY;
        this.chargeLevel = 0;
    }

    public void setup(Scanner reader) {
        id = reader.nextInt();
        row = reader.nextInt();
        col = reader.nextInt();
    }

    public void assignTask(Task task) {
        this.task = task;
        this.status = AgentStatus.ASSIGNED_TASK;
    }

    public void assignPath(Path path) {
        this.path = path;
    }

    public void move(Direction dir) {

    }

    // ===============================================================================================
    //
    // Public Getters & Setters
    //

    public int getId() {
        return this.id;
    }

    public Position getPosition() {
        return new Position(this.row, this.col);
    }

    public Position getTargetPosition() {
        return (task != null ? task.rack.getPosition() : null);
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public AgentStatus getStatus() {
        return this.status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }


    @Override
    public int compareTo(Agent rhs) {
        int cmp = (priority - rhs.priority);
        if (cmp == 0) {
            return id - rhs.id;
        }
        return cmp;
    }
}
