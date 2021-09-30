package fr.inria.corese.kgram.sorter.core;

import fr.inria.corese.kgram.api.query.Producer;

/**
 * Interface for estimating the cost of nodes and edges in QPGraph
 *
 * @author Fuqi Song, WImmics Inria I3S
 * @date 19 mai 2014
 */
public interface IEstimate {

    public final static double MAX_COST = 1.0;
    public final static double MIN_COST = 0.0;
    //approximate minimum value, but not equal to 0
    public final static double MIN_COST_0 = 1.0 / Double.MAX_VALUE;
    public final static double NA_COST = -1;

    /**
     * Estimate and assign the selectvity (or other criteria) for each node in
     * the given BP graph
     *
     * @param plein graph
     * @param producer producer
     * @param parameters parameters for different implementations
     */
    public void estimate(QPGraph plein, Producer producer, Object parameters);
    
}
