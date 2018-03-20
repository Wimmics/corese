package fr.inria.corese.core.stats.data;

import fr.inria.corese.kgram.api.core.Node;

/**
 * Simple average, number of all triples/number of distinct resources
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 10 juin 2014
 */
public class SimpleAverage extends BaseMap {

    @Override
    public int get(Node n) {
        return total / size();
    }
}
