package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;


/**
 * RouteComputer
 *
 * @author David Farah (341017)
 * @author Wesley Nana Davies(344592)
 */
public final class RouteComputer {
    private final Graph graph;
    private final CostFunction costFunction;
    private final PriorityQueue<WeightedNode> nodesToExplore;
    private final float[] nodesDistanceToOrigin;
    private final int[] predecessors;
    private int nodeChosenId;


    /**
     * Constructor of the class which creates a RouteComputer with the given graph and cost function.
     *
     * @param graph        : graph used for the route
     * @param costFunction : cost function of the route
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        nodesDistanceToOrigin = new float[graph.nodeCount()];
        predecessors = new int[graph.nodeCount()];
        nodesToExplore = new PriorityQueue<>();
        this.costFunction = costFunction;
    }


    /**
     * This method computes the shortest route between the nodes given as parameters
     *
     * @param startNodeId : ID of the initial node
     * @param endNodeId   : ID of the final node
     * @return a route
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        Preconditions.checkArgument(startNodeId != endNodeId);

        boolean value = true;
        for (int i = 0; i < graph.nodeOutDegree(endNodeId); i++) {
            if (costFunction.costFactor(endNodeId, graph.nodeOutEdgeId(endNodeId, i)) != Double.POSITIVE_INFINITY) {
                value = false;
            }
        }
        if (value) return null;

        Arrays.fill(nodesDistanceToOrigin, Float.POSITIVE_INFINITY);
        nodesDistanceToOrigin[startNodeId] = 0;

        nodesToExplore.add(new WeightedNode(startNodeId, nodesDistanceToOrigin[startNodeId],
                (float) graph.nodePoint(startNodeId).distanceTo(graph.nodePoint(endNodeId))));


        while (!nodesToExplore.isEmpty()) {
            nodeChosenId = chooseNode().nodeId;

            if (nodeChosenId == endNodeId) {
                List<Edge> edges = reconstructRoute(startNodeId, endNodeId);
                nodesToExplore.clear();
                return new SingleRoute(edges);
            }

            addNodesToExplore(endNodeId);
            nodesDistanceToOrigin[nodeChosenId] = Float.NEGATIVE_INFINITY;
        }
        return null;
    }


    /**
     * Adds weighted nodes to the nodesToExplore list which are connected to the nodeChosen
     * (if the distance computed is smaller than the one stored in the array).
     *
     * @param endNodeId : ID of the node at the end of the route
     */
    private void addNodesToExplore(int endNodeId) {
        int currentEdgeId;
        int edgeEndNodeId;
        float nodeDistanceToOrigin;

        for (int i = 0; i < graph.nodeOutDegree(nodeChosenId); i++) {

            currentEdgeId = graph.nodeOutEdgeId(nodeChosenId, i);
            edgeEndNodeId = graph.edgeTargetNodeId(currentEdgeId);

            nodeDistanceToOrigin = nodesDistanceToOrigin[nodeChosenId]
                    + (float) (graph.edgeLength(currentEdgeId)
                    * costFunction.costFactor(nodeChosenId, currentEdgeId));


            if (nodeDistanceToOrigin < nodesDistanceToOrigin[edgeEndNodeId]) {
                predecessors[edgeEndNodeId] = nodeChosenId;
                nodesDistanceToOrigin[edgeEndNodeId] = nodeDistanceToOrigin;

                nodesToExplore.add(
                        new WeightedNode(
                                edgeEndNodeId,
                                nodeDistanceToOrigin,
                                (float) graph.
                                        nodePoint(edgeEndNodeId).
                                        distanceTo(graph.nodePoint(endNodeId))
                        )
                );
            }
        }
    }


    /**
     * Returns the weighted node which distance from the start node and end node is smallest
     * (ignores weighted nodes already explored)
     *
     * @return the weighted node
     */
    private WeightedNode chooseNode() {
        WeightedNode nodeChosen;
        do {
            nodeChosen = nodesToExplore.remove();
        } while (nodesDistanceToOrigin[nodeChosen.nodeId] == Float.NEGATIVE_INFINITY);

        return nodeChosen;
    }


    /**
     * Returns the list of edges which compose the path/itinerary.
     *
     * @param startNodeId : ID of the node at the start of the route
     * @param endNodeId   : ID of the node at the end of the route
     * @return a list
     */
    private List<Edge> reconstructRoute(int startNodeId, int endNodeId) {
        LinkedList<Edge> edges = new LinkedList<>();
        int start;
        int end = endNodeId;
        int edgeId;

        while ((end != startNodeId)) {
            start = predecessors[end];

            //iterate over edges going out of the node to find the corresponding edge
            for (int l = 0; l < graph.nodeOutDegree(start); l++) {
                edgeId = graph.nodeOutEdgeId(start, l);
                if (graph.edgeTargetNodeId(edgeId) == end) {
                    edges.addFirst(Edge.of(graph, edgeId, start, end));
                    break;
                }
            }

            end = start;
        }

        return edges;
    }


    /**
     * Inner record to represent a node, implementing the comparable interface
     * to let the PriorityQueue know which element is the smallest in the queue.
     */
    private record WeightedNode(int nodeId, float distance, float distanceStraightLine)
            implements Comparable<WeightedNode> {
        @Override
        public int compareTo(WeightedNode that) {
            return Float.compare(this.distance + this.distanceStraightLine,
                    that.distance + that.distanceStraightLine);
        }
    }
}


