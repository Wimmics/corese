package fr.inria.edelweiss.kgram.sorter.core;

/**
 * Interface for getting statistics data from a graph
 *
 * @author Fuqi Song, WImmics Inria I3S
 * @date 4 juin 2014
 */
public interface IStatistics {

    public static final int SUBJECT = 0;
    public static final int PREDICATE = 1;
    public static final int OBJECT = 2;

    int getAllTriplesNumber();

    int getResourceNumber();

    int getPropertyNumber();

    int getObjectNumber();

    int getCountByValue(String val, int type);
}
