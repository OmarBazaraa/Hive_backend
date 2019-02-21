package models;

import utils.Position;

public class Rack {

    //
    // Member Variables
    //

    private int mRow, mCol;
    private int mItemId;
    private int mItemsCount;

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Rack(int r, int c) {
        this.mRow = r;
        this.mCol = c;
        this.mItemsCount = 0;
        this.mItemId = -1;
    }

    // ===============================================================================================
    //
    // Public Getters & Setters
    //

    public Position getPosition() {
        return new Position(this.mRow, this.mCol);
    }

    public int getItemId() {
        return this.mItemId;
    }

    public int getItemsCount() {
        return this.mItemsCount;
    }
}
