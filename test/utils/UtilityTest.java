package utils;

import utils.Constants;
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
        int dir;

        dir = Utility.rotateLeft(Constants.DIR_DOWN);
        Assert.assertEquals(dir, Constants.DIR_RIGHT);

        dir = Utility.rotateRight(Constants.DIR_RIGHT);
        Assert.assertEquals(dir, Constants.DIR_DOWN);

        dir = Utility.rotateRight(Constants.DIR_RIGHT);
        Assert.assertEquals(dir, Constants.DIR_DOWN);

        dir = Utility.rotateLeft(Constants.DIR_DOWN);
        Assert.assertEquals(dir, Constants.DIR_RIGHT);
    }
}
