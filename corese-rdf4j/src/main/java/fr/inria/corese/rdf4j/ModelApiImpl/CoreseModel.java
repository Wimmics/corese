package fr.inria.corese.rdf4j.ModelApiImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.core.Graph;
import fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl.AddMethods;
import fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl.ClearMethods;
import fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl.ContainsMethods;
import fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl.OtherMethods;
import fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl.RemoveMethods;

public class CoreseModel implements Model {

    private Graph corese_graph;

    /****************
     * Constructors *
     ****************/

    public CoreseModel() {
        this.corese_graph = Graph.create();
    }

    /*****************
     * add functions *
     *****************/

    @Override
    public boolean add(Resource subj, IRI pred, Value obj, Resource... contexts) {
        return AddMethods.getInstance().addSPO(this.corese_graph, subj, pred, obj, contexts);
    }

    @Override
    public boolean add(Statement statement) {
        return AddMethods.getInstance().addStatement(this.corese_graph, statement);
    }

    @Override
    public boolean addAll(Collection<? extends Statement> statements) {
        return AddMethods.getInstance().addAll(this.corese_graph, statements);
    }

    /*******************
     * clear functions *
     *******************/

    @Override
    public void clear() {
        ClearMethods.getInstance().clearAll(this.corese_graph);
    }

    @Override
    public boolean clear(Resource... contexts) {
        return ClearMethods.getInstance().clearGraph(this.corese_graph, contexts);
    }

    /**********************
     * contains functions *
     **********************/

    @Override
    public boolean contains(Object statement) {
        return ContainsMethods.getInstance().containsStatement(corese_graph, statement);
    }

    @Override
    public boolean containsAll(Collection<?> statements) {
        return ContainsMethods.getInstance().containsAllStatement(corese_graph, statements);
    }

    @Override
    public boolean contains(Resource subj, IRI pred, Value obj, Resource... contexts) {
        return ContainsMethods.getInstance().containsSPO(this.corese_graph, subj, pred, obj, contexts);
    }

    /********************
     * remove functions *
     ********************/

    @Override
    public boolean remove(Resource subj, IRI pred, Value obj, Resource... contexts) {
        return RemoveMethods.getInstance().removeSPO(this.corese_graph, subj, pred, obj, contexts);
    }

    @Override
    public boolean remove(Object statements) {
        return RemoveMethods.getInstance().removeStatement(this.corese_graph, statements);
    }

    @Override
    public boolean removeAll(Collection<?> statements) {
        return RemoveMethods.getInstance().removeAll(this.corese_graph, statements);
    }

    /*******************
     * other functions *
     *******************/

    @Override
    public boolean isEmpty() {
        return OtherMethods.getInstance().isEmpty(this.corese_graph);
    }

    @Override
    public int size() {
        return OtherMethods.getInstance().size(this.corese_graph);
    }

    @Override
    public Iterator<Statement> iterator() {
        return OtherMethods.getInstance().iterator(this.corese_graph);
    }

    @Override
    public Iterable<Statement> getStatements(Resource subject, IRI predicate, Value object, Resource... contexts) {
        return OtherMethods.getInstance().getStatements(this.corese_graph, subject, predicate, object, contexts);
    }

    /*******************
     *******************/

    @Override
    public boolean retainAll(Collection<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Namespace> getNamespaces() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CoreseModel unmodifiable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNamespace(Namespace namespace) {
        // TODO Auto-generated method stub

    }

    @Override
    public Optional<Namespace> removeNamespace(String prefix) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CoreseModel filter(Resource subj, IRI pred, Value obj, Resource... contexts) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Resource> subjects() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<IRI> predicates() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Value> objects() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return this.corese_graph.toString();
    }

    public Graph getCoreseGraph() {
        return this.corese_graph;
    }

}
