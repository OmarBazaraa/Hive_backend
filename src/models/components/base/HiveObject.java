package models.components.base;


/**
 * This {@code HiveObject} class is the base class of all the basic Hive system components.
 */
public class HiveObject implements Comparable<HiveObject> {

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
     * Indicates whether some other object is equal to this one.
     *
     * @param obj the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        // Not the same object type
        if (!(obj instanceof HiveObject)) {
            return false;
        }
        // Cast, then compare coordinates
        HiveObject rhs = (HiveObject) obj;
        return (id == rhs.id);
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

    /**
     * Compares whether some other object is less than, equal to, or greater than this one.
     *
     * @param rhs the reference object with which to compare.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(HiveObject rhs) {
        return id - rhs.id;
    }
}
