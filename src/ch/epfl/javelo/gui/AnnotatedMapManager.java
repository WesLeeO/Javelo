package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

import static java.lang.Double.NaN;

/**
 * Annotated Map Manager class
 * Manages the global graphic interface
 *
 * @author Wesley Nana Davies (344592)
 * @author David Farah (341017)
 */
public final class AnnotatedMapManager {
    private final static int MAX_NUMBER_OF_PIXELS = 15;
    private final static int INITIAL_ZOOM_LEVEL = 12;
    private final static double INITIAL_X_UPPER_LEFT = 543_200;
    private final static double INITIAL_Y_UPPER_LEFT = 370_650;
    private final StackPane stackPane;
    private final SimpleDoubleProperty mousePositionOnRouteProperty;
    private final ObjectProperty<Point2D> currentMousePosition;
    private final ObjectProperty<MapViewParameters> mapViewParametersP;
    private final RouteBean routeBean;


    /**
     * Constructor
     *
     * @param graph       : Graph used for the entire map (loaded from ch_west or lausanne)
     * @param tileManager : TileManager which manages
     *                    the access to the tiles (located in osm-cache or loaded from server)
     * @param routeBean   : RouteBean which contains information about the route
     *                    (waypoints, highlighted position, elevation profile, etc...)
     * @param consumer    : Consumer need for WayPointsManager to display errors to the screen.
     */
    public AnnotatedMapManager(Graph graph, TileManager tileManager, RouteBean routeBean, Consumer<String> consumer) {

        this.routeBean = routeBean;

        // initial settings for the map
        mapViewParametersP = new SimpleObjectProperty<>(
                new MapViewParameters(INITIAL_ZOOM_LEVEL, INITIAL_X_UPPER_LEFT, INITIAL_Y_UPPER_LEFT));


        // instantiating some objects
        currentMousePosition = new SimpleObjectProperty<>(Point2D.ZERO);
        mousePositionOnRouteProperty = new SimpleDoubleProperty(NaN);
        RouteManager routeManager = new RouteManager(routeBean, mapViewParametersP);
        WayPointsManager wayPointsManager =
                new WayPointsManager(graph, mapViewParametersP, routeBean.getWaypoints(), consumer);

        BaseMapManager baseMapManager = new BaseMapManager(tileManager, wayPointsManager, mapViewParametersP);

        // initializing the pane and adding some stylesheets
        stackPane = new StackPane(baseMapManager.pane(), routeManager.pane(), wayPointsManager.pane());
        stackPane.getStylesheets().add("map.css");

        // adding listeners to properties
        mapViewParametersP.addListener(e -> {
            if (!(routeBean.getRouteProperty().get() == null))
                recalculateMousePositionOnRouteProperty();
        });

        routeBean.getRouteProperty().addListener(e -> {
            if (!(routeBean.getRouteProperty().get() == null))
                recalculateMousePositionOnRouteProperty();
        });

        currentMousePosition.addListener(e -> {
            if (!(routeBean.getRouteProperty().get() == null)) {
                recalculateMousePositionOnRouteProperty();
            }
        });

        stackPane.setOnMouseMoved(e -> currentMousePosition.setValue(new Point2D(e.getX(), e.getY())));
        stackPane.setOnMouseExited(e -> mousePositionOnRouteProperty.setValue(NaN));


    }

    /**
     * Returns the pane of the annotated map (stacking baseMap pane, wayPoints pane and routeManager pane)
     *
     * @return stackPane (Pane)
     */
    public Pane pane() {
        return stackPane;
    }


    /**
     * Getter for the mousePositionOnProfileProperty, containing the position along the route of the mouse
     *
     * @return mousePositionOnRouteProperty (ReadOnlyDoubleProperty)
     */
    public ReadOnlyDoubleProperty mousePositionOnRouteProperty() {
        return mousePositionOnRouteProperty;
    }

    /**
     * This method sets a value for the mousePositionOnRouteProperty, called whenever some changes happen to
     * the routeProperty, the mapViewParameters property or the current mouse position on profile
     * property.
     */
    private void recalculateMousePositionOnRouteProperty() {

        MapViewParameters mapViewParameters = mapViewParametersP.get();
        PointWebMercator pointUnderMouse = mapViewParameters.pointAt(currentMousePosition.get().getX(),
                currentMousePosition.get().getY());

        if (pointUnderMouse.toPointCh() != null) {
            RoutePoint rp = routeBean.getRouteProperty().get()
                    .pointClosestTo(pointUnderMouse.toPointCh());

            PointWebMercator projectedPoint = PointWebMercator.ofPointCh(rp.point());

            double norm = Math2.
                    norm(mapViewParameters.viewX(pointUnderMouse) - mapViewParameters.viewX(projectedPoint),
                            mapViewParameters.viewY(pointUnderMouse) - mapViewParameters.viewY(projectedPoint));

            mousePositionOnRouteProperty.setValue((norm <= MAX_NUMBER_OF_PIXELS) ? rp.position() : NaN);

        }
    }
}
