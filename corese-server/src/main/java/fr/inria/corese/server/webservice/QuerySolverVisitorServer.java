package fr.inria.corese.server.webservice;

import fr.inria.corese.compiler.eval.QuerySolverVisitorBasic;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.PointerObject;
import fr.inria.corese.sparql.datatype.extension.CoreseMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

/**
 *
 * @author corby
 */
public class QuerySolverVisitorServer extends QuerySolverVisitorBasic {
    
    static final String MESSAGE = "@message";
    
    public QuerySolverVisitorServer() {
        super(create());
    }
    
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
    
    public IDatatype message(HttpServletRequest request) { 
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
