package algorithms.TaskAllocator;

import models.HiveObject;
import models.agents.Agent;
import models.facilities.Rack;
import models.maps.utils.Position;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RobotAssigner {

    /**
     * Using Hungarian algorithm for solving the agent assignment problem where we should assign the agents to the
     * selected racks.
     *
     * @param gatePos         {@code Position} the position of the gate.
     * @param selectedRacks   list of the selected {@code Rack}s.
     * @param candidateAgents list of the candidate agents {@code agent}s
     * @return A matching solution for the assignment problem.
     */
    public static MatchingAlgorithm.Matching<HiveObject, DefaultWeightedEdge> assignAgents(Position gatePos,
                                                                                           List<Rack> selectedRacks,
                                                                                           List<Agent> candidateAgents) {
        // Create the cost bipartite graph.
        SimpleDirectedWeightedGraph<HiveObject, DefaultWeightedEdge> costGraph = new
                SimpleDirectedWeightedGraph<HiveObject, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        // Create the two bipartite graph sets.
        Set<HiveObject> racksSet = new HashSet<>();
        Set<HiveObject> agentsSet = new HashSet<>();

        for (Rack rack : selectedRacks) {
            racksSet.add(rack);
            for (Agent agent : candidateAgents) {
                costGraph.addEdge(rack, agent);
                costGraph.setEdgeWeight(rack, agent, calculateOperationCost(rack.getPosition(), gatePos, agent.getPosition()));
                agentsSet.add(agent);
            }
        }

        // Run Hungarian algorithm on the current racks and the current available agents.
        KuhnMunkresMinimalWeightBipartitePerfectMatching<HiveObject, DefaultWeightedEdge> assigner = new
                KuhnMunkresMinimalWeightBipartitePerfectMatching<HiveObject, DefaultWeightedEdge>(costGraph, racksSet, agentsSet);

        return assigner.getMatching();
    }

    /**
     * Calculate the estimated operational cost for a certain {@code Agent} transferring a {@code Rack} to a certain
     * {@code Gate}.
     *
     * @param rackPos  {@code Position} of the rack.
     * @param gatePos  {@code Position} of the gate.
     * @param agentPos {@code Position} of the agent.
     * @return the estimated operational cost.
     */
    private static double calculateOperationCost(Position rackPos, Position gatePos, Position agentPos) {
        // TODO @Samir55 try different fixed and dynamic cost values.
        return agentPos.distanceTo(rackPos) * 10 + agentPos.distanceTo(gatePos) * 20;
    }

}
