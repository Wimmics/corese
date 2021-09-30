package fr.inria.corese.core.visitor.solver;

import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author corby
 */
public class QuerySolverVisitorTransformerUser extends QuerySolverVisitorTransformer {
    
    public QuerySolverVisitorTransformerUser() {}
    
    public QuerySolverVisitorTransformerUser(Transformer t, Eval e) { super(t, e); }
    
    public QuerySolverVisitorTransformerUser(Eval e) { super(e); }

    
    @Override
    public IDatatype afterTransformer(String uri, String res) {
        System.out.println("User defined transformer: " + uri);
        System.out.println(res);
        return DatatypeMap.TRUE;
    }


}
    
