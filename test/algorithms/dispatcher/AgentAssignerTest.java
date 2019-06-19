package algorithms.dispatcher;

import algorithms.dispatcher.task_allocator_helpers.AgentAssigner;
import models.HiveObject;
import models.agents.Agent;
import models.facilities.Rack;
import models.maps.utils.Position;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class AgentAssignerTest {
    @BeforeClass
    public static void before() {

    }

    @AfterClass
    public static void after() {

    }

    @Test
    public void agentAssignerTest() throws Exception {
        /*
         * R: rack, R*: selected rack, A: candidate agent
         * ---------------------
         * | G| .| .|R*| R| .| A|
         * |--------------------|
         * | .| .| .| R| R| .| .|
         * |--------------------|
         * | A| .| .| R| R| .| .|
         * |--------------------|
         * | .| .| .| .| .| .|R*|
         * ----------------------
         */
        List<Rack> selectedRackList = new ArrayList<>();

        Rack r1 = new Rack();
        Rack r2 = new Rack();
        r1.setPosition(0, 3);
        r2.setPosition(3, 6);
        selectedRackList.add(r1);
        selectedRackList.add(r2);

        Set<Agent> candidateAgents = new HashSet<>();
        Agent a1 = new Agent();
        Agent a2 = new Agent();
        Agent a3 = new Agent();
        a1.setPosition(0, 6);
        a2.setPosition(2, 0);
        a3.setPosition(3, 5);
        candidateAgents.add(a1);
        candidateAgents.add(a2);
        candidateAgents.add(a3);

        Position gatePos = new Position(0, 0);

        Map<HiveObject, HiveObject> assignment = AgentAssigner.assignAgents(gatePos, selectedRackList, candidateAgents);

        assertEquals(a2, assignment.get(selectedRackList.get(0)));
        assertEquals(a1, assignment.get(selectedRackList.get(1)));
    }
}
