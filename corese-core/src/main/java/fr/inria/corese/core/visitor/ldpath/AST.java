package fr.inria.corese.core.visitor.ldpath;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class AST {
    
    static final String PROPERTY_VAR = "?p";
    static final String COUNT_VAR = "?count";
    static final String DISTINCT_VAR = "?count2";
    static final String SERVICE_VAR = "?uri2";
    static final String LOCAL_VAR = "?uri1";
    static final String TRIPLE_VAR = "?s";
    static final String COUNT_FUNCTION = "count";
          
    LinkedDataPath ldp;
    
    AST(LinkedDataPath ldp) {
        this.ldp = ldp;
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
        return service(aa, uri1, uri2, i, true);
    }
    
    ASTQuery service (ASTQuery aa, String uri1, String uri2, int i, boolean count) {
        ASTQuery a = ASTQuery.create().nsm(aa.getNSM());
                        
        ASTQuery sub = a.subCreate();
        Triple t = tripleVariable(sub, i);
        Triple t2 = sub.createTriple(t.object(), t.predicate(), t.subject());        
        
        sub.where(sub.union(t, t2));
        sub.select(t.subject().getVariable()).distinct(true);
        Exp body = copyBody(aa);
        body = uri(body, i);
        Service s1 = a.service(a.uri(uri1), body);
        Service s2 = a.service(a.uri(uri2), Query.create(sub));                     
        a.where(s1, s2);
        
        if (count) {
            a.select(a.variable(COUNT_VAR), a.count(t.subject()).distinct(true))
             .select(a.variable(LOCAL_VAR), a.uri(uri1))
             .select(a.variable(SERVICE_VAR), a.uri(uri2));
        }
        else {
            a.select(t.subject().getVariable()).distinct(true);
            a.orderby(t.subject());
        }
        return a;
    }
    
    /**
     * Return predicate and count subject or object
     */
    ASTQuery servicePath(ASTQuery aa, String uri1, String uri2, int i) {
        if (ldp.hasOption(LinkedDataPath.SUBJECT)) {
            return servicePathSubject(aa, uri1, uri2, i);
        } else {
            return servicePathObject(aa, uri1, uri2, i);
        }
    }

    
    /**
     * count object on remote endpoint
     * select ?p (count(distinct ?sj) as ?dist) (count(?sj) as ?count) (s1 as ?uri1) (s2 as ?uri2) 
     * where {
     * s1: service s1 EXP 
     * s2: service s2 { ?si ?p ?sj }
     * }
     * group by ?p
    */
     ASTQuery servicePathObject (ASTQuery aa, String uri1, String uri2, int i) {
        ASTQuery a = ASTQuery.create().nsm(aa.getNSM());
                               
        Triple t = tripleVariable(a, i); 
        Exp body = copyBody(aa);
        body = uri(body, i);
        Service s1 = a.service(a.uri(uri1), body);
        Service s2 = a.service(a.uri(uri2), t);  
        
        a.where(s1, s2).groupby(t.predicate());
        
        a.select(a.variable(DISTINCT_VAR), a.count(t.object()).distinct(true));
        a.select(a.variable(COUNT_VAR), a.count(t.object()));
        
        a.select(t.predicate().getVariable());
        a.select(t.subject().getVariable()); // documentation
        a.select(a.variable(LOCAL_VAR), a.uri(uri1));
        a.select(a.variable(SERVICE_VAR), a.uri(uri2));
        return a;
    }
     
    Exp uri(Exp body, int i) {
        if (! ldp.hasOption(LinkedDataPath.LITERAL)) {
            body.add(Term.function("isURI", variable(i)));
        }
        return body;
    }
    
     
    /**
     * count subject on remote endpoint
     * select ?p (count(distinct ?si) as ?dist) (count(?si) as ?count) (s1 as ?uri1) (s2 as ?uri2) 
     * where {
     * s1: service s1 EXP 
     * s2: service s2 { select distinct ?si ?p where { ?si ?p ?sj } }
     * }
     * group by ?p
    */ 
    ASTQuery servicePathSubject(ASTQuery aa, String uri1, String uri2, int i) {
        ASTQuery a = ASTQuery.create().nsm(aa.getNSM());

        Triple t = tripleVariable(a, i);
        Variable subject = t.subject().getVariable();
        ASTQuery sub = a.subCreate();        
        sub.where(t);
        sub.select(subject);
        sub.select(t.predicate().getVariable());
        if (isDistinct()) {
            sub.distinct(true);
        }
        
        Exp body = copyBody(aa);
        body = uri(body, i);
        Service s1 = a.service(a.uri(uri1), body);
        Service s2 = a.service(a.uri(uri2), Query.create(sub));
        a.where(s1, s2).groupby(t.predicate());

        a.select(a.variable(DISTINCT_VAR), a.count(subject).distinct(true));
        if (! isDistinct()) {
            a.select(a.variable(COUNT_VAR), a.count(subject));
        }
        a.select(t.predicate().getVariable());
        a.select(t.subject().getVariable()); // documentation
        a.select(a.variable(LOCAL_VAR), a.uri(uri1));
        a.select(a.variable(SERVICE_VAR), a.uri(uri2));
        return a;
    }
    
    // count only distinct subject on remote endpoint for path 
    boolean isDistinct() {
        return ldp.hasOption(LinkedDataPath.DISTINCT);
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
        Variable p = Variable.create(PROPERTY_VAR);
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
        
        a.select(a.variable(COUNT_VAR), a.count(t.object()));        
        a.select(a.variable(DISTINCT_VAR), a.count(t.object()).distinct(true));
        
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
        return Variable.create(TRIPLE_VAR + i);
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
                        && t.subject().getLabel().startsWith(TRIPLE_VAR)
                        && t.object().getLabel().startsWith(TRIPLE_VAR)) {
                    i++;
                }
            }
        }
        return i;
    }
    
    // modify ast
    ASTQuery complete(ASTQuery ast, List<IDatatype> list, int i) {
        ast.where(copyBody(ast));
        for (IDatatype dt : list) {
            Constant p = Constant.create(dt);
            Variable s = variable(i++);
            Variable o = variable(i);
            Triple t = ast.createTriple(s, p, o);
            Expression f = filter(1, i);
            Expression isuri = Term.function("isURI", o);
            ast.where().add(isuri).add(f).add(t);
        }
        return ast;
    }   
    
}
