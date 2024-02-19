package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;


/**
 * Interface that represents a Javelo Route
 *
 * @author David Farah (341017)
 * @author Wesley Nana Davies(344592)
 */
public interface Route {
    /**
     * Returns the index of the segment at the given position (in meters)
     *
     * @param position given position (in meters)
     * @return the index
     */
    int indexOfSegmentAt(double position);

    /**
     * @return the length of the route, in meters
     */
    double length();

    /**
     * @return List containing all the edges of the route
     */
    List<Edge> edges();

    /**
     * @return List containing all the points located at the extremities of the edges of the route
     */
    List<PointCh> points();

    /**
     * @param position (Double)
     * @return the point at the given position along the route
     */
    PointCh pointAt(double position);

    /**
     * @param position (Double)
     * @return the elevation at a given point along the itinerary
     */
    double elevationAt(double position);

    /**
     * Returns the identity of the node belonging to the route and located closest to the given position
     *
     * @param position (Double)
     * @return the index
     */
    int nodeClosestTo(double position);

    /**
     * Returns the point on the route that is closest to the given reference point
     *
     * @param point (PointCh)
     * @return the point
     */
    RoutePoint pointClosestTo(PointCh point);
}
