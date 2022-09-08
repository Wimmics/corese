package fr.inria.corese.storage.jenatdb1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb.TDBFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.core.producer.MetadataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import java.util.HashMap;

/**
 * Implements the Corese Datamanger interface for Jena-TDB.
 */
public class JenaDataManager implements DataManager, AutoCloseable {

    private Dataset jena_dataset;
    private String storage_path;
    private int readCounter = 0;
    private boolean  reentrant = true;
    // each thread has its own counter for read transaction
    // there may be several start/end read transaction in each thread
    HashMap<Thread, Integer> threadCounter;
    private MetadataManager metadataManager;

    /****************
     * Constructors *
     ****************/

    /**
     * Constructor of JenaTdbDataManager. Create or connect to a Jena dataset backed
     * by an in-memory block manager. For testing.
     */
    public JenaDataManager() {
        this.storage_path = null;
        this.jena_dataset = TDBFactory.createDataset();
        init();
    }

    /**
     * Constructor of JenaTdbDataManager. Create or connect to a Jena dataset backed
     * in file system.
     * 
     * @param storage_path Path of the directory where the data is stored.
     */
    public JenaDataManager(String storage_path) {
        this.storage_path = storage_path;
        this.jena_dataset = TDBFactory.createDataset(storage_path);
        init();
    }

    /**
     * Constructor of JenajDataManager from a Jena dataset.
     * 
     * @param jena_dataset Jena dataset.
     */
    public JenaDataManager(Dataset jena_dataset) {
        this.jena_dataset = jena_dataset;
        init();
    }
    
    void init() {
        threadCounter = new HashMap<>();
    }

    /*********
     * Count *
     *********/

    @Override
    public int graphSize() {
        int result;

        result = (int) this.jena_dataset.asDatasetGraph().stream().count();

        return result;

    }

    @Override
    public int countEdges(Node predicate) {
        int result;

        // convert Corese node to Jena RdfNode
        org.apache.jena.graph.Node jena_predicate = ConvertJenaCorese.coreseNodeToJenaNode(predicate);

        result = (int) this.jena_dataset.asDatasetGraph().stream(null, null, jena_predicate, null).count();

        return result;
    }

    /************
     * GetEdges *
     ************/

    @Override
    public Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts) {
        return () -> this.choose(subject, predicate, object, contexts);
    }

    /*************
     * Get lists *
     *************/

    @Override
    public Iterable<Node> subjects(Node corese_context) {

        Function<Edge, Node> convertIteratorQuadToEdge = new Function<Edge, Node>() {
            @Override
            public Node apply(Edge edge) {
                return edge.getSubjectNode();
            }
        };

        Iterator<Node> result;

        result = Iterators.transform(this.choose(null, null, null, Arrays.asList(corese_context)),
                convertIteratorQuadToEdge);

        return () -> result;
    }

    @Override
    public Iterable<Node> predicates(Node corese_context) {

        Function<Edge, Node> convertIteratorQuadToEdge = new Function<Edge, Node>() {
            @Override
            public Node apply(Edge edge) {
                return edge.getPropertyNode();
            }
        };

        Iterator<Node> result;

        result = Iterators.transform(this.choose(null, null, null, Arrays.asList(corese_context)),
                convertIteratorQuadToEdge);

        return () -> result;
    }

    @Override
    public Iterable<Node> objects(Node corese_context) {

        Function<Edge, Node> convertIteratorQuadToEdge = new Function<Edge, Node>() {
            @Override
            public Node apply(Edge edge) {
                return edge.getObjectNode();
            }
        };

        Iterator<Node> result;

        result = Iterators.transform(this.choose(null, null, null, Arrays.asList(corese_context)),
                convertIteratorQuadToEdge);

        return () -> result;
    }

    @Override
    public Iterable<Node> contexts() {

        Function<Resource, Node> convertIteratorResourceToNode = new Function<Resource, Node>() {
            @Override
            public Node apply(Resource resource) {
                return ConvertJenaCorese.jenaContextToCoreseContext(resource.asNode());
            }
        };

        Iterator<Node> result;

        Iterator<Node> temp;

        temp = Iterators.transform(this.jena_dataset.listModelNames(), convertIteratorResourceToNode);

        // Add default graph
        if (!this.jena_dataset.getDefaultModel().isEmpty()) {
            Node default_context = ConvertJenaCorese.jenaContextToCoreseContext(Quad.defaultGraphIRI);
            result = Iterators.concat(temp, Arrays.asList(default_context).iterator());
        } else {
            result = temp;
        }

        return () -> result;
    }

    /**********
     * Insert *
     **********/
    @Override
    public Iterable<Edge> insert(Node subject, Node predicate, Node object, List<Node> contexts) {
        ArrayList<Edge> added = new ArrayList<>();

        if (subject == null || predicate == null || object == null || contexts == null) {
            throw new UnsupportedOperationException("Incomplete statement");
        }

        for (Node context : contexts) {
            if (context == null) {
                this.jena_dataset.abort();
                throw new UnsupportedOperationException("Context can't be null");
            }

            Edge corese_edge = EdgeImpl.create(context, subject, predicate, object);
            Quad jena_quad = ConvertJenaCorese.edgeToQuad(corese_edge);

            if (!this.jena_dataset.asDatasetGraph().contains(jena_quad)) {
                this.jena_dataset.asDatasetGraph().add(jena_quad);
                added.add(corese_edge);
            }
        }

        return added;
    }

    /**********
     * Delete *
     **********/
    @Override
    public Iterable<Edge> delete(Node subject, Node predicate, Node object, List<Node> contexts) {
        ArrayList<Edge> edges = new ArrayList<>();

        try {
            edges = Lists.newArrayList(this.choose(subject, predicate, object, contexts));

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

    /*******************
     * Graph operation *
     *******************/

    @Override
    public boolean add(Node source, Node target, boolean silent) {
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
    public Dataset getDataset() {
        return this.jena_dataset;
    }

    @Override
    public void close() {
        this.jena_dataset.close();
    }

    /*********
     * Utils *
     *********/

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
    public Iterator<Edge> choose(Node subject, Node predicate, Node object, List<Node> contexts) {

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

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public void startReadTransaction() {
        if (isReentrant()) {
            startReentrantReadTransaction();
        }
        else {
            startBasicReadTransaction();
        }
    }
    
    @Override
    public void endReadTransaction() {
       if (isReentrant()) {
           endReentrantReadTransaction();
       }
       else {
           endBasicReadTransaction();
       }
    }

    void startBasicReadTransaction() {
        this.jena_dataset.begin(ReadWrite.READ);
    }
    
    
    void endBasicReadTransaction() {
        this.jena_dataset.end();
    }

    public synchronized void startReentrantReadTransaction() {
        int count = getReadCounter();
        if (count == 0) {
            this.jena_dataset.begin(ReadWrite.READ);
        }
        setReadCounter(count+1);
    }
    
    public synchronized void endReentrantReadTransaction() {
        int count = getReadCounter();
        if (count>0) {
            setReadCounter(count-1);
        }
        if (count == 1) {
            // count was 1, now 0
            this.jena_dataset.end();
        }
    }
    

    @Override
    public void startWriteTransaction() {
        this.jena_dataset.begin(ReadWrite.WRITE);
    }
    
    

    @Override
    public void abortTransaction() {
        this.jena_dataset.abort();
    }

    @Override
    public void endWriteTransaction() {
        this.jena_dataset.commit();
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

    @Override
    public String getStoragePath() {
        return this.storage_path;
    }

    Integer getReadCounter() {
        Integer count = threadCounter.get(Thread.currentThread());
        if (count == null) {
            threadCounter.put(Thread.currentThread(), 0);
            count = 0;
        }
        return count;
    }

    void setReadCounter(int count) {
        threadCounter.put(Thread.currentThread(), count);
    }

    public boolean isReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    @Override
    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    @Override
    public void setMetadataManager(MetadataManager metaDataManager) {
        this.metadataManager = metaDataManager;
    }
}