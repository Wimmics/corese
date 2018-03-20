package fr.inria.corese.core.stats;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.stats.data.ReducedMap;
import fr.inria.corese.core.stats.data.HashBucket;
import static fr.inria.corese.core.stats.IStats.OBJECT;
import static fr.inria.corese.core.stats.IStats.PREDICATE;
import static fr.inria.corese.core.stats.IStats.SUBJECT;
import fr.inria.corese.core.stats.data.BaseMap;
import fr.inria.corese.core.Graph;
import static fr.inria.corese.core.stats.Options.DEF_OPT_OBJ;
import static fr.inria.corese.core.stats.Options.DEF_OPT_TRIPLE;
import static fr.inria.corese.core.stats.Options.DEF_OPT_PRE;
import static fr.inria.corese.core.stats.Options.DEF_OPT_SUB;
import static fr.inria.corese.core.stats.Options.DEF_PARA_CUTOFF;
import static fr.inria.corese.core.stats.Options.DEF_PARA_HTT;
import static fr.inria.corese.core.stats.Options.HT_CUTOFF;
import static fr.inria.corese.core.stats.Options.HT_FULL;
import static fr.inria.corese.core.stats.Options.HT_HASH;
import static fr.inria.corese.core.stats.Options.HT_TRIPLE_HASH;
import fr.inria.corese.core.stats.data.SimpleAverage;
import fr.inria.corese.core.stats.data.TripleHashTable2;
import fr.inria.corese.core.load.Load;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Meta data that used to collect statistics from a graph such as, the number of
 * triples, distinct subject number, etc..
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 20 mai 2014
 * @deprecated
 */
public class MetaData {

    public static void main(String[] args) {
        Graph gg = Graph.create();
        Load ld = Load.create(gg);
        ld.load("/Users/fsong/Downloads/cog-2012.ttl");
    }

    public static final int NA = -1;

    private static BaseMap subMap, preMap, objMap;
    private static TripleHashTable2 thtable;

    private static Graph graph = null;
    private static int noOfAllResource, noOfAllObjects;
    private static MetaData meta = null;

    //Enable stats or not
    public static boolean enabled = false;

    //Enable stats or not
    public static boolean enable_htt = true;

    //options
    private static final Map<Integer, Options> options = new HashMap<Integer, Options>();

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
     * Create an instance of MetaData with options
     *
     * @param g Graph
     * @return
     */
    public static MetaData createInstance(Graph g) {
        if (!enabled) {
            System.out.println("Statistics not enabled, use \"MetaData.enabled = true\" to enable stats!");
            return meta;
        }

        meta = (meta == null) ? new MetaData() : meta;
        graph = g;
        checkOptions();
        process();
        return meta;
    }

    /**
     * Add options to meta data statistics
     *
     * @param value
     * @param opt
     */
    public static void addOption(int value, Options opt) {
        options.put(value, opt);
    }

    //pre-process the options
    private static void checkOptions() {
        //set default options
        if (options.isEmpty()) {
            addOption(DEF_OPT_SUB, new Options(HT_CUTOFF, IStats.SUBJECT, DEF_PARA_CUTOFF));
            addOption(DEF_OPT_PRE, new Options(HT_FULL, IStats.PREDICATE, null));
            addOption(DEF_OPT_OBJ, new Options(HT_HASH, IStats.OBJECT, DEF_PARA_CUTOFF));
            addOption(DEF_OPT_TRIPLE, new Options(HT_TRIPLE_HASH, IStats.TRIPLE, DEF_PARA_HTT));
        }

        //stas for whole triple
        if (options.containsKey(DEF_OPT_TRIPLE)) {
            thtable = new TripleHashTable2(options.get(DEF_OPT_TRIPLE).getParameters());
        }

        //stats for each single map
        for (Options opt : options.values()) {
            //1 obtain the variable according to the heuristic type
            BaseMap map = getInstance(opt.getHeuristic(), opt.getParameters());
            //2 create a new instance for corresponding var
            setInstance(opt.getNodeType(), map);
        }
    }

    //Main method for generating the stats data
    private static void process() {
        long start = System.currentTimeMillis();
        
        //todo
        Iterator<Node> itt = graph.getGraphNodes().iterator();
        while(itt.hasNext()){
           Node gNode = itt.next();
           graph.getNodes(gNode);
        }
        
        //**1 iterate all the triples and do statistics meanwhile
        Iterator<Entity> it = graph.getEdges().iterator();
        while (it.hasNext()) {
            Edge e = (Edge) it.next();
            //** 1 add nodes to each map
            //subject
            subMap.add(e.getNode(0));
            //predicate
            preMap.add(e.getEdgeVariable() == null ? e.getEdgeNode() : e.getEdgeVariable());
            //object
            objMap.add(e.getNode(1));
            
            //todo: stats graph
            //graph.getGraphNodes();
            //((Entity)e).getGraph();
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
     * @param type
     * @return
     */
    public int getCountByTriple(Edge e, int type) {
        return thtable == null ? -1 : thtable.get(e, type);
    }

    
    public int getCountByGraph(Node gNode){
        
        return 0;
        //return graph.getGraphNodes()
    }
    /**
     * reset the stats to empty with an empty graph
     */
    public static void reset() {
        setGraph(Graph.create());
    }

    //Set the graph that statistics is performed on
    public static void setGraph(Graph g) {
        graph = g;
        update();
    }

    //monitor the state of graph, when the graph is changed, 
    //update the meta data
    public static void update() {
        checkOptions();
        process();
        //ex. number of triples
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
