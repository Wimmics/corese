package fr.inria.corese.server.webservice;

import fr.inria.corese.compiler.eval.QuerySolverVisitorBasic;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.PointerObject;
import fr.inria.corese.sparql.datatype.extension.CoreseMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

/**
 * Visitor call LDScript event function @beforeRequest for SPARQL endpoint /sparql?query=
 * Call @message event function 
 * Current graph is SPARQL endpoint graph
 * Draft event function in data/demo/system/event.rq
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class QuerySolverVisitorServer extends QuerySolverVisitorBasic {
    
    static final String MESSAGE = "@message";
    static final String MESSAGE_FUN = NSManager.USER + "messenger";
    
    public QuerySolverVisitorServer() {
        super(create());
    }
    
    /**
     * Current graph is SPARQL endpoint graph.
     */
    static Eval create() {
        QueryProcess exec = QueryProcess.create(SPARQLRestAPI.getTripleStore().getGraph());
        try {
            return exec.getEval();
        } catch (EngineException ex) {
            Logger.getLogger(QuerySolverVisitorServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
    
}
