package models;


/**
 * This {@code Entity} class is the base class of all entity objects.
 * <p>
 * An entity object is just an object with a unique id representing and defining it.
 */
public class Entity implements Comparable<Entity> {

    //
    // Member Variables
    //

    /**
     * The id of this {@code Entity}.
     */
    protected int id;

    // ===============================================================================================
    //
    // Member Methods
    //

    /**
     * Constructs a new {@code Entity} object.
     *
     * @param id the id of the {@code Entity}.
     */
    public Entity(int id) {
        this.id = id;
    }

    /**
     * Returns the id of this {@code Entity}.
     *
     * @return an integer unique id of this {@code Entity}.
     */
    public int getId() {
        return id;
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
        if (!(obj instanceof Entity)) {
            return false;
        }
        // Cast, then compare coordinates
        Entity rhs = (Entity) obj;
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
        return id;
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
    public int compareTo(Entity rhs) {
        return id - rhs.id;
    }
}
