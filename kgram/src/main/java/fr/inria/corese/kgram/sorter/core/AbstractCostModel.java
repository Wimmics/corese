package fr.inria.corese.kgram.sorter.core;

import java.util.List;

/**
 * CostModel.java 
 * Cost model used to estimate the cost for node or edge in a QPG
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 20 oct. 2014 added
 */
public abstract class AbstractCostModel {

    


    /**
     * Abstract method for estimating the cost using the model
     * 
     * @param params 
     */
    public abstract void estimate(List<Object> params);
    
    
    /**
     * Check if the paramters for method estimate(List params) are ok
     * 
     * @param params
     * @return true:ok, false: not ok
     */
    public abstract boolean isParametersOK(List<Object> params);
    
    /**
     * Check whether the cost can be estimated using the model
     * 
     * @return true:ok, false: not ok
     */
    public abstract boolean estimatable();
    
}
