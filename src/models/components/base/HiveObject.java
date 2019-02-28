package models.components.base;


/**
 * This {@code HiveObject} class is the base class of all the basic Hive system components.
 */
public class HiveObject {

    //
    // Member Variables
    //

    /**
     * The id of this Hive object.
     */
    protected int id;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new Hive object.
     *
     * @param id the id of the Hive object.
     */
    public HiveObject(int id) {
        this.id = id;
    }

    /**
     * Returns the id of this Hive object.
     *
     * @return an integer unique id of this Hive object.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Computes a hash code for this object.
     * This method is supported for the benefit of hash tables such as those provided by
     * {@link java.util.HashMap}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return this.id;
    }
}
