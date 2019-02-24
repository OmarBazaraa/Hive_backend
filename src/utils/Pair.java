package utils;


/**
 * This {@code Pair} class holds a pair of objects as a unit.
 */
public class Pair<T extends Comparable<T>, U extends Comparable<U>> implements Comparable<Pair<T, U>> {

    /**
     * The first object in the pair.
     */
    public T x;

    /**
     * The second object in the pair.
     */
    public U y;

    /**
     * Constructs a {@code Position} object with the given coordinates.
     *
     * @param x the first object.
     * @param y the second object.
     */
    public Pair(T x, U y) {
        this.x = x;
        this.y = y;
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
        int hash = 17;
        hash = hash * 31 + x.hashCode();
        hash = hash * 31 + y.hashCode();
        return hash;
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
        if (!(obj instanceof Pair)) {
            return false;
        }
        // Cast, then compare individual objects
        Pair rhs = (Pair) obj;
        return (x == rhs.x && y == rhs.y);
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
    public int compareTo(Pair<T, U> rhs) {
        int cmp = x.compareTo(rhs.x);
        if (cmp == 0) {
            return y.compareTo(rhs.y);
        }
        return cmp;
    }
}
