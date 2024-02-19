package ch.epfl.javelo.data;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * GraphSectors record
 *
 * @param buffer : containing information about all sectors (number of nodes, start node ID)
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public record GraphSectors(ByteBuffer buffer) {
    private static final int OFFSET_NODE_ID = 0;
    private static final int OFFSET_NUMBER_OF_NODES = OFFSET_NODE_ID + Integer.BYTES; // 4
    private static final int NUMBER_OF_BYTES_PER_SECTOR = OFFSET_NUMBER_OF_NODES + Short.BYTES; // 6
    private static final int SUBDIVISIONS_PER_SIDE = 128;
    private static final double SECTOR_WIDTH = SwissBounds.WIDTH / SUBDIVISIONS_PER_SIDE;
    private static final double SECTOR_HEIGHT = SwissBounds.HEIGHT / SUBDIVISIONS_PER_SIDE;

    /**
     * Returns a list of sectors which are within the given distance from the point.
     *
     * @param center   : point of interest (PointCh)
     * @param distance :  maximal distance (meters)
     * @return a list of sectors
     * @throws IllegalArgumentException if the distance is negative
     */
    public List<Sector> sectorsInArea(PointCh center, double distance) {

        Preconditions.checkArgument(distance >= 0);
        // getting the coordinates of the summits of the square which defines the range
        double upper_left_x = center.e() - SwissBounds.MIN_E - distance;
        double upper_left_y = center.n() - SwissBounds.MIN_N + distance;
        double lower_right_x = center.e() - SwissBounds.MIN_E + distance;
        double lower_right_y = center.n() - SwissBounds.MIN_N - distance;

        // if the bounds are exceeded, taking into the account the extremes of the Swiss Coordinates

        if (upper_left_x < 0) {
            upper_left_x = 0;
        }
        if (lower_right_y < 0) {
            lower_right_y = 0;
        }
        if (upper_left_y >= SwissBounds.HEIGHT) {
            upper_left_y = SwissBounds.HEIGHT - 0.1;
        }
        if (lower_right_x >= SwissBounds.WIDTH) {
            lower_right_x = SwissBounds.WIDTH - 0.1;
        }

        // defining some indexes along the horizontal and vertical axes which will be
        // needed to compute the actual index of every sector
        int xMin = (int) Math.floor(upper_left_x / SECTOR_WIDTH);
        int yMin = (int) Math.floor(lower_right_y / SECTOR_HEIGHT);
        int xMax = (int) Math.floor(lower_right_x / SECTOR_WIDTH);
        int yMax = (int) Math.floor(upper_left_y / SECTOR_HEIGHT);


        ArrayList<Sector> sectorsInArea = new ArrayList<>();

        // adding the indexes of all the sectors
        // which intersect with square centered at the input (PointCh)
        int sectorIndex;
        int startNodeId;
        int numberOfNodes;
        int endNodeId;
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                sectorIndex = 128 * y + x;
                startNodeId = buffer.getInt(NUMBER_OF_BYTES_PER_SECTOR * sectorIndex + OFFSET_NODE_ID);
                numberOfNodes = Short.toUnsignedInt(
                        buffer.getShort(NUMBER_OF_BYTES_PER_SECTOR * sectorIndex + OFFSET_NUMBER_OF_NODES)
                );
                endNodeId = startNodeId + numberOfNodes;
                sectorsInArea.add(new Sector(startNodeId, endNodeId));
            }
        }
        return sectorsInArea;
    }

    /**
     * This nested record represents a sector
     *
     * @param startNodeId : ID of the first node of the sector
     * @param endNodeId   : ID the end node of the sector (does not belong to the sector)
     */
    public record Sector(int startNodeId, int endNodeId) {
    }
}
