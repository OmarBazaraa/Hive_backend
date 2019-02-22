package models;

import utils.Position;

import java.util.Scanner;

public class Rack {

    //
    // Member Variables
    //

    private int mId;
    private int mRow, mCol;
    private int mItemId;
    private int mItemCount;

    // ===============================================================================================
    //
    // Static Functions
    //

    public static Rack create(Scanner reader) {
        Rack ret = new Rack();
        ret.setup(reader);
        return ret;
    }

    // ===============================================================================================
    //
    // Public Member Functions
    //

    public Rack() {

    }

    public Rack(int id, int row, int col, int itemId, int itemCount) {
        this.mId = id;
        this.mRow = row;
        this.mCol = col;
        this.mItemId = itemId;
        this.mItemCount = itemCount;
    }

    public void setup(Scanner reader) {
        mId = reader.nextInt();
        mRow = reader.nextInt();
        mCol = reader.nextInt();
        mItemId = reader.nextInt();
        mItemCount = reader.nextInt();
    }

    // ===============================================================================================
    //
    // Public Getters & Setters
    //

    public int getId() {
        return this.mId;
    }

    public Position getPosition() {
        return new Position(this.mRow, this.mCol);
    }

    public int getItemId() {
        return this.mItemId;
    }

    public int getItemCount() {
        return this.mItemCount;
    }
}
