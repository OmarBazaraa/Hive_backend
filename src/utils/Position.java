package utils;

public class Position implements Comparable<Integer> {

    public int r, c;

    public Position(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            Position rhs = (Position) obj;
            return (r == rhs.r && c == rhs.c);
        }
        return false;
    }

    @Override
    public int compareTo(Integer o) {
        return 0;
    }
}
