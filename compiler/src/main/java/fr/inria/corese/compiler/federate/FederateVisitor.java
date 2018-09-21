package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Or;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.compiler.eval.QuerySolver;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Prototype for federated query
 *
 * @federate <s1> <s2> 
 * select * where { } 
 * Recursively rewrite every triple t as:
 * service <s1> <s2> { t } Generalized service statement with several URI
 * Returns the union of Mappings without duplicate (select distinct *) 
 * PRAGMA:
 * Property Path evaluated in each of the services but not on the union 
 * (hence PP is not federated)
 * graph ?g { } by default is evaluated as federated onto servers
 * @skip kg:distributeNamed : 
 * named graph as a whole on each server 
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class FederateVisitor implements QueryVisitor {

    private static Logger logger = LoggerFactory.getLogger(FederateVisitor.class);
    
    public static final String PROXY = "_proxy_";
    // false: evaluate named graph pattern as a whole on each server 
    // true:  evaluate the triples of the named graph pattern on the merge of 
    // the named graphs of the servers
    boolean distributeNamed = true;
    boolean rewriteNamed = false;
    // same in case of select from where
    boolean distributeDefault = false;
    // service selection for triples
    boolean select = true;
    // group triples with same service into one service s { BGP }
    boolean group = true;
    // factorize unique service in optional/minus/union
    boolean simplify  = true;
    boolean exist = false;
    private boolean bounce = false;
    boolean verbose = false;

    ASTQuery ast;
    Stack stack;
    Selector selector;
    QuerySolver exec;
    RewriteBGP rew;
    RewriteTriple rwt;
    Simplify sim;
    
    public FederateVisitor(QuerySolver e){
        stack = new Stack();
        exec = e;
        rew = new RewriteBGP(this);
        rwt = new RewriteTriple(this);
        sim = new Simplify(this);
    }
    
    /**
     * Query Visitor just before compiling AST
     */
    @Override
    public void visit(ASTQuery ast) {
        this.ast = ast;
        rew.setDebug(ast.isDebug());
        option();
        
        if (verbose) {
            System.out.println("\nbefore:");
            System.out.println(ast.getBody());
        }

        rewrite(ast);

        if (verbose) {
            System.out.println("\nafter:");
            System.out.println(ast.getBody());
            System.out.println();
        }
    }
    
    
    @Override
    public void visit(fr.inria.corese.kgram.core.Query query) {
        query.setFederate(true);
    }
    
    /**
     * Metadata: 
     * default is true:
     * @skip kg:select kg:group kg:simplify kg:distributeNamed
     * default is false:
     * @type kg:exist kg:verbose
     */
    void option() {
        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.VERBOSE)) {
            verbose = true;
        }
        if (ast.hasMetadataValue(Metadata.SKIP, Metadata.DISTRIBUTE_NAMED)) {
            distributeNamed = false;
        }
        if (ast.hasMetadataValue(Metadata.SKIP, Metadata.SELECT)) {
            select = false;
        }
        if (ast.hasMetadataValue(Metadata.SKIP, Metadata.GROUP)) {
            group = false;
        }
        if (ast.hasMetadataValue(Metadata.SKIP, Metadata.SIMPLIFY)) {
            simplify = false;
        }
        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.EXIST)) {
            exist = true;
        }
        if (ast.hasMetadata(Metadata.BOUNCE)) {
            bounce = true;
        }
        if (select) {
            selector = new Selector(exec, ast);
            selector.process();
        }
    }
    
    boolean isExist() {
        return exist;
    }

    /**
     * Rewrite select (exists {BGP} as ?b) order by group by having body
     */
    void rewrite(ASTQuery ast) {
        rewrite(null, ast);
    }
    
    /**
     * ast is global or subquery
     * name is embedding named graph if any
     */
    void rewrite(Atom name, ASTQuery ast) {
        for (Expression exp : ast.getSelectFunctions().values()) {
            rewriteFilter(name, exp);
        }
        for (Expression exp : ast.getGroupBy()) {
            rewriteFilter(name, exp);
        }
        for (Expression exp : ast.getOrderBy()) {
            rewriteFilter(name, exp);
        }
        if (ast.getHaving() != null) {
            rewriteFilter(name, ast.getHaving());
        }

        rewrite(name, ast.getBody());
    }
    
    /**
     * Recursively rewrite every triple t as: service <s1> <s2> { t } Add
     * filters that are bound by the triple (except exists {} which must stay
     * local)
     */
    Exp rewrite(Atom name, Exp body) {
        ArrayList<Exp> filterList = new ArrayList<>();
        
        if (group && body.isBGP()) {
            // body may be modified
            // triples may be replaced by service URI { t1 t2 }
            // these service clauses will not be rewritten afterward
            rew.prepare(name, body, filterList);
        }
        
        ArrayList<Exp> expandList = new ArrayList<> ();
        
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);

            if (exp.isQuery()) {
                // TODO: graph name ??
                rewrite(name, exp.getQuery());
            } else if (exp.isService() || exp.isValues()) {
                // keep it
            } else if (exp.isFilter() || exp.isBind()) {
                // rewrite exists { }
                if (! filterList.contains(exp)){
                    // not already processed by prepare
                    rewriteFilter(name, exp.getFilter());
                }
            } else if (exp.isTriple()) {
                // triple t -> service <Si> { t }
                // copy relevant filters in service
                Exp res = rwt.rewrite(name, exp.getTriple(), body, filterList);
                body.set(i, res);
            } else if (exp.isGraph()) {
                Exp res = rewrite(exp.getNamedGraph());
                if (distributeNamed) {
                    expandList.add(res);
                } 
                body.set(i, res);
            }  
            else if (exp.isMinus() || exp.isOptional() || exp.isUnion()) {
                exp = rewrite(name, exp);
                if (simplify) {
                    Exp simple = sim.simplifyStatement(exp);
                    body.set(i, simple);
                } else {
                    body.set(i, exp);
                }
            }
            else {
                // BGP
                rewrite(name, exp);
            }
        }
        
        // remove filters that have been copied into services
        for (Exp filter : filterList) {
            body.getBody().remove(filter);
        }
        
        if (!expandList.isEmpty()) {
            expand(body, expandList);
        }
        
        if (body.isBGP()) {
            sim.simplifyBGP(body);
        }
        
        return body;
    }
    
     ASTQuery getAST() {
         return ast;
     }
     
     RewriteTriple getRewriteTriple() {
         return rwt;
     }
    
   /**
    * graph ?g EXP
    * when from named gi is provided:
    * rewrite every triple t in EXP as UNION(i) graph gi t 
    * otherwise graph ?g EXP is left as is and is evaluated 
    * as is on each endpoint.
    * graph URI EXP is rewritten as graph URI t for all t in EXP
    * TODO: compute the remote dataset
    * PB: some endpoints such as dbpedia/virtuoso do not evaluate correctly
    * the optimized query that computes the dataset and we would be obliged to
    * evaluate a query that enumerates all triples on remote endpoint ...
    */
    Exp rewrite(Source exp) {
        if (distributeNamed) {
            return rewriteNamed(exp);
        } else {
            return simpleNamed(exp);
        }
    }
    
    /**
     * named graph sent as a whole in service clause    
     */
    Exp simpleNamed(Source exp) {
        // send named graph as is to remote servers
        if (ast.getDataset().hasNamed()) {
            Query q = rwt.query(BasicGraphPattern.create(exp));
            q.getAST().getDataset().setNamed(ast.getNamed());
            return Service.create(ast.getServiceList(), q, false);
        }
        return Service.create(ast.getServiceList(), BasicGraphPattern.create(exp), false);
    }
    
    
    Exp rewriteNamed(Source exp) {  
        Atom name = exp.getSource();
        if (name.isVariable()) {
            return rewriteNamed(name.getVariable(), exp.getBodyExp(), exp);
        }
        else {
            return rewrite(name.getConstant(), exp.getBodyExp());
        }
    }
    
   /**
    * from named G = {g1 .. gn}  -- remote dataset
    * graph ?g { tj } -> rewrite as:
    * union(g in G) { 
    * values ?g { g }
    * service (si) { select * from g where { tj } } 
    * }
    * TODO: subquery.copy() 
    */
    Exp rewriteNamed(Variable var, Exp body, Source exp) {
        if (ast.getNamed().isEmpty()) {
            return simpleNamed(exp);
        }

        ArrayList<Exp> list = new ArrayList<>();
        
        for (Constant namedGraph : ast.getNamed()) {
            Values values = Values.create(var, namedGraph);
            Exp res = rewrite(namedGraph, body.copy());
            res.add(values);
            list.add(res);
        }
        
        return union(list, 0);
    }
                 
    Exp union(List<Exp> list, int n) {
        if (n == list.size() - 1){
            return list.get(n);
        }
        else {
            return Or.create(list.get(n), union(list, n+1));
        }
    }    
        
    
    
    
    /**
     * expandList contains rewrite of graph ?g { } if any
     * each element of expandList is the rewrite of the BGP of a graph ?g BGP
     * This function includes the elements of the BGP directly in the body
     * in other words, it removes the { } from the BGP and add the elements 
     */
    void expand(Exp body, ArrayList<Exp> expandList) {
        // CARE: in this loop, the i++ is done explicitely in the body 
        for (int i = 0; i < body.size();  ) {
            Exp exp = body.get(i);
            if (exp.isBGP() && expandList.contains(exp)) {
                body.remove(i);
                for (Exp e : exp) {
                    if (e.isBGP()){
                        for (Exp ee : e){
                           body.add(i++, ee); 
                        }
                    }
                    else {
                        body.add(i++, e);
                    }
                }
            } else {
                i++;
            }
        }
    }
     
         
    List<Atom> getServiceList(Triple t) {
        if (t.isPath()){
            return getServiceListPath(t);
        }
        return getServiceListTriple(t);
    }
    
    List<Atom> getServiceListPath(Triple t) {
        List<Atom> serviceList = new ArrayList<>();
        for (Constant p : t.getRegex().getPredicateList()) {
            for (Atom serv : getServiceList(p)) {
                add(serviceList, serv);
            }
        }
        return serviceList;
    }
    
    void add(List<Atom> list, Atom at) {
        if (! list.contains(at)) {
            list.add(at);
        }
    }
    
     List<Atom> getServiceList(Constant p) {          
        if (select) {
            List<Atom> list = selector.getPredicateService(p);
            if (! list.isEmpty()) {
                return list;
            }
        }
        return ast.getServiceList();
    }
    
    List<Atom> getServiceListTriple(Triple t) {
        if (t.getPredicate().isVariable()) {
            return ast.getServiceList();
        }        
        if (select) {
            List<Atom> list = selector.getPredicateService(t);
            if (! list.isEmpty()) {
                return list;
            }
        }
        return ast.getServiceList();
    }
           
   
    boolean rewriteFilter(Atom name, Expression exp) {
        boolean exist = false;
        if (exp.isTerm()) {
            if (exp.getTerm().isTermExist()) {
                exist = true;
                rewriteExist(name, exp);
            } else {
                for (Expression arg : exp.getArgs()) {
                    if (rewriteFilter(name, arg)) {
                        exist = true;
                    }
                }
            }
        }
        return exist;
    }
    
     /*
     * Rewrite filter exists { t }
     * as:
     * filter exists { service <Si> { t } }
     * PRAGMA: it returns all Mappings whereas in this case
     * it could return only one. However, in the general case: 
     * exists { t1 t2 } it must return all Mappings.
     */
    void rewriteExist(Atom name, Expression exp) {
        rewrite(name, exp.getTerm().getExist().get(0));
    }
    

    /**
     * Find filters bound by t in body, except exists {} Add them to bgp
     */
    void filter(Exp body, Triple t, Exp bgp, List<Exp> list) {
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (! isRecExist(exp)) {
                    Expression f = exp.getFilter();
                    if (t.bind(f) && ! bgp.getBody().contains(exp)) {
                        bgp.add(exp);
                        if (! list.contains(exp)) {
                            list.add(exp);
                        }
                    }
                }
            }
        }
    }
    
    boolean isRecExist(Exp f) {
        return f.getFilter().isTerm() && f.getFilter().getTerm().isTermExistRec() ;
    }
    
    boolean isExist(Exp f) {
        return f.getFilter().isTerm() && f.getFilter().getTerm().isTermExist() ;
    }

    boolean isNotExist(Exp f) {
        return f.getFilter().isTerm() && 
               f.getFilter().getTerm().isNot() && 
               f.getFilter().getTerm().getArg(0).isTerm() &&
               f.getFilter().getTerm().getArg(0).getTerm().isTermExist() ;
    }
    
      /**
     * @return the bounce
     */
    public boolean isBounce() {
        return bounce;
    }
}
