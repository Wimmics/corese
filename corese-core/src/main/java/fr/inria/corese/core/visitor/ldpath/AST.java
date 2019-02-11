package fr.inria.corese.core.visitor.ldpath;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class AST {
    
    static final String PROPERTY_VARIABLE = "?p";
    static final String COUNT_VARIABLE = "?count";
    static final String DISTINCT_VARIABLE = "?count2";
    static final String SERVICE_VARIABLE = "?uri2";
    static final String LOCAL_VARIABLE = "?uri1";
    static final String TRIPLE_VARIABLE = "?s";
    static final String COUNT_FUNCTION = "count";
   
    
    AST() {
    }
    
 
    /**
     * federate s1 select where EXP
     * ->
     * select (count(distinct ?si) as ?count) (s1 as ?uri1) (s2 as ?uri2) 
     * where {
     * s1: service s1 EXP 
     * s2: service s2 {
     * ssub: select distinct ?si where {{?si ?p ?sj} union {?sj ?p ?si}}
     * }.
     */
    
    ASTQuery service (ASTQuery aa, String uri1, String uri2, int i) {
        ASTQuery a = ASTQuery.create().nsm(aa.getNSM());
                        
        ASTQuery sub = a.subCreate();
        Triple t = tripleVariable(sub, i);
        Triple t2 = sub.createTriple(t.object(), t.predicate(), t.subject());        
        
        sub.where(sub.union(t, t2));
        sub.select(t.subject().getVariable()).distinct(true);
        
        Service s1 = a.service(a.uri(uri1), aa.where());
        Service s2 = a.service(a.uri(uri2), Query.create(sub));                     
        a.where(s1, s2);
        
        a.select(a.variable(COUNT_VARIABLE), a.count(t.subject()).distinct(true))
                .select(a.variable(LOCAL_VARIABLE), a.uri(uri1))
                .select(a.variable(SERVICE_VARIABLE), a.uri(uri2));
        return a;
    }
    
    /**
     * select ?p (count(distinct ?sj) as ?dist) (count(?sj) as ?count) (s1 as ?uri1) (s2 as ?uri2) 
     * where {
     * s1: service s1 EXP 
     * s2: service s2 { ?si ?p ?sj }
     * }
     * group by ?p
    */
     ASTQuery servicePath (ASTQuery aa, String uri1, String uri2, int i) {
        ASTQuery a = ASTQuery.create().nsm(aa.getNSM());
                               
        Triple t = tripleVariable(a, i);       
        Service s1 = a.service(a.uri(uri1), aa.where());
        Service s2 = a.service(a.uri(uri2), t);                   
        a.where(s1, s2).groupby(t.predicate());
        
        a.select(a.variable(DISTINCT_VARIABLE), a.count(t.object()).distinct(true));
        a.select(a.variable(COUNT_VARIABLE), a.count(t.object()));
        
        a.select(t.predicate().getVariable());
        a.select(t.subject().getVariable()); // documentation
        a.select(a.variable(LOCAL_VARIABLE), a.uri(uri1));
        a.select(a.variable(SERVICE_VARIABLE), a.uri(uri2));
        return a;
    }


     /**
     * complete with 
     * select distinct ?p where { EXP ?si ?p ?sj }
     */
    ASTQuery variable(ASTQuery aa, int i) {
       ASTQuery a = copy(aa);
               
        Triple t = tripleVariable(a, i);        
        Expression term = filter(1, i+1);
        Expression isuri = Term.function("isURI", variable(i));
        
        a.where().add(term).add(isuri).add(t);
        a.select(t.getVariable()).distinct(true).orderby(t.getVariable());

        return a;
    }
    
    Triple tripleVariable(ASTQuery ast, int i) {
        Variable s = variable(i);
        Variable p = Variable.create(PROPERTY_VARIABLE);
        Variable v = variable(i + 1);
        Triple t = ast.createTriple(s, p, v);
        return t;
    }
    
    Triple tripleProperty(ASTQuery ast, Constant p, int i) {
        Triple l = ast.where().get(ast.where().size() -1).getTriple();
        Triple t = ast.createTriple(l.subject(), p, l.object());
        return t;
    }
   
    /**
     * replace last triple ?si ?p ?sj by ?si p ?sj
     * return select (count(*) as ?count) where { EXP ?si p ?sj }
     */
    ASTQuery property(ASTQuery aa, Constant p, int i) {
        ASTQuery a = copy(aa);
        
        // replace last triple with copy with p as property 
        Triple t = tripleProperty(a, p, i);
        a.where().set(a.where().size() -1, t);
        
        a.select(a.variable(COUNT_VARIABLE), a.count(t.object()));        
        a.select(a.variable(DISTINCT_VARIABLE), a.count(t.object()).distinct(true));
        
        return a;
    }
    
    ASTQuery propertyVariable(ASTQuery a, Constant p, int i) {
        // add ?si p ?sj
        ASTQuery ast2 = property(a, p, i);
        // add ?sj ?p ?sk
        ASTQuery ast3 = variable(ast2, i + 1);
        return ast3;
    }
    
    /**
     * filter (?s1 != ?si && .. && ?si-1 != ?si)
     */
    Expression filter(int i, int n) {
        Term t = Term.create("!=", variable(i), variable(n));
        if (i+1 >= n) {
            return t;
        }
        return Term.create("&&", t, filter (i+1, n));
    }
    
    Variable  variable(int i) {
        return Variable.create(TRIPLE_VARIABLE + i);
    }
    
      
    ASTQuery copy(ASTQuery a) {
        ASTQuery ast = ASTQuery.create();
        ast.setNSM(a.getNSM());
        ast.addMetadata(a.getMetadata());
        ast.where(copyBody(a));    
        return ast;
    }
    
    BasicGraphPattern copyBody(ASTQuery ast) {
        BasicGraphPattern exp = ast.bgp();
        for (Exp ee : ast.where()) {
            exp.add(ee);
        }
        return exp;
    }
    
     // nb triples with two variables ?si ?sj
    int length(ASTQuery ast) {
        int i = 0;
        for (Exp exp : ast.where()) {
            if (exp.isTriple() && !exp.isFilter()) {
                Triple t = exp.getTriple();
                if (t.subject().isVariable() && t.object().isVariable()
                        && t.subject().getLabel().startsWith(TRIPLE_VARIABLE)
                        && t.object().getLabel().startsWith(TRIPLE_VARIABLE)) {
                    i++;
                }
            }
        }
        return i;
    }
    
    // modify ast
    ASTQuery complete(ASTQuery ast, List<Constant> list, int i) {
        ast.where(copyBody(ast));
        for (Constant p : list) {
            Variable s = variable(i++);
            Variable o = variable(i);
            Triple t = ast.createTriple(s, p, o);
            Expression f = filter(1, i);
            Expression isuri = Term.function("isURI", o);
            ast.where().add(f).add(f).add(t);
        }
        return ast;
    }   
    
}
