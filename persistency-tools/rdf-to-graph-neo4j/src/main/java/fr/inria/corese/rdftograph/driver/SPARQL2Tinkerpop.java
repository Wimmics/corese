package fr.inria.corese.rdftograph.driver;

import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 *
 * Compile SPARQL filter to Tinkerpop filter
 * Given Exp(EDGE) and node index (subject, property, object)
 * extract relevant filters that match node 
 * relevant filter: fst arg is variable same as node, 2nd arg is constant
 * compile filter as Tinkerprop predicate
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class SPARQL2Tinkerpop {

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    static final BiPredicate<String, String> cont, start, end, regex, atrue ;
    static final Processor proc ;
    
    private boolean debug = false; 
    
    /**
     * Extended Predicates for SPARQL filters
     */
    static {
         proc  = Processor.create();
         cont  = (x, y) -> x.contains(y);
         start = (x, y) -> x.startsWith(y);
         end   = (x, y) -> x.endsWith(y);
         regex = (x, y) -> proc.regex(x, y, null);
         atrue = (x, y) -> { return true; };
    }
    
    
    /**
     * 
     * @param exp: Exp(EDGE) with its filters
     * @param node: subject|property|object
     * @return Tinkerpop Predicate translation of node relevant SPARQL filters
     */
    P getPredicate(Exp exp, int node){
        return getPredicate(exp, node, ExprType.TINKERPOP, e -> filter(e));        
    }
           
     /**
     * 
     * @return isURI, isBlank, isLiteral
     */
    P getKind(Exp exp, int node){
       return getPredicate(exp, node, ExprType.KIND, e -> kind(e));        
    }
    
    
    P getPredicate(Exp exp, int node, int type, Function<Expr, P> trans){
        if (exp == null){
            return null;
        }
        List<Filter> list = exp.getFilters(node, type);
        if (list == null || list.isEmpty()){
            return null;
        }
        setDebug(exp.isDebug());
        return translate(list, 0, trans);
    }
     
      
    
    P translate(List<Filter> list, int n, Function<Expr, P> trans){
        P pred = translate(list.get(n).getExp(), trans);
        if (n == list.size() -1){
            return pred;
        }
        return pred.and(translate(list, n+1, trans));
    }
    
    
    P translate(Expr exp, Function<Expr, P> trans){
        if (isDebug()){
            System.out.println("SP2T: " + exp);
        }
        switch (exp.oper()){
            case ExprType.AND:
                return translate(exp.getExp(0), trans).and(translate(exp.getExp(1), trans));
                
            case ExprType.OR:
                return translate(exp.getExp(0), trans).or(translate(exp.getExp(1), trans));
                
            case ExprType.NOT:
                return P.not(translate(exp.getExp(0), trans));
                
            default: return trans.apply(exp);
        }
    }          
    
    /**
     * filter on VALUE slot
     * @param exp
     * @return 
     */
    P filter(Expr exp){
        DatatypeValue val = exp.getExp(1).getDatatypeValue();
                                      
        switch (exp.oper()){
            case ExprType.CONTAINS:                 
                return P.test(cont, val.stringValue()); 
                
            case ExprType.STARTS:
                return P.test(start, val.stringValue()); 
                
            case ExprType.ENDS:
                return P.test(end, val.stringValue()); 
                               
            case ExprType.REGEX: 
                if (exp.arity() == 2){
                    return P.test(regex, val.stringValue()); 
                }
                else if (exp.getExp(2).isConstant()) {
                    String mode = exp.getExp(2).getDatatypeValue().stringValue();
                    BiPredicate<String, String> reg = (x, y) -> proc.regex(x, y, mode);
                    return P.test(reg, val);             
                } 
                else {
                    return P.test(atrue, "");
                }  
                
            case ExprType.IN:
                return P.within(getInList(exp));
                
            case ExprType.EQ:
            case ExprType.SAMETERM:
                return P.eq(val.objectValue());
                
            case ExprType.NEQ:
                    return P.neq(val.objectValue());
                            
            case ExprType.LT: return P.lt(val.objectValue());
            case ExprType.LE: return P.lte(val.objectValue());
            case ExprType.GE: return P.gte(val.objectValue());
            case ExprType.GT: return P.gt(val.objectValue());
            
            default: return P.test(atrue, "");
                
        }        
    }
    
    /**
     * filter on KIND slot
     * @param exp
     * @return 
     */
    P kind(Expr exp) {
        switch (exp.oper()) {
            case ExprType.ISURI:
                return P.eq(RdfToBdMap.IRI);
            case ExprType.ISBLANK:
                return P.eq(RdfToBdMap.BNODE);
            case ExprType.ISLITERAL: 
                return P.eq(RdfToBdMap.LITERAL).or(P.eq(RdfToBdMap.LARGE_LITERAL));
            default:
                return P.test(atrue, "");
        }
    }
          
    /**
     * 
     * @param exp: ?x in (v1, .. vn)
     * @return List(v1, .. vn)
     */
    List<Object> getInList(Expr exp) {
        ArrayList<Object> ls = new ArrayList<>();
        for (Expr ee : exp.getExp(1).getExpList()) {
            ls.add(ee.getDatatypeValue().objectValue());
        }
        return ls;
    }           

}
