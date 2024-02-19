package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;


/**
 * EdgeRecord
 *
 * @param fromNodeId : ID of the starting node
 * @param toNodeId   : ID of the ending node
 * @param fromPoint  :  point (PointCh) form which the edge starts
 * @param toPoint:   point (PointCh) to which the edge goes
 * @param length     : length of an edge
 * @param profile    : elevation profile of the edge
 * @author David Farah (341017)
 * @author Wesley Nana Davies(344592)
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint,
                   double length, DoubleUnaryOperator profile) {

    /**
     * Another constructor for Edge
     *
     * @param graph      :  graph which models nodes and edges.
     * @param edgeId:    ID of the Edge
     * @param fromNodeId : ID of the starting node
     * @param toNodeId   : ID of the ending node
     * @return a new Edge
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {
        return new Edge(fromNodeId, toNodeId, graph.nodePoint(fromNodeId),
                graph.nodePoint(toNodeId), graph.edgeLength(edgeId), graph.edgeProfile(edgeId));
    }

    /**
     * Returns the position along the edge, in meters,
     * that is closest to the given point (could be outside the edge)
     *
     * @param point : point of interest (PointCh)
     * @return the position
     */
    public double positionClosestTo(PointCh point) {
        return Math2.projectionLength(fromPoint.e(), fromPoint.n(), toPoint.e(), toPoint.n(), point.e(), point.n());
    }

    /**
     * Returns the point at the given position on the edge, expressed in meters.
     *
     * @param position : position along the edge
     * @return the position
     */
    public PointCh pointAt(double position) {
        double proportion = position / length;
        double east = Math2.interpolate(fromPoint.e(), toPoint.e(), proportion);
        double north = Math2.interpolate(fromPoint.n(), toPoint.n(), proportion);
        return new PointCh(east, north);
    }

    /**
     * Returns the altitude, in meters, at the given position on the edge.
     *
     * @param position : position along the edge
     * @return elevation at <code>position</code>
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }
}
