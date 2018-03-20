package fr.inria.corese.core.util;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.transform.Transformer;
import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

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
    private boolean isDebug = false;
   
    boolean isCheck = true;
    private boolean isUpdate = true;
    
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
        exec.setDebug(isDebug);
        return exec.query(str);
    }

    
    /**
     * Modifies a query before processing
     */
    String process(String q) throws EngineException {
        SPINProcess sp = SPINProcess.create();
        Graph g = sp.toSpinGraph(q);
        if (isCheck){
            typecheck(g);
        }
        if (isUpdate){
            update(g);
        }
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
        Transformer pp = Transformer.create(qp, Transformer.RDFTYPECHECK);
        Node res = pp.process();
        if (! res.isBlank()){
            System.out.println("Type Check:\n" + res.getLabel());
        }
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
            LogManager.getLogger(QueryManager.class.getName()).log(Level.ERROR, "", ex);
             return new Mappings();
        }
        QueryProcess exec = QueryProcess.create(g);
        exec.add(graph);
        try {
            Mappings map = exec.query(query);
            return map;
        } catch (EngineException ex) {
            LogManager.getLogger(QueryManager.class.getName()).log(Level.ERROR, "", ex);
        }
        return new Mappings();
    }

    
    
    /**
     * Graph is a SPIN Query Graph
     * Rewrite the query, eg add optional {}
     */
    
      Graph update(Graph g) {
           update(g, UPDATE + "optional.rq");
           return g;
      }
      
    Graph update2(Graph g) {
//        update(g, UPDATE + "optional.rq");
//        update(g, UPDATE + "list.rq");
//        update(g, UPDATE + "ext.rq");
        update(g, UPDATE + "path.rq");
        int size = 0;
        while (size != g.size()){
           size = g.size();
           update(g, UPDATE + "loop.rq");
        }
        update(g, UPDATE + "loopend.rq");

//        PPrinter pp = PPrinter.create(g, PPrinter.TURTLE);
//        pp.getNSM().definePrefix("sp", NSManager.SPIN);
//        System.out.println(pp);
//        System.out.println(g.display());
        return g;
    }
     
    Graph update(Graph g, String q) {
        String update;
        try {
            update = load(q);
        } catch (LoadException ex) {
            LogManager.getLogger(QueryManager.class.getName()).log(Level.ERROR, "", ex);
            return g;
        }
        QueryProcess up = QueryProcess.create(g);
        try {
            up.query(update);
        } catch (EngineException ex) {
            LogManager.getLogger(QueryManager.class.getName()).log(Level.ERROR, "", ex);
        }
        return g;
    }

    String load(String src) throws LoadException {
        InputStream stream = getClass().getResourceAsStream(src);
        if (stream == null) {
            throw LoadException.create(new IOException(src));
        }
        QueryLoad ld = QueryLoad.create();
        return ld.readWE(stream);
    }

    /**
     * @return the isDebug
     */
    public boolean isDebug() {
        return isDebug;
    }

    /**
     * @param isDebug the isDebug to set
     */
    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    /**
     * @return the isCheck
     */
    public boolean isCheck() {
        return isCheck;
    }

    /**
     * @param isCheck the isCheck to set
     */
    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    /**
     * @return the isUpdate
     */
    public boolean isUpdate() {
        return isUpdate;
    }

    /**
     * @param isUpdate the isUpdate to set
     */
    public void setUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }
}
