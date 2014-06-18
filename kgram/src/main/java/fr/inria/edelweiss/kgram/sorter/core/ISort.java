package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.core.Exp;
import java.util.List;

/**
 * Interface for sorting and rewriting the BP node in a given BP graph
 * 
 * @author Fuqi Song, WImmics Inria I3S
 * @date 19 mai 2014
 */
public interface ISort {

    /**
     * Generate a directed graph
     *
     * @param unsorted graph
     * @return directed graph
     */
    public List<BPGNode> sort(BPGraph unsorted);

    public void rewrite(Exp exp, List<BPGNode> nodes);
}
