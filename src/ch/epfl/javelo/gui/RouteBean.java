package ch.epfl.javelo.gui;

import ch.epfl.javelo.LRUCache;
import ch.epfl.javelo.routing.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * RouteBean class
 * This class contains information about a route (elevation profile, waypoints, highlighted position, etc)
 *
 * @author Wesley Nana Davies (344592)
 * @author David Farah (341017)
 */
public final class RouteBean {
    private final static int RECOMMENDED_STEP_LENGTH = 5, CACHE_CAPACITY = 10;
    private final static float LOAD_FACTOR = 0.75f;
    public static ObservableList<Waypoint> waypoints;
    private final RouteComputer routeComputer;
    private final ObjectProperty<Route> route;
    private final DoubleProperty highlightedPosition;
    private final ObjectProperty<ElevationProfile> elevationProfile;
    private final Map<Integer, Route> hashRouteMap;

    /**
     * Constructor
     *
     * @param routeComputer : routeComputer which computes best routes between waypoints
     */
    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer;
        waypoints = FXCollections.observableArrayList();
        route = new SimpleObjectProperty<>();
        elevationProfile = new SimpleObjectProperty<>();
        highlightedPosition = new SimpleDoubleProperty(Double.NaN);
        hashRouteMap = new LRUCache<>(CACHE_CAPACITY, LOAD_FACTOR);

        // this listener calls recalculates route and profile
        // each time a change is made in the waypoints list
        waypoints.addListener((InvalidationListener) e -> {
            if (waypoints.size() > 0)
                recalculateRouteAndProfile();
        });
    }


    /**
     * This method recalculates the route and the profile
     */
    private void recalculateRouteAndProfile() {

        // if there is only one waypoint, setting properties to null and doing no calculations
        if (waypoints.size() == 1) {
            route.setValue(null);
            elevationProfile.setValue(null);
            return;
        }

        List<Route> singleRoutes = new ArrayList<>();
        Iterator<Waypoint> it = waypoints.iterator();
        Waypoint oldWaypoint = it.next();
        Waypoint currentWaypoint;

        // iterating through all waypoints
        while (it.hasNext()) {
            currentWaypoint = it.next();

            // using the method equals which we have overridden in Waypoint record
            if (!currentWaypoint.equals(oldWaypoint)) {

                // using the hash method from Objects to identify effectively retrieve routes in the hashRouteMap.
                int hashCode = Objects.hash(oldWaypoint.nodeID(), currentWaypoint.nodeID());

                Waypoint finalOldWaypoint = oldWaypoint;
                Waypoint finalCurrentWaypoint = currentWaypoint;

                singleRoutes.add(hashRouteMap.computeIfAbsent(hashCode, hashCodeKey ->
                        routeComputer.bestRouteBetween(finalOldWaypoint.nodeID(), finalCurrentWaypoint.nodeID())));

            }

            oldWaypoint = currentWaypoint;
        }

        // if all routes exist, we can store a new route, or we store null otherwise.
        if (!singleRoutes.contains(null)) {
            route.setValue(new MultiRoute(singleRoutes));
            elevationProfile.setValue(ElevationProfileComputer.elevationProfile(route.get(),
                    RECOMMENDED_STEP_LENGTH));
        }
        else {
            route.setValue(null);
            elevationProfile.setValue(null);
        }

    }


    /**
     * Returns the index of the segment containing it, ignoring empty segments
     *
     * @param position : position (meters) along the route
     * @return the index of the segment
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = route.get().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i += 1) {
            int n1 = waypoints.get(i).nodeID();
            int n2 = waypoints.get(i + 1).nodeID();
            if (n1 == n2) index += 1;
        }
        return index;
    }


    /**
     * Returns the highlighted position in meters along the route
     *
     * @return the highlighted position
     */
    public double highlightedPosition() {
        return highlightedPosition.get();
    }

    /**
     * Returns the DoubleProperty containing the highlighted position along the route
     *
     * @return a DoubleProperty
     */
    public DoubleProperty getHighlightedPositionP() {
        return highlightedPosition;
    }


    /**
     * This method sets a value for the highlighted position property
     *
     * @param value : double value to be contained in highlightedPosition
     */
    public void setHighlightedPositionProperty(double value) {
        highlightedPosition.set(value);
    }

    /**
     * This method is a getter which enables other classes
     * to have access to the list of waypoints which constitute the route
     *
     * @return an ObservableList of waypoints
     */
    public ObservableList<Waypoint> getWaypoints() {
        return waypoints;
    }

    /**
     * This method returns the route property of the route bean
     *
     * @return a ReadOnlyObjectProperty containing the route
     */
    public ReadOnlyObjectProperty<Route> getRouteProperty() {
        return route;
    }

    /**
     * This method returns the elevation profile property of the route bean
     *
     * @return a ReadOnlyObjectProperty containing the elevation profile
     */
    public ReadOnlyObjectProperty<ElevationProfile> getElevationProfileProperty() {
        return elevationProfile;
    }

}
