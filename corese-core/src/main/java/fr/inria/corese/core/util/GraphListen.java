/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.core.util;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.api.GraphListener;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;

import org.slf4j.LoggerFactory;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 * Graph Listener implements callbacks as FunSPARQL functions Manage an Eval
 * initialized with function definitions Execute callback functions with this
 * Eval
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class GraphListen implements GraphListener {

    private static final String EXT = ExpType.EXT;
    private static final String ONINSERT = EXT + "onInsert";
    private static final String INSERT = EXT + "insert";
    private static final String DELETE = EXT + "delete";
    private static final String BEGIN = EXT + "begin";
    private static final String END = EXT + "end";
    private static final String LOAD = EXT + "load";
    
    Graph graph;
    // Eval initialized with callback function definitions
    Eval eval;
    Query query;
    String init;

    GraphListen() {
    }

    /**
     * str is a query with callback function definitions
     *
     * @param str
     */
    public GraphListen(String str) {
        init = str;
    }
    
     public GraphListen(Query q) {
        query = q;
    }
     
    public GraphListen(Eval ev) {
        eval = ev;
    }

    @Override
    public void addSource(Graph g) {
        graph = g;
        init();
    }

    // create an Eval with function definitions
    void init() {
        try {
            if (query != null){
                System.out.println("Listen: " + query.getAST());
                eval = QueryProcess.createEval(graph, query);
            }
            else if (init != null) {
                System.out.println("Listen: " + init);
                eval = QueryProcess.createEval(graph, init);
            }
        } catch (EngineException ex) {
            LoggerFactory.getLogger(GraphListen.class.getName()).error(  "", ex);
        }
    }

    @Override
    public boolean onInsert(Graph g, Edge ent) {
       exec(ONINSERT, param(ent));
        return true;
    }

    @Override
    public void insert(Graph g, Edge ent) {
        exec(INSERT, param(ent));
    }

    @Override
    public void delete(Graph g, Edge ent) {
        exec(DELETE, param(ent));
    }

    @Override
    public void start(Graph g, Query q) {
        exec(BEGIN, param(q, q.getAST()));
    }

    @Override
    public void finish(Graph g, Query q, Mappings m) {
        if (m == null){
            m = Mappings.create(q);
        }
        exec(END, param(q, q.getAST(), m));
    }

    @Override
    public void load(String path) {
        exec(LOAD, param(path));
    }

    /**
     * name is a callback
     *
     * @param name
     */
    IDatatype exec(String name, IDatatype[] param) {
        //return (IDatatype) eval.eval(name, param);
        return DatatypeMap.TRUE;
    }

    IDatatype value(Object obj) {
        if (obj instanceof IDatatype) {
            return (IDatatype) obj;
        }
        return  eval.getProducer().getValue(obj);
    }

    IDatatype[] param() {
        return new IDatatype[0];
    }

    IDatatype[] param(Object dt) {
        IDatatype[] param = new IDatatype[1];
        param[0] = value(dt);
        return param;
    }

    IDatatype[] param(Object dt1, Object dt2) {
        IDatatype[] param = new IDatatype[2];
        param[0] = value(dt1);
        param[1] = value(dt2);
        return param;
    }

    IDatatype[] param(Object dt1, Object dt2, Object dt3) {
        IDatatype[] param = new IDatatype[3];
        param[0] = value(dt1);
        param[1] = value(dt2);
        param[2] = value(dt3);
        return param;
    }
}