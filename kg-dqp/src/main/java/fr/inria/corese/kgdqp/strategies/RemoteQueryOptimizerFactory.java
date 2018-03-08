/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.strategies;


/**
 * A factory to construct edge request optimizers
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteQueryOptimizerFactory {
    
    public static RemoteQueryOptimizer createSimpleOptimizer() {
        return new RemoteQueryOptimizerSimple();
    }
    
    public static RemoteQueryOptimizer createFilterOptimizer() {
        return new RemoteQueryOptimizerFilter();
    }
    
    public static RemoteQueryOptimizer createBindingOptimizer() {
        return new RemoteQueryOptimizerBinding();
    }
    
    public static RemoteQueryOptimizer createFullOptimizer() {
        return new RemoteQueryOptimizerFull();
    }
}
