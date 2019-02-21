package models;

import utils.Constants;
import utils.Position;

public class Agent {

    //
    // Constants & Enums
    //

    // Agent status
    enum Status {
        READY,
        ACTIVE,
        CHARGING,
        OUT_OF_SERVICE
    }

    // ===============================================================================================
    //
    // Member Variables
    //

    private int mId;
    private int mPriority;
    private int mRow, mCol;

    private int mCapacity;
    private int mChargeMaxCap;
    private int mChargeLevel;

    private Status mStatus;

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Agent(int id) {
        this.mId = id;

        this.mPriority = 0;
        this.mRow = -1;
        this.mCol = -1;

        this.mCapacity = Constants.AGENT_DEFAULT_CAPACITY;
        this.mChargeMaxCap = Constants.AGENT_DEFAULT_CHARGE_CAPACITY;
        this.mChargeLevel = 0;

        this.mStatus = Status.OUT_OF_SERVICE;
    }

    // ===============================================================================================
    //
    // Public Getters & Setters
    //

    public int getId() {
        return this.mId;
    }

    public int getPriority() {
        return this.mPriority;
    }

    public Position getPosition() {
        return new Position(this.mRow, this.mCol);
    }

    public int getCapacity() {
        return this.mCapacity;
    }

    public int getChargeLevel() {
        return this.mChargeLevel;
    }

    public Status getStatus() {
        return this.mStatus;
    }
}
