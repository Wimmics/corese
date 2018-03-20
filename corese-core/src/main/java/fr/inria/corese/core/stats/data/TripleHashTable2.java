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
public class TripleHashTable2 {

    private final static int SUB = 0, OBJ = 1;

    private double[] options = Options.DEF_PARA_HTT;

    //Size of table = Max * size of predicates * Max (500)
    private final Map<Tuple, Integer> ps = new HashMap<Tuple, Integer>();
    private final Map<Tuple, Integer> po = new HashMap<Tuple, Integer>();
    private final Map<Tuple, Integer> so = new HashMap<Tuple, Integer>();

    public TripleHashTable2(double[] options) {
        if (options != null) {
            this.options = options;
        }
    }

    public TripleHashTable2(double[] options, int noOfResources, int noOfObjects) {
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
        this.add(ps, e, OBJECT);
        this.add(po, e, SUBJECT);
        this.add(so, e, PREDICATE);
    }

    private void add(Map<Tuple, Integer> map, Edge e, int type) {
        Tuple t = new Tuple(e, type);
        if (map.containsKey(t)) {
            map.put(t, map.get(t) + 1);
        } else {
            map.put(new Tuple(e, type), 1);
        }
    }

    public int get(Edge e, int type) {
        Integer count;
        switch (type) {
            case SUBJECT:
                count = po.get(new Tuple(e, SUBJECT));
                break;
            case PREDICATE:
                count = so.get(new Tuple(e, PREDICATE));
                break;
            case OBJECT:
                count = ps.get(new Tuple(e, OBJECT));
                break;
            default:
                count = 0;
        }
        return count == null ? 0 : count;
    }

    class Tuple {

        private final Object x, y;
        //private final int type;

        Tuple(Edge e, int type) {
            long s = hash(e.getNode(0), (int) options[SUB]);
            Node en = e.getEdgeVariable() == null ? e.getEdgeNode() : e.getEdgeVariable();
            String p = en.getLabel();
            long o = hash(e.getNode(1), (int) options[OBJ]);

            switch (type) {
                case SUBJECT:
                    this.x = p;
                    this.y = o;
                    break;
                case PREDICATE:
                    this.x = s;
                    this.y = o;
                    break;
                case OBJECT:
                    this.x = s;
                    this.y = p;
                    break;
                default:
                    x = null;
                    y = null;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            final Tuple other = (Tuple) obj;
            boolean eq = x.getClass().equals(other.x.getClass())
                    && y.getClass().equals(other.y.getClass());
            if (!eq) {
                return false;
            }

            boolean ex, ey;
            if (this.x instanceof Long && other.x instanceof Long) {
                ex = this.x == other.x;
            } else {
                ex = (this.x == null) ? (other.x == null) : this.x.equals(other.x);
            }

            if (this.y instanceof Long && other.y instanceof Long) {
                ey = this.y == other.y;
            } else {
                ey = (this.y == null) ? (other.y == null) : this.y.equals(other.y);
            }

            return ex && ey;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            if (x instanceof Integer) {
                hash = 53 * hash + (int) ((Long) this.x ^ ((Long) this.x >>> 32));
            } else {
                hash = 53 * hash + (this.x != null ? this.x.hashCode() : 0);
            }

            if (y instanceof Integer) {
                hash = 53 * hash + (int) ((Long) this.y ^ ((Long) this.y >>> 32));
            } else {
                hash = 53 * hash + (this.y != null ? this.y.hashCode() : 0);
            }

            return hash;
        }

//        public boolean match(Triple other, int type) {
//            boolean es = this.s == other.s;
//            boolean ep = (this.p == null) ? (other.p == null) : this.p.equals(other.p);
//            boolean eo = this.o == other.o;
//
//            switch (type) {
//                case SUBJECT:
//                    return ep && eo;
//                case PREDICATE:
//                    return es && eo;
//                case OBJECT:
//                    return es && ep;
//                default:
//                    return false;
//            }
//        }
    }
}
