package fr.inria.corese.kgenv.federate;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.acacia.corese.triple.parser.Or;
import fr.inria.acacia.corese.triple.parser.Query;
import fr.inria.acacia.corese.triple.parser.Service;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Values;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Prototype for federated query
 *
 * @service <s1> <s2> 
 * select * where { } 
 * Recursively rewrite every triple t as:
 * service <s1> <s2> { t } Generalized service statement with several URI
 * Returns the union of Mappings without duplicate (select distinct *) 
 * PRAGMA:
 * Property Path evaluated in each of the services but not on the union 
 * (hence PP is not federated)
 * graph ?g { } by default is evaluated as a whole, not federated onto servers
 * @type kg:distributeNamed : 
 * draft federate named graph with rewrite 
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class FederateVisitor implements QueryVisitor {
    private static Logger logger = LogManager.getLogger(FederateVisitor.class);
    
    public static final String PROXY = "_proxy_";
    // false: evaluate named graph pattern as a whole on each server 
    // true:  evaluate the triples of the named graph pattern on the merge of 
    // the named graphs of the servers
    boolean distributeNamed = false;
    boolean rewriteNamed = false;
    // same in case of select from where
    boolean distributeDefault = false;

    ASTQuery ast;
    Stack stack;
    
    public FederateVisitor(){
        stack = new Stack();
    }
    
    /**
     * Query Visitor just before compiling AST
     */
    @Override
    public void visit(ASTQuery ast) {
        this.ast = ast;
        option();
        
        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.VERBOSE)) {
            System.out.println("before:");
            System.out.println(ast.getBody());
        }

        rewrite(ast);

        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.VERBOSE)) {
            System.out.println("after:");
            System.out.println(ast.getBody());           
        }
    }
    
    void option(){
        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.DISTRIBUTE_NAMED)){
           distributeNamed = true; 
        }
        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.REWRITE_NAMED)){
           rewriteNamed = true; 
        }
        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.DISTRIBUTE_DEFAULT)){
           distributeDefault = true; 
        }
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
            rewrite(name, exp);
        }
        for (Expression exp : ast.getGroupBy()) {
            rewrite(name, exp);
        }
        for (Expression exp : ast.getOrderBy()) {
            rewrite(name, exp);
        }
        if (ast.getHaving() != null) {
            rewrite(name, ast.getHaving());
        }

        rewrite(name, ast.getBody());
    }
    
    /**
     * Recursively rewrite every triple t as: service <s1> <s2> { t } Add
     * filters that are bound by the triple (except exists {} which must stay
     * local)
     */
    Exp rewrite(Atom name, Exp body) {
        ArrayList<Exp> expandList = new ArrayList<Exp> ();
        
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);

            if (exp.isQuery()) {
                // TODO: graph name ??
                rewrite(name, exp.getQuery());
            } else if (exp.isService() || exp.isValues()) {
                // keep it
            } else if (exp.isFilter() || exp.isBind()) {
                // rewrite exists { }
                rewrite(name, exp.getFilter());
            } else if (exp.isTriple()) {
                // triple t -> service <Si> { t }
                Exp res = rewrite(name, exp.getTriple(), body);
                body.set(i, res);
            } else if (exp.isGraph()) {
                Exp res = rewrite(exp.getNamedGraph());
                if (distributeNamed) {
                    expandList.add(res);
                } 
                body.set(i, res);
            }                       
            else {
                // rewrite BGP, union, optional, mminus
                rewrite(name, exp);
            }
        }
        
        if (!expandList.isEmpty()) {
            expand(body, expandList);
        }

        return body;
    }
    
   
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
        Exp res = exp;
        // send named graph as is to remote servers
        if (ast.getDataset().hasNamed()) {
            Query q = query(BasicGraphPattern.create(exp));
            q.getAST().getDataset().setNamed(ast.getNamed());
            res = q;
        }
        return Service.create(ast.getServiceList(), res, false);
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
    */
    Exp rewriteNamed(Variable var, Exp body, Source exp) {
        if (ast.getNamed().isEmpty()) {
            return simpleNamed(exp);
        }

        ArrayList<Exp> list = new ArrayList<>();
        
        for (Constant namedGraph : ast.getNamed()) {
            Values values = Values.create(var, namedGraph);
            Exp res = rewrite(namedGraph, copy(body));
            res.add(values);
            list.add(res);
        }
        
        return union(list, 0);
    }
    
    Exp copy(Exp body) {
        BasicGraphPattern bgp = new BasicGraphPattern();
        for (Exp exp : body) {
            bgp.add(exp);
        }  
        return bgp;
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
    
    /**
     * Rewrite Triple t as: 
     * service <Si> { t }  -- name == null
     * service <Si> { select * from g1 .. from gn { t }} -- name == null && query = select from g1 .. from gn 
     * service <Si> { select * from g { t }} -- name == g
     * Add filters of body bound by t in the BGP, except exists filters.
     */
    Exp rewrite(Atom name, Triple t, Exp body) {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        bgp.add(t);
        filter(body, t, bgp);  
        if (name == null) {
            // std triple
            Exp exp = from(name, bgp);
            Service s = Service.create(ast.getServiceList(), exp, false);
            return s;
        }
        else {
            // graph <name> { triple }
            Query q = query(bgp);
            q.getAST().getDataset().addFrom(name.getConstant());
            Exp exp = BasicGraphPattern.create(q);
            Service s = Service.create(ast.getServiceList(), exp, false);
            return s;
        }
    }
          
    Exp from(Atom name, Exp exp) {
        if (ast.getDataset().hasFrom()) {
            Query q = query(exp);
            q.getAST().getDataset().setFrom(ast.getFrom());
            return BasicGraphPattern.create(q);
        }
        return exp;
    }
    
    Query query(Exp exp){
         ASTQuery as = ASTQuery.create(exp);
         as.setSelectAll(true);
         return Query.create(as);
    }
    

   
    boolean rewrite(Atom name, Expression exp) {
        boolean exist = false;
        if (exp.isTerm()) {
            if (exp.getTerm().isTermExist()) {
                exist = true;
                process(name, exp);
            } else {
                for (Expression arg : exp.getArgs()) {
                    if (rewrite(name, arg)) {
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
    void process(Atom name, Expression exp) {
        rewrite(name, exp.getTerm().getExist().get(0));
    }
    

    /**
     * Find filters bound by t in body, except exists {} Add them to bgp
     */
    void filter(Exp body, Triple t, Exp bgp) {
        for (Exp exp : body) {
            if (exp.isFilter()) {
                Expression f = exp.getFilter();
                if (!(f.isTerm() && f.getTerm().isTermExistRec())) {
                    if (t.bind(f)) {
                        bgp.add(exp);
                    }
                }
            }
        }
    }

    @Override
    public void visit(fr.inria.edelweiss.kgram.core.Query query) {
    }
}
