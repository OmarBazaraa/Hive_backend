package utils;

import utils.Constants.*;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class UtilityTest {

    /**
     * Initializes the required objects.
     */
    @BeforeClass
    public static void before() throws Exception {

    }

    @Test
    public void directionTest() {
        Direction dir;

        dir = Utility.nextDir(Direction.DOWN, AgentAction.ROTATE_LEFT);
        Assert.assertEquals(dir, Direction.RIGHT);

        dir = Utility.prevDir(Direction.RIGHT, AgentAction.ROTATE_LEFT);
        Assert.assertEquals(dir, Direction.DOWN);

        dir = Utility.nextDir(Direction.RIGHT, AgentAction.ROTATE_RIGHT);
        Assert.assertEquals(dir, Direction.DOWN);

        dir = Utility.prevDir(Direction.DOWN, AgentAction.ROTATE_RIGHT);
        Assert.assertEquals(dir, Direction.RIGHT);
    }
}
