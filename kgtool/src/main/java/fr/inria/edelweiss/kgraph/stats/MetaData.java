package fr.inria.edelweiss.kgraph.stats;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.stats.data.ReducedMap;
import fr.inria.edelweiss.kgraph.stats.data.HashBucket;
import static fr.inria.edelweiss.kgram.sorter.core.IProducer.OBJECT;
import static fr.inria.edelweiss.kgram.sorter.core.IProducer.PREDICATE;
import static fr.inria.edelweiss.kgram.sorter.core.IProducer.SUBJECT;
import fr.inria.edelweiss.kgraph.stats.data.BaseMap;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.stats.data.SimpleAverage;
import fr.inria.edelweiss.kgraph.stats.data.TripleHashTable;
import fr.inria.edelweiss.kgtool.load.Load;
import java.util.Iterator;
import java.util.List;

/**
 * Meta data that used to collect statistics from a graph such as, the number of
 * triples, distinct subject number, etc..
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 20 mai 2014
 */
public class MetaData {

    public static void main(String[] args) {
        Graph gg = Graph.create();
        Load ld = Load.create(gg);
        ld.load("/Users/fsong/Downloads/cog-2012.ttl");
    }

    public static final int NA = -1;

    private static BaseMap subMap, preMap, objMap;
    private static TripleHashTable thtable;

    private static Graph graph = null;
    private static int noOfAllResource, noOfAllObjects;
    private static final MetaData meta = new MetaData();

    //Enable stats or not
    public static boolean enabled = false;

    //Enable stats or not
    public static boolean enable_htt = true;

    //options
    private static final Options[] options = Options.DEFAULT_OPTIONS;

    //private constructor for singleton
    private MetaData() {

    }

    /**
     * Return the singleton instance of Meta datga
     *
     * @return
     */
    public static MetaData getInstance() {
        return meta;
    }

    /**
     * Return the status of using meta data or not
     *
     * @return
     */
    public static boolean enabled() {
        return enabled;
    }

    /**
     * Create an instance of MetaData, singleton
     *
     * @param g Graph
     * @return
     */
    public static MetaData createInstance(Graph g) {
        return createInstance(g, null);
    }

    /**
     * Create an instance of MetaData with options
     *
     * @param g Graph
     * @param options setting different stats methods
     * @return
     */
    public static MetaData createInstance(Graph g, List<Options> options) {
        if (!enabled) {
            return meta;
        }

        graph = g;
        checkOptions(options);
        process();
        return meta;
    }

    /**
     * Set options for generating different stats data
     *
     * @param list of options
     */
    public static void setOptions(List<Options> list) {
        //check options
        //todo: when not all nodes are set
        if (list != null) {
            list.toArray(options);
        }
    }

    //pre-process the options
    private static void checkOptions(List<Options> list) {
        setOptions(list);
        for (Options opt : options) {
            if (opt.getHeuristic() == Options.HT_TRIPLE_HASH) {
                thtable = new TripleHashTable(opt.getParameters());
                continue;
            }
            //1 obtain the variable according to the heuristic type
            BaseMap map = getInstance(opt.getHeuristic(), opt.getParameters());
            //2 create a new instance for corresponding var
            setInstance(opt.getNodeType(), map);
        }
    }

    //Main method for generating the stats data
    private static void process() {
        long start = System.currentTimeMillis();

        //**1 iterate all the triples and do statistics meanwhile
        Iterator<Entity> it = graph.getEdges().iterator();
        while (it.hasNext()) {
            Edge e = (Edge) it.next();
            //** 1 add nodes to each map
            //subject
            subMap.add(e.getNode(0));
            //predicate
            preMap.add(e.getEdgeNode());
            //object
            objMap.add(e.getNode(1));
        }

        //** 2 get the number of distinct subject/object
        noOfAllResource = subMap.size();
        noOfAllObjects = objMap.size();

        //***2.1 create triple hash table
        if (enable_htt && thtable != null) {
            it = graph.getEdges().iterator();
            thtable.setOptions(noOfAllResource, noOfAllObjects);
            while (it.hasNext()) {
                thtable.add((Edge) it.next());
            }
        }

        //**3 if the map is reduced map, cut off
        reduce();

        //**4 clear unused map to save memory
        clear();

        long end = System.currentTimeMillis();
        System.out.println("====Meta data stats time (" + graph.size() + " triples):" + (end - start) + " ms====");
    }

    //If using reduced map method, then reduce the size of map
    private static void reduce() {
        if (subMap instanceof ReducedMap) {
            ((ReducedMap) subMap).cut();
        }

        if (preMap instanceof ReducedMap) {
            ((ReducedMap) preMap).cut();
        }

        if (objMap instanceof ReducedMap) {
            ((ReducedMap) objMap).cut();
        }
    }

    private static void clear() {
        clear(subMap);
        clear(preMap);
        clear(objMap);
    }

    private static void clear(BaseMap map) {
        if (map instanceof HashBucket
                || map instanceof ReducedMap) {
            map.clear();
        }
    }

    /**
     * Return the number of all triples contained in a graph
     *
     * @return
     */
    public int getAllTriplesNumber() {
        return graph.size();
    }

    /**
     * Return the number of distinct subjects (resources)
     *
     * @return
     */
    public int getResourceNumber() {
        return noOfAllResource;
    }

    /**
     * Return the number of distinct property (precidate)
     *
     * @return
     */
    public int getPropertyNumber() {
        return graph.getIndex().size();
    }

    /**
     * Return the number of distinct objects
     *
     * @return
     */
    public int getObjectNumber() {
        return noOfAllObjects;
    }

    /**
     * Get the estimated selected triples by single value
     *
     * @param n
     * @param type subject | predicate|object
     * @return
     */
    public int getCountByValue(Node n, int type) {
        switch (type) {
            case SUBJECT:
                return subMap.get(n);
            case PREDICATE:
                return preMap.get(n);
            case OBJECT:
                return objMap.get(n);
            default:
                return NA;
        }
    }
    
    
    /**
     * Get the estimated selected triples number according to the whole triple
     *
     * @param e Edge
     * @return
     */
    public int getCountByTriple(Edge e) {
        return thtable.get(e);
    }

    //private double selSubject, selPredicate, selObject;
    //monitor the state of graph, when the graph is changed, 
    //update the meta data
    public void update() {
        process();
        //ex. number of triples
    }

    //reset the stats to empty with an empty graph
    public void reset() {
        setGraph(Graph.create());
    }

    //Set the graph that statistics is performed on
    public static void setGraph(Graph g) {
        graph = g;
        process();
    }

    //todo with parameters
    private static BaseMap getInstance(int type, double[] params) {
        switch (type) {
            case Options.HT_HASH:
                return new HashBucket(graph.size(), params);
            case Options.HT_FULL:
                return new BaseMap();
            case Options.HT_CUTOFF:
                return new ReducedMap(params);
            case Options.HT_AVERAGE:
                return new SimpleAverage();
        }
        return null;
    }

    private static void setInstance(int type, BaseMap map) {
        switch (type) {
            case SUBJECT:
                subMap = map;
                break;
            case PREDICATE:
                preMap = map;
                break;
            case OBJECT:
                objMap = map;
                break;
        }
    }
}
