package utils;


public class Pair<T extends Comparable<T>, U extends Comparable<U>> implements Comparable<Pair<T, U>> {

    public T x;
    public U y;

    public Pair(T x, U y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair rhs = (Pair) obj;
            return (x == rhs.x && y == rhs.y);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + x.hashCode();
        hash = hash * 31 + y.hashCode();
        return hash;
    }

    @Override
    public int compareTo(Pair<T, U> rhs) {
        int cmp = x.compareTo(rhs.x);
        if (cmp == 0) {
            return y.compareTo(rhs.y);
        }
        return cmp;
    }
}
