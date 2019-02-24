package utils;

import utils.Constants.*;
import utils.Position;

import java.util.List;
import java.util.Queue;

public class Path {

    public int curIdx;
    public Position pos;
    public List<Direction> mainPath;
    public List<List<Direction>> alternatePaths;

    public Path() {

    }

    public Path(List<Direction> mainPath, List<List<Direction>> alternatePaths) {
        this.mainPath = mainPath;
        this.alternatePaths = alternatePaths;
    }
}
