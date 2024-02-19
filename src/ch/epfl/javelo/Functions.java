package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * Functions
 * This class implements multiple mathematical functions
 *
 * @author David Farah (341017)
 * @author Wesley Nana Davies(344592)
 */
public final class Functions {
    private Functions() {
    }

    /**
     * Returns a constant function
     *
     * @param y : constant which is the output of the function to be returned
     * @return a constant function
     */
    public static DoubleUnaryOperator constant(double y) {
        return x -> y;
    }


    /**
     * Returns a function obtained by linear interpolation between samples,
     * regularly spaced and covering the range from 0 to xMax
     *
     * @param samples : array of samples to make the function out of it
     * @param xMax    : end of the range
     * @return the function
     * @throws IllegalArgumentException if the size of the samples array is not
     *                                  greater or equal to 2 or if xMax is negative
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        Preconditions.checkArgument(samples.length >= 2 && xMax > 0);
        return new Sampled(samples, xMax);
    }

    private static final class Sampled implements DoubleUnaryOperator {
        private final float[] samples;
        private final double xMax;

        public Sampled(float[] samples, double xMax) {
            this.samples = samples;
            this.xMax = xMax;
        }

        @Override
        public double applyAsDouble(double operand) {

            if (operand >= xMax) {
                return samples[samples.length - 1];
            } else if (operand < 0) {
                return samples[0];
            } else {

                // length of an interval
                double intervalLength = xMax / (samples.length - 1);

                double operand_index = operand / intervalLength;

                //get the 2 sample indexes that are closest to the operand
                int lower_index = (int) Math.floor(operand_index);
                int upper_index = (int) Math.ceil(operand_index);

                return Math2.interpolate(samples[lower_index], samples[upper_index],
                        (operand - lower_index * intervalLength) / intervalLength);

            }

        }
    }

}
