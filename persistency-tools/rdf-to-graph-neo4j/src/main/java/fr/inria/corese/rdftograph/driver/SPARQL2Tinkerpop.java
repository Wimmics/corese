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
         atrue = (x, y) -> true;
    }
    
    
    /**
     * 
     * @param exp: Exp(EDGE) with its filters
     * @param node: subject|property|object
     * @return Tinkerpop Predicate translation of node relevant SPARQL filters
     */
    public P getPredicate(Exp exp, int node){
        if (exp == null){
            return null;
        }
        List<Filter> list = exp.getFilters(node, ExprType.TINKERPOP);
        if (list == null || list.isEmpty()){
            return null;
        }
        return translate(list, 0);
    }
    
    P translate(List<Filter> list, int n){
        P pred = genTranslate(list.get(n).getExp());
        if (n == list.size() -1){
            return pred;
        }
        return pred.and(translate(list, n+1));
    }
    
    P genTranslate(Expr exp){
        if (isDebug()){
            System.out.println("SP2T: " + exp);
        }
        switch (exp.oper()){
            case ExprType.AND:
                return genTranslate(exp.getExp(0)).and(genTranslate(exp.getExp(1)));
                
            case ExprType.OR:
                return genTranslate(exp.getExp(0)).or(genTranslate(exp.getExp(1)));
                
            case ExprType.NOT:
                return P.not(genTranslate(exp.getExp(0)));
                
            default: return translate(exp);
        }
    }
    
    
    P translate(Expr exp){
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
                    return P.test(atrue, true);
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
            
            default: return P.test(atrue, true);
                
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
         
     
    /**
     * 
     * @return kind if isURI, isBlank, isLiteral
     */
    String getKind(Exp exp, int node){
        if (exp == null){
            return null;
        }
        List<Filter> list = exp.getFilters(node, ExprType.KIND);
        if (list == null || list.isEmpty()){
            return null;
        }
        Expr e = list.get(0).getExp();
        switch (e.oper()){
            case ExprType.ISURI:     return RdfToBdMap.IRI;
            case ExprType.ISBLANK:   return RdfToBdMap.BNODE;
            //case ExprType.ISLITERAL: return RdfToBdMap.LITERAL;
        }
        return null;
    }

}
