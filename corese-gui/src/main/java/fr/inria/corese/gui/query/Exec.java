package fr.inria.corese.gui.query;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.gui.event.MyEvalListener;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.event.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.util.SPINProcess;
import org.apache.logging.log4j.Level;

/**
 * Exec KGRAM Query in a // thread to enable interacting with EvalListener
 * through the GUI
 */
public class Exec extends Thread {

    private static Logger logger = LogManager.getLogger(Exec.class);

    static final String qvalidate = "template { st:apply-templates-with(st:spintypecheck) } where {}";
    static final String qshacl = "template { xt:turtle(?g) } where { bind (sh:shacl() as ?g) }";
    static final String qshex  = "template { xt:turtle(?g) } where { bind (sh:shex() as ?g) }";

    //static final String qvalidate = "template {st:apply-templates-with('/home/corby/AData/template/spintypecheck/template/')} where {}";
    static final String qgraph = NSManager.STL + "query";

    MainFrame frame;
    String query;
    Buffer buffer;
    MyJPanelQuery panel;
    boolean debug = false;
    private boolean validate = false;
    private boolean shacl = false;
    private boolean shex = false;
    QueryExec current;

    public Exec(MainFrame f, String q, boolean b) {
        frame = f;
        query = q;
        debug = b;
    }

    /**
     * run the thread in // the buffer is used by listener to wait for user
     * interaction with buttons: next, quit, etc.
     */
    public void process() {
        buffer = new Buffer();
        start();
    }

    /**
     * run the thread in //
     */
    @Override
    public void run() {
        Mappings res = null;
        MyJPanelQuery panel = frame.getPanel();
        if (isValidate()) {
            //res = validate();
            res = compile();
            if (res != null) {
                if (res.getQuery().isDebug()) {
                    logger.info("\n" + res.getQuery());
                }
            }
        } else {
            res = query();
        }
        frame.setBuffer(null);
        panel.display(res, getQueryExec().getQueryProcess().getBinding());
    }

    public void finish(boolean kill) {
        if (kill) {
            stop();
        } else if (current != null) {
            current.finish();
        }
    }

    void setCurrent(QueryExec exec) {
        current = exec;
    }
    
    QueryExec getQueryExec() {
        return current;
    }
    
    Mappings query() {
        QueryExec exec = QueryExec.create(frame.getMyCorese());
        setCurrent(exec);
        if (debug) {
            debug(exec);
        }
        Date d1 = new Date();
        try {
            String q = query;
            if (isShacl()) {
                q = (isShex()) ? qshex : qshacl;
            }
            Mappings l_Results = exec.SPARQLQuery(q);
            Date d2 = new Date();
            System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
            return l_Results;
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            frame.getPanel().getTextArea().setText(e.toString());
        }
        return null;
    }

    Mappings compile() {
        QueryExec exec = QueryExec.create(frame.getMyCorese());
        setCurrent(exec);
        if (debug) {
            debug(exec);
        }
        Date d1 = new Date();
        try {
            Query q = exec.compile(query);
            q.setValidate(true);
            Mappings map = exec.SPARQLQuery(q);
            Date d2 = new Date();
            System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / (1000.0));
            return map;
        } catch (EngineException e) {
            e.printStackTrace();
            frame.getPanel().getTextArea().setText(e.toString());
        }
        return null;
    }

    /**
     * Translate SPARQL query to SPIN graph Apply spin typecheck transformation
     */
    Mappings validate() {
        try {
            SPINProcess sp = SPINProcess.create();
            Graph qg = sp.toSpinGraph(query);
            qg.init();
            Graph gg = ((GraphEngine) frame.getMyCorese()).getGraph();
            gg.setNamedGraph(qgraph, qg);
            QueryProcess exec = QueryProcess.create(gg, true);
            Mappings map = exec.query(qvalidate);
            return map;
        } catch (EngineException ex) {
            LogManager.getLogger(Exec.class.getName()).log(Level.ERROR, "", ex);
        }
        return new Mappings();
    }

    /**
     * Create EvalListener
     */
    void debug(QueryExec exec) {
        MyEvalListener el = MyEvalListener.create();
        el.handle(Event.ALL, true);

        el.setFrame(frame);
        el.setUser(buffer);

        frame.setEvalListener(el);
        frame.setBuffer(buffer);

        exec.addEventListener(el);
    }

    /**
     * @return the validate
     */
    public boolean isValidate() {
        return validate;
    }

    /**
     * @param validate the validate to set
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    /**
     * @return the shacl
     */
    public boolean isShacl() {
        return shacl;
    }

    /**
     * @param shacl the shacl to set
     */
    public void setShacl(boolean shacl) {
        this.shacl = shacl;
    }

    /**
     * @return the shex
     */
    public boolean isShex() {
        return shex;
    }

    /**
     * @param shex the shex to set
     */
    public void setShex(boolean shex) {
        this.shex = shex;
    }

}
