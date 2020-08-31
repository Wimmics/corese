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

/**
 *
 * @author corby
 */
public class QuerySolverVisitorServer extends QuerySolverVisitorBasic {
    
    public QuerySolverVisitorServer() {
        super(create());
    }
    
    static Eval create() {
        QueryProcess exec = QueryProcess.create(Graph.create());
        try {
            return exec.getEval();
        } catch (EngineException ex) {
            Logger.getLogger(QuerySolverVisitorServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
    public IDatatype beforeRequest(HttpServletRequest request, String query) { 
        test(request);
        IDatatype dt = callback(getEval(), BEFORE_REQUEST, toArray(new RequestProxy(request), query));
        return dt;
    }
    
    public IDatatype afterRequest(HttpServletRequest request, String query, Mappings map) {
        IDatatype dt = callback(getEval(), AFTER_REQUEST, toArray(new RequestProxy(request), query, map));
        return dt;
    }
    
    void test(HttpServletRequest request) {
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            System.out.println("param: " + name + " " + request.getParameter(name));
        }
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
            CoreseMap map = DatatypeMap.map();
            Enumeration<String> en = request.getHeaderNames();
            while (en.hasMoreElements()) {
                String name = en.nextElement();
                map.getMap().put(DatatypeMap.newInstance(name), DatatypeMap.newInstance(request.getHeader(name)));
            }
            return map;
        }
    }
    
}
