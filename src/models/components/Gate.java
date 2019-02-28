package models.components;

import models.components.base.DstHiveObject;


/**
 * This {@code Gate} class is a model for gate where delivery should occur at.
 * <p>
 * A gate is the location where robot agents deliver their assigned orders.
 */
public class Gate extends DstHiveObject {

    /**
     * Constructs a new gate of items.
     *
     * @param id  the id of the gate.
     * @param row the row position of the gate.
     * @param col the column position of the gate.
     */
    public Gate(int id, int row, int col) {
        super(id, row, col);
    }
}
