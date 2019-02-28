package models.components.base;


/**
 * This {@code SrcHiveObject} class is the base class of all the source terminal Hive System's components
 * such as {@code Agent}, ..etc.
 * <p>
 * Source Hive object's are by default dynamic object that are capable of moving around in the Hive's map.
 */
public class SrcHiveObject extends TerminalHiveObject {

    /**
     * Constructs a new source Hive object.
     *
     * @param id  the id of the Hive object.
     * @param row the row position of the Hive object.
     * @param id  the column position of the Hive object.
     */
    public SrcHiveObject(int id, int row, int col) {
        super(id, row, col);
    }
}
