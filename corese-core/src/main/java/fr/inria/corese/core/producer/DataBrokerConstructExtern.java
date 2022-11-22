package fr.inria.corese.core.producer;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataBrokerConstruct;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.update.Basic;

/**
 * Broker between GraphManager and external graph DataManager
 * For SPARQL Update
 * Update -> GraphManager -> DataBrokerUpdateExtern -> DataManager -> external
 * graph
 * DataBroker is here to adapt api between GraphManager and DataManager
 * For example: Constant -> Node
 * DataBrokerUpdateExtern implement relevant subset of DataBrokerConstruct
 * 
 */
public class DataBrokerConstructExtern extends DataBrokerExtern implements DataBrokerConstruct {

    public DataBrokerConstructExtern(DataManager mgr) {
        super(mgr);
    }

    @Override
    public void startRuleEngine() {
        getDataManager().startRuleEngine();
        getDataManager().startWriteTransaction();
    }

    @Override
    public void endRuleEngine() {
        getDataManager().endWriteTransaction();
        getDataManager().endRuleEngine();
    }

    @Override
    public void startRule() {
    }

    @Override
    public void endRule() {
        getDataManager().endWriteTransaction();
        getDataManager().startWriteTransaction();
    }
    
//    @Override
//    public boolean exist(Node property, Node subject, Node object) {
//        for (Edge edge : getEdgeList(subject, property, object, null)){
//            return true;
//        }
//        return false;
//    }

    @Override
    public String blankNode() {
        return getDataManager().blankNode();
    }

    /**
     * Delete occurrences of edge in named graphs of from list
     * keep other occurrences
     * edge has no named graph
     * Return list of deleted edges
     * 
     * @todo: Constant -> IDatatype as Node
     */
    @Override
    public List<Edge> delete(Edge edge, List<Constant> from) {
        List<Edge> deleted = new ArrayList<>();
        Iterable<Edge> it = getDataManager().delete(edge.getSubjectNode(), edge.getProperty(), edge.getObjectNode(),
                nodeList(from));
        if (it != null) {
            it.forEach(deleted::add);
        }
        return deleted;
    }

    @Override
    public boolean load(Query q, Basic ope, Access.Level level, AccessRight access) throws EngineException {
        Graph g = Graph.create();
        Load load = Load.create(g);
        load.setDataManager(getDataManager());
        load.setSparqlUpdate(true);
        try {
            load.parse(ope.getURI(), ope.getTarget());
        } catch (LoadException ex) {
            throw new EngineException(ex);
        }
        return true;
    }

    List<Node> nodeList(List<Constant> list) {
        ArrayList<Node> nodeList = new ArrayList<>();
        for (Constant cst : list) {
            nodeList.add(cst.getDatatypeValue());
        }
        return nodeList;
    }

}
