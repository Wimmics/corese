package fr.inria.corese.core.stats.data;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import static fr.inria.corese.core.stats.IStats.OBJECT;
import static fr.inria.corese.core.stats.IStats.PREDICATE;
import static fr.inria.corese.core.stats.IStats.SUBJECT;
import fr.inria.corese.core.stats.Options;
import java.util.HashMap;
import java.util.Map;

/**
 * TripleHashTable.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 20 juin 2014
 */
public class TripleHashTable {

    private final static int SUB = 0, OBJ = 1;

    private double[] options = Options.DEF_PARA_HTT;

    //Size of table = Max * size of predicates * Max (500)
    private final Map<Triple, Integer> table = new HashMap<Triple, Integer>();

    public TripleHashTable(double[] options) {
        if (options != null) {
            this.options = options;
        }
    }

    public TripleHashTable(double[] options, int noOfResources, int noOfObjects) {
        this(options);
        this.setOptions(noOfResources, noOfObjects);
    }

    public final void setOptions(int noOfResources, int noOfObjects) {
        double ratio = noOfResources * 1.0 / noOfObjects;
        double max = Options.DEF_PARA_HTT[0];
        if (ratio > 1.0) {
            this.options[OBJ] = max / (ratio);
        } else {
            this.options[SUB] = max * (ratio);
        }
    }

    private long hash(Node n, int mod) {
        //todo: change to unique hash code...
        long h = Long.valueOf(Integer.MAX_VALUE) + n.getLabel().hashCode();
        if (mod == 0 || mod == 1) {
            return h;
        }
        return (h % mod) + 1;
    }

    public void add(Edge e) {
        Triple t = new Triple(e);

        if (table.containsKey(t)) {
            table.put(t, table.get(t) + 1);
        } else {
            table.put(new Triple(e), 1);
        }
    }

    public int get(Edge e, int type) {
        Triple other = new Triple(e);
        int count = 0;
        for (Triple t : this.table.keySet()) {
            if (t.match(other, type)) {
                count += this.table.get(t);
            }
        }
        return count;
        //return table.containsKey(t) ? table.get(t) : 0;
    }

    class Triple {

        private final String index;
        private final long s, o;
        private final String p;

        Triple(Edge e) {
            this.s = hash(e.getNode(0), (int) options[SUB]);
            Node en = e.getEdgeVariable() == null ? e.getEdgeNode() : e.getEdgeVariable();
            this.p = en.getLabel();
            this.o = hash(e.getNode(1), (int) options[OBJ]);

            //index: sss-ppppp-ooo
            this.index = s + "-" + p + "-" + o;
        }

        public String getIndex() {
            return index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            final Triple other = (Triple) obj;
            boolean es = this.s == other.s;
            boolean ep = (this.p == null) ? (other.p == null) : this.p.equals(other.p);
            boolean eo = this.o == other.o;

            //(s ? o) | (? p o) | (s p ?)
//            if ((es && eo) || (ep && eo) || (es && ep)) {
//                return true;
//            }
            //(s p o)
            return es && ep && eo;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + (int) (this.s ^ (this.s >>> 32));
            hash = 53 * hash + (int) (this.o ^ (this.o >>> 32));
            hash = 53 * hash + (this.p != null ? this.p.hashCode() : 0);
            return hash;
        }

        public boolean match(Triple other, int type) {
            boolean es = this.s == other.s;
            boolean ep = (this.p == null) ? (other.p == null) : this.p.equals(other.p);
            boolean eo = this.o == other.o;

            switch (type) {
                case SUBJECT:
                    return ep && eo;
                case PREDICATE:
                    return es && eo;
                case OBJECT:
                    return es && ep;
                default:
                    return false;
            }
        }

    }
}
