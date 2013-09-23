package fr.inria.edelweiss.kgtool.util;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.PPrinter;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Query Manager that process and modify a query before processing
 * May embed statements in optional 
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class QueryManager {

    static final String QUERY =  "/fr/inria/edelweiss/resource/query/";
    static final String UPDATE = "/update/";
    static final String[] QUERIES = {"typecheck.rq"}; //{"triple.rq", "subject.rq", "object.rq", "filter.rq", "count.rq"};
   

    QueryProcess exec;
    Graph graph;

    QueryManager(Graph g) {
        graph = g;
        exec = QueryProcess.create(g);
    }

    public static QueryManager create(Graph g) {
        return new QueryManager(g);
    }

    public Mappings query(String q) throws EngineException {       
        String str = process(q);
        return exec.query(str);
    }

    
    /**
     * Modifies a query before processing
     */
    String process(String q) throws EngineException {
        SPINProcess sp = SPINProcess.create();
        Graph g = sp.toSpinGraph(q);
        typecheck(g);
        update(g);
        String qq = sp.toSparql(g);
        return qq;
    }

    /**
     * Type checker written using templates
     * Operates on query graph union target graph
     */
    void typecheck(Graph g){
        QueryProcess qp = QueryProcess.create(g, true);
        qp.add(graph);
        PPrinter pp = PPrinter.create(qp, PPrinter.TYPECHECK);
        System.out.println(pp.toString());
    }
    
    /**
     * Process type check queries
     */
    void query(Graph g) {
        for (String q : QUERIES) {
            Mappings map = query(g, QUERY + q);
                if (map.getQuery().isTemplate()){
                    System.out.println(map.getTemplateResult().getLabel());
                }
                else if (map.size() > 0) {
                    System.out.println(map);
                }
            }
        }
    

   

    Mappings query(Graph g, String q) {
        String query;
        try {
            query = load(q);
        } catch (LoadException ex) {
            Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
             return new Mappings();
        }
        QueryProcess exec = QueryProcess.create(g);
        exec.add(graph);
        try {
            Mappings map = exec.query(query);
            return map;
        } catch (EngineException ex) {
            Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Mappings();
    }

    
    
    /**
     * Graph is a SPIN Query Graph
     * Rewrite the query, eg add optional {}
     */
     Graph update(Graph g) {
        update(g, UPDATE + "optional.rq");
        update(g, UPDATE + "list.rq");
        update(g, UPDATE + "ext.rq");
        return g;
    }
     
    Graph update(Graph g, String q) {
        String update;
        try {
            update = load(q);
        } catch (LoadException ex) {
            Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
            return g;
        }
        QueryProcess up = QueryProcess.create(g);
        try {
            up.query(update);
        } catch (EngineException ex) {
            Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return g;
    }

    String load(String src) throws LoadException {
        InputStream stream = getClass().getResourceAsStream(src);
        if (stream == null) {
            throw LoadException.create(new IOException(src));
        }
        QueryLoad ld = QueryLoad.create();
        try {
            String str = ld.read(stream);
            return str;
        } catch (IOException ex) {
            throw LoadException.create(ex);
        }
    }
}
