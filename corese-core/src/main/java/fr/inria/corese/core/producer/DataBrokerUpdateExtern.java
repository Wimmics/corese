package fr.inria.corese.core.producer;

import fr.inria.corese.core.api.DataBrokerConstruct;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.update.Basic;
import java.util.ArrayList;
import java.util.List;

/**
 * Broker between GraphManager and external graph DataManager
 * For SPARQL Update
 * Update -> GraphManager -> DataBroker -> DataManager -> external graph
 * DataBroker is here to adapt api between GraphManager and DataManager
 * For example: Constant -> Node
 */
public class DataBrokerUpdateExtern implements DataBrokerConstruct {
    
    private DataManager dataManager;
    
    public DataBrokerUpdateExtern(DataManager mgr) {
        setDataManager(mgr);
    }
    
    
    /**
     * Return null if edge already exists 
     */
    @Override
    public Edge insert(Edge edge) {
        return getDataManager().insert(edge);
    }
    
    /**
     * If Edge have a named graph: delete this occurrence
     * Otherwise: delete all occurrences of edge 
     * Return list of deleted edge
     */
    @Override
    public List<Edge> delete(Edge edge) {
        return getDataManager().delete(edge);
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
        return getDataManager().load(q, ope);
    }
    
    
    
    
    
    List<Node> nodeList(List<Constant> list) {
        ArrayList<Node> nodeList = new ArrayList<>();
        for (Constant cst : list) {
            nodeList.add(cst.getDatatypeValue());
        }
        return nodeList;
    }
    

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    
}
