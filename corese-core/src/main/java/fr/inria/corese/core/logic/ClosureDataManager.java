package fr.inria.corese.core.logic;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Distinct;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.List;

/**
 * 
 * @author corby
 */
public class ClosureDataManager extends Closure {

    private DataManager dataManager;

    ClosureDataManager(Graph g, Distinct d) {
        super(g, d);
    }
    
    public ClosureDataManager(Graph g, DataManager man, Distinct d) {
        this(g, d);
        setDataManager(man);
    }
     
    @Override
    Node ruleGraphNode() {
        return DatatypeMap.newResource(Entailment.RULE);
    }

    @Override
    Node propertyNode(Node p) {
        return p;
    }

    @Override
    Iterable<Edge> getEdges(Node p) {
        return getDataManager().getEdges(null, p, null, null);
    }

    // i = 0
    @Override
    Iterable<Edge> getEdges(Node p, Node n, int i) {
        return getDataManager().getEdges(n, p, null, null);
    }
      
    // edge index >= index
    @Override
    Iterable<Edge> getEdges(Node p, Node n, int i, int index) {
        if (isFilterEdgeIndex()) {
            return getDataManager().getEdges(n, p, null, null, ExprType.GE, index);
        }
        return getEdges(p, n, i);
    }

    @Override
    void insert(Node p, List<Edge> edgeList) {
        for (Edge edge : edgeList) {
            getDataManager().insert(edge);
        }
    }

    @Override
    boolean exist(Node p, Node n1, Node n2) {
        for (Edge edge : getDataManager().getEdges(n1, p, n2, null)) {
            return true;
        }
        return false;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

}
