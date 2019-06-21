import algorithms.AlgorithmsTestRunner;
import communicators.CommunicatorTest;
import models.ModelsTestRunner;
import utils.UtilityTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({AlgorithmsTestRunner.class, ModelsTestRunner.class, UtilityTest.class, CommunicatorTest.class})
public class TestRunner {
    @BeforeClass
    public static void before() {

    }

    @AfterClass
    public static void after() {

    }
}