package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import java.util.Collection;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.rdf4j.Rdf4jValueToCoreseDatatype;

public class AddMethods {

    private static final AddMethods instance = new AddMethods();

    private AddMethods() {
    }

    public static AddMethods getInstance() {
        return instance;
    }

    /**
     * Add one statement ({@code subj}, {@code pred}, {@code obj}, {@code context})
     * in the graph {@code corese_graph}.
     * 
     * @param corese_graph Graph in which the statement is added.
     * @param subj         Subject of the statement.
     * @param pred         Predicate of the statement.
     * @param obj          Object of the statement.
     * @param context      Context of the statement.
     * @return True if the graph is modify, else false.
     */
    private Boolean loadInCoreseGraph(Graph corese_graph, IDatatype subj, IDatatype pred, IDatatype obj,
            IDatatype context) {

        Node subj_node = corese_graph.addNode(subj);
        Node pred_node = corese_graph.addProperty(pred.getLabel());
        Node obj_node = corese_graph.addNode(obj);

        Edge edge;
        if (context == null) {
            edge = corese_graph.addEdge(subj_node, pred_node, obj_node);
        } else {
            Node context_node = corese_graph.addGraph(context.getLabel());
            edge = corese_graph.addEdge(context_node, subj_node, pred_node, obj_node);
        }

        return edge != null;
    }

    /**
     * Add one or more statements to the graph.
     * 
     * This method creates a statement for each specified context and adds those to
     * the graph. If no contexts are specified, a single statement with no
     * associated context is added.
     * 
     * @param corese_graph Graph in which statements are added.
     * @param subj         Subject of the statement.
     * @param pred         Predicate of the statement.
     * @param obj          Object of the statement.
     * @param contexts     Contexts of the statement.
     * @return True if the graph is modify, else false.
     */
    public boolean addSPO(Graph corese_graph, Resource subj, IRI pred, Value obj, Resource... contexts) {

        // Convert subject, predicate, object into IDatatype
        IDatatype subj_corese = Rdf4jValueToCoreseDatatype.convert(subj);
        IDatatype pred_corese = Rdf4jValueToCoreseDatatype.convert(pred);
        IDatatype obj_corese = Rdf4jValueToCoreseDatatype.convert(obj);

        // With no graph context
        if (contexts == null || contexts.length == 0) {
            return this.loadInCoreseGraph(corese_graph, subj_corese, pred_corese, obj_corese, null);
        }

        // With one or more graph contexts
        boolean changed = false;
        for (Resource context : contexts) {
            IDatatype context_corese = Rdf4jValueToCoreseDatatype.convert(context);
            changed |= this.loadInCoreseGraph(corese_graph, subj_corese, pred_corese, obj_corese, context_corese);
        }
        return changed;
    }

    /**
     * Add one statement to the graph.
     * 
     * @param corese_graph Graph in which the statement is added.
     * @param statement    Statement to add.
     * @return True if the graph is modify, else false.
     */
    public boolean addStatement(Graph corese_graph, Statement statement) {
        Resource subject = statement.getSubject();
        IRI predicate = statement.getPredicate();
        Value object = statement.getObject();
        Resource context = statement.getContext();

        if (context != null) {
            return this.addSPO(corese_graph, subject, predicate, object, context);
        } else {
            return this.addSPO(corese_graph, subject, predicate, object);
        }
    }

    /**
     * Add all statements to graph.
     * 
     * @param corese_graph Graph in which statements are added.
     * @param statements   Collection of statements to add.
     * @return True if the graph is modify, else false.
     */
    public boolean addAll(Graph corese_graph, Collection<? extends Statement> statements) {
        boolean modify = false;
        for (Statement statement : statements) {
            modify |= this.addStatement(corese_graph, statement);
        }
        return modify;
    }

}
