package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Interface for calling Producer from kgraph, such as get stats data, etc..
 *
 * @author Fuqi Song, WImmics Inria I3S
 * @date 4 juin 2014
 */
public interface IProducer {

    public static final int SUBJECT = 1;
    public static final int PREDICATE = 2;
    public static final int OBJECT = 3;
    public static final int NA = 0;

    int getAllTriplesNumber();

    int getResourceNumber();

    int getPropertyNumber();

    int getObjectNumber();

    int getCountByValue(String val, int type);

    int getCountByTriple(Edge e);

    boolean enabled();

    boolean isURI(Node n);
}
