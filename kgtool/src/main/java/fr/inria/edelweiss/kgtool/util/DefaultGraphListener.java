/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgtool.util;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class DefaultGraphListener implements GraphListener {

    private static final String KG = ExpType.KGRAM;
    private static final String ALL = KG + "all";
    private static final String INSERT = KG + "insert";
    private static final String DELETE = KG + "delete";
    private static final String LOAD = KG + "load";
    private static final String START = KG + "start";
    private static final String FINISH = KG + "finish";
    private static final String RESULT = KG + "result";
    private static final String[] LISTEN = {INSERT, DELETE, LOAD, START, FINISH, RESULT};
    private static final String QUERY = KG + "query";
    private static final String SPIN = NSManager.SPIN + "query";
    
    Table table;

    class Table extends HashMap<String, Boolean> {

        Table() {
            init(true);
        }

        void init(boolean b) {
            for (String name : LISTEN) {
                put(name, b);
            }
        }
    }

    DefaultGraphListener() {
        table = new Table();
    }

    public static DefaultGraphListener create() {
        return new DefaultGraphListener();
    }

    void setProperty(String name, boolean value) {
        if (name.equals(ALL)) {
            table.init(value);
        } else {
            table.put(name, value);
        }
    }

    boolean isListen(String name) {
        Boolean b = table.get(name);
        if (b == null) {
            return false;
        }
        return b;
    }

    @Override
    public void addSource(Graph g) {
    }

    @Override
    public boolean onInsert(Graph g, Entity ent) {
        return true;
    }

    @Override
    public void insert(Graph g, Entity ent) {
        if (isListen(INSERT)) {
            log("Insert: " + ent);
        }
    }

    @Override
    public void delete(Graph g, Entity ent) {
        if (isListen(DELETE)) {
            log("Delete: " + ent);
        }
    }

    @Override
    public void start(Graph g, Query q) {
        if (isListen(START)) {
            log("Start: \n" + q.getAST());
        }
        if (isListen(QUERY) || isListen(SPIN)) {
            query(g, q);
        }
    }

    @Override
    public void finish(Graph g, Query q, Mappings m) {
        if (isListen(FINISH)) {
            log("Finish:\n" + q.getAST());
            if (m != null && isListen(RESULT)) {
                log(m);
                log("map size: " + m.size());
            }
        }
    }

    @Override
    public void load(String path) {
        if (isListen(LOAD)) {
            log("Load: " + path);
        }
    }

    void log(Object str) {
        System.out.println(str);
    }

    void help() {
        log("kg:listen kg:all true|false");
        log("kg:listen kg:load true|false");
        log("kg:listen kg:start true|false");
        log("kg:listen kg:finish true|false");
        log("kg:listen kg:result true|false");
        log("kg:listen kg:insert true|false");
        log("kg:listen kg:delete true|false");
        log("kg:store  kg:query true|false");

    }

    // store query in kg:query graph
    void query(Graph g, Query q) {
        ASTQuery ast = (ASTQuery) q.getAST();
        Exp body = ast.getBody();
        if (body.size() > 0 && body.get(0).isGraph()) {
            Source src = (Source) body.get(0);
            if (src.getSource().getLabel().equals(QUERY)) {
                return;
            }
        }

        if (!(g instanceof GraphStore)) {
            return;
        }
        
        GraphStore gs = (GraphStore) g;
        Graph gg = gs.getCreateNamedGraph(QUERY);
        
        if (isListen(QUERY)){
            Node sub = gg.addBlank(gg.newBlankID());
            Node pre = gg.addProperty(QUERY);
            Node obj = gg.addLiteral(ast.toString());
            gg.addEdge(sub, pre, obj);
        }
        else if (isListen(SPIN)){
            SPINProcess sp = SPINProcess.create();
            try {
                sp.toSpinGraph(ast, gg, "kg:query");
            } catch (EngineException ex) {
                LogManager.getLogger(DefaultGraphListener.class.getName()).log(Level.ERROR, "", ex);
            }
        }

    }
}
