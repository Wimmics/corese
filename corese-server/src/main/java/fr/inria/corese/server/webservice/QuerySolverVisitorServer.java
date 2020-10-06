package fr.inria.corese.server.webservice;

import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.PointerObject;
import fr.inria.corese.sparql.datatype.extension.CoreseMap;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Visitor call LDScript event function @beforeRequest for SPARQL endpoint /sparql?query=
 * Call @message event function 
 * Current graph is SPARQL endpoint graph
 * Draft event function in data/demo/system/event.rq
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class QuerySolverVisitorServer extends QuerySolverVisitor {
    static private final Logger logger = LogManager.getLogger(QuerySolverVisitorServer.class);
    
    static final String MESSAGE = "@message";
    static final String MESSAGE_FUN = NSManager.USER + "messenger";
    
    public QuerySolverVisitorServer() {
        super();
    }
    
    public QuerySolverVisitorServer(Eval ev) {
        super(ev);
    }
    
    Graph getGraph() {
        return SPARQLRestAPI.getTripleStore().getGraph();
    }
    
   
    public IDatatype beforeRequest(HttpServletRequest request, String query) { 
        IDatatype dt = callback(getEval(), BEFORE_REQUEST, toArray(request, query));
        return dt;
    }
    
    public IDatatype afterRequest(HttpServletRequest request, String query, Mappings map) {
        IDatatype dt = callback(getEval(), AFTER_REQUEST, toArray(request, query, map));
        return dt;
    }
    
    /**
     * /agent?action=test
     * funcall @message function us:message(request) {}
     * Draft event function in webapp/data/demo/system/event.rq
     * loaded by st:default service content in profile.ttl
     */
    public IDatatype message(HttpServletRequest request) { 
        //IDatatype dt = funcall(getEval(), MESSAGE_FUN, toArray(request));
        IDatatype dt = callback(getEval(), MESSAGE, toArray(request));
        return dt;
    }
    
 
    void test(HttpServletRequest request) {
        ServletContext cn = request.getServletContext();      
    }
    
    class RequestProxy extends PointerObject {
        
        HttpServletRequest request;
        
        
        public HttpServletRequest getRequest() {
            return request;
        }
        
        RequestProxy(HttpServletRequest r) {
            super(r);
            request = r;
        }
        
        
        public IDatatype getParam() {
            CoreseMap map = DatatypeMap.map();
            Enumeration<String> en = request.getParameterNames();
            while (en.hasMoreElements()) {
                String name = en.nextElement();
                map.getMap().put(DatatypeMap.newInstance(name), DatatypeMap.newInstance(request.getParameter(name)));
            }
            return map;
        }

        @Override
        public Iterable getLoop() {
            return getParam();
        }
        
        
    }
    
    static String getVisitorName() {
        return QueryProcess.getServerVisitorName();
    }
    
    public static QuerySolverVisitorServer create(Eval eval) {
        if (getVisitorName() == null) {
            return new QuerySolverVisitorServer(eval);
        }
        QuerySolverVisitorServer vis = create(eval, getVisitorName());
        if (vis == null) {
            return new QuerySolverVisitorServer(eval);
        }
        return vis;
    }

    static QuerySolverVisitorServer create(Eval eval, String name) {
        try {
            Class visClass = Class.forName(name);
            Object obj = visClass.getDeclaredConstructor(Eval.class).newInstance(eval);
            if (obj instanceof QuerySolverVisitorServer) {
                return (QuerySolverVisitorServer) obj;
            } else {
                logger.error("Uncorrect Visitor: " + name);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            java.util.logging.Logger.getLogger(QueryProcess.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            logger.error("Undefined Visitor: " + name);
        }

        return null;
    }
    
}
