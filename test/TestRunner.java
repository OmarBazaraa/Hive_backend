import algorithms.AlgorithmsTestRunner;
import models.ModelsTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import server.ServerTest;
import utils.UtilityTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({AlgorithmsTestRunner.class, ModelsTestRunner.class, UtilityTest.class, ServerTest.class})
public class TestRunner {
    @BeforeClass
    public static void before() {

    }

    @AfterClass
    public static void after() {

    }
}