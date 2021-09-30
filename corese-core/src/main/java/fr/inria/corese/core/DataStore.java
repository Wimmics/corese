package fr.inria.corese.core;

import fr.inria.corese.core.producer.DataProducer;
import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple DataStore to define what is the default graph
 * By default it is the union of named graphs
 * It can be redefined using addDefaultGraph(node)
 * Producer uses getDefault()
 * getDefault().iterate()
 * getNamed().iterate()
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataStore {

    Graph graph;
    private ArrayList<Node> defaultGraphList;

    DataStore(Graph g) {
        graph = g;
        defaultGraphList = new ArrayList<>();
    }

    /**
     * Return a DataProducer with default or named graph Use case: iterate edges
     */
    public DataProducer getNamed() {
        return new DataProducer(graph).named();
    }

    public DataProducer getNamed(List<Node> list, Node source) {
        return getNamed().from(list, source);
    }

    public DataProducer getDefaultBasic() {
        return new DataProducer(graph);
    }
    
    public DataProducer getDefaultUnion() {
        return new DataProducer(graph);
    }
    
    public DataProducer getDefaultGraph() {
        return getDefaultBasic().from(graph.getNodeDefault());
    }

    public DataProducer getDefault() {
        if (defaultGraphList.isEmpty()) {
            return getDefaultUnion();
        }
        return getDefaultBasic().from(getDefaultGraphList());
    }

    public DataProducer getDefault(List<Node> list) {
        if (list == null || list.isEmpty()) {
            return getDefault();
        }
        return getDefaultBasic().from(list);
    }

    public DataStore addDefaultGraph(Node node) {
        if (node != null) {
            defaultGraphList.add(node);
        }
        return this;
    }
    
    public DataStore addDefaultGraph() {
        return addDefaultGraph(graph.getNodeDefault());
    }

    public List<Node> getDefaultGraphList() {
        return defaultGraphList;
    }
}
