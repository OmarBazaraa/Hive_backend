package models;

import utils.Position;

public class Rack {

    //
    // Member Variables
    //

    private int mId;
    private int mRow, mCol;
    private int mItemId;
    private int mItemsCount;

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Rack(int id, int row, int col, int itemId, int itemCount) {
        this.mId = id;
        this.mRow = row;
        this.mCol = col;
        this.mItemId = itemId;
        this.mItemsCount = itemCount;
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
