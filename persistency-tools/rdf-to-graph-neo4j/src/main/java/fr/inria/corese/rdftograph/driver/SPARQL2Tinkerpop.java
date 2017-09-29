package fr.inria.corese.rdftograph.driver;

import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;

/**
 * Compile SPARQL filter to Tinkerpop filter
 * Given Exp(EDGE) and node index (subject, property, object)
 * extract relevant filters that match node 
 * relevant filter: fst arg is variable same as node, 2nd arg is constant
 * compile filter as Tinkerprop predicate
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
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
    GraphTraversal<? extends Element, ? extends Element>
        getPredicate(Exp exp, int node) {
        return getPredicate(exp, node, getType(node));        
    }
        
    int getType(int node) {
        if (node == Exp.PREDICATE) {
            return ExprType.TINKERPOP_RESTRICT;
        }
        return ExprType.TINKERPOP;
    }
               
    GraphTraversal<? extends Element, ? extends Element>
         getPredicate(Exp exp, int node, int type){
        if (exp == null){
            return null;
        }
        List<Filter> list = exp.getFilters(node, type);
        if (list == null || list.isEmpty()){
            return null;
        }
        setDebug(exp.isDebug());
        return translate(list, 0, node);
    }
     
      
    GraphTraversal<? extends Element, ? extends Element>
         translate(List<Filter> list, int n, int node){
        GraphTraversal<? extends Element, ? extends Element> pred = translate(list.get(n).getExp(), node);
        if (n == list.size() -1){
            return pred;
        }
        return pred.and(translate(list, n+1, node));
    }
    
    
    GraphTraversal<? extends Element, ? extends Element>
         translate(Expr exp, int node){
        if (isDebug()){
            System.out.println("SP2T: " + exp);
        }
        switch (exp.oper()){
            case ExprType.AND:
                return __.and(translate(exp.getExp(0), node), translate(exp.getExp(1), node));
                
            case ExprType.OR:
                return __.or(translate(exp.getExp(0), node), translate(exp.getExp(1), node));
                
            case ExprType.NOT:
                return __.not(translate(exp.getExp(0), node));
                
            default:
                return filter(exp, node);
        }
    }          
    
    /**
     * This Neo4j mapping splits RDF terms in several slots:
     * value, kind, datatype, lang
     */        
    GraphTraversal<? extends Element, Edge> getTraversal(Expr exp, P p, int node) {
        if (exp.match(ExprType.KIND)) {
            return __.has(KIND, p);
        }

        if (exp.arity() >= 1) {
            switch (exp.getExp(0).oper()) {
                case ExprType.DATATYPE:
                    // datatype(var) is slot TYPE
                    return __.has(TYPE, p);

                case ExprType.LANG:
                    // lang(var) is slot LANG 
                    return __.has(LANG, p);
            }
        }

        if (node == Exp.PREDICATE) {
            return __.has(EDGE_P, p);
        }

        switch (exp.oper()){
            case ExprType.CONTAINS:                                 
            case ExprType.STARTS:                
            case ExprType.ENDS:                               
            case ExprType.REGEX: 
                return __.has(VERTEX_VALUE, p)   ; 
        }
        
        //System.out.println("S2T: " + exp + " " + dt);
        //if (exp.getExp(0).getLabel().equals("?test")) return __.has(VERTEX_VALUE, p)   ; 
        DatatypeValue dt = exp.getExp(1).getDatatypeValue();
        return getVertexPredicate(p, dt);
    }
    
    /**
     * BGP ?x p value 
     * generate appropriate Tinkerpop filter to match value = dt
     */    
    GraphTraversal<? extends Element, Edge> getVertexPredicate(P p, DatatypeValue dt){
        if (dt.isLiteral()){
            if (dt.getLang() != null && !dt.getLang().isEmpty()) {
                return __.and(__.has(KIND, LITERAL), __.has(VERTEX_VALUE, dt.stringValue()), __.has(LANG, dt.getLang()));
            } else if (dt.getDatatypeURI() != null) {
                return __.and(__.has(KIND, LITERAL), __.has(VERTEX_VALUE, dt.stringValue()), __.has(TYPE, dt.getDatatypeURI()));
            } else {
                return __.and(__.has(KIND, LITERAL), __.has(VERTEX_VALUE, dt.stringValue()));
            }
            }
        
        return __.and(__.has(KIND, (dt.isBlank()) ? BNODE : IRI), __.has(VERTEX_VALUE, dt.stringValue()));
    }
            
    /**
     * filter on VALUE slot
     * @param exp
     * @return 
     */
    GraphTraversal<? extends Element, Edge> filter(Expr exp, int node) {       
        return getTraversal(exp, getPredicate(exp, node), node);
    }
    
    P getPredicate(Expr exp, int node){
                
        switch (exp.oper()) {
            case ExprType.ISURI:
                return P.eq(RdfToBdMap.IRI);
            case ExprType.ISBLANK:
                return P.eq(RdfToBdMap.BNODE);
            case ExprType.ISLITERAL:
                return P.eq(RdfToBdMap.LITERAL).or(P.eq(RdfToBdMap.LARGE_LITERAL));
        }

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
                } else if (exp.getExp(2).isConstant()) {
                    String mode = exp.getExp(2).getDatatypeValue().stringValue();
                    BiPredicate<String, String> reg = (x, y) -> proc.regex(x, y, mode);
                    return P.test(reg, val);             
                } else {
                    return P.test(atrue, "");
                }  
                
            case ExprType.IN:
                return P.within(getInList(exp));
                
            case ExprType.EQ:
            case ExprType.SAMETERM:
                return P.eq(val.stringValue());
                
            case ExprType.NEQ:
                    return P.neq(val.stringValue());
                            
            case ExprType.LT:
                return P.lt(val.stringValue());
            case ExprType.LE:
                return P.lte(val.stringValue());
            case ExprType.GE:
                return P.gte(val.stringValue());
            case ExprType.GT:
                return P.gt(val.stringValue());
                      
            default:
                return P.test(atrue, "");
                
        }        
    }
          
    /**
     * @param exp: ?x in (v1, .. vn)
     * @return List(v1, .. vn)
     */
    List<Object> getInList(Expr exp) {
        ArrayList<Object> ls = new ArrayList<>();
        for (Expr ee : exp.getExp(1).getExpList()) {
            ls.add(ee.getDatatypeValue().stringValue());
        }
        return ls;
    }           

}
