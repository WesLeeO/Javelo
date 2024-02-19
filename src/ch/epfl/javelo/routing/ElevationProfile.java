package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleUnaryOperator;

/**
 * Elevation Profile of a Route
 *
 * @author David Farah (341017)
 * @author Wesley Nana Davies(344592)
 */

public final class ElevationProfile {
    private final double length;
    private final double ascent;
    private final double descent;
    private final DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
    private final DoubleUnaryOperator profile;


    /**
     * Constructor of the class which builds the elevation profile of an edge
     * using its length and an array of the elevations
     *
     * @param length           : length of the edge
     * @param elevationSamples : elevations of all the samples
     * @throws IllegalArgumentException if the length <= 0 or elevationSamples < 2
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(length > 0 && elevationSamples.length >= 2);
        this.length = length;

        double difference;
        double ascentCalc = 0.0;
        double descentCalc = 0.0;

        for (int i = 0; i < elevationSamples.length - 1; i++) {
            difference = elevationSamples[i + 1] - elevationSamples[i];
            if (difference > 0) {
                ascentCalc += difference;
            } else if (difference < 0) {
                descentCalc += difference;
            }
        }

        ascent = ascentCalc;
        descent = descentCalc;

        for (float number : elevationSamples) {
            statistics.accept(number);
        }
        profile = Functions.sampled(elevationSamples, length);
    }


    /**
     * Returns the length of the profile (meters)
     *
     * @return the length
     */
    public double length() {
        return length;
    }


    /**
     * returns the minimum value among the altitudes
     *
     * @return the min value
     */
    public double minElevation() {
        return statistics.getMin();
    }

    /**
     * returns the maximum value among the altitudes
     *
     * @return the max value
     */
    public double maxElevation() {
        return statistics.getMax();
    }

    /**
     * Returns the sum of all positive differences between a sample and its predecessor.
     *
     * @return the totalAscent
     */
    public double totalAscent() {
        return ascent;
    }

    /**
     * Returns the positive value of the sum of all negative differences between a sample and its predecessor.
     *
     * @return the totalDescent (positive)
     */
    public double totalDescent() {
        return Math.abs(descent);
    }

    /**
     * Returns the value of the function sampled (class Function) applied for the x-input position.
     *
     * @param position : position of interest (in meters)
     * @return the elevation at <code>position</code>
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }

}
