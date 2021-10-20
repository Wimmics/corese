package fr.inria.corese.core.producer;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataBrokerConstruct;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
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
 * Update -> GraphManager -> DataBrokerUpdateExtern -> DataManager -> external graph
 * DataBroker is here to adapt api between GraphManager and DataManager
 * For example: Constant -> Node
 * DataBrokerUpdateExtern implement relevant subset of DataBrokerConstruct
 * 
 */
public class DataBrokerConstructExtern extends DataBrokerExtern implements DataBrokerConstruct {

    public DataBrokerConstructExtern(DataManager mgr) {
        super(mgr);
    }

    /**
     * Delete occurrences of edge in named graphs of from list
     * keep other occurrences
     * edge has no named graph
     * Return list of deleted edges
     * @todo: Constant -> IDatatype as Node
     */  
    @Override
    public List<Edge> delete(Edge edge, List<Constant> from) {
        return getDataManager().delete(edge, nodeList(from));
    }

    @Override
    public boolean load(Query q, Basic ope, Access.Level level, AccessRight access) throws EngineException {
        Graph g = Graph.create();
        Load load = Load.create(g);
        load.setDataManager(getDataManager());
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
