package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * WayPointsManager class
 * Manages the interactions with the waypoints
 *
 * @author David Farah (341017)
 */
public final class WayPointsManager {
    private static final double SEARCH_DISTANCE = 1000;
    private final Graph graph;
    private final ObservableList<Waypoint> wayPoints;
    private final ObjectProperty<MapViewParameters> mapViewParameters;
    private final Consumer<String> signalError;
    private final gui gui;
    private final String PROXIMITY_ERROR_MSG = "Aucune route à proximité !";


    /**
     * Constructor of WayPointsManager
     *
     * @param graph             the road network graph
     * @param mapViewParameters a JavaFX property containing the parameters of the displayed map
     * @param wayPoints         the (observable) list of all waypoints
     * @param signalError       an object to report errors (used to display error messages to the user)
     */
    public WayPointsManager(Graph graph, ObjectProperty<MapViewParameters> mapViewParameters,
                            ObservableList<Waypoint> wayPoints, Consumer<String> signalError) {
        this.graph = graph;
        this.mapViewParameters = mapViewParameters;
        this.wayPoints = wayPoints;
        this.signalError = signalError;
        this.gui = new gui();
    }


    /**
     * This method adds a waypoint to the map
     *
     * @param x coordinate of a point to add (WebMercator)
     * @param y coordinate of a point to add (WebMercator)
     */
    public void addWaypoint(double x, double y) {
        int closestNodeId = getClosestNodeId(x, y);

        if (closestNodeId < 0) {
            signalError.accept(PROXIMITY_ERROR_MSG);
            return;
        }

        PointCh pt = PointWebMercator.of(mapViewParameters.get().zoomLevel(), x, y).toPointCh();
        wayPoints.add(new Waypoint(pt, closestNodeId));

    }

    /**
     * This method replacesReplace an already existing waypoint by another one (on the same rank)
     *
     * @param x coordinate of a point to add (WebMercator system)
     * @param y coordinate of a point to add (WebMercator system)
     * @return true if a WayPoint has been replaced, false otherwise
     */
    private boolean replaceWaypoint(double x, double y, Waypoint oldWaypoint) {
        int closestNodeId = getClosestNodeId(x, y);

        if (closestNodeId < 0) {
            signalError.accept(PROXIMITY_ERROR_MSG);
            return false;
        } else if (!wayPoints.contains(oldWaypoint)) {
            return false;
        }

        PointCh pt = PointWebMercator.of(mapViewParameters.get().zoomLevel(), x, y).toPointCh();
        wayPoints.set(wayPoints.indexOf(oldWaypoint), new Waypoint(pt, closestNodeId));
        return true;
    }

    /**
     * This method obtain the ID of the closest Node at a given position withing the Search Distance
     *
     * @param x coordinate of a point to add (WebMercator)
     * @param y coordinate of a point to add (WebMercator)
     * @return the id of the Node
     */
    private int getClosestNodeId(double x, double y) {
        PointWebMercator point = PointWebMercator.of(mapViewParameters.get().zoomLevel(), x, y);
        return graph.nodeClosestTo(point.toPointCh(), SEARCH_DISTANCE);
    }


    /**
     * This method removes a Waypoint from the map
     *
     * @param waypoint : waypoint to be removed
     */
    private void removeWaypoint(Waypoint waypoint) {
        wayPoints.remove(waypoint);
    }


    /**
     * Returns the pane displaying the pins
     *
     * @return the pane
     */
    public Pane pane() {
        return gui.pane;
    }

    /**
     * Nested class that is in charge of the graphical interface for Waypoints
     */
    private final class gui {
        private final Pane pane;
        private final Map<Group, Waypoint> pinWaypointMap;
        private final MouseCoordinates coordinatesBeforeDrag;

        /**
         * Constructor of the gui nested class, initialises the attributes and sets listeners on
         * wayPoints and mapViewParameters
         */
        public gui() {
            this.pane = new Pane();
            pane.setPickOnBounds(false);
            pinWaypointMap = new HashMap<>();
            coordinatesBeforeDrag = new MouseCoordinates(0, 0);
            wayPoints.addListener((InvalidationListener) observable -> redrawWaypoints());
            mapViewParameters.addListener(o -> repositionWayPoints());
            redrawWaypoints();
        }


        /**
         * This method repositions waypoints to right place on the map
         */
        private void repositionWayPoints() {
            pinWaypointMap.forEach(
                    (pin, waypoint) -> {
                        PointWebMercator waypointMercator = PointWebMercator.ofPointCh(waypoint.point());
                        pin.setLayoutX(mapViewParameters.get().viewX(waypointMercator));
                        pin.setLayoutY(mapViewParameters.get().viewY(waypointMercator));
                    }
            );
        }


        /**
         * This method redraws Waypoints from the list (adds the Pin Groups to the pane)
         */
        public void redrawWaypoints() {
            pane.getChildren().clear();
            pinWaypointMap.clear();

            for (Waypoint wayPoint : wayPoints) {
                Group pin = createPinGroup();
                PointWebMercator anchorPoint = PointWebMercator.ofPointCh(wayPoint.point());
                pin.setLayoutX(mapViewParameters.get().viewX(anchorPoint));
                pin.setLayoutY(mapViewParameters.get().viewY(anchorPoint));
                addPinListeners(pin);
                pane.getChildren().add(pin);
                pinWaypointMap.put(pin, wayPoint);
            }

            if (pane.getChildren().size() > 0) {
                pane.getChildren().get(0).getStyleClass().clear();
                pane.getChildren().get(0).getStyleClass().add("pin");
                pane.getChildren().get(0).getStyleClass().add("first");

            }

            if (pane.getChildren().size() > 1) {
                pane.getChildren().get(wayPoints.size() - 1).getStyleClass().clear();
                pane.getChildren().get(wayPoints.size() - 1).getStyleClass().add("pin");
                pane.getChildren().get(wayPoints.size() - 1).getStyleClass().add("last");
            }

        }


        /**
         * This method creates the Group that represents a pin/waypoint on the map
         *
         * @return the pin Group created
         */
        private Group createPinGroup() {
            SVGPath outside = new SVGPath();
            String svgOutside = "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";
            outside.setContent(svgOutside);
            outside.getStyleClass().add("pin_outside");

            SVGPath inside = new SVGPath();
            String svgInside = "M0-23A1 1 0 000-29 1 1 0 000-23";
            inside.setContent(svgInside);
            inside.getStyleClass().add("pin_inside");

            Group pin = new Group(outside, inside);
            pin.getStyleClass().add("pin");
            pin.getStyleClass().add("middle");
            return pin;
        }

        /**
         * This method adds event listeners to a waypoint
         *
         * @param pin : Group representing the waypoint
         */
        private void addPinListeners(Group pin) {
            pin.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.isStillSincePress()) removeWaypoint(pinWaypointMap.get(pin));
            });

            pin.setOnMousePressed(mouseEvent -> {
                coordinatesBeforeDrag.setX(mouseEvent.getX());
                coordinatesBeforeDrag.setY(mouseEvent.getY());
            });

            pin.setOnMouseDragged(mouseEvent -> {
                pin.setLayoutX(pin.getLayoutX() + (mouseEvent.getX() - coordinatesBeforeDrag.getX()));
                pin.setLayoutY(pin.getLayoutY() + (mouseEvent.getY() - coordinatesBeforeDrag.getY()));
                pin.setOnMouseReleased(mouseEvent1 -> {
                    if (!replaceWaypoint(mapViewParameters.get().xUpperLeftMapView() + pin.getLayoutX(),
                            mapViewParameters.get().yUpperLeftMapView() + pin.getLayoutY(),
                            pinWaypointMap.get(pin))) {
                        redrawWaypoints();
                    }
                });
            });
        }
    }
}
