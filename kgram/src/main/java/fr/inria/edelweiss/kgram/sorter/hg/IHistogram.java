package fr.inria.edelweiss.kgram.sorter.hg;

/**
 * Interface for implementing histogram
 * 
 * @author Fuqi Song, WImmics Inria I3S
 * @date 21 mai 2014
 */
public interface IHistogram {

    public void add(Object index, Object value);

    public int get(Object index, Object value);

    public int get(Object value);
}
