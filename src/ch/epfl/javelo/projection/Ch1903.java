package ch.epfl.javelo.projection;

import static java.lang.Math.*;


/**
 * Ch1903 convention coordinates
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public final class Ch1903 {

    private Ch1903() {
    }

    /**
     * Returns the east coordinate of a point in the Swiss Coordinate system
     *
     * @param lon : longitude to convert (RAD)
     * @param lat : latitude to convert (RAD)
     * @return the east coordinate
     */
    public static double e(double lon, double lat) {
        // Conversion in degrees
        lon = toDegrees(lon);
        lat = toDegrees(lat);

        double lambda1 = pow(10, -4) * (3_600 * lon - 26_782.5);
        double phi1 = pow(10, -4) * (3_600 * lat - 169_028.66);
        return 2_600_072.37
                + 211_455.93 * lambda1
                - 10_938.51 * lambda1 * phi1
                - 0.36 * lambda1 * pow(phi1, 2)
                - 44.54 * pow(lambda1, 3);
    }

    /**
     * Returns the north coordinate of a point in the Swiss Coordinate system
     *
     * @param lon : longitude to convert (RAD)
     * @param lat : latitude to convert (RAD)
     * @return the north coordinate
     */
    public static double n(double lon, double lat) {
        // Conversion in degrees
        lon = toDegrees(lon);
        lat = toDegrees(lat);

        double lambda1 = pow(10, -4) * (3_600 * lon - 26_782.5);
        double phi1 = pow(10, -4) * (3_600 * lat - 169_028.66);
        return 1_200_147.07
                + 308_807.95 * phi1
                + 3_745.25 * pow(lambda1, 2)
                + 76.63 * pow(phi1, 2)
                - 194.56 * pow(lambda1, 2) * phi1
                + 119.79 * pow(phi1, 3);
    }

    /**
     * Returns the longitude in radians of a given point (PointCh)
     *
     * @param e : east coordinate
     * @param n : north coordinate
     * @return the longitude
     */
    public static double lon(double e, double n) {
        double x = pow(10, -6) * (e - 2_600_000);
        double y = pow(10, -6) * (n - 1_200_000);
        double lambda0 = 2.6779094
                + 4.728982 * x
                + 0.791484 * x * y
                + 0.1306 * x * pow(y, 2)
                - 0.0436 * pow(x, 3);

        double longitude = lambda0 * 100 / 36;
        return toRadians(longitude);
    }

    /**
     * Returns the latitude in radians of a given point (PointCh)
     *
     * @param e : east coordinate
     * @param n : north coordinate
     * @return the latitude
     */
    public static double lat(double e, double n) {
        double x = pow(10, -6) * (e - 2_600_000);
        double y = pow(10, -6) * (n - 1_200_000);
        double phi0 = 16.9023892
                + 3.238272 * y
                - 0.270978 * pow(x, 2)
                - 0.002528 * pow(y, 2)
                - 0.0447 * pow(x, 2) * y
                - 0.0140 * pow(y, 3);
        double latitude = phi0 * 100 / 36;
        return toRadians(latitude);
    }
}

