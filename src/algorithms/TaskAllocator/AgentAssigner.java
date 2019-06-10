package algorithms.dispatcher;

import models.HiveObject;
import models.agents.Agent;
import models.facilities.Rack;
import models.maps.utils.Position;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

public class AgentAssigner {

    /**
     * Using Hungarian algorithm for solving the agent assignment problem where we should assign the agents to the
     * selected racks.
     *
     * @param gatePos         {@code Position} the position of the gate.
     * @param selectedRacks   list of the selected {@code Rack}s.
     * @param candidateAgents list of the candidate agents {@code agent}s
     * @return A matching solution for the assignment problem.
     */
    public static Map<HiveObject, HiveObject> assignAgents(Position gatePos, List<Rack> selectedRacks, List<Agent> candidateAgents) {
        // Create the cost bipartite graph
        SimpleDirectedWeightedGraph<HiveObject, DefaultWeightedEdge> costGraph = new
                SimpleDirectedWeightedGraph<HiveObject, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        // Create the two bipartite graph sets
        Set<HiveObject> racksSet = new HashSet<>();
        Set<HiveObject> agentsSet = new HashSet<>();

        for (Rack rack : selectedRacks) {
            racksSet.add(rack);
            costGraph.addVertex(rack);
            for (Agent agent : candidateAgents) {
                if (!costGraph.containsVertex(agent))
                    costGraph.addVertex(agent);
                costGraph.addEdge(rack, agent);
                costGraph.setEdgeWeight(rack, agent, calculateOperationCost(rack.getPosition(), gatePos, agent.getPosition()));
                agentsSet.add(agent);
            }
        }

        // Run Hungarian algorithm on the current racks and the current available agents
        KuhnMunkresMinimalWeightBipartitePerfectMatching<HiveObject, DefaultWeightedEdge> assigner = new
                KuhnMunkresMinimalWeightBipartitePerfectMatching<HiveObject, DefaultWeightedEdge>(costGraph, racksSet, agentsSet);

        MatchingAlgorithm.Matching<HiveObject, DefaultWeightedEdge> matching = assigner.getMatching();

        // Prepare result
        Map<HiveObject, HiveObject> assignment = new HashMap<>();
        for (DefaultWeightedEdge edge : matching.getEdges()) {
            assignment.put(costGraph.getEdgeSource(edge), costGraph.getEdgeTarget(edge));
        }

        return assignment;
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
        return agentPos.distanceTo(rackPos) * 19 + agentPos.distanceTo(gatePos) * 30 * 2;
    }

}