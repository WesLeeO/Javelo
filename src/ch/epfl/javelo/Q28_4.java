package ch.epfl.javelo;

/**
 * Q28_4
 * Convert numbers between Q28.4 representation and other ones.
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public final class Q28_4 {
    private Q28_4() {
    }

    /**
     * Returns the Q28.4 representation of the given input
     * (shifted by 4 decimal places left)
     *
     * @param i : input integer
     * @return an integer
     */
    public static int ofInt(int i) {
        i = i << 4;
        return i;
    }

    /**
     * Converts the Q28_4 representation of a number to a double
     *
     * @param q28_4 : integer
     * @return the double value
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -4);
    }

    /**
     * Converts the Q28_4 representation of a number to a float
     *
     * @param q28_4 :  integer
     * @return the float value
     */
    public static float asFloat(int q28_4) {
        return Math.scalb((float) q28_4, -4);
    }
}
