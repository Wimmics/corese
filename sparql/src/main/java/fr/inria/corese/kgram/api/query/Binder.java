
package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import java.util.List;

/**
 *
 * @author corby
 */
public interface Binder {
    
    void clear();
    List<Expr> getVariables();
    boolean hasBind();
    boolean isBound(String label);
    Node get(Expr var);
    IDatatype getGlobalVariable(String var);
    //void bind(Expr exp, Expr var, Node val);
//    void set(Expr exp, Expr var, Node val);
//    void set(Expr exp, List<Expr> lvar, Node[] value);
    //void unset(Expr exp, Expr var, Node value);
    //void unset(Expr exp, List<Expr> lvar);
    void share(Binder b);
    void setVisitor(ProcessVisitor vis);
    ProcessVisitor getVisitor();
    StringBuilder getTrace();
    Mappings getMappings();
    Binder setMappings(Mappings map);
}
