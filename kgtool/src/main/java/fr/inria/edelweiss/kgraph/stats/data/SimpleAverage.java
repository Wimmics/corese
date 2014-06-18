
package fr.inria.edelweiss.kgraph.stats.data;

import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * SimpleAverage.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 10 juin 2014
 */
public class SimpleAverage extends BaseMap {

    @Override
    public int get(Node n) {
        return this.get(n.getLabel());
    }

    @Override
    public int get(String s) {
        return total / size();
    }

}
