package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * WayPoint record
 * This record represents a waypoint
 *
 * @param point  : PointCh which represents a Waypoint
 * @param nodeID : nodeID associated to a specific Waypoint
 * @author Wesley Nana Davies (344592)
 * @author David Farah (341017)
 */
public record Waypoint(PointCh point, int nodeID) {

    /**
     * Compare two Waypoints by their corresponding node id
     *
     * @param o : other Waypoint to be compared with <code>this</code>
     * @return true iff the 2 waypoints have the same node ID
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Waypoint) {
            return o.hashCode() == this.hashCode();
        }
        return false;
    }

    /**
     * This method makes the hash code of a Waypoint its node id
     *
     * @return node ID of the Waypoint
     */
    @Override
    public int hashCode() {
        return nodeID;
    }
}
