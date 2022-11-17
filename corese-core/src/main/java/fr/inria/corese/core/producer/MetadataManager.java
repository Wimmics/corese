package fr.inria.corese.core.producer;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.logic.Distance;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 * Corese object associated to DataManager
 * enables corese core to manage additional data and computation such as:
 * Distance, transitiveRelation
 */
public class MetadataManager {
    private static Logger logger = LoggerFactory.getLogger(MetadataManager.class);

    private DataManager dataManager;
    private Distance distance;
    private boolean debug = false;

    public MetadataManager() {
    }

    public MetadataManager(DataManager man) {
        setDataManager(man);
    }

    // called by StorageFactory
    public void startDataManager() {
        trace("create data manager");
    }

    void start() {
        try {
            QueryLoad ql = QueryLoad.create();
            String q = ql.getResource("/query/indexproperty.rq");
            QueryProcess exec = QueryProcess.create(getDataManager());
            Mappings map = exec.query(q);
            System.out.println("Storage content:\n");
            System.out.println(map);
        } catch (IOException | EngineException ex) {
            logger.error(ex.getMessage());
        }
    }

    public void endDataManager() {
        trace("end data manager");
    }

    public void startReadTransaction() {
        trace("start read");
    }

    public void endReadTransaction() {
        trace("end read");
    }

    public void startWriteTransaction() {
        trace("start write");
    }

    public void endWriteTransaction() {
        clean();
        trace("end write");
    }

    void clean() {
        setDistance(null);
    }

    public Distance getCreateDistance() {
        if (getDistance() == null) {
            setDistance(new Distance(getDataManager()));
            getDistance().start();
        }
        return getDistance();
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    // n1 subClassOf* n2
    public boolean transitiveRelation(Node n1, Node predicate, Node n2) {
        if (n1.equals(n2)) {
            return true;
        }
        return isTransitive(n1, predicate, n2, new HashMap<>());
    }

    /**
     * Take loop into account
     */
    boolean isTransitive(Node subject, Node predicate, Node object, HashMap<String, Node> t) {
        Iterable<Edge> it = getDataManager().getEdges(subject, predicate, null, null);

        if (it == null) {
            return false;
        }

        t.put(subject.getLabel(), subject);

        for (Edge ent : it) {
            Node node = ent.getNode(1);
            if (node.equals(object)) {
                return true;
            }
            if (node.equals(subject)) {
                continue;
            }
            if (t.containsKey(node.getLabel())) {
                continue;
            }
            if (isTransitive(node, predicate, object, t)) {
                return true;
            }
        }

        t.remove(subject.getLabel());

        return false;
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

    public void trace(String mes, Object... list) {
        if (isDebug()) {
            logger.info(String.format(mes, list));
        }
    }

}
