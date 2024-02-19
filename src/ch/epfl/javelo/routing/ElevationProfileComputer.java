package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import java.util.Arrays;


/**
 * ElevationProfileComputer
 *
 * @author David Farah (341017)
 * @author Wesley Nana Davies(344592)
 */
public final class ElevationProfileComputer {
    private static float[] profile;
    private static double intervalLength;

    private ElevationProfileComputer() {
    }

    /**
     * Computes an elevationProfile for a route with maxStepLength
     *
     * @param route         : a route (implementing the interface) representing the itinerary
     * @param maxStepLength : maximal distance between two samples from the profile
     * @return a new Elevation Profile
     * @throws IllegalArgumentException if maxStepLength <= 0
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
        Preconditions.checkArgument(maxStepLength > 0);
        int numberOfSamples = 1 + (int) Math.ceil(route.length() / maxStepLength);
        intervalLength = route.length() / (numberOfSamples - 1);
        profile = new float[numberOfSamples];

        fillInitialArray(route);
        int indexFirstNumber = firstNumberIndex();

        if (indexFirstNumber == profile.length) {
            Arrays.fill(profile, 0);
        } else {
            Arrays.fill(profile, 0, indexFirstNumber, profile[indexFirstNumber]);
            int indexFirstNumberEnd = fillNanEnd();
            fillIntermediateNanValues(indexFirstNumber, indexFirstNumberEnd);
        }

        return new ElevationProfile(route.length(), profile);
    }

    /**
     * Fill the array with the initial values from the route
     *
     * @param route : route of interest
     */
    private static void fillInitialArray(Route route) {
        for (int sample = 0; sample < profile.length; sample++) {
            profile[sample] = (float) route.elevationAt(sample * intervalLength);
        }
    }


    /**
     * returns the index of the first real number
     *
     * @return the index
     */
    private static int firstNumberIndex() {
        int indexFirstNumber = 0;
        while ((indexFirstNumber < profile.length && Float.isNaN(profile[indexFirstNumber]))) {
            indexFirstNumber++;
        }
        return indexFirstNumber;
    }


    /**
     * Returns the index of the first real number starting from the end of the array
     *
     * @return the index
     */
    private static int fillNanEnd() {
        int indexFirstNumberEnd = profile.length - 1;
        while (Float.isNaN(profile[indexFirstNumberEnd])) {
            indexFirstNumberEnd--;
        }
        Arrays.fill(profile, indexFirstNumberEnd + 1, profile.length, profile[indexFirstNumberEnd]);
        return indexFirstNumberEnd;
    }


    /**
     * Fill the remaining NaN values in the array
     *
     * @param indexFirstNumber    : index where we start on the array
     * @param indexFirstNumberEnd : index where we stop on the array
     */
    private static void fillIntermediateNanValues(int indexFirstNumber, int indexFirstNumberEnd) {
        int firstNanIndex = indexFirstNumber;
        int realNumberAfterIndex;
        int realNumberBeforeIndex;

        while (firstNanIndex < indexFirstNumberEnd) {
            firstNanIndex++;

            //skip index if it's a real number
            if (!Float.isNaN(profile[firstNanIndex])) {
                continue;
            }

            realNumberBeforeIndex = firstNanIndex - 1;
            realNumberAfterIndex = realNumberBeforeIndex + 2;

            // finding the next real number after the hole
            while (Float.isNaN(profile[realNumberAfterIndex])) {
                realNumberAfterIndex++;
            }

            // doing the interpolation for all NaN samples using the valid samples at the extremities of the hole
            for (int NanIndex = firstNanIndex; NanIndex < realNumberAfterIndex; NanIndex++) {
                profile[NanIndex] = (float) Math2.interpolate(
                        profile[realNumberBeforeIndex],
                        profile[realNumberAfterIndex],
                        (double) (NanIndex - realNumberBeforeIndex) /
                                (realNumberAfterIndex - realNumberBeforeIndex)
                );
            }

        }
    }

}






