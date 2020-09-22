package fr.inria.corese.server.webservice;

import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import javax.servlet.http.HttpServletRequest;



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
        System.out.println("User Defined Server Visitor before: " + query);
        return DatatypeMap.TRUE;
    }
    
    @Override
    public IDatatype afterRequest(HttpServletRequest request, String query, Mappings map) {
        System.out.println("User Defined Server Visitor after: " + map);
        return DatatypeMap.TRUE;
    }

}
