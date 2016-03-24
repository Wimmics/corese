package fr.inria.edelweiss.kgraph.query;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.update.Basic;
import fr.inria.acacia.corese.triple.update.Update;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.Workflow;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.LoadException;

/**
 * SPARQL 1.1 Update
 *
 * KGRAM Extensions:
 *
 * create/drop graph kg:entailment create graph kg:rule
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class ManagerImpl implements Manager {

    // default loader, by meta protocol to preserve modularity
    static final String LOADER = "fr.inria.edelweiss.kgtool.load.Load";
    static Logger logger = Logger.getLogger(ManagerImpl.class);
    Graph graph;
    Loader load;
    static final int COPY = 0;
    static final int MOVE = 1;
    static final int ADD = 2;
    private static final String DEFAULT = Entailment.DEFAULT;

    ManagerImpl(Graph g) {
        graph = g;
        //graph.init();
        load = getLoader(LOADER);
        load.init(graph);
    }

    ManagerImpl() {
    }

    public static ManagerImpl create(Graph g) {
        ManagerImpl m = new ManagerImpl(g);
        return m;
    }

    static Loader getLoader() {
        return getLoader(LOADER);
    }

    static Loader getLoader(String name) {
        try {
            Class<Loader> loadClass = (Class<Loader>) Class.forName(name);
            Loader ld = loadClass.newInstance();
            return ld;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean process(Query q, Basic ope, Dataset ds) {
        String uri = ope.getGraph();
        boolean isDefault = ope.isDefault();
        boolean isNamed = ope.isNamed();
        boolean isAll = ope.isAll();
        boolean isSilent = ope.isSilent();

        system(ope);

        switch (ope.type()) {

            case Update.LOAD:
                return load(q, ope);

            case Update.CREATE:
                return create(ope);

            case Update.CLEAR:
                return clear(ope, ds);

            case Update.DROP:
                return drop(ope, ds);

            case Update.ADD:
                return add(ope, ds);

            case Update.MOVE:
                return move(ope, ds);

            case Update.COPY:
                return copy(ope, ds);

            case Update.PROLOG:
                return true;

        }

        return false;

    }

    /**
     * kgraph extension:
     *
     * clear graph kg:entailment suspend entailments add graph kg:entailment
     * resume entailments
     *
     * add graph kg:rule process rules if any
     *
     */
    void system(Basic ope) {
        String uri = ope.getGraph();

        if (!isSystem(uri)) {
            return;
        }

        RuleEngine rengine = load.getRuleEngine();

        Workflow wf = graph.getWorkflow();

        switch (ope.type()) {

            case Update.DROP:

                if (isRule(uri)) {
                    // clear also the rule base
                    wf.removeEngine(Engine.RULE_ENGINE);
                }

            case Update.CLEAR:

                if (isEntailment(uri)) {
                    graph.setEntailment(false);
                } else if (isRule(uri)) {
                    wf.setActivate(Engine.RULE_ENGINE, false);
                }
                break;


            case Update.CREATE:

                if (isEntailment(uri)) {
                    graph.setEntailment(true);
                    graph.setEntail(true);
                } else if (isRule(uri)) {
                    wf.setActivate(Engine.RULE_ENGINE, true);
                    graph.setEntail(true);
                }
                break;
        }
    }

    boolean isSystem(String uri) {
        return uri != null && uri.startsWith(Entailment.KGRAPH);
    }

    boolean isEntailment(String uri) {
        return uri.equals(Entailment.ENTAIL);
    }

    boolean isRule(String uri) {
        return uri.equals(Entailment.RULE);
    }

    private boolean clear(Basic ope, Dataset ds) {
        return clear(ope, ds, false);
    }

    private boolean drop(Basic ope, Dataset ds) {
        return clear(ope, ds, true);
    }

    private boolean clear(Basic ope, Dataset ds, boolean drop) {

        if (ds != null && !ds.isEmpty()) {
            if (ds.hasNamed() && (ope.isNamed() || ope.isAll())) {
                for (Constant gg : ds.getNamed()) {
                    clear(gg, ope, drop);
                }
            }

            if (ds.hasFrom() && (ope.isDefault() || ope.isAll())) {
                for (Constant gg : ds.getFrom()) {
                    clear(gg, ope, drop);
                }
            }
        }

        if (ope.getGraph() != null) {
            graph.clear(ope.getGraph(), ope.isSilent());
            if (drop) {
                graph.deleteGraph(ope.getGraph());
            }
        } else if (ds == null || ds.isEmpty()) {
            // no prescribed dataset
            if (ope.isNamed() || ope.isAll()) {
                graph.clearNamed();
                if (drop) {
                    graph.dropGraphNames();
                }
            } else if (ope.isDefault()) {
                graph.clearDefault();
            }

        }
        return true;
    }

    void clear(Constant g, Basic ope, boolean drop) {
        graph.clear(g.getLabel(), ope.isSilent());
        if (drop) {
            graph.deleteGraph(g.getLabel());
        }
    }

    /**
     *
     * copy graph | default to target | default
     */
    private boolean update(Basic ope, Dataset ds, int mode) {
        String source = ope.getGraph();
        String target = ope.getTarget();

        if (source != null) {
            if (target != null) {
                update(ope, mode, source, target);
            } else if (ds != null && ds.hasFrom()) {
                // copy g to default
                // use from as default specification
                String name = ds.getFrom().get(0).getLabel();
                update(ope, mode, source, name);
            } else {
                // use case: move g to default
                update(ope, mode, source, DEFAULT);
            }
        } else if (target != null && ds != null && ds.hasFrom()) {
            // copy default to g
            // use from as default specification
            for (Constant gg : ds.getFrom()) {
                String name = gg.getLabel();
                update(ope, mode, name, target);
            }
        }

        return true;
    }

    private boolean update(Basic ope, int mode, String source, String target) {
        if (source.equals(target)) {
            return true;
        }

        switch (mode) {
            case ADD:
                return graph.add(source, target, ope.isSilent());
            case MOVE:
                return graph.move(source, target, ope.isSilent());
            case COPY:
                return graph.copy(source, target, ope.isSilent());
        }
        return true;
    }

    private boolean copy(Basic ope, Dataset ds) {
        return update(ope, ds, COPY);
    }

    private boolean move(Basic ope, Dataset ds) {
        return update(ope, ds, MOVE);
    }

    private boolean add(Basic ope, Dataset ds) {
        return update(ope, ds, ADD);
    }

    private boolean create(Basic ope) {
        String uri = ope.getGraph();
        graph.addGraph(uri);
        return true;
    }

    private boolean load(Query q, Basic ope) {
        if (load == null) {
            logger.error("Load " + ope.getURI() + ": Loader is undefined");
            return ope.isSilent();
        }
        String uri = ope.getURI();
        String src = ope.getTarget();
        graph.logStart(q);
        if (graph.size() == 0) {
            // graph is empty, optimize loading as if the graph is to be indexed
            // because edges are added directly
            graph.setIndex(true);
        }
        if (ope.isSilent()) {
            try {
                load.parse(uri, src);
            } catch (LoadException ex) {
                logger.error(ex);
            }
            graph.logFinish(q);
        } else {
            try {
                load.parse(uri, src);
                graph.logFinish(q);
            } catch (LoadException e) {
                
                boolean error = false;
                
                if (load.getFormat(uri) == Loader.UNDEF_FORMAT
                        && e.getException() != null
                        && e.getException().getMessage().contains("{E301}")) {
                    try {
                        //load.parse(uri, src, src, Loader.TURTLE_FORMAT);
                        load.parse(uri, src, uri, Loader.TURTLE_FORMAT);
                    } catch (LoadException ex) {
                        error = true;
                    }
                }

                if (error) {
                    logger.error("Load error: " + ope.getURI() + "\n" + e);
                    q.addError("Load error: ", ope.getURI() + "\n" + e);
                }
                graph.logFinish(q);
                return ope.isSilent();
            } finally {
                if (graph.isIndex()) {
                    graph.index();
                }
            }
        }

        if (load.isRule(uri) && load.getRuleEngine() != null) { 
            // load rule base into workflow
            // TODO ? load <rulebase.rul> into kg:workflow
            // pros: if there are several rule base load, they will be process() together
            // cons: it is stored in the workflow and run forever on update
            // (des)activate
            // pragma {kg:kgram kg:rule true/false}
            graph.addEngine(load.getRuleEngine());
            graph.setEntail(true);
        }

        return true;
    }

    @Override
    public void insert(Query query, Mappings lMap, Dataset ds) {
        Construct cons = Construct.create(query);
        cons.setDebug(query.isDebug());

        Graph gg = graph;
        gg = cons.insert(lMap, gg, ds);

        lMap.setGraph(gg);
    }

    @Override
    public void delete(Query query, Mappings lMap, Dataset ds) {
        Construct cons = Construct.create(query);
        cons.setDebug(query.isDebug());

        Graph gg = graph;
        gg = cons.delete(lMap, gg, ds);
        lMap.setGraph(gg);
    }
}
