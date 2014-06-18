package fr.inria.edelweiss.kgraph.stats;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.stats.data.ReducedMap;
import fr.inria.edelweiss.kgraph.stats.data.HashBucket;
import fr.inria.edelweiss.kgram.sorter.core.IStatistics;
import fr.inria.edelweiss.kgraph.stats.data.BaseMap;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.stats.data.SimpleAverage;
import fr.inria.edelweiss.kgtool.load.Load;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Meta data that used to collect statistics from a graph such as, the number of
 * triples, distinct subject number, etc..
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 20 mai 2014
 */
public class MetaData implements IStatistics {

    public static void main(String[] args) {
        Graph gg = Graph.create();
        Load ld = Load.create(gg);
        ld.load("/Users/fsong/Downloads/cog-2012.ttl");
        //ld.load("/Users/fsong/NetBeansProjects/bsbmtools-0.2/scale5000.ttl");
        //MetaData md = MetaData.createInstance(gg);
        //((EqualWidthHG) md.getHG()).stats();
        //System.out.println(mapSN);
    }

    public static final int NA = -1;

    private static BaseMap subMap, preMap, objMap;
    private static Graph graph = null;
    private static int noOfAllResource, noOfAllObjects;
    private static final MetaData meta = new MetaData();

    //Enable stats or not
    public static boolean enabled = true;

    //options
    private static Options[] options = Options.DEFAULT_OPTIONS;

    //private constructor for singlenton
    private MetaData() {

    }

    public static MetaData getInstance() {
        return meta;
    }

    /**
     * Create a instance of MetaData, singleton
     *
     * @param g Graph
     * @return
     */
    public static MetaData createInstance(Graph g) {
        return createInstance(g, null);
    }

    public static MetaData createInstance(Graph g, List<Options> options) {
        if (!enabled) {
            return null;
        }

        graph = g;
        checkOptions(options);
        process(g);
        return meta;
    }

    public static void setOptions(List<Options> list) {
         //check options
        //todo: when not all nodes are set
        if (list != null && list.size() == 3) {
            list.toArray(options);
        }
    }

    private static void checkOptions(List<Options> list) {
        setOptions(list);
        for (Options opt : options) {
            //1 obtain the variable according to the heuristic type
            BaseMap map = getInstance(opt.getHeuristic(), opt.getParameters());
            //2 create a new instance for corresponding var
            setInstance(opt.getNodeType(), map);
        }
    }

    private static void process(Graph g) {
        long start = System.currentTimeMillis();
        BaseMap mapSub = new BaseMap(), mapObj = new BaseMap();

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

            //** 2 calculate the distinct number of subject/object
            mapSub.add(e.getNode(0));
            mapObj.add(e.getNode(1));
        }

        //** 2 get the number of distinct subject/object
        noOfAllResource = mapSub.size();
        noOfAllObjects = mapObj.size();

        //**3 if the map is reduced map, cut off
        reduce(subMap);
        reduce(preMap);
        reduce(objMap);

        long end = System.currentTimeMillis();
        System.out.println("====Meta data stats time (" + graph.size() + " triples):" + (end - start) + " ms====");
    }

    private static void reduce(BaseMap map) {
        if (map instanceof ReducedMap) {
            ((ReducedMap) map).cut();
        }
    }

    //number of all triples
    @Override
    public int getAllTriplesNumber() {
        return graph.size();
    }

    //number of discinct resources
    @Override
    public int getResourceNumber() {
        return noOfAllResource;
    }

    //number of predicates
    @Override
    public int getPropertyNumber() {
        return graph.getIndex().size();
    }

    @Override
    public int getObjectNumber() {
        return noOfAllObjects;
    }

    @Override
    public int getCountByValue(String val, int type) {
        switch (type) {
            case SUBJECT:
                return subMap.get(val);
            case PREDICATE:
                return preMap.get(val);
            case OBJECT:
                return objMap.get(val);
            default:
                return NA;
        }
    }

    //private double selSubject, selPredicate, selObject;
    //monitor the state of graph, when the graph is changed, 
    //update the meta data
    public void update() {
        process(graph);
        //ex. number of triples
    }

    //reset the stats to empty with an empty graph
    public void reset() {
        setGraph(Graph.create());
    }

    //Set the graph that statistics is performed on
    public static void setGraph(Graph g) {
        process(g);
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
