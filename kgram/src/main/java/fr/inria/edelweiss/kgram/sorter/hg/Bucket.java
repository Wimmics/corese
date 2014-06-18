package fr.inria.edelweiss.kgram.sorter.hg;

import java.util.ArrayList;
import java.util.List;

/**
 * Bucket.java Bucket
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 21 mai 2014
 */
public class Bucket {

    private double start, end;//range/interval
    private int frequency = 0;
    //double index;
    List index = new ArrayList();//index to the predicates (or subject, or objects)

    public Bucket(double start, double end) {

        this.start = start;
        this.end = end;
    }

    public Bucket(int size) {
        this.frequency = size;
    }

    public Bucket(double start, double end, int size) {
        this(start, end);
        this.frequency = size;
    }

    public void addIndex(Object o) {
        index.add(o);
    }

    public Object getIndex(Object o) {
        int i = index.indexOf(o);
        if (i == -1) {
            return null;
        }
        return index.get(i);
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public int getSize() {
        return frequency;
    }

    public void setSize(int size) {
        this.frequency = size;
    }

    public void setSize() {
        this.frequency++;
    }
}
