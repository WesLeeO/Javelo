package ch.epfl.javelo.routing;

/**
 * A CostFunction for a route
 *
 * @author David Farah (341017)
 * @author Wesley Nana Davies(344592)
 */
public interface CostFunction {
    double costFactor(int nodeId, int edgeId);
}
