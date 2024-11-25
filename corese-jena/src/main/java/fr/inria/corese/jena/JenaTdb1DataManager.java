package fr.inria.corese.jena;

import static fr.inria.corese.jena.convert.ConvertJenaCorese.RULE_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.tdb.TDBFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import fr.inria.corese.core.logic.Entailment;

import fr.inria.corese.core.producer.MetadataManager;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.jena.convert.ConvertJenaCorese;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 * Implements Corese DataManager interface for Jena-TDB.
 */
public class JenaTdb1DataManager implements DataManager, AutoCloseable {

    private Dataset jena_dataset;
    private Graph default_graph;
    private String storage_path;
    // when true, iterate edge as quad to get named graph kg:rule_i to get edge index i 
    private boolean ruleDataManager = false;
    // Each thread has its own counter for read transaction there may be several
    // start/end read transaction in each thread
    HashMap<Thread, Integer> threadCounter;

    // Manage meta-data
    private MetadataManager metadataManager;

    /****************
     * Constructors *
     ****************/

    /**
     * Constructor of JenaTdbDataManager. Create to a Jena dataset backed by an
     * in-memory block manager. For testing.
     * Please use the JenaTdb1DataManagerBuilder to create a JenaTdb1DataManager.
     */
    protected JenaTdb1DataManager() {
        this.storage_path = null;
        this.jena_dataset = TDBFactory.createDataset();
        init();
    }
    
    /**
     * Constructor of JenaTdbDataManager. Create or connect to a Jena dataset backed
     * in file system.
     * Please use the JenaTdb1DataManagerBuilder to create a JenaTdb1DataManager.
     * 
     * @param storage_path Path of the directory where the data is stored.
     */
    protected JenaTdb1DataManager(String storage_path) {
        this.storage_path = storage_path;
        this.jena_dataset = TDBFactory.createDataset(storage_path);
        init();
    }

    /**
     * Constructor of JenajDataManager from a Jena dataset.
     * Please use the JenaTdb1DataManagerBuilder to create a JenaTdb1DataManager.
     * 
     * @param dataset      Jena dataset.
     * @param storage_path Path of the directory where the dataset is stored, null
     *                     if it is in memory.
     */
    protected JenaTdb1DataManager(Dataset dataset, String storage_path) {
        this.storage_path = storage_path;
        this.jena_dataset = dataset;
        init();
    }

    private void init() {
        this.threadCounter = new HashMap<>();
        this.default_graph = this.defaultGraph();
    }

    /*********
     * Count *
     *********/

    @Override
    public int graphSize() {
        int size = 0;

        // Count the number of triples in the default Jena model
        size += this.jena_dataset.getDefaultModel().size();

        // Count the number of triplets in the named Jena models
        Iterator<String> modelNameList = this.jena_dataset.listNames();
        while (modelNameList.hasNext()) {
            String modelName = modelNameList.next();
            size += this.jena_dataset.getNamedModel(modelName).size();
        }

        return size;
    }
    
//    public int graphSize() {
//        return (int) this.jena_dataset.asDatasetGraph().stream().count();
//    }

    @Override
    public int countEdges(Node predicate) {
        // Convert Corese node to Jena RdfNode
        org.apache.jena.graph.Node jena_predicate = ConvertJenaCorese.coreseNodeToJenaNode(predicate);
        return (int) this.jena_dataset.asDatasetGraph().stream(null, null, jena_predicate, null).count();
    }

    /************
     * GetEdges *
     ************/
    
    // @todo: test exist in storage without creating Edge
    @Override
    public boolean exist(Node subject, Node predicate, Node object) {
        return find(subject, predicate, object);
//        for (Edge edge : getEdges(subject, predicate, object, null)) {
//            return true;
//        }
//        return false;
    }

    @Override
    public Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts) {

        if (contexts != null) {
            // Clear null values in list of contexts
            List<Node> clear_contexts = contexts.stream().filter(x -> x != null).collect(Collectors.toList());

            // if context == 1, no need for union. (in order not to lose information of
            // context)
            if (clear_contexts.size() == 1 || isRuleDataManager()) {
                return () -> this.chooseQuadDuplicatesWrite(subject, predicate, object, clear_contexts);
            } else {
                return () -> this.chooseTripleWithoutDuplicatesReadOnly(subject, predicate, object, clear_contexts);
            }
        } 
        else if (isRuleDataManager()) {
            return () -> this.chooseQuadDuplicatesWrite(subject, predicate, object, contexts);
        }
        else {
            return () -> this.chooseTripleWithoutDuplicatesReadOnly(subject, predicate, object, contexts);
        }
    }
    
    // iterate edge with edge.index >= index
    @Override
    public Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts, int oper, int index) {
        return () -> this.chooseQuadDuplicatesWrite1(subject, predicate, object, null, oper, index, true);
    }

    /*************
     * Get lists *
     *************/

    @Override
    public Iterable<Node> predicates(Node corese_context) {

        Function<org.apache.jena.graph.Node, Node> convertIteratorQuadToEdge = new Function<org.apache.jena.graph.Node, Node>() {
            @Override
            public Node apply(org.apache.jena.graph.Node resource) {
                return ConvertJenaCorese.JenaNodeToCoreseNode(resource);
            }
        };

        Graph graph = null;
        if (corese_context == null) {
            graph = this.default_graph;
        } else {
            String context_iri = ConvertJenaCorese.coreseContextToJenaContext(corese_context).getURI();
            graph = this.jena_dataset.getNamedModel(context_iri).getGraph();
        }

        Iterator<org.apache.jena.graph.Node> iter = GraphUtil.listPredicates(
                graph,
                org.apache.jena.graph.Node.ANY,
                org.apache.jena.graph.Node.ANY);

        return () -> Iterators.transform(
                iter,
                convertIteratorQuadToEdge);
    }

    @Override
    public Iterable<Node> getNodes(Node context) {

        Function<org.apache.jena.graph.Node, Node> convertIteratorJenaNodeToCoreseNode = new Function<org.apache.jena.graph.Node, Node>() {
            @Override
            public Node apply(org.apache.jena.graph.Node resource) {
                return ConvertJenaCorese.JenaNodeToCoreseNode(resource);
            }
        };

        Graph graph = null;
        if (context == null) {
            graph = this.default_graph;
        } else {
            String context_IRI = ConvertJenaCorese.coreseContextToJenaContext(context).getURI();
            graph = this.jena_dataset.getNamedModel(context_IRI).getGraph();
        }

        Iterator<org.apache.jena.graph.Node> iter = GraphUtils.allNodes(graph);

        return () -> Iterators.transform(
                iter,
                convertIteratorJenaNodeToCoreseNode);

    }

    @Override
    public Iterable<Node> contexts() {

        Function<Resource, Node> convertIteratorResourceToNode = new Function<Resource, Node>() {
            @Override
            public Node apply(Resource resource) {
                return ConvertJenaCorese.jenaContextToCoreseContext(resource.asNode());
            }
        };

        // Get list of contexts names
        Iterator<Node> contextsNamedGraphs = Iterators.transform(
                this.jena_dataset.listModelNames(),
                convertIteratorResourceToNode);

        if (this.jena_dataset.getDefaultModel().isEmpty()) {
            return () -> contextsNamedGraphs;
        } else {
            // Add default graph in list of context if is not empty
            Node default_context = ConvertJenaCorese.jenaContextToCoreseContext(Quad.defaultGraphIRI);
            return () -> Iterators.concat(contextsNamedGraphs, Arrays.asList(default_context).iterator());
        }
    }

    /**********
     * Insert *
     **********/

    @Override
    public Edge insert(Edge edge) {
        Quad jena_quad = ConvertJenaCorese.edgeToQuad(edge);

        if (!this.jena_dataset.asDatasetGraph().contains(jena_quad)) {
            this.jena_dataset.asDatasetGraph().add(jena_quad);
            return edge;
        }

        return null;
    }

    /**********
     * Delete *
     **********/

    @Override
    public Iterable<Edge> delete(Node subject, Node predicate, Node object, List<Node> contexts) {
        ArrayList<Edge> edges = new ArrayList<>();

        try {
            edges = Lists.newArrayList(this.chooseQuadDuplicatesWrite(subject, predicate, object, contexts));

            for (Edge edge : edges) {
                Quad quad = ConvertJenaCorese.edgeToQuad(edge);

                if (this.jena_dataset.asDatasetGraph().contains(quad)) {
                    this.jena_dataset.asDatasetGraph().delete(quad);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return edges;
    }

    /**************
     * Blank node *
     **************/

    @Override
    public String blankNode() {
        return java.util.UUID.randomUUID().toString();
    }

    /*******************
     * Graph operation *
     *******************/

    @Override
    public boolean addGraph(Node source, Node target, boolean silent) {
        long nb_graph_before;
        long nb_graph_after;

        nb_graph_before = this.jena_dataset.asDatasetGraph().size();

        // Convert source and target to Jena context
        org.apache.jena.graph.Node source_context = ConvertJenaCorese.coreseContextToJenaContext(source);
        org.apache.jena.graph.Node target_context = ConvertJenaCorese.coreseContextToJenaContext(target);

        // Add Graph
        Graph graph_source = this.jena_dataset.asDatasetGraph().getGraph(source_context);
        this.jena_dataset.asDatasetGraph().addGraph(target_context, graph_source);

        nb_graph_after = this.jena_dataset.asDatasetGraph().size();

        return nb_graph_before != nb_graph_after;
    }

    /*********
     * Other *
     *********/

    /**
     * Getter for the internal Jena Dataset object.
     * 
     * @return internal Jena Dataset object
     */
    public Dataset getDataset() {
        return this.jena_dataset;
    }

    @Override
    public void close() {
        this.jena_dataset.close();
    }

    /**
     * Database storage path getter.
     * 
     * @return Database storage path or {@code null} if the DB has no storage path.
     *         For example, the data is stored in the RAM of the computer.
     */
    public String getStoragePath() {
        return this.storage_path;
    }

    /*********************
     * Choose statements *
     *********************/

      private boolean find(Node subject, Node predicate, Node object) {
        org.apache.jena.graph.Node jena_subject = ConvertJenaCorese.coreseNodeToJenaNode(subject);
        org.apache.jena.graph.Node jena_predicate = ConvertJenaCorese.coreseNodeToJenaNode(predicate);
        org.apache.jena.graph.Node jena_object = ConvertJenaCorese.coreseNodeToJenaNode(object);

        return default_graph.contains(jena_subject, jena_predicate, jena_object);
    }
    /**
     * Return edge iterator with the specified subject, predicate, object and
     * (optionally) context exist in this model. The subject, predicate, object and
     * context parameters can be null to indicate wildcards. The contexts parameter
     * is a wildcard and accepts zero or more values. If contexts is {@code null},
     * edge will match disregarding their context. If one or more contexts are
     * specified, edge with a context matching one of these will match.
     * 
     * @param subject   The subject of the edge to match, null to match
     *                  edge with any subject.
     * @param predicate Predicate of the edge to match, null to match
     *                  edge with any predicate.
     * @param object    Object of the edge to match, null to match edge
     *                  with any object.
     * @param contexts  Contexts of the edge to match, null to match
     *                  edge with any contexts.
     * @return Edge iteraor that match the specified pattern.
     */
    private Iterator<Edge> chooseTripleWithoutDuplicatesReadOnly(Node subject, Node predicate, Node object,
            List<Node> contexts) {

        Function<Triple, Edge> convertIteratorQuadToEdge = new Function<Triple, Edge>() {
            @Override
            public Edge apply(Triple triple) {
                return ConvertJenaCorese.tripleToEdge(triple);
            }
        };

        // Convert Corese (subject, predicate, object) to Jena format
        org.apache.jena.graph.Node jena_subject = ConvertJenaCorese.coreseNodeToJenaNode(subject);
        org.apache.jena.graph.Node jena_predicate = ConvertJenaCorese.coreseNodeToJenaNode(predicate);
        org.apache.jena.graph.Node jena_object = ConvertJenaCorese.coreseNodeToJenaNode(object);

        // Create a graph that is the union of all requested graphs/contexts.
        Graph unionGraph = Graph.emptyGraph;
        if (contexts == null || contexts.stream().allMatch(Objects::isNull)) {
            // If contexts is null, then defaut graph
            unionGraph = default_graph;
        } else {
            // If the contexts are not null, then the union of the requested graphs
            for (Node context : contexts) {
                if (context != null) {
                    String context_IRI = ConvertJenaCorese.coreseContextToJenaContext(context).getURI();
                    unionGraph = this.union(unionGraph, this.jena_dataset.getNamedModel(context_IRI).getGraph());
                }
            }
        }
        Iterator<Triple> iterator = unionGraph.find(jena_subject, jena_predicate, jena_object);
        Iterator<Edge> edges = Iterators.transform(iterator, convertIteratorQuadToEdge);

        return edges;
    }

    private Graph union(Graph... graphs) {
        Graph result = Graph.emptyGraph;

        for (Graph graph : graphs) {
            result = new Union(result, graph);
        }

        return result;
    }

    private Graph defaultGraph() {
        // In Corese, the concept of an explicit default graph as defined by Jena does
        // not exist.
        // The default graph as defined in Corese is the union of all named graphs.
        return this.union(
                this.jena_dataset.asDatasetGraph().getDefaultGraph(),
                this.jena_dataset.asDatasetGraph().getUnionGraph());
    }
    
    // when context is integer it represents a timestamp
    // we want edge.timestamp >= timestamp
    int timestamp(List<Node> nodeList){
       if (nodeList==null||nodeList.isEmpty()){
           return -1;
       }
       IDatatype dt = nodeList.get(0).getDatatypeValue();
       if (dt.isNumber()) {
           return dt.intValue();
       }
       return -1;
    }
    

    
    // index represents a timestamp
    // we want edge.timestamp >= timestamp 
    // to be used when isRuleDataManager()
    // use case: rule engine with closure for transitive rule
    private Iterator<Edge> chooseQuadDuplicatesWrite1(Node subject, Node predicate, Node object, List<Node> contexts, 
            int oper, int index, boolean bindex) {
        
        Function<Quad, Edge> convertIteratorQuadToEdge = new Function<Quad, Edge>() {
            @Override
            public Edge apply(Quad quad) {
                return ConvertJenaCorese.quadToEdge(quad);
            }
        };
        
        // edge with timestamp >= timestamp
        Function<Quad, Edge> convertIteratorQuadToEdgeTimestamp = new Function<Quad, Edge>() {
            @Override
            public Edge apply(Quad quad) {
                return ConvertJenaCorese.quadToEdge(quad, oper, index);
            }
        };

        org.apache.jena.graph.Node jena_subject = ConvertJenaCorese.coreseNodeToJenaNode(subject);
        org.apache.jena.graph.Node jena_predicate = ConvertJenaCorese.coreseNodeToJenaNode(predicate);
        org.apache.jena.graph.Node jena_object = ConvertJenaCorese.coreseNodeToJenaNode(object);

        Iterator<Edge> edges = Collections.emptyIterator();
        
        if (contexts == null || contexts.stream().allMatch(Objects::isNull)) {
            if (bindex) {
                Iterator<Quad> iterator = this.jena_dataset.asDatasetGraph().find(
                        null, jena_subject, jena_predicate, jena_object);

                edges = Iterators.concat(edges, Iterators.transform(iterator, convertIteratorQuadToEdgeTimestamp));
            }
            else {
                Iterator<Quad> iterator = this.jena_dataset.asDatasetGraph().find(
                        null, jena_subject, jena_predicate, jena_object);

                edges = Iterators.transform(iterator, convertIteratorQuadToEdge);
            }
        } else {
            for (Node context : contexts) {
                if (context != null) {

                    org.apache.jena.graph.Node jena_context = ConvertJenaCorese.coreseContextToJenaContext(context);

                    Iterator<Quad> iterator = this.jena_dataset.asDatasetGraph().find(
                            jena_context, jena_subject, jena_predicate, jena_object);

                    edges = Iterators.concat(edges, Iterators.transform(iterator, convertIteratorQuadToEdge));
                }
            }
        }

        return edges;
    }

    private Iterator<Edge> chooseQuadDuplicatesWrite(Node subject, Node predicate, Node object, List<Node> contexts) {

        Function<Quad, Edge> convertIteratorQuadToEdge = new Function<Quad, Edge>() {
            @Override
            public Edge apply(Quad quad) {
                return ConvertJenaCorese.quadToEdge(quad);
            }
        };

        org.apache.jena.graph.Node jena_subject = ConvertJenaCorese.coreseNodeToJenaNode(subject);
        org.apache.jena.graph.Node jena_predicate = ConvertJenaCorese.coreseNodeToJenaNode(predicate);
        org.apache.jena.graph.Node jena_object = ConvertJenaCorese.coreseNodeToJenaNode(object);

        Iterator<Edge> edges = Collections.emptyIterator();
        if (contexts == null || contexts.stream().allMatch(Objects::isNull)) {
            Iterator<Quad> iterator = this.jena_dataset.asDatasetGraph().find(
                    null, jena_subject, jena_predicate, jena_object);

            edges = Iterators.transform(iterator, convertIteratorQuadToEdge);
        } else {
            for (Node context : contexts) {
                if (context != null) {
                    org.apache.jena.graph.Node jena_context = ConvertJenaCorese.coreseContextToJenaContext(context);

                    Iterator<Quad> iterator = this.jena_dataset.asDatasetGraph().find(
                            jena_context, jena_subject, jena_predicate, jena_object);

                    edges = Iterators.concat(edges, Iterators.transform(iterator, convertIteratorQuadToEdge));
                }
            }
        }

        return edges;
    }

    /****************
     * Transactions *
     ****************/

    @Override
    public boolean transactionSupport() {
        return true;
    }

    @Override
    public void startReadTransaction() {
        int count = getReadTransactionCounter();
        if (count == 0) {
            this.jena_dataset.begin(ReadWrite.READ);
            if (hasMetadataManager()) {
                getMetadataManager().startReadTransaction();
            }
        }
        setReadTransactionCounter(count + 1);
    }

    @Override
    public void endReadTransaction() {
        int count = getReadTransactionCounter();
        if (count > 0) {
            count--;
            setReadTransactionCounter(count);
        }
        if (count == 0) {
            this.jena_dataset.end();
            if (hasMetadataManager()) {
                getMetadataManager().endReadTransaction();
            }
        }
    }

    /**
     * Get the number of open read transactions for the current thread.
     * 
     * @return number of open read transactions for the current thread.
     */
    private Integer getReadTransactionCounter() {
        Integer count = threadCounter.get(Thread.currentThread());
        if (count == null) {
            threadCounter.put(Thread.currentThread(), 0);
            count = 0;
        }
        return count;
    }

    /**
     * Set the number of open read transactions for the current thread.
     * 
     * @param count new value for the number of open read transactions for the
     *              current thread.
     */
    private void setReadTransactionCounter(int count) {
        threadCounter.put(Thread.currentThread(), count);
    }

    @Override
    public void startWriteTransaction() {
        if (hasMetadataManager()) {
            getMetadataManager().startWriteTransaction();
        }
        this.jena_dataset.begin(ReadWrite.WRITE);
    }

    @Override
    public void endWriteTransaction() {
        try {
            this.jena_dataset.commit();
        } finally {
            if (hasMetadataManager()) {
                getMetadataManager().endWriteTransaction();
            }
        }
    }

    @Override
    public void abortTransaction() {
        this.jena_dataset.abort();
    }

    @Override
    public boolean isInTransaction() {
        return this.jena_dataset.isInTransaction();
    }

    @Override
    public boolean isInReadTransaction() {
        ReadWrite transaction = this.jena_dataset.transactionMode();

        if (transaction == null) {
            return false;
        }
        return transaction.equals(ReadWrite.READ);
    }

    @Override
    public boolean isInWriteTransaction() {
        ReadWrite transaction = this.jena_dataset.transactionMode();

        if (transaction == null) {
            return false;
        }
        return transaction.equals(ReadWrite.WRITE);
    }

    /*******************
     * MetaDataManager *
     *******************/

    @Override
    public boolean hasMetadataManager() {
        return getMetadataManager() != null;
    }

    @Override
    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    @Override
    public void setMetadataManager(MetadataManager metaDataManager) {
        this.metadataManager = metaDataManager;
    }
 
    @Override
    public void startRuleEngine() {
    }

    @Override
    public void endRuleEngine() {
        if (isRuleDataManager()) {
            if (Property.hasValue(Property.Value.RULE_DATAMANAGER_CLEAN, false)) {
                // skip
            } else {
                cleanNamedGraph();
            }
        }
    }
    
    // move graph kg:rule_i into kg:rule
    void cleanNamedGraph() {
        startWriteTransaction();
        Node target = DatatypeMap.newResource(Entailment.RULE);
        for (Node node : contextList()) {
            if (node.getLabel().startsWith(RULE_NAME)) {
                addGraph(node, target, true);
                unDeclareContext(node);
            }
        }
        endWriteTransaction();
    }
    
    List<Node> contextList() {
        ArrayList<Node> nodeList = new ArrayList<>();
        for (Node node : contexts()) {
            nodeList.add(node);
        }
        return nodeList;
    }

    
    // manage edge index i as named graph kg:rule_i
    @Override
    public boolean isRuleDataManager() {
        return ruleDataManager;
    }

    @Override
    public void setRuleDataManager(boolean optimize) {
        this.ruleDataManager = optimize;
    }
    
}