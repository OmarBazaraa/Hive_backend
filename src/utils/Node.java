package utils;

import utils.Constants.*;

public class Node implements Comparable<Node> {

    Position pos;
    Direction dir;

    public Node(Position pos, Direction dir) {
        this.pos = pos;
        this.dir = dir;
    }

    @Override
    public int compareTo(Node o) {
        return 0;
    }
}
