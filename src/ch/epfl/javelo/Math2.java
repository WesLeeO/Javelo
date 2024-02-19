package ch.epfl.javelo;


/**
 * Math2
 * This class offers mathematical tools
 * which are used in different classes
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public final class Math2 {

    private Math2() {
    }

    /**
     * @param x : integer to be divided
     * @param y : integer to be divided by
     * @return the integer part by excess of the division of x by y
     * @throws IllegalArgumentException if x < 0 or y <= 0
     */
    public static int ceilDiv(int x, int y) {
        Preconditions.checkArgument(x >= 0 && y > 0);
        return ((x + y - 1) / y);
    }

    /**
     * @param y0 : y-coordinate of the first point belonging to the line
     * @param y1 : y-coordinate of the second point belonging to the line
     * @param x  : x-coordinate of the point of interest
     * @return the y-coordinate of the point of interest that belongs to the line
     * passing through the points (0,y0) and (1,y1)
     */
    public static double interpolate(double y0, double y1, double x) {
        double slope = y1 - y0;
        return Math.fma(slope, x, y0);
    }


    /**
     * @param min : lower bound
     * @param v   : value of interest
     * @param max : upper bound
     * @return the value of v restricted to the interval [min, max]
     * @throws IllegalArgumentException if min > max
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        v = Math.max(v, min);
        v = Math.min(v, max);
        return v;
    }

    /**
     * @param min : lower bound
     * @param v   : value of interest
     * @param max : upper bound
     * @return the value of v restricted to the interval [min, max]
     * @throws IllegalArgumentException if min > max
     */
    public static double clamp(double min, double v, double max) {
        Preconditions.checkArgument(min <= max);
        v = Math.max(v, min);
        v = Math.min(v, max);
        return v;
    }

    /**
     * @param x : double value (input)
     * @return the value of the function asinh evaluated at the point of coordinate x
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + Math.pow(x, 2)));
    }

    /**
     * @param uX : x-coordinate of the first vector
     * @param uY : y-coordinate of the first vector
     * @param vX : x-coordinate of the second vector
     * @param vY : y-coordinate of the second vector
     * @return the dot product between two vectors
     */
    public static double dotProduct(double uX, double uY, double vX, double vY) {
        return uX * vX + uY * vY;
    }

    /**
     * @param uX : x-coordinate of the vector
     * @param uY : y-coordinate of the vector
     * @return the norm to the square of the vector of interest
     */
    public static double squaredNorm(double uX, double uY) {
        return dotProduct(uX, uY, uX, uY);
    }

    /**
     * @param uX : x-coordinate of the vector
     * @param uY : y-coordinate of the vector
     * @return the norm of a vector
     */
    public static double norm(double uX, double uY) {
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * @param aX : x-coordinate of the point A
     * @param aY : y-coordinate of the point A
     * @param bX : x-coordinate of the point B
     * @param bY : y-coordinate of the point B
     * @param pX : x-coordinate of the point P
     * @param pY : y-coordinate of the point P
     * @return which returns the length of the projection of the vector
     * from point A to point P on the vector from point A to point B
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX, double pY) {
        double apX = pX - aX;
        double apY = pY - aY;
        double abX = bX - aX;
        double abY = bY - aY;
        return dotProduct(apX, apY, abX, abY) / norm(abX, abY);
    }

}


