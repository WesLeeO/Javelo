package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

import javax.naming.Binding;

import static java.lang.Float.NaN;

/**
 * RouteManager class
 * This class manages the display of the route and of its highlighted position
 * (polyline and circle) about a route
 *
 * @author Wesley Nana Davies (344592)
 * @author David Farah (341017)
 */
public final class RouteManager {
    private final RouteBean routeBean;
    private final ObjectProperty<MapViewParameters> mapViewParametersP;
    private final Pane pane;
    private final Polyline polyline;
    private final Circle circle;

    /**
     * Constructor
     *
     * @param routeBean          : routeBean which contains information about the route
     * @param mapViewParametersP : a mapViewParameters property
     */
    public RouteManager(RouteBean routeBean, ObjectProperty<MapViewParameters> mapViewParametersP) {
        this.routeBean = routeBean;
        this.mapViewParametersP = mapViewParametersP;

        polyline = new Polyline();
        polyline.setId("route");
        circle = new Circle(5);
        circle.setId("highlight");

        pane = new Pane();
        pane.setPickOnBounds(false);
        pane.getChildren().addAll(polyline, circle);

        addListeners();
    }

    /**
     * This method adds listener to different properties
     */
    private void addListeners() {

        routeBean.getWaypoints().addListener((InvalidationListener) e -> reconstructPolyline());

        this.mapViewParametersP.addListener(
                (p, oldParams, newParams) -> {
                    if ((oldParams.zoomLevel() == newParams.zoomLevel())) {
                        repositionNodes();
                    } else {
                        reconstructPolyline();
                    }
                });



        circle.setOnMouseClicked(e -> {

            PointWebMercator pointRelatedToPane = mapViewParametersP.get().pointAt(
                    circle.localToParent(e.getX(), e.getY()).getX(),
                    circle.localToParent(e.getX(), e.getY()).getY());

            PointCh pointRelatedToPaneCh = pointRelatedToPane.toPointCh();

            int id = routeBean.getRouteProperty().get().nodeClosestTo(routeBean.highlightedPosition());
            Waypoint w = new Waypoint(pointRelatedToPaneCh, id);
            routeBean.getWaypoints().add(routeBean.indexOfNonEmptySegmentAt(routeBean.highlightedPosition()) + 1,
                    w);
        });


        routeBean.getHighlightedPositionP().addListener(e -> {
            if (!Double.isNaN(routeBean.highlightedPosition()))
                repositionCircle();
            else
                circle.setVisible(false);
        });


    }

    private void reconstructPolyline() {
        polyline.getPoints().clear();

        if (routeBean.getRouteProperty().get() != null) {
            polyline.setVisible(true);

            for (PointCh routePoint : routeBean.getRouteProperty().get().points()) {
                PointWebMercator routePointMercator = PointWebMercator.ofPointCh(routePoint);
                polyline.getPoints().addAll(routePointMercator.xAtZoomLevel(mapViewParametersP.get().zoomLevel()),
                        routePointMercator.yAtZoomLevel(mapViewParametersP.get().zoomLevel()));
            }

            polyline.setLayoutX(-mapViewParametersP.get().xUpperLeftMapView());
            polyline.setLayoutY(-mapViewParametersP.get().yUpperLeftMapView());

            if (!Double.isNaN(routeBean.highlightedPosition())) {
                repositionCircle();
            }
        }
        else {
            circle.setVisible(false);
            polyline.setVisible(false);
        }


    }


    /**
     * This method simply repositions the circle along the route
     * whenever it is needed (zoomLevel changes, etc)
     */
    private void repositionCircle() {
        circle.setVisible(true);
        PointWebMercator highlightedPoint = PointWebMercator.
                ofPointCh(routeBean.getRouteProperty().get().pointAt(routeBean.highlightedPosition()));
        circle.setCenterX(highlightedPoint.xAtZoomLevel(mapViewParametersP.get().zoomLevel()));
        circle.setCenterY(highlightedPoint.yAtZoomLevel(mapViewParametersP.get().zoomLevel()));
        circle.setLayoutX(-mapViewParametersP.get().xUpperLeftMapView());
        circle.setLayoutY(-mapViewParametersP.get().yUpperLeftMapView());
    }


    /**
     * This method repositions nodes each time the MapViewParameters change
     */
    private void repositionNodes() {
        if (routeBean.getRouteProperty().get() != null) {
            polyline.setLayoutX(-mapViewParametersP.get().xUpperLeftMapView());
            polyline.setLayoutY(-mapViewParametersP.get().yUpperLeftMapView());
            circle.setLayoutX(-mapViewParametersP.get().xUpperLeftMapView());
            circle.setLayoutY(-mapViewParametersP.get().yUpperLeftMapView());
        }
    }

    /**
     * This method returns the pane of the route manager
     *
     * @return a Pane
     */
    public Pane pane() {
        return pane;
    }
}
