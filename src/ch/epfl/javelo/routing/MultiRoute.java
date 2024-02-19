package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * A MultiRoute
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public final class MultiRoute implements Route {
    private final List<Route> segments;
    private final double routeLength;


    /**
     * Constructor of the class which builds a MultiRoute consisting of the given segments,
     *
     * @param segments : list of segments forming the MultiRoute
     * @throws IllegalArgumentException if the list of segments is empty
     */
    public MultiRoute(List<Route> segments) {
        Preconditions.checkArgument(!segments.isEmpty());
        this.segments = List.copyOf(segments);
        routeLength = calculateLength();
    }


    /**
     * Returns the length of the MultiRoute
     *
     * @return length of the MultiRoute
     */
    private double calculateLength() {
        double length = 0;
        for (Route segment : segments) {
            length += segment.length();
        }
        return length;
    }


    /**
     * Returns the index of the segment of the MultiRoute at the given position
     * (considering SingleRoute segments)
     *
     * @param position : position from the start of the itinerary (in meters)
     * @return the index of the segment
     */
    @Override
    public int indexOfSegmentAt(double position) {
        position = Math2.clamp(0, position, routeLength);

        int segmentIndex = 0;
        int globalIndex = globalIndexOfSegmentAt(position);
        int previousLength = 0;

        for (Route route : segments.subList(0, globalIndex)) {
            previousLength += route.length();
            segmentIndex += route.indexOfSegmentAt(route.length()) + 1;
        }

        segmentIndex += segments.get(globalIndex).indexOfSegmentAt(position - previousLength);

        return segmentIndex;
    }


    /**
     * Returns the global index of the segment of the MultiRoute
     * at the given position (first layer)
     *
     * @param position : position from the start of the itinerary (in meters)
     * @return the global index at the given position
     */
    private int globalIndexOfSegmentAt(double position) {
        if (position == routeLength) return segments.size() - 1;

        int segmentIndex = 0;
        for (Route segment : segments) {
            if (position >= segment.length()) {
                position -= segment.length();
                segmentIndex++;
            } else break;
        }
        return segmentIndex;
    }


    /**
     * Returns the length of the MultiRoute
     *
     * @return the length of the route (meters)
     */
    @Override
    public double length() {
        return this.routeLength;
    }


    /**
     * Returns all the edges that constitute the MultiRoute
     *
     * @return a list containing all the edges
     */
    @Override
    public List<Edge> edges() {
        List<Edge> edges = new ArrayList<>();
        for (Route segment : segments) {
            edges.addAll(segment.edges());
        }
        return edges;
    }


    /**
     * Returns a list containing all the points located at the extremities of the edges of the route
     *
     * @return a list containing all the points of interest
     */
    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        ListIterator<Route> segmentIterator = segments.listIterator();
        Route segment;

        while (segmentIterator.hasNext()) {
            //add all points except last
            segment = segmentIterator.next();
            points.addAll(segment.points().subList(0, segment.points().size() - 1));

            if (!segmentIterator.hasNext()) {
                //add the last point
                int lastPointIndex = segment.points().size() - 1;
                points.add(segment.points().get(lastPointIndex));
            }
        }

        return points;
    }

    /**
     * Returns the length of the route up until the specified route index (exclusive)
     *
     * @param index index of the route to know the distance before
     * @return the length of the route up until the given index (exclusive)
     */
    private double lengthBeforeRoute(int index) {
        double length = 0;
        for (Route route : segments.subList(0, index)) {
            length += route.length();
        }
        return length;
    }


    /**
     * Returns the point at the given position along the route
     *
     * @param position position from the start of the itinerary (in meters)
     * @return the point at the given position along the route
     */
    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position, routeLength);
        int routeIndex = globalIndexOfSegmentAt(position);
        return segments.get(routeIndex).pointAt(position - lengthBeforeRoute(routeIndex));
    }


    /**
     * Returns the elevation at the given position along the route
     *
     * @param position position from the start of the itinerary (in meters)
     * @return the elevation at a given point along the itinerary, NaN if the edge has no profile
     */
    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position, routeLength);
        int routeIndex = globalIndexOfSegmentAt(position);
        return segments.get(routeIndex).elevationAt(position - lengthBeforeRoute(routeIndex));
    }


    /**
     * Returns the node closest to the point at the given position along the route
     *
     * @param position position from the start of the itinerary (in meters)
     * @return a node ID
     */
    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position, routeLength);
        int routeIndex = globalIndexOfSegmentAt(position);
        return segments.get(routeIndex).nodeClosestTo(position - lengthBeforeRoute(routeIndex));
    }


    /**
     * Returns the RoutePoint closest to the point given as a parameter
     *
     * @param point point of interest (PointCh)
     * @return the RoutePoint closest to the given point
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint currentPoint;
        RoutePoint closestPoint = RoutePoint.NONE;

        for (int index = 0; index < segments.size(); index++) {
            currentPoint = segments.get(index).
                    pointClosestTo(point).
                    withPositionShiftedBy(lengthBeforeRoute(index));

            closestPoint = closestPoint.min(currentPoint);
        }
        return closestPoint;
    }
}
