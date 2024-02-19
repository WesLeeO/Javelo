package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * GraphEdges record
 *
 * @param edgesBuffer : ByteBuffer containing information about all edges
 *                    (direction, identity of the destination node, length, elevation, attributes)
 * @param profileIds  : IntBuffer which contains for each edge,
 *                    the type of the profile and the index of the first sample
 * @param elevations  : ShortBuffer containing all the samples of the profiles,
 *                    compressed or not.
 * @author Wesley Nana Davies (344592)
 * @author David Farah (341017)
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {
    private static final int OFFSET_EDGE_DIRECTION_AND_ID = 0;
    private static final int OFFSET_LENGTH = OFFSET_EDGE_DIRECTION_AND_ID + 4;
    private static final int OFFSET_ELEVATION_GAIN = OFFSET_LENGTH + 2;
    private static final int OFFSET_IDS_OSM = OFFSET_ELEVATION_GAIN + 2;
    private static final int NUMBER_OF_INTS_PER_EDGE = OFFSET_IDS_OSM + 2;

    /**
     * Returns true iff the edge goes in the opposite direction to the OSM road it belongs to
     *
     * @param edgeId : ID of the edge
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isInverted(int edgeId) {
        int edgeIndex = NUMBER_OF_INTS_PER_EDGE * edgeId + OFFSET_EDGE_DIRECTION_AND_ID;
        return edgesBuffer.getInt(edgeIndex) < 0;
    }

    /**
     * Returns the identity of the destination node of the given identity edge
     *
     * @param edgeId : ID of the edge
     * @return the ID of the node
     */
    public int targetNodeId(int edgeId) {
        int edgeIndex = NUMBER_OF_INTS_PER_EDGE * edgeId + OFFSET_EDGE_DIRECTION_AND_ID;
        int edgeInt = edgesBuffer.getInt(edgeIndex);
        return (edgeInt >= 0) ? edgeInt : ~edgeInt;
    }

    /**
     * Returns the length, in meters, of the given identity edge
     *
     * @param edgeId : ID of the edge
     * @return the length of the edge
     */
    public double length(int edgeId) {
        int lengthIndex = NUMBER_OF_INTS_PER_EDGE * edgeId + OFFSET_LENGTH;
        return Q28_4.asDouble(Short.toUnsignedInt(edgesBuffer.getShort(lengthIndex)));
    }

    /**
     * Returns the positive elevation, in meters, of the edge with the given identity
     *
     * @param edgeId : ID of the edge
     * @return the positive elevation
     */
    public double elevationGain(int edgeId) {
        int elevationIndex = NUMBER_OF_INTS_PER_EDGE * edgeId + OFFSET_ELEVATION_GAIN;
        return Q28_4.asDouble(Short.toUnsignedInt(edgesBuffer.getShort(elevationIndex)));
    }

    /**
     * Returns true iff the given edge has a profile.
     *
     * @param edgeId : ID of the edge
     * @return <code>true</code> or <code>false</code>
     */
    public boolean hasProfile(int edgeId) {
        int profileId = profileIds.get(edgeId);
        return Bits.extractUnsigned(profileId, 30, 2) != 0;
    }

    /**
     * Returns the array of samples of the profile of the edge with the given identity,
     * which is empty if the edge does not have a profile
     *
     * @param edgeId : ID of the edge
     * @return the float array
     */
    public float[] profileSamples(int edgeId) {
        if (!hasProfile(edgeId)) {
            return new float[]{};
        }

        int lengthIndex = NUMBER_OF_INTS_PER_EDGE * edgeId + OFFSET_LENGTH;
        int nbSamples = 1 + Math2.ceilDiv(Short.toUnsignedInt(edgesBuffer.getShort(lengthIndex)), Q28_4.ofInt(2));

        ArrayList<Float> profileSamples = new ArrayList<>();
        int idFirstSample = Bits.extractUnsigned(profileIds.get(edgeId), 0, 30);
        profileSamples.add(Q28_4.asFloat(Short.toUnsignedInt(elevations.get(idFirstSample))));

        int profileId = profileIds.get(edgeId);
        switch (Bits.extractUnsigned(profileId, 30, 2)) {
            case 1 -> {
                for (int j = 1; j < nbSamples; j++) {
                    profileSamples.add(
                            Q28_4.asFloat(
                                    Short.toUnsignedInt(
                                            elevations.get(idFirstSample + j)
                                    )
                            )
                    );
                }
            }

            case 2 -> updateArrayFromCompressed(8, nbSamples, idFirstSample, profileSamples);

            case 3 -> updateArrayFromCompressed(4, nbSamples, idFirstSample, profileSamples);
        }

        if (isInverted(edgeId)) {
            Collections.reverse(profileSamples);
        }

        return toArray(profileSamples);
    }

    /**
     * Returns a float array containing the same elements as the list
     *
     * @param list : list containing variables of type Float
     * @return float array
     */
    private float[] toArray(List<Float> list) {
        float[] toArray = new float[list.size()];
        for (int z = 0; z < list.size(); z++) {
            toArray[z] = list.get(z);
        }
        return toArray;
    }

    /**
     * Updates the profile samples list
     *
     * @param bitsPerValue   : bits per value in the compressed format
     * @param nbSamples      : total number of samples on the edge
     * @param idFirstSample  : ID of the first sample
     * @param profileSamples : list that stores the samples (already contains the first one)
     */
    private void updateArrayFromCompressed
    (int bitsPerValue, int nbSamples, int idFirstSample, List<Float> profileSamples) {
        int i = 1;
        int idCounter = 1;
        int bitCounter = 16 - bitsPerValue;

        while (i < nbSamples) {
            short elevationShort = elevations.get(idFirstSample + idCounter);
            while (bitCounter >= 0 && i < nbSamples) {
                profileSamples.add(profileSamples.get(i - 1) +
                        Q28_4.asFloat(Bits.extractSigned(elevationShort, bitCounter, bitsPerValue)));
                bitCounter -= bitsPerValue;
                i += 1;
            }
            bitCounter = 16 - bitsPerValue;
            idCounter++;
        }
    }

    /**
     * Returns the index of the AttributeSet of the given edge.
     *
     * @param edgeId : ID of the edge
     * @return the index
     */
    public int attributesIndex(int edgeId) {
        int attributeSetIndex = NUMBER_OF_INTS_PER_EDGE * edgeId + OFFSET_IDS_OSM;
        return Short.toUnsignedInt(edgesBuffer.getShort(attributeSetIndex));
    }
}
