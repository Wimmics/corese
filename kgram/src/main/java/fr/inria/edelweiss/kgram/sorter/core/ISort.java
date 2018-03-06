package fr.inria.corese.kgram.sorter.core;

import fr.inria.corese.kgram.core.Exp;
import java.util.List;

/**
 * Interface for sorting and rewriting the QPG nodes
 * 
 * @author Fuqi Song, WImmics Inria I3S
 * @date 19 mai 2014
 */
public interface ISort {

    /**
     * Sort the QPG node
     *
     * @param unsorted graph
     * @return List of sorted QPG Node
     */
    public List<QPGNode> sort(QPGraph unsorted);

    /**
     * Rewrite the SPARQL exp according to give order of nodes
     * @param exp
     * @param nodes 
     * @param start 
     */
    public void rewrite(Exp exp, List<QPGNode> nodes, int start);
}
