package fr.inria.corese.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.api.Engine;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.sparql.exceptions.EngineException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * Manage a set of inference engines Loop until no inference is performed It is
 * automatically called when isUpdate==true to perform inference and restore
 * consistency
 *
 */
public class Workflow implements Engine {
    private static Logger logger = LoggerFactory.getLogger(Workflow.class);

    Graph graph;
    ArrayList<Engine> engines;
    // RDFS entailment
    private Entailment inference;
    boolean isDebug = false,
            isIdle = true,
            isActivate = true,
            isClearEntailment = false;

    Workflow(Graph g) {
        engines = new ArrayList<Engine>();
        graph = g;
    }

    void addEngine(Engine e) {
        if (!engines.contains(e)) {
            engines.add(e);
            e.init();
        }
    }

    public List<Engine> getEngines() {
        return engines;
    }

    public void removeEngine(Engine e) {
        engines.remove(e);
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    public void setClearEntailment(boolean b) {
        isClearEntailment = b;
    }

    /**
     * When isUpdate==true manager.process() is called before executing a query
     * to perform inference
     */
    @Override
    public synchronized boolean process() throws EngineException {
        boolean b = false;
        if (isAvailable()) {
            isIdle = false;
            b = run();
            isIdle = true;
        }
        return b;
    }

    public synchronized boolean process(Engine e) throws EngineException {
        boolean b = false;
        if (isBasicAvailable()) {
            isIdle = false;
            b = run(e);
            isIdle = true;
        }
        return b;
    }
    
    boolean isAvailable() {
        return isBasicAvailable() && engines.size() > 0;
    }
    
     boolean isBasicAvailable() {
        return isActivate && isIdle ;
    }
    
    EventManager getEventManager() {
        return graph.getEventManager();
    }

    /**
     * Run submitted engines until no inference is performed
     */
    boolean run() throws EngineException {
        int count = 2;
        boolean isSuccess = false;

        getEventManager().start(Event.WorkflowEngine);

        while (count > 1) {

            count = 0;

            for (Engine e : engines) {
                if (e.isActivate()) {
                    boolean b = e.process();
                    if (b) {
                        isSuccess = true;
                        count++;
                    }
               }
            }
        }

        getEventManager().finish(Event.WorkflowEngine);
        return isSuccess;
    }

    /**
     * Run engine and submitted engines until no inference is performed
     */
    boolean run(Engine e) throws EngineException {
        int size = 0;
        boolean isSuccess = false;
        int count = 2;

        while (count > 1) {
            count = 0;

            if (isDebug) {
                System.out.println("** W run: " + e.getClass().getName());
            }

            if (e.isActivate()) {
                boolean b = e.process();
                if (b) {
                    isSuccess = true;
                    count++;
                }
            }

            boolean b = run();
            if (b) {
                isSuccess = true;
                count++;
            }
        }

        return isSuccess;
    }

    @Override
    public void init() {
        for (Engine e : engines) {
            e.init();
        }
    }

    @Override
    public void onDelete() {
        if (isClearEntailment) {
            remove();
        }

        for (Engine e : engines) {
            e.onDelete();
        }
    }

    @Override
    public void onInsert(Node gNode, Edge edge) {
        for (Engine e : engines) {
            e.onInsert(gNode, edge);
        }
    }

    @Override
    public void onClear() {
        for (Engine e : engines) {
            e.onClear();
        }
    }

    /**
     * Remove entailments
     */
    @Override
    public void remove() {
        for (Engine e : engines) {
            e.remove();
        }
    }

    @Override
    public void setActivate(boolean b) {
        isActivate = b;
    }

    @Override
    public boolean isActivate() {
        return isActivate;
    }

    @Override
    public int type() {
        return WORKFLOW_ENGINE;
    }

    public void clear() {
        engines.clear();
    }

    public void removeEngine(int type) {
        for (int i = 0; i < engines.size();) {
            if (engines.get(i).type() == type) {
                engines.remove(engines.get(i));
            } else {
                i++;
            }
        }
    }

    public void setActivate(int type, boolean b) {

        for (Engine e : engines) {
            if (e.type() == type) {
                e.setActivate(b);
            }
        }
    }

    public Entailment getEntailment() {
        return inference;
    }

    public void setEntailment(Entailment inference) {
        this.inference = inference;
    }

    public void setEntailment() {
        Entailment i = Entailment.create(graph);
        setEntailment(i);
        addEngine(i);
    }

    public void setRDFSEntailment(boolean b) {
        pragmaRDFSentailment(b);
        if (!b) {
            getEntailment().remove();
        }
    }

    public void pragmaRDFSentailment(boolean b) {
        if (b) {
            if (getEntailment() == null) {
                setEntailment();
            } else {
                getEntailment().setActivate(true);
            }
        } else if (getEntailment() != null) {
            getEntailment().setActivate(false);
        }
    }

}
