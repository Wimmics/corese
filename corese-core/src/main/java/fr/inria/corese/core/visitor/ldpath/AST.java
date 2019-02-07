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
    
    static String PROPERTY_VARIABLE = "?p";
    static String COUNT_VARIABLE = "?count";
    static String COUNT_DISTINCT_VARIABLE = "?count2";
    static String SERVICE_VARIABLE = "?uri2";
    static String TRIPLE_VARIABLE = "?s";
    
    
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
    
    ASTQuery service (ASTQuery a, String uri1, String uri2, int i) {
        ASTQuery ast = ASTQuery.create();
        ast.setNSM(a.getNSM());
                        
        ASTQuery sub = ast.subCreate();
        Triple t = tripleVariable(sub, i);
        Triple t2 = sub.createTriple(t.getObject(), t.getPredicate(), t.getSubject());        
        Union union = Union.create(BasicGraphPattern.create(t), BasicGraphPattern.create(t2));
        
        sub.setBody(BasicGraphPattern.create(union));
        sub.setSelect(t.getSubject().getVariable());
        sub.setDistinct(true);
        
        Service s2 = Service.create(Constant.create(uri2), BasicGraphPattern.create(Query.create(sub)));      
        
        Service s1 = Service.create(Constant.create(uri1), a.getBody());
        
        BasicGraphPattern bgp = BasicGraphPattern.create(s1, s2);
        ast.setBody(bgp);
        Term count = Term.function("count", t.getSubject());
        count.setDistinct(true);
        ast.setSelect(Variable.create(COUNT_VARIABLE), count);
        ast.setSelect(Variable.create("?uri1"), Constant.create(uri1));
        ast.setSelect(Variable.create(SERVICE_VARIABLE), Constant.create(uri2));
        return ast;
    }
    
    /**
     * select ?p (count(distinct ?si) as ?count) (s1 as ?uri1) (s2 as ?uri2) 
     * where {
     * s1: service s1 EXP 
     * s2: service s2 {
     * sub: select distinct ?si ?p where {{?si ?p ?sj} union {?sj ?p ?si}}
     * }
     * group by ?p
    */
     ASTQuery servicePath (ASTQuery a, String uri1, String uri2, int i) {
        ASTQuery ast = ASTQuery.create();
        ast.setNSM(a.getNSM());
                        
        ASTQuery sub = ast.subCreate();
        Triple t = tripleVariable(sub, i);
        Triple t2 = sub.createTriple(t.getObject(), t.getPredicate(), t.getSubject());        
        Union union = Union.create(BasicGraphPattern.create(t), BasicGraphPattern.create(t2));
        
        sub.setBody(BasicGraphPattern.create(union));
        sub.setSelect(t.getSubject().getVariable());
        sub.setSelect(t.getVariable());
        sub.setDistinct(true);
        
        Service s2 = Service.create(Constant.create(uri2), BasicGraphPattern.create(Query.create(sub)));      
        
        Service s1 = Service.create(Constant.create(uri1), a.getBody());
        
        BasicGraphPattern bgp = BasicGraphPattern.create(s1, s2);
        ast.setBody(bgp);
        Term count = Term.function("count", t.getSubject());
        count.setDistinct(true);
        ast.setGroup(t.getPredicate());
        ast.setSelect(Variable.create(COUNT_VARIABLE), count);
        ast.setSelect(t.getVariable());
        ast.setSelect(t.getSubject().getVariable());
        ast.setSelect(Variable.create("?uri1"), Constant.create(uri1));
        ast.setSelect(Variable.create(SERVICE_VARIABLE), Constant.create(uri2));
        return ast;
    }


     /**
     * complete with 
     * select distinct ?p where { EXP ?si ?p ?sj }
     */
    ASTQuery variable(ASTQuery a, int i) {
       ASTQuery ast = copy(a);
               
        Triple t = tripleVariable(ast, i);        
        Expression term = filter(1, i+1);
        Expression isuri = Term.function("isURI", variable(i+1));
        
        ast.getBody().add(Triple.create(term));
        ast.getBody().add(Triple.create(isuri));
        
        ast.getBody().add(t);

        ast.setSelect(t.getVariable());
        ast.setSort(t.getVariable());
        ast.setDistinct(true);

        return ast;
    }
    
    Triple tripleVariable(ASTQuery ast, int i) {
        Variable s = variable(i);
        Variable p = Variable.create(PROPERTY_VARIABLE);
        Variable v = variable(i + 1);
        Triple t = ast.createTriple(s, p, v);
        return t;
    }
    
    Triple tripleProperty(ASTQuery ast, Constant p, int i) {
        Triple l = ast.getBody().get(ast.getBody().size() -1).getTriple();
        Triple t = ast.createTriple(l.getSubject(), p, l.getObject());
        return t;
    }
   
    /**
     * replace last triple ?si ?p ?sj by ?si p ?sj
     * return select (count(*) as ?count) where { EXP ?si p ?sj }
     */
    ASTQuery property(ASTQuery a, Constant p, int i) {
        ASTQuery ast = copy(a);
        
        // replace last triple with copy with p as property 
        Triple t = tripleProperty(ast, p, i);
        ast.getBody().set(ast.getBody().size() -1, t);
        
        Term count = Term.function("count", t.getSubject());
        ast.setSelect(Variable.create(COUNT_VARIABLE), count);
        
        Term count2 = Term.function("count", t.getSubject());
        count2.setDistinct(true);
        ast.setSelect(Variable.create(COUNT_DISTINCT_VARIABLE), count2);
        
        return ast;
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
        // copy body
        ast.setBody(BasicGraphPattern.create());
        for (Exp exp : a.getBody()) {
            ast.getBody().add(exp);
        }       
        return ast;
    }
    
     // nb triples with two variables ?si ?sj
    int length(ASTQuery ast) {
        int i = 0;
        for (Exp exp : ast.getBody()) {
            if (exp.isTriple() && !exp.isFilter()) {
                Triple t = exp.getTriple();
                if (t.getSubject().isVariable() && t.getObject().isVariable()
                        && t.getSubject().getLabel().startsWith(TRIPLE_VARIABLE)
                        && t.getObject().getLabel().startsWith(TRIPLE_VARIABLE)) {
                    i++;
                }
            }
        }
        return i;
    }
    
  
    ASTQuery complete(ASTQuery ast, List<Constant> list, int i) {
        for (Constant p : list) {
            Variable s = variable(i++);
            Variable o = variable(i);
            Triple t = ast.createTriple(s, p, o);
            Expression f = filter(1, i);
            Expression isuri = Term.function("isURI", o);
            ast.getBody().add(Triple.create(f));
            ast.getBody().add(Triple.create(isuri));
            ast.getBody().add(t);
        }
        return ast;
    }
    
}
