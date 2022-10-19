package fr.inria.corese.core.logic;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 * Provide access to graph for distance processing
 */
public class BrokerDistanceDataManager extends BrokerDistance {
    static final String TOP_LEVEL_LIST = "select distinct ?y where {?x <%1$s> ?y "
            + "filter not exists {?y <%1$s> ?z}}";
    static final String TOP_LEVEL_CLASS = "select * where {values ?o {<%s>} ?s ?p ?o} limit 1";
    static final String PREDICATE = "select ?p where {values ?p {<%s>} ?x ?p ?y} limit 1";

    private DataManager dataManager;
    private boolean debug = false;

    BrokerDistanceDataManager(Graph g, DataManager man) {
        setGraph(g);
        setDataManager(man);
    }

    @Override
    Node getPropertyNode(String name) {
        String q = String.format(PREDICATE, name);
        QueryProcess exec = QueryProcess.create(getDataManager());
        try {
            Mappings map = exec.query(q);
            Node res = map.getNode("?p");
            if (isDebug())
                System.out.println("DM property: " + name + " " + res);
            return res;
        } catch (EngineException ex) {
            Distance.logger.error(ex.getMessage());
            return null;
        }
    }

    // owl:Thing or rdfs:Resource
    @Override
    Node getTopLevel(String defaut, String... nameList) {
        for (String name : nameList) {
            String q = String.format(TOP_LEVEL_CLASS, name);
            QueryProcess exec = QueryProcess.create(getDataManager());
            try {
                Mappings map = exec.query(q);
                Node res = map.getNode("?o");
                if (isDebug())
                    System.out.println("DM top: " + name + " " + res);
                if (res != null) {
                    return res;
                }
            } catch (EngineException ex) {
                Distance.logger.error(ex.getMessage());
            }
        }
        return DatatypeMap.newResource(defaut);
    }

    // top level classes y s.t.
    // x subClassOf y and not(y subClassOf z)
    @Override
    List<Node> getTopLevelList(Node predicate) {
        String q = String.format(TOP_LEVEL_LIST, predicate.getLabel());
        QueryProcess exec = QueryProcess.create(getDataManager());
        try {
            Mappings map = exec.query(q);
            List<Node> list = map.getNodeValueList("?y");
            if (isDebug())
                System.out.println("DM top level list: " + predicate + " " + list);
            return list;
        } catch (EngineException ex) {
            Distance.logger.error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    // retrieve edges of predicate where node is node at index
    // return opposite nodes
    @Override
    Iterable<Node> getNodeList(Node predicate, Node node, int index) {
        Node subject = index == 0 ? node : null;
        Node object = index == 1 ? node : null;
        int place = index == 0 ? 1 : 0;
        ArrayList<Node> list = new ArrayList<>();

        for (Edge edge : getDataManager().getEdges(subject, predicate, object, null)) {
            Node n = edge.getNode(place);
            if (!list.contains(n)) {
                list.add(n);
            }
        }
        if (isDebug())
            System.out.println("DM node list: " + node + " " + list);
        return list;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
