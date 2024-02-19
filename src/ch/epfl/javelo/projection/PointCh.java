package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import static java.lang.Math.abs;

/**
 * PointCh record
 *
 * @param e east coordinate (meters)
 * @param n north coordinate (meters)
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public record PointCh(double e, double n) {

    /**
     * @throws IllegalArgumentException if the point with the given coordinates
     *                                  is not contained within the Swiss bounds
     */
    public PointCh {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * Returns the squared distance between two points (PointCh)
     *
     * @param that : point of interest
     * @return the square of the distance
     */
    public double squaredDistanceTo(PointCh that) {
        double x = that.e() - this.e;
        double y = that.n() - this.n;
        return Math2.squaredNorm(x, y);
    }

    /**
     * Returns the distance between two points (PointCh)
     *
     * @param that : point of interest
     * @return the distance between the two points
     */
    public double distanceTo(PointCh that) {
        double distanceE = abs(this.e - that.e);
        double distanceN = abs(this.n - that.n);
        return Math.hypot(distanceE, distanceN);
    }


    /**
     * Returns the longitude in radians of the point (PointCH)
     *
     * @return longitude in rad W... system (RAD)
     */
    public double lon() {
        return Ch1903.lon(e, n);
    }

    /**
     * Returns the latitude in radians of the point (PointCH)
     *
     * @return latitude in rad W... system (RAD)
     */
    public double lat() {
        return Ch1903.lat(e, n);
    }

}
