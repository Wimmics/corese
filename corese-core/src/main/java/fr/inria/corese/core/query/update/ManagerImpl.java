package fr.inria.corese.core.query.update;

import fr.inria.corese.core.query.Construct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.update.Basic;
import fr.inria.corese.sparql.triple.update.Update;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;

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

   
    static Logger logger = LoggerFactory.getLogger(ManagerImpl.class);
    private GraphManager graphManager;
    static final int COPY = 0;
    static final int MOVE = 1;
    static final int ADD = 2;
    
    private Level level = Level.USER_DEFAULT;
    private AccessRight accessRight;

    public ManagerImpl(GraphManager gm) {
        this.graphManager = gm;
        
    }

    ManagerImpl() {
    }

    public static ManagerImpl create(GraphManager g) {
        ManagerImpl m = new ManagerImpl(g);
        return m;
    } 

    @Override
    public boolean process(Query q, Basic ope, Dataset ds) throws EngineException  {
//        String uri = ope.getGraph();
//        boolean isDefault = ope.isDefault();
//        boolean isNamed = ope.isNamed();
//        boolean isAll = ope.isAll();
//        boolean isSilent = ope.isSilent();

        getGraphManager().system(ope);

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
    
    boolean load(Query q, Basic ope) throws  EngineException {
        return getGraphManager().load(q, ope, getLevel(), getAccessRight());
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
            getGraphManager().clear(ope.getGraph(), ope.isSilent());
            if (drop) {
                getGraphManager().deleteGraph(ope.getGraph());
            }
        } else if (ds == null || ds.isEmpty()) {
            // no prescribed dataset
            if (ope.isNamed() || ope.isAll()) {
                getGraphManager().clearNamed();
                if (drop) {
                    getGraphManager().dropGraphNames();
                }
            } else if (ope.isDefault()) {
                getGraphManager().clearDefault();
            }

        }
        return true;
    }

    void clear(Constant g, Basic ope, boolean drop) {
        getGraphManager().clear(g.getLabel(), ope.isSilent());
        if (drop) {
            getGraphManager().deleteGraph(g.getLabel());
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
                update(ope, mode, source, getGraphManager().getDefaultGraphNode().getLabel());
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
        if (target != null && source.equals(target)) {
            return true;
        }

        switch (mode) {
            case ADD:
                return getGraphManager().add(source, target, ope.isSilent());
            case MOVE:
                return getGraphManager().move(source, target, ope.isSilent());
            case COPY:
                return getGraphManager().copy(source, target, ope.isSilent());
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
        getGraphManager().addGraph(uri);
        return true;
    }

   
    @Override
    public void insert(Query query, Mappings lMap, Dataset ds) {
        Construct cons = Construct.createInsert(query, getGraphManager());
        cons.setAccessRight(getAccessRight());
        cons.setDebug(query.isDebug());
        cons.insert(lMap, ds);
        lMap.setGraph(getGraphManager().getGraph());
    }

    @Override
    public void delete(Query query, Mappings lMap, Dataset ds) {
        Construct cons = Construct.createDelete(query, getGraphManager());
        cons.setAccessRight(getAccessRight());
        cons.setDebug(query.isDebug());
        cons.delete(lMap, ds);
        lMap.setGraph(getGraphManager().getGraph());
    }

    /**
     * @return the level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * @return the accessRight
     */
    public AccessRight getAccessRight() {
        return accessRight;
    }

    /**
     * @param accessRight the accessRight to set
     */
    public void setAccessRight(AccessRight accessRight) {
        this.accessRight = accessRight;
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }

    public void setGraphManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }
}
