package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A Single Route
 *
 * @author David Farah (341017)
 * @author Wesley Nana Davies(344592)
 */
public final class SingleRoute implements Route {
    private final List<Edge> edges;
    private final List<PointCh> points;
    private final double[] edgesLength;
    private final double routeLength;


    /**
     * @param edges :  given edges that form the route
     *              constructs the simple route composed of the given edges,
     * @throws IllegalArgumentException if the list of edges is empty
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        this.routeLength = calculateLength();
        this.edgesLength = buildEdgesSearch();
        this.points = List.copyOf(buildPoints());
    }


    /**
     * @return the length of the route calculated using the list of edges
     */
    private double calculateLength() {
        double length = 0;
        for (Edge edge : edges) {
            length += edge.length();
        }
        return length;
    }


    /**
     * @return the double array containing the position of the end nodes from the start of the route
     * used to determine the index of an edge by dichotomous search
     */
    private double[] buildEdgesSearch() {
        double[] edgesSearch = new double[edges.size() + 1];
        int i = 1;
        double totalLength = 0;

        for (Edge edge : this.edges) {
            totalLength += edge.length();
            edgesSearch[i] = totalLength;
            i++;
        }
        return edgesSearch;
    }


    /**
     * @return the list of points located at the ends of the edges of the route
     */
    private List<PointCh> buildPoints() {
        List<PointCh> points = new ArrayList<>();
        points.add(edges.get(0).fromPoint());
        for (Edge edge : this.edges) {
            points.add(edge.toPoint());
        }
        return points;
    }


    /**
     * @param position :  position of interest
     * @return the index of the segment at the given position of the route (in meters), which is always 0 in
     * the case of a simple route
     */
    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }


    /**
     * @return the length of the route, in meters
     */
    @Override
    public double length() {
        return routeLength;
    }


    /**
     * @return a list containing all the edges of the route
     */
    @Override
    public List<Edge> edges() {
        return edges;
    }


    /**
     * @return a list containing all the points located at the extremities of the edges of the route
     */
    @Override
    public List<PointCh> points() {
        return points;
    }


    /**
     * @param position : position of interest
     * @return the point at the given position along the route
     */
    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position, routeLength);
        int result = Arrays.binarySearch(edgesLength, position);

        if (result >= 0) {
            return points.get(result);
        } else {
            int edgeIndex = -result - 2;
            double x = position - edgesLength[edgeIndex];
            return edges.get(edgeIndex).pointAt(x);
        }
    }


    /**
     * @param position: position of interest
     * @return the elevation at a given point along the itinerary,
     * NaN if the edge has no profile
     */
    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position, routeLength);
        int result = Arrays.binarySearch(edgesLength, position);

        if (result >= 0) {
            return result == 0 ?
                    edges.get(0).elevationAt(0)
                    : edges.get(result - 1).elevationAt(edges.get(result - 1).length());

        } else {
            int edgeIndex = -result - 2;
            double x = position - edgesLength[edgeIndex];
            return edges.get(edgeIndex).elevationAt(x);
        }
    }


    /**
     * @param position : position of interest
     * @return the identity of the node belonging to the route and located closest to the given position
     */
    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position, routeLength);
        int result = Arrays.binarySearch(edgesLength, position);

        if (result == edges.size()) {
            return edges.get(result - 1).toNodeId();
        } else if (result >= 0) {
            return edges.get(result).fromNodeId();
        } else {
            int edgeIndex = -result - 2;
            double distStartOfEdge = position - edgesLength[edgeIndex];
            double distEndOfEdge = edgesLength[edgeIndex + 1] - position;

            return (distStartOfEdge <= distEndOfEdge) ? edges.get(edgeIndex).fromNodeId()
                    : edges.get(edgeIndex).toNodeId();
        }
    }


    /**
     * @param point : point of interest (PointCh)
     * @return the point on the route that is closest to the given reference point
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        double clampedToEdgeLength;
        double distanceClampedToPoint;
        double pointProjectionLength;
        double edgeLength;

        Edge edge;
        PointCh projectedPoint;
        RoutePoint RoutePointClosestTo = RoutePoint.NONE;

        for (int i = 0; i < edges.size(); i++) {
            edge = edges.get(i);
            edgeLength = edge.length();

            pointProjectionLength = edge.positionClosestTo(point);
            clampedToEdgeLength = Math2.clamp(0, pointProjectionLength, edgeLength);
            projectedPoint = edge.pointAt(clampedToEdgeLength);

            //we try to find the closest projection to the point in question
            distanceClampedToPoint = projectedPoint.distanceTo(point);

            RoutePointClosestTo = RoutePointClosestTo.min(
                    projectedPoint,
                    edgesLength[i] + clampedToEdgeLength,
                    distanceClampedToPoint
            );
        }
        return RoutePointClosestTo;
    }
}
