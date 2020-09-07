package fr.inria.corese.core.logic;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;

/**
 * Semantic distance & similarity with Corese 2.4 Algorithm Extended to property
 * hierarchy distance: kg:pSimilarity
 *
 * PRAGMA: need a graph with Node set/get Depth need a root class, default is
 * rdfs:Resource or owl:Thing
 *
 * If a class is not subClassOf root, depth simulate to 1 Exploit owl:sameAs &
 * owl:equivalentClass/Property
 *
 * Generalized to property hierarchy with a step of 1/8^n instead of 1/2^n steps
 * can be changed with pragma: kg:similarity kg:cstep 3.0 kg:similarity kg:pstep
 * 4.0
 *
 * TODO: Expensive with Entailment because of rdf:type generated rdfs:domain and
 * rdfs:range A resource may have several types, in Corese 2.4 types were
 * synthetized into one type
 *
 * @author Olivier Corby & Fabien Gandon, Edelweiss INRIA 2011
 */
public class Distance {
    // property step is: 1 / 8^n

    private static double PSTEP = 8;
    // class step is: 1 / 2^n
    public static double CSTEP = 2;

    private static int FIRST_STEP = 5;

    private Graph graph;
    Node root, subEntityOf, sameAs, equivAs;
    NodeList topList;
    List<Node> topLevel;
    Integer MONE = new Integer(-1);
    Integer ONE = new Integer(1);

    int depthMax = 0;
    private double step = CSTEP;
    double K = 1, dmax = 0;
    boolean isProperty = false,
            isExtended = true,
            hasSame = false,
            hasEquiv = false;
    
    private String subClassOf = RDFS.SUBCLASSOF;
    private String broader    = SKOS.BROADER;

    private Hashtable<Node, Node> table;
    private Hashtable<Node, Integer> depth;

    static Logger logger = LoggerFactory.getLogger(Distance.class);

    void init(List<Node> top, boolean isProp) {
        isProperty = isProp;

        if (isProperty) {
            subEntityOf = getGraph().getPropertyNode(RDFS.SUBPROPERTYOF);
            equivAs = getGraph().getPropertyNode(OWL.EQUIVALENTPROPERTY);
            setStep(PSTEP);
        } else {
            subEntityOf = getGraph().getPropertyNode(getSubClassOf());
            if (subEntityOf == null) {
                subEntityOf = getGraph().getPropertyNode(getBroader());
            }
            equivAs = getGraph().getPropertyNode(OWL.EQUIVALENTCLASS);
            //setStep(CSTEP);
        }

        // rdfs:Resource or owl:Thing
        topList = new NodeList(top);
        topLevel = new ArrayList<>(); 
        // top level classes which are subClassOf nobody
        if (subEntityOf != null) {
            topLevel = graph.getTopLevel(subEntityOf);
        }
        table = new Hashtable<>();
        depth = new Hashtable<>();
        sameAs = getGraph().getPropertyNode(OWL.SAMEAS);
        hasSame = sameAs != null;
        hasEquiv = equivAs != null;
        // reset all depth
        reset();
        init();
    }
    
    public void start() {
        ArrayList<Node> top = new ArrayList<>();
        top.add(getGraph().getTopClass());
        init(top, false);
    }
    
    public Distance(Graph g) {
        graph = g;
    }

    public static Distance classDistance(Graph g) {
        Distance dist = new Distance(g);
        dist.start();
        return dist;
    }

    /**
     * Manage two hierarchies with topObject and topData
     *
     */
    public static Distance propertyDistance(Graph g) {
        Distance dist = new Distance(g);
        List<Node> top = g.getTopProperties();
        dist.init(top, true);
        return dist;
    }

    public void setExtended(boolean b) {
        isExtended = b;
    }

    void setProperty(boolean b) {
        isProperty = b;
    }

    public void setStep(double f) {
        step = f;
    }

    public static void setPropertyStep(double d) {
        PSTEP = d;
    }

    public static void setClassStep(double d) {
        CSTEP = d;
    }

    public Integer getDepth(Node n) {
        //Integer d = (Integer) n.getProperty(Node.DEPTH) ;
        Integer d = depth.get(n);
        return d;
    }

    void setDepth(Node n, Integer i) {
        //n.setProperty(Node.DEPTH, i);
        depth.put(n, i);
    }

    void init() {
        depthMax = 0;
        dmax = 0;
        K = 1;
        initDepth();
    }

    void reinit() {
        reset();
        init();
    }

    void initDepth() {
        for (Node sup : topList) {
            initDepth(sup, 0);
        }
        initDepthTopLevel();
    }
    
    /**
     * depth of hierarchy of classes that are subClassOf nobody
     * depth of such top level classes is 1
     * Hence we emulate subClassOf rdfs:Resource
     * */
    void initDepthTopLevel() {
        for (Node node : topLevel) {
            initDepth(node, 1);
        }
    }

    void initDepth(Node sup, int depth) {
        setDepth(sup, depth);
        table.clear();
        depth(sup);
        if (depthMax == 0) {
            // in case there is no rdfs:subClassOf, choose  max depth 1
            depthMax = 1;
        }
        setDmax(depthMax);
    }

    /**
     * Recursively compute depth Take multiple inheritance into account TODO: in
     * this case depth is 1+ least depth of superclasses
     */
    void depth(Node sup) {
        visit(sup);

        Integer depth = getDepth(sup);
        Integer dd = depth + 1;
        for (Node sub : getSubEntities(sup)) {
            if (sub != null && !visited(sub)) {
                Integer d = getDepth(sub);
                if (d == null || dd > d) {
                    if (dd > depthMax) {
                        depthMax = dd;
                    }
                    setDepth(sub, dd);
                    depth(sub);
                }
            }
        }

        leave(sup);
    }

    boolean visited(Node node) {
        return table.containsKey(node);
    }

    void visit(Node node) {
        table.put(node, node);
    }

    void leave(Node node) {
        table.remove(node);
    }

    void reset() {
        depth.clear();
    }

//    void reset(Node sup) {
//        setDepth(sup, null);
//        for (Node sub : getSubEntities(sup)) {
//            if (sub != null) {
//                reset(sub);
//            }
//        }
//    }

    /**
     * Used by semantic distance Node with no depth (not in subClassOf hierarchy
     * of root) is considered at depth 1 just under root (at depth 0)
     */
    Integer getDDepth(Node n) {
        Integer d = getDepth(n);
        if (d == null) {
            d = ONE;
        }
        return d;
    }

    double step(Node f) {
        return step(getDDepth(f));
    }

    double step(int depth) {
        return (1 / Math.pow(step, depth));
    }

    void setDmax(int max) {
        dmax = 0;
        for (int i = 1; i <= max; i++) {
            dmax += step(i);
        }
        dmax = 2 * dmax;
        K = Math.pow(step, max) / 100.0;
    }

    public double maxDistance() {
        return dmax;
    }

    public double similarity(double distance) {
        return similarity(distance, 1);
    }

    public double similarity(double distance, int num) {
        double dist = distance / (dmax * num);
        double sim = 1 / (1 + (K * dist)); //  1/1+0=1 1/1+1 = 1/2    1/1+2 = 1/3
        return sim;
    }

    boolean isRoot(Node n) {
        for (Node sup : topList) {
            if (n.same(sup)) {
                return true;
            }
        }
        return false;
    }
    
    boolean isTopLevel(Node n) {
        return topLevel.contains(n);
    }

    /**
     * Node with no super class is considered subClassOf root
     */
    public Iterable<Node> getSuperEntities(Node node) {
        if (subEntityOf == null) {
            return topList;
        }
        Iterable<Node> it = getGraph().getNodes(subEntityOf, node, 0);
        if (!it.iterator().hasNext()) {
            return topList;
        }
        return it;
    }

    public Iterable<Node> getSubEntities(Node node) {
        if (subEntityOf == null) {
            return new ArrayList<>();
        }
        return getGraph().getNodes(subEntityOf, node, 1);
    }

    /**
     * Return ontological distance between two concepts Distance is the sum of
     * distance (by steps) to the deepest common ancestor Compute the deepest
     * common ancestor by climbing step by step through ancestors always walk
     * through deepest ancestors of c1 or c2 (i.e. delay less deep ancestors)
     * hence the first common ancestor is the deepest
     */
    public Double sdistance(Node c1, Node c2) {
        return distance(c1, c2);
    }

    public double similarity(Node c1, Node c2) {
        if (c1.equals(c2)) {
            return 1;
        }
        Double d = distance(c1, c2);
        return similarity(d, 1);
    }

    public double distance(Node c1, Node c2) {
        return (Double) distance(c1, c2, true);
    }

    public Node ancestor(Node c1, Node c2) {
        if (c1.equals(c2)) {
            return c1;
        }
        return (Node) distance(c1, c2, false);
    }

    class NodeList extends ArrayList<Node> {

        NodeList() {
        }

        NodeList(Node n) {
            add(n);
        }

        NodeList(List<Node> l) {
            for (Node n : l) {
                add(n);
            }
        }
    }

    class Table extends Hashtable<Node, Double> {

        boolean hasRoot = false;

        public Double put(Node n, Double d) {
            if (isRoot(n)) {
                hasRoot = true;
            }
            return super.put(n, d);
        }

        public boolean contains(Node n) {
            if (containsKey(n)) {
                return true;
            }
            return false;
        }

    }

    /**
     * isDist = true  : return distance Double
     * isDist = false : return common ancestor Node 
     */
    Object distance(Node n1, Node n2, boolean isDist) {

        Table t1 = new Table();
        Table t2 = new Table();

        NodeList current1 = new NodeList();
        NodeList current2 = new NodeList();

        boolean end = false;
        boolean endC1 = false;
        boolean endC2 = false;
        int max1, max2; // maximal (deepest) depth of current

        Double i = 0.0;
        Double j = 0.0;
        Node common = null;
        int count = 0;
        t1.put(n1, i);
        t2.put(n2, j);
        max1 = getDDepth(n1);
        max2 = getDDepth(n2);
        current1.add(n1);
        current2.add(n2);

        if (max1 == 0) {
            if (isRoot(n1)) {
                endC1 = true;
            } else {
                return result(null, 0.0, isDist);
            }
        }

        if (max2 == 0) {
            if (isRoot(n2)) {
                endC2 = true;
            } else {
                return result(null, 0.0, isDist);
            }
        }
        
        if (max1 == 1) {
            if (isTopLevel(n1)) {
                endC1 = true;
            } 
        }

        if (max2 == 1) {
            if (isTopLevel(n2)) {
                endC2 = true;
            } 
        }
        
        

        if (t1.contains(n2)) {
            end = true;
        }

        while (!end) {

            if (count++ > 10000) {
                logger.debug("** Node distance suspect a loop " + n1 + " " + n2);
                break;
            }

            if (!endC1 && max1 >= max2) {
                // distance from current to their fathers
                endC1 = distance(current1, t1, max2);
                max1 = getMax(current1); // max depth of current 1
            }

            // on ne considere comme candidat a type commun que ceux qui sont
            // aussi profond que le plus profond des types deja parcourus
            // dit autrement, on ne considere  un type commun qu'apres avoir explore
            // tous les types plus profonds que lui de maniere a trouver en premier
            // le type commun le plus profond
            double dd;

            for (Node node : current2) {

                if (getDDepth(node) < max1) {
                    break;
                }

                if (t1.contains(node)) {
                    dd = distance(node, t1, t2);
                    return result(node, dd, isDist);
                }

                if (isDist) {
                    dd = extDistance(node, t1, t2);
                    if (dd != -1) {
                        return result(node, dd, isDist);
                    }
                }

            }

            if (!endC2 && max2 >= max1) {
                // distance from current to their fathers
                endC2 = distance(current2, t2, max1);
                max2 = getMax(current2); // max depth of current 2
            }

            for (Node node : current1) {

                if (getDDepth(node) < max2) {
                    break;
                }

                if (t2.contains(node)) {
                    dd = distance(node, t1, t2);
                    return result(node, dd, isDist);
                }

                if (isDist) {
                    dd = extDistance(node, t2, t1);
                    if (dd != -1) {
                        return result(node, dd, isDist);
                    }
                }
            }

        }

        return result(null, 0.0, isDist);
    }

    Object result(Node n, double d, boolean isDist) {
        if (isDist) {
            return d;
        } else {
            return n;
        }
    }

    /**
     * Exploit aa sameAs bb
     */
    double extDistance(Node node, Table ta, Table tb) {
        if (!isExtended) {
            return -1;
        }
        if (hasSame) {
            double dd = distance(node, sameAs, ta, tb);
            if (dd != -1) {
                return dd;
            }
        }
        if (hasEquiv) {
            double dd = distance(node, equivAs, ta, tb);
            if (dd != -1) {
                return dd;
            }
        }
        return -1;
    }

    double distance(Node node, Node pred, Table ta, Table tb) {
        for (Node same : getGraph().getNodes(pred, node, 0)) {
            if (ta.contains(same)) {
                return ta.get(same) + tb.get(node);
            }
        }
        for (Node same : getGraph().getNodes(pred, node, 1)) {
            if (ta.contains(same)) {
                return ta.get(same) + tb.get(node);
            }
        }

        return -1;
    }

    int getMax(NodeList v) {
        if (v.size() == 0) {
            return 0;
        }
        Node ct = v.get(0);
        if (ct != null) {
            return getDDepth(ct); //.depth;
        } else {
            return 0;
        }
    }

    double distance(Node c, Table hct1, Table hct2) {
        return hct1.get(c) + hct2.get(c);
    }

    /**
     * compute distance from each current to its fathers
     * <br>side effect : set current to (current's) father list store in ht the
     * distance from source type to each father max is the deepest depth of the
     * other list of current should stay below this minimal depth to target the
     * deepest common ancestor first
     *
     * @return true if reach root
     */
    boolean distance(NodeList current, Table ht, int max) {
        boolean step = true;
        NodeList father = new NodeList();
        boolean endC1 = false;
        father.clear();
        double i, d;
        Node node;
        // Calcul des peres des derniers concepts traites
        while (current.size() > 0) {
            node = current.get(0);
            if (getDDepth(node) < max) {
                // process only deepest types ( >= max depth)
                break;
            }
            current.remove(node);
            i = ht.get(node);
            // distance of the fathers of ct
            for (Node sup : getSuperEntities(node)) {
                d = i + ((step) ? step(sup) : 1);
                if (ht.get(sup) == null) {
                    father.add(sup);
                    ht.put(sup, d);
                } else { // already passed through father f, is distance best (less) ?
                    if (d < ht.get(sup)) {
                        //logger.debug(f + " is cheaper ");
                        ht.put(sup, d);
                    }
                }
            }
        }

        // concepts courants += concepts peres
        sort(current, father);
        if (current.size() > 0) {
            node = current.get(0);
            if (isRoot(node)) {
                endC1 = true;
            }
        }
        return endC1;
    }

    /**
     * sort father by decreasing depth, in order to find the deepest common
     * first
     */
    void sort(NodeList current, NodeList father) {
        int j = 0;
        for (int i = 0; i < father.size(); i++) {
            for (j = 0; j < current.size() && getDDepth(father.get(i)) <= getDDepth(current.get(j)); j++) {
                // do nothing
            }
            current.add(j, father.get(i));
        }
    }

    /**
     * @return the subClassOf
     */
    public String getSubClassOf() {
        return subClassOf;
    }

    /**
     * @param subClassOf the subClassOf to set
     */
    public void setSubClassOf(String subClassOf) {
        this.subClassOf = subClassOf;
    }

    /**
     * @return the broader
     */
    public String getBroader() {
        return broader;
    }

    /**
     * @param broader the broader to set
     */
    public void setBroader(String broader) {
        this.broader = broader;
    }

    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @param graph the graph to set
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

}
