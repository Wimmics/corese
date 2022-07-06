package fr.inria.corese.storage.jenatdb1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb.TDBFactory;

import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Implements the Corese Datamanger interface for Jena-TDB.
 */
public class JenaDataManager implements DataManager, AutoCloseable {

    private Dataset jena_dataset;

    /****************
     * Constructors *
     ****************/

    /**
     * Constructor of JenaTdbDataManager. Create or connect to a Jena dataset backed
     * by an in-memory block manager. For testing.
     */
    public JenaDataManager() {
        this.jena_dataset = TDBFactory.createDataset();
    }

    /**
     * Constructor of JenaTdbDataManager. Create or connect to a Jena dataset backed
     * in file system.
     * 
     * @param directory Path of the directory where the data is stored.
     */
    public JenaDataManager(String directory) {
        this.jena_dataset = TDBFactory.createDataset(directory);
    }

    /**
     * Constructor of JenajDataManager from a Jena dataset.
     * 
     * @param jena_dataset Jena dataset.
     */
    public JenaDataManager(Dataset jena_dataset) {
        this.jena_dataset = jena_dataset;
    }

    /*********
     * Count *
     *********/

    @Override
    public int graphSize() {
        int result;

        this.jena_dataset.begin(ReadWrite.READ);
        try {

            result = (int) this.jena_dataset.asDatasetGraph().stream().count();

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

        return result;

    }

    @Override
    public int countEdges(Node predicate) {
        int result;

        this.jena_dataset.begin(ReadWrite.READ);
        try {

            // convert Corese node to Jena RdfNode
            org.apache.jena.graph.Node jena_predicate = ConvertJenaCorese.coreseNodeToJenaNode(predicate);

            result = (int) this.jena_dataset.asDatasetGraph().stream(null, null, jena_predicate, null).count();

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }
        return result;
    }

    /************
     * GetEdges *
     ************/

    @Override
    public Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts) {
        Iterable<Edge> statements;

        this.jena_dataset.begin(ReadWrite.READ);
        try {
            statements = this.choose(subject, predicate, object, contexts);

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

        // remove duplicate edges (same edge with different context)
        HashMap<Integer, Edge> result = new HashMap<>();
        for (Edge statement : statements) {
            int hash = Objects.hash(statement.getSubject(), statement.getPredicate(), statement.getObject());
            result.put(hash, statement);
        }

        return result.values();
    }

    /*************
     * Get lists *
     *************/

    @Override
    public Iterable<Node> subjects(Node corese_context) {
        Iterable<Node> result;
        this.jena_dataset.begin(ReadWrite.READ);
        try {
            result = this.choose(null, null, null, Arrays.asList(corese_context))
                    .stream()
                    .map((o) -> o.getSubjectNode())
                    .filter(distinctByKey(o -> o.getValue().stringValue()))
                    .collect(Collectors.toList());

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

        return result;
    }

    @Override
    public Iterable<Node> predicates(Node corese_context) {
        Iterable<Node> result;

        this.jena_dataset.begin(ReadWrite.READ);
        try {
            result = this.choose(null, null, null, Arrays.asList(corese_context))
                    .stream()
                    .map((o) -> o.getPropertyNode())
                    .filter(distinctByKey(o -> o.getValue().stringValue()))
                    .collect(Collectors.toList());

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

        return result;
    }

    @Override
    public Iterable<Node> objects(Node corese_context) {
        Iterable<Node> result;

        this.jena_dataset.begin(ReadWrite.READ);
        try {

            result = this.choose(null, null, null, Arrays.asList(corese_context))
                    .stream()
                    .map((o) -> o.getObjectNode())
                    .filter(distinctByKey(o -> o.getValue().stringValue()))
                    .collect(Collectors.toList());

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

        return result;
    }

    @Override
    public Iterable<Node> contexts() {
        ArrayList<Node> result = new ArrayList<>();

        this.jena_dataset.begin(ReadWrite.READ);
        try {

            Iterator<Resource> iterator = this.jena_dataset.listModelNames();

            // Add named named graph
            while (iterator.hasNext()) {
                result.add(ConvertJenaCorese.jenaContextToCoreseContext(iterator.next().asNode()));
            }

            // Add default graph
            if (!this.jena_dataset.getDefaultModel().isEmpty()) {
                result.add(ConvertJenaCorese.jenaContextToCoreseContext(Quad.defaultGraphIRI));
            }

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

        return result;
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

        this.jena_dataset.begin(ReadWrite.WRITE);
        try {

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

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

        return added;
    }

    /**********
     * Delete *
     **********/
    @Override
    public Iterable<Edge> delete(Node subject, Node predicate, Node object, List<Node> contexts) {
        ArrayList<Edge> results = new ArrayList<>();

        this.jena_dataset.begin(ReadWrite.WRITE);
        try {

            Iterable<Edge> edges = this.choose(subject, predicate, object, contexts);

            for (Edge edge : edges) {

                Quad quad = ConvertJenaCorese.edgeToQuad(edge);

                if (this.jena_dataset.asDatasetGraph().contains(quad)) {
                    this.jena_dataset.asDatasetGraph().delete(quad);
                    results.add(edge);
                }
            }

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

        return results;
    }

    /*******************
     * Graph operation *
     *******************/

    @Override
    public boolean add(Node source, Node target, boolean silent) {
        long nb_graph_before;
        long nb_graph_after;

        this.jena_dataset.begin(ReadWrite.WRITE);
        try {

            nb_graph_before = this.jena_dataset.asDatasetGraph().size();

            // Convert source and target to Jena context
            org.apache.jena.graph.Node source_context = ConvertJenaCorese.coreseContextToJenaContext(source);
            org.apache.jena.graph.Node target_context = ConvertJenaCorese.coreseContextToJenaContext(target);

            // Add Graph
            Graph graph_source = this.jena_dataset.asDatasetGraph().getGraph(source_context);
            this.jena_dataset.asDatasetGraph().addGraph(target_context, graph_source);

            nb_graph_after = this.jena_dataset.asDatasetGraph().size();

            this.jena_dataset.commit();
        } finally {
            this.jena_dataset.end();
        }

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
     * Return edge with the specified subject, predicate, object and
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
     * @return Edge that match the specified pattern.
     */
    public ArrayList<Edge> choose(Node subject, Node predicate, Node object, List<Node> contexts) {

        org.apache.jena.graph.Node jena_subject = ConvertJenaCorese.coreseNodeToJenaNode(subject);
        org.apache.jena.graph.Node jena_predicate = ConvertJenaCorese.coreseNodeToJenaNode(predicate);
        org.apache.jena.graph.Node jena_object = ConvertJenaCorese.coreseNodeToJenaNode(object);

        ArrayList<Edge> statements = new ArrayList<>();

        if (contexts == null || contexts.stream().allMatch(Objects::isNull)) {
            Iterator<Quad> iterator = this.jena_dataset.asDatasetGraph().find(
                    null, jena_subject, jena_predicate, jena_object);

            while (iterator.hasNext()) {
                statements.add(ConvertJenaCorese.quadToEdge(iterator.next()));
            }
        } else {
            for (Node context : contexts) {
                if (context != null) {
                    org.apache.jena.graph.Node jena_context = ConvertJenaCorese.coreseContextToJenaContext(context);

                    Iterator<Quad> iterator = this.jena_dataset.asDatasetGraph().find(
                            jena_context, jena_subject, jena_predicate, jena_object);

                    while (iterator.hasNext()) {
                        statements.add(ConvertJenaCorese.quadToEdge(iterator.next()));
                    }
                }
            }
        }
        return statements;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}