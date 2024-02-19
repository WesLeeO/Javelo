package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

import static java.lang.Math.*;

/**
 * WebMercator
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public final class WebMercator {
    private WebMercator() {
    }

    /**
     * Returns the x-coordinate (WebMercator system) of the point
     *
     * @param lon : longitude (in radians)
     * @return the x-coordinate
     */
    public static double x(double lon) {
        return (1 / (2 * PI)) * (lon + PI);
    }

    /**
     * Returns the y-coordinate (WebMercator system) of the point
     *
     * @param lat : latitude (in radians)
     * @return the y-coordinate
     */
    public static double y(double lat) {//?
        return (1 / (2 * PI)) * (PI - Math2.asinh(tan((lat))));
    }

    /**
     * Returns the longitude in radians of the point of interest
     *
     * @param x x-coordinate in the Web Mercator system
     * @return the longitude
     */
    public static double lon(double x) {
        return (2 * PI * x - PI);
    }

    /**
     * Returns the latitude of the point of interest
     *
     * @param y : y-coordinate in the Web Mercator system
     * @return the latitude
     */
    public static double lat(double y) {
        return atan(sinh(PI - 2 * PI * y));
    }

}
