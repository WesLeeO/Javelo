package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;


/**
 * PointWebMercator record
 *
 * @param x :(Web Mercator system) x-coordinate (between 0 and 1)
 * @param y :(Web Mercator system) y-coordinate (between 0 and 1)
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public record PointWebMercator(double x, double y) {
    private static final int VALUE_FORMULA = 8;

    /**
     * @throws IllegalArgumentException if the point coordinates are not contained in [0,1]
     */
    public PointWebMercator {
        Preconditions.checkArgument(x >= 0 && x <= 1 && y >= 0 && y <= 1);
    }

    /**
     * Returns a PointWebMercator with corresponding coordinates at zoom level = 0
     *
     * @param zoomLevel : current zoom level
     * @param x         : x-coordinate with applied zoom (Web Mercator)
     * @param y         : y-coordinate with applied zoom (Web Mercator)
     * @return new PointWebMercator with corresponding coordinates at zoom 0
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(Math.scalb(x, -(VALUE_FORMULA + zoomLevel)),
                Math.scalb(y, -(VALUE_FORMULA + zoomLevel)));
    }

    /**
     * Returns a PointWebMercator with coordinates converted
     *
     * @param pointCh : point of interest (PointCh)
     * @return the point of interest as a PointCh
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        return new PointWebMercator(WebMercator.x(pointCh.lon()), WebMercator.y(pointCh.lat()));
    }


    /**
     * Returns the x-coordinate at a certain zoom level
     *
     * @param zoomLevel current zoom level
     * @return the x-coordinate for a certain zoom level
     */
    public double xAtZoomLevel(int zoomLevel) {
        return Math.scalb(x, VALUE_FORMULA + zoomLevel);
    }

    /**
     * Returns the y-coordinate at a certain zoom level
     *
     * @param zoomLevel current zoom level
     * @return the y-coordinate for a certain zoom level
     */
    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(y, VALUE_FORMULA + zoomLevel);
    }

    /**
     * Returns the longitude of a given PointWebMercator
     *
     * @return the longitude of the point
     */
    public double lon() {
        return WebMercator.lon(x);
    }

    /**
     * Returns the longitude of a given PointWebMercator
     *
     * @return the latitude of the point
     */
    public double lat() {
        return WebMercator.lat(y);
    }

    /**
     * Returns the PointWebMercator as a PointCh (conversion)
     *
     * @return a point in the Swiss coordinates system,
     * or null if it does not exist
     */
    public PointCh toPointCh() {
        double longitude = lon();
        double latitude = lat();
        double e = Ch1903.e(longitude, latitude);
        double n = Ch1903.n(longitude, latitude);
        return (!SwissBounds.containsEN(e, n) ? null : new PointCh(e, n));
    }
}
