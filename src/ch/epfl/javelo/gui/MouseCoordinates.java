package ch.epfl.javelo.gui;

/**
 * MouseCoordinates class (used in WayPointsManager)
 * Class representing x and y coordinates of the mouse
 *
 * @author David Farah (341017)
 */
public class MouseCoordinates {
    private double x;
    private double y;

    /**
     * Constructor
     *
     * @param x current x coordinate
     * @param y current y coordinate
     */
    MouseCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Getter for x
     *
     * @return x
     */
    public double getX() {
        return x;
    }

    /**
     * Set the value of x
     *
     * @param x current x coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Getter for y
     *
     * @return y
     */
    public double getY() {
        return y;
    }

    /**
     * Set the value of y
     *
     * @param y current y coordinate
     */
    public void setY(double y) {
        this.y = y;
    }
}
