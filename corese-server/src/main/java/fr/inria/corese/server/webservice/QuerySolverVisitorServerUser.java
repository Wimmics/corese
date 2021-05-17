package fr.inria.corese.server.webservice;

import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;



/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class QuerySolverVisitorServerUser extends QuerySolverVisitorServer {
    
    
    public QuerySolverVisitorServerUser() {
        super();
    }
    
    public QuerySolverVisitorServerUser(Eval ev) {
        super(ev);
    }
    
    
    @Override
    public IDatatype beforeRequest(HttpServletRequest request, String query) {
        System.out.println("User Defined Server Visitor before");
        System.out.println("url: " + request.getRequestURL());
        return DatatypeMap.TRUE;
    }
    
    @Override
    public IDatatype afterRequest(HttpServletRequest request, Response resp, String query, Mappings map, String res) {
        System.out.println("User Defined Server Visitor after");
        //System.out.println(map.toString(false, true, 10));
        System.out.println(res);
        return DatatypeMap.TRUE;
    }
    
    @Override
    public IDatatype afterRequest(HttpServletRequest request, String query, Mappings map) {
        System.out.println("User Defined Server Visitor after");
        //System.out.println(map.toString(false, true, 10));
        return DatatypeMap.TRUE;
    }
    
    
    void trace(HttpServletRequest request) {
//        System.out.println("path: " +request.getContextPath());
//        System.out.println("uri: " +request.getRequestURI());
//        System.out.println("host: " + request.getRemoteHost());
//        System.out.println("servlet: " + request.getServletPath());
//        System.out.println("local name: " + request.getLocalName());
//        System.out.println("path info: "+request.getPathInfo());
//        System.out.println("query string: "+request.getQueryString());
//        System.out.println("addr: "+request.getLocalAddr());
//        System.out.println("path trans: "+request.getPathTranslated());
//        System.out.println("user: "+request.getRemoteUser());
//        System.out.println("server: "+request.getServerName());
    }

}
