package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 * Interface for estimating the selectivity of BP node in a given BP Graph
 *
 * @author Fuqi Song, WImmics Inria I3S
 * @date 19 mai 2014
 */
public interface IEstimateSelectivity {

    //value of selectivity [0, 1], when selected results cover all data set, then 
    //selectivity =1.0 (max), when no selected results, sel =1
    //when only one triple is selected, is approximate minimum
    public final static double MAX_SEL = 1.0;
    public final static double MIN_SEL = 0.0;
    //approximate minimum value, but not equal to 0
    public final static double MIN_SEL_APP = 1.0 / Double.MAX_VALUE;

    /**
     * Estimate and assign the selectvity (or other criteria) for each node in
     * the given BP graph
     *
     * @param plein graph
     * @param producer
     */
    public void estimate(BPGraph plein, Producer producer);
}
