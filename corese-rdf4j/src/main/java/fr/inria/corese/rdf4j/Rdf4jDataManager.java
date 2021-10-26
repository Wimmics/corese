package fr.inria.corese.rdf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

public class Rdf4jDataManager implements DataManager {

    private Model rdf4j_model;

    /****************
     * Constructors *
     ****************/

    /**
     * Constructor of Rdf4jDataManager from a RDF4J Model.
     * 
     * @param rdf4j_model RDF4J model.
     */
    public Rdf4jDataManager(Model rdf4j_model) {
        this.rdf4j_model = rdf4j_model;
    }

    /*********
     * Count *
     *********/

    @Override
    public int graphSize() {
        return this.rdf4j_model.size();
    }

    @Override
    public int countEdges(Node predicate) {
        // convert Corese node to RDF4J Value
        IRI rdf4j_predicate = (IRI) Convert.coreseNodeToRdf4jValue(predicate);

        return this.rdf4j_model.filter(null, rdf4j_predicate, null).size();
    }

    /************
     * GetEdges *
     ************/

    @Override
    public Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts) {
        Iterable<Statement> statements = this.choose(subject, predicate, object, contexts);

        // convert Statements to Edges
        // remove duplicate edges (same edge with different context)
        HashMap<Integer, Edge> result = new HashMap<>();
        for (Edge statement : Convert.statementsToEdges(statements)) {
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
        Set<Resource> subjects;

        // if corese_context is null then match with all contexts in graph
        if (corese_context == null) {
            subjects = this.rdf4j_model.subjects();
        } else {
            Resource rdf4j_context = Convert.coreseContextToRdf4jContext(corese_context);
            subjects = this.rdf4j_model.filter(null, null, null, rdf4j_context).subjects();
        }

        return Convert.rdf4jvaluestoCoresNodes(new ArrayList<>(subjects));
    }

    @Override
    public Iterable<Node> predicates(Node corese_context) {
        Set<IRI> predicates;

        // if corese_context is null then match with all contexts in graph
        if (corese_context == null) {
            predicates = this.rdf4j_model.predicates();
        } else {
            Resource rdf4j_context = Convert.coreseContextToRdf4jContext(corese_context);
            predicates = this.rdf4j_model.filter(null, null, null, rdf4j_context).predicates();
        }

        return Convert.rdf4jvaluestoCoresNodes(new ArrayList<>(predicates));
    }

    @Override
    public Iterable<Node> objects(Node corese_context) {
        Set<Value> objects;

        // if corese_context is null then match with all contexts in graph
        if (corese_context == null) {
            objects = this.rdf4j_model.objects();
        } else {
            Resource rdf4j_context = Convert.coreseContextToRdf4jContext(corese_context);
            objects = this.rdf4j_model.filter(null, null, null, rdf4j_context).objects();
        }

        return Convert.rdf4jvaluestoCoresNodes(new ArrayList<>(objects));
    }

    @Override
    public Iterable<Node> contexts() {
        Set<Resource> rdf4j_contexts = this.rdf4j_model.contexts();
        Node[] corese_contexts = Convert
                .rdf4jContextsToCoreseContexts(rdf4j_contexts.toArray(new Resource[rdf4j_contexts.size()]));
        return Arrays.asList(corese_contexts);
    }

    /**********
     * Insert *
     **********/
    @Override
    public Iterable<Edge> insert(Node subject, Node predicate, Node object, List<Node> contexts) {

        if (subject == null || predicate == null || object == null || contexts == null) {
            throw new UnsupportedOperationException("Incomplete statement");
        }

        ArrayList<Edge> added = new ArrayList<>();
        for (Node context : contexts) {
            if (context == null) {
                throw new UnsupportedOperationException("Context can't be null");
            }

            Edge edge = EdgeImpl.create(context, subject, predicate, object);
            if (this.rdf4j_model.add(edge)) {
                added.add(edge);
            }
        }
        return added;
    }

    /****************
     * Delete edges *
     ****************/
    @Override
    public Iterable<Edge> delete(Node subject, Node predicate, Node object, List<Node> contexts) {
        Iterable<Statement> statements = this.choose(subject, predicate, object, contexts);

        ArrayList<Edge> results = new ArrayList<>();
        for (Statement statement : statements) {
            if (this.rdf4j_model.remove(statement)) {
                results.add(Convert.statementToEdge(statement));
            }
        }

        return results;
    }

    /*******************
     * Graph operation *
     *******************/
    @Override
    public boolean add(Node source, Node target, boolean silent) {
        // convert source and target to RDF4J context
        ValueFactory vf = SimpleValueFactory.getInstance();
        Resource source_context = Convert.coreseContextToRdf4jContext(source);
        Resource target_context = Convert.coreseContextToRdf4jContext(target);

        // get list of statement in source context
        ArrayList<Statement> source_statements = new ArrayList<>();
        this.rdf4j_model.getStatements(null, null, null, source_context).forEach(source_statements::add);

        boolean is_modified = false;
        for (Statement source_statement : source_statements) {
            // build statement to add
            Statement target_statement = vf.createStatement(source_statement.getSubject(),
                    source_statement.getPredicate(), source_statement.getObject(), target_context);
            // add statement in target context
            if (this.rdf4j_model.add(target_statement)) {
                is_modified = true;
            }
        }

        return is_modified;
    }

    /*********
     * Utils *
     *********/

    /**
     * Convert a list of node to a equivalent java array.
     * 
     * @param list List of node to cnvert.
     * @return Equivalent java array.
     */
    private Node[] ConvertToJavaArrayNode(List<Node> list) {
        if (list == null) {
            return null;
        } else {
            return list.toArray(new Node[list.size()]);
        }
    }

    /**
     * Return statements with the specified subject, predicate, object and
     * (optionally) context exist in this model. The subject, predicate, object and
     * context parameters can be null to indicate wildcards. The contexts parameter
     * is a wildcard and accepts zero or more values. If contexts is {@code null},
     * statements will match disregarding their context. If one or more contexts are
     * specified, statements with a context matching one of these will match.
     * 
     * @param subject   The subject of the statements to match, null to match
     *                  statements with any subject.
     * @param predicate Predicate of the statements to match, null to match
     *                  statements with any predicate.
     * @param object    Object of the statements to match, null to match statements
     *                  with any object.
     * @param contexts  Contexts of the statements to match, null to match
     *                  statements with any contexts.
     * @return Statements that match the specified pattern.
     */
    private Iterable<Statement> choose(Node subject, Node predicate, Node object, List<Node> contexts) {
        // convert Corese node to RDF4J Value
        Resource rdf4j_subject = (Resource) Convert.coreseNodeToRdf4jValue(subject);
        IRI rdf4j_predicate = (IRI) Convert.coreseNodeToRdf4jValue(predicate);
        Value rdf4j_object = Convert.coreseNodeToRdf4jValue(object);

        // convert list of corese contexts to list of RDF4J contexts
        Resource[] rdf4j_contexts = Convert.coreseContextsToRdf4jContexts(this.ConvertToJavaArrayNode(contexts));

        ArrayList<Statement> statements = new ArrayList<>();
        this.rdf4j_model.getStatements(rdf4j_subject, rdf4j_predicate, rdf4j_object, rdf4j_contexts)
                .forEach(statements::add);

        return statements;
    }

}
