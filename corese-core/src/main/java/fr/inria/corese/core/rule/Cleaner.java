package fr.inria.corese.core.rule;

import static fr.inria.corese.core.util.Property.Value.LOG_RULE_CLEAN;
import static fr.inria.corese.core.util.Property.Value.OWL_CLEAN_QUERY;

import java.io.IOException;
import java.util.Date;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;

/**
 * Remove redundant bnodes from an RDF/OWL graph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Cleaner {
    public static final int OWL = 0;
    static final String data = "/query/clean/";
    static final String[] queries = { "allsome.rq", "card.rq", "intersection.rq", "union.rq" };

    Graph graph;
    private DataManager dataManager;
    private ProcessVisitor visitor;
    private boolean debug = false;

    public Cleaner(Graph g, DataManager man) {
        graph = g;
        setDataManager(man);
    }

    public Cleaner(Graph g) {
        graph = g;
    }

    void clean(int mode) throws IOException, EngineException, LoadException {
        switch (mode) {

            case OWL:
                process();
                break;
        }
    }

    public void process() throws IOException, EngineException, LoadException {
        clean(graph, queries, true);
        if (Property.stringValue(OWL_CLEAN_QUERY) != null) {
            clean(graph, Property.stringValueList(OWL_CLEAN_QUERY), false);
        }
    }

    /**
     * Replace different bnodes that represent same OWL expression
     * by same bnode
     */
    void clean(Graph g, String[] lq, boolean resource) throws IOException, EngineException, LoadException {
        Date d1 = new Date();
        QueryLoad ql = QueryLoad.create();
        QueryProcess exec = QueryProcess.create(g, getDataManager());
        // escape QueryProcess write lock in case
        // RuleEngine was run by Workflow Manager by init() by query()
        // because query() have read lock
        // it works because init() is also synchronized
        exec.setSynchronized(true);
        if (getDataManager()!=null){
            exec.setProcessTransaction(false);
        }
        for (String q : lq) {
            //RuleEngine.logger.info("clean: " + q);
            String qq = (resource) ? ql.getResource(data + q) : ql.readWE(q);
            // RuleEngine.logger.info("clean: " + qq);
             try {
           Mappings map = exec.query(qq, createMapping(getVisitor()));
             
             if (Property.booleanValue(LOG_RULE_CLEAN) && map.size() > 0) {
                RuleEngine.logger.info(
                        String.format("Clean: %s solutions\n%s", map.size(), qq));
            }
            if (isDebug()) {
                RuleEngine.logger.info(q + " nb res: " + map.size());
            }
            }
             catch(Exception e){
                 RuleEngine.logger.equals(e);
                 throw e;
             }
        }
        Date d2 = new Date();
        System.out.println("Clean: " + ((d2.getTime() - d1.getTime()) / 1000.0));
    }

    Mapping createMapping(ProcessVisitor vis) {
        if (vis == null) {
            return null;
        }
        Binding b = Binding.create();
        b.setVisitor(vis);
        return Mapping.create(b);
    }

    /**
     * Replace duplicate OWL expressions by one of them
     * DRAFT
     */
    void owlrlFull() throws IOException, EngineException {
        QueryLoad ql = QueryLoad.create();
        QueryProcess exec = QueryProcess.create(graph);
        String unify = ql.getResource("/query/unify2.rq");
        // remove triples with obsolete bnodes as subject
        String clean = ql.getResource("/query/clean.rq");
        // tell Transformer to cache st:hash transformation result
        exec.getEvaluator().setMode(Evaluator.CACHE_MODE);
        // replace duplicate OWL expressions by one of them
        Mappings m1 = exec.query(unify);
        Mappings m2 = exec.query(clean);
        exec.getEvaluator().setMode(Evaluator.NO_CACHE_MODE);
    }

    /**
     * @return the visitor
     */
    public ProcessVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

}
