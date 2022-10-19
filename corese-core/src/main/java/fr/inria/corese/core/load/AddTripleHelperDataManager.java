package fr.inria.corese.core.load;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 */
public class AddTripleHelperDataManager extends AddTripleHelper {

    private DataManager dataManager;

    public AddTripleHelperDataManager(Graph g, DataManager man) {
        super(g);
        setDataManager(man);
    }

    @Override
    String blankNode() {
        return getDataManager().blankNode();
    }

    Node node(String name) {
        return DatatypeMap.newResource(name);
    }

    @Override
    Node addGraph(String name) {
        return node(name);
    }

    @Override
    Node addDefaultGraphNode() {
        return node(Entailment.DEFAULT);
    }

    @Override
    void addGraphNode(Node node) {
    }

    @Override
    Edge create(Node g, Node s, Node p, Node o) {
        return getGraph().create(g, s, p, o);
    }

    @Override
    Edge addEdge(Edge e) {
        return getDataManager().insert(e);
    }

    @Override
    Node addProperty(String p) {
        return node(p);
    }

    @Override
    Node addLiteral(String predicate, String value, String type, String lang) {
        return getGraph().createLiteral(predicate, value, type, lang);
    }

    @Override
    Node addResource(String name) {
        return node(name);
    }

    @Override
    Node addBlank(String id) {
        return DatatypeMap.createBlank(id);
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

}
