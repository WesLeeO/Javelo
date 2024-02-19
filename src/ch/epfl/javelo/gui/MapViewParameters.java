package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;


/**
 * MapViewParameters record that manages the settings of the basemap in the GUI
 *
 * @param zoomLevel         : zoom level of the map
 * @param xUpperLeftMapView : x-coordinate of the top-left corner of the map portion displayed on the screen
 * @param yUpperLeftMapView : y-coordinate of the top-left corner of the map portion displayed on the screen
 * @author Wesley Nana Davies (344592)
 * @author David Farah (341017)
 */
public record MapViewParameters(int zoomLevel, double xUpperLeftMapView, double yUpperLeftMapView) {

    /**
     * Returns the upper-left corner of the map as a Point2D
     * @return the top-left point
     */
    public Point2D topLeft() {
        return new Point2D(xUpperLeftMapView, yUpperLeftMapView);
    }

    /**
     * Returns a new instance of MapViewParameter almost identical to <code>this</code>,
     * except that the coordinates of the top-left corner are
     * those passed as arguments to the method (same zoomLevel)
     *
     * @param newXUpperLeftMapView : X coordinate to override
     * @param newYUpperLeftMapView : Y coordinate to override
     * @return a new instance of MapViewParameters
     */
    public MapViewParameters withMinXY(double newXUpperLeftMapView, double newYUpperLeftMapView) {
        return new MapViewParameters(zoomLevel, newXUpperLeftMapView, newYUpperLeftMapView);
    }

    /**
     * Returns a PointWebMercator representation of the point
     * whose coordinates are relative to those of the top left point
     *
     * @param xCoordinate : x coordinate of a point, expressed in relation to the top-left corner of the map portion
     *                    displayed on the screen
     * @param yCoordinate : y coordinate of a point, expressed in relation to the top-left corner of the map portion
     *                    displayed on the screen
     * @return a PointWebMercator with coordinates (xCoordinate, yCoordinate)
     */
    public PointWebMercator pointAt(double xCoordinate, double yCoordinate) {
        return PointWebMercator.of(zoomLevel, xUpperLeftMapView + xCoordinate,
                yUpperLeftMapView + yCoordinate);
    }

    /**
     * Returns the corresponding x position, expressed relative
     * to the top-left corner of the map portion displayed on the screen
     *
     * @param point : a PointWebMercator
     * @return the x-position
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel) - xUpperLeftMapView;
    }

    /**
     * Returns the corresponding y position, expressed relative
     * to the top-left corner of the map portion displayed on the screen
     *
     * @param point : a PointWebMercator
     * @return the corresponding y-position
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - yUpperLeftMapView;
    }
}