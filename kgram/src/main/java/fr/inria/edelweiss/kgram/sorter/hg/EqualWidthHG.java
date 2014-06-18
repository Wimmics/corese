package fr.inria.edelweiss.kgram.sorter.hg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Equi-width histogram
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 21 mai 2014
 */
public class EqualWidthHG implements IHistogram {

    private double width = -1;

    private final double min, max;
    private final Map<Object, List<Bucket>> hg = new HashMap<Object, List<Bucket>>();
    private final List<Bucket> buckets = new ArrayList<Bucket>();
    private int noOfBuckets = -1;

    public EqualWidthHG(int size, double min, double max) {
        this.noOfBuckets = size;

        this.max = max;
        this.min = min;
        width = (max - min) / size;
        //equal width
        for (int i = 0; i < size; i++) {
            //[0, 5) [5, 10)...[95, 100]
            Bucket itl = new Bucket(min + (i) * width, min + (i + 1) * width);
            buckets.add(itl);
        }
    }

    //add a pair (index, value) to the histo gram
    @Override
    public void add(Object index, Object value) {
        if (!hg.containsKey(index)) {
            hg.put(index, buckets);
        }

        // get the interval object and +1
        int id = id(value);
        Bucket itl = buckets.get(id);
        itl.setSize();
        // add the index to the indexing of this interval
        if (null == itl.getIndex(index)) {
            itl.addIndex(index);
        }
    }

    public void add(Object value) {
        // get the interval object and +1
        int id = id(value);
        Bucket itl = buckets.get(id);
        itl.setSize();
    }

    //get the number of values in a specific bins by given (index, value)
    @Override
    public int get(Object index, Object value) {
        if (hg.containsKey(index)) {
            return hg.get(index).get(id(value)).getSize();
        }
        return -1;
    }

    @Override
    public int get(Object value) {
        return this.buckets.get(id(value)).getSize();
    }

    public Map getHG() {
        return this.hg;
    }

    public List getHGWithoutIndex() {
        return this.buckets;
    }

    private int id(Object value) {
        long hash = Long.valueOf(Integer.MAX_VALUE) + value.hashCode();
        return (int) (hash % this.noOfBuckets);
    }

    public String stats() {
        String s = "";
        s += hg.size() + " list of buckets[size:" + this.buckets.size() + "]";
        int to = 0;
        for (Bucket bk : buckets) {
            to += bk.getSize();
            //System.out.println(bk.getSize()+":"+bk.index.size());
        }

        System.out.print(to);
        return s;
    }
}
