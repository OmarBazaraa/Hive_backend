package algorithms;

import algorithms.dispatcher.DispatcherTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import algorithms.planner.PlannerTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({PlannerTest.class, DispatcherTest.class})
public class AlgorithmsTestRunner {
    @BeforeClass
    public static void before() {

    }

    @AfterClass
    public static void after() {

    }
}