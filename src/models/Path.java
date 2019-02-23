package models;

import utils.Constants.*;
import utils.Position;

import java.util.List;
import java.util.Queue;

public class Path {

    int curIdx;
    Position pos;
    Queue<Direction> mainPath;
    List<Queue<Direction>> alternatePaths;

    public Path(Queue<Direction> mainPath, List<Queue<Direction>> alternatePaths) {
        this.mainPath = mainPath;
        this.alternatePaths = alternatePaths;
    }
}
