package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Q28_4;

import java.nio.IntBuffer;


/**
 * GraphNodes record
 *
 * @param buffer : containing all the information about the nodes
 *               (coordinates, number of edges going out, ID of a certain edge)
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public record GraphNodes(IntBuffer buffer) {
    private static final int OFFSET_E = 0;
    private static final int OFFSET_N = OFFSET_E + 1;
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
    private static final int NUMBER_OF_INTS_PER_NODE = OFFSET_OUT_EDGES + 1;  //number of integers needed to represent a node, i.e. 3


    /**
     * Returns the total number of nodes
     *
     * @return the number of nodes
     */
    public int count() {
        return buffer.capacity() / NUMBER_OF_INTS_PER_NODE;
    }

    /**
     * Returns the east coordinate of the node
     *
     * @param nodeId : ID of the node
     * @return the east coordinate
     */
    public double nodeE(int nodeId) {
        int nodeIndex = nodeId * NUMBER_OF_INTS_PER_NODE + OFFSET_E;
        return Q28_4.asDouble(buffer.get(nodeIndex));
    }

    /**
     * Returns the north coordinate of the node
     *
     * @param nodeId ID of the node
     * @return the north coordinate
     */
    public double nodeN(int nodeId) {
        int nodeIndex = nodeId * NUMBER_OF_INTS_PER_NODE;
        return Q28_4.asDouble(buffer.get(nodeIndex + OFFSET_N));
    }

    /**
     * Returns the number of edges going out from the given node
     *
     * @param nodeId : ID of the node
     * @return the number of edges
     */
    public int outDegree(int nodeId) {
        int nodeIndex = nodeId * NUMBER_OF_INTS_PER_NODE;
        return Bits.extractUnsigned(buffer.get(nodeIndex + OFFSET_OUT_EDGES), 28, 4);
    }

    /**
     * Returns the identity of the edgeIndex-th edge going out from the given node
     *
     * @param nodeId    : ID of the node
     * @param edgeIndex : index of the edge for the given node
     * @return the identity of the edgeIndex-th edge
     */
    public int edgeId(int nodeId, int edgeIndex) {
        assert 0 <= edgeIndex && edgeIndex < outDegree(nodeId);
        int nodeIndex = nodeId * NUMBER_OF_INTS_PER_NODE;
        return Bits.extractUnsigned(buffer.get(nodeIndex + OFFSET_OUT_EDGES), 0, 28) + edgeIndex;
    }
}
