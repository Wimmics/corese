package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Minus;
import fr.inria.corese.sparql.triple.parser.Optional;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.compiler.api.QueryVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Prototype for federated query
 *
 * @federate <s1>
 * @federate <s2> 
 * select * where { } 
 * Recursively rewrite every triple t as:
 * service <s1> <s2> { t } Generalized service statement with several URI
 * Returns the union of Mappings without duplicate (select distinct *) 
 * PRAGMA:
 * Property Path evaluated in each of the services but not on the union 
 * (hence PP is not federated)
 * graph ?g { } by default is evaluated as a whole, not federated onto servers
 * @type kg:distributeNamed : 
 * draft federate named graph with rewrite of variable ?g in optional, minus and exists. 
 * This is working draft, filter and subquery are not rewritten.
 * graph ?g { exp optional { ?x ex:p ?g } }
 * -> 
 * graph ?g { exp optional { ?x ex:p ?g_proxy } } filter coalesce(?g = ?g_proxy, true)
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
@Deprecated
public class ServiceVisitor implements QueryVisitor {
    private static Logger logger = LoggerFactory.getLogger(ServiceVisitor.class);
    
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
    RewriteVariable rewriteVariable;
    
    public ServiceVisitor(){
        stack = new Stack();
        rewriteVariable = new RewriteVariable();
    }
    
    class RewriteVariable extends HashMap<Variable, Variable> {
        
        RewriteVariable copy(){
            return (RewriteVariable) clone();
        }
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
            System.out.println(ast);
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
            else if (exp.isOptional()){
                process(name, exp.getOptional()); 
            }
            else if (exp.isMinus()){
                process(name, exp.getMinus()); 
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
    
    /**
     * graph ?g { ?x ex:p ?y optional { ?y ex:q ?g }}
     * rewrite as
     * graph ?g { ?x ex:p ?y optional { ?y ex:q ?g_proxy }}
     * filter coalesce(?g = ?g_proxy, true)
     */
    Exp process(Atom name, Optional exp){
        rewrite(name, exp.get(0));
        if (name != null && name.isVariable()){
            rewriteVariable(name.getVariable(), exp.get(1));            
        }
        else {
            rewrite(name, exp.get(1));
        }
        return exp; 
    }
    
     Exp process(Atom name, Minus exp){
        if (name != null && name.isVariable()){
            rewriteVariable(name.getVariable(), exp.get(0));            
            rewriteVariable(name.getVariable(), exp.get(1));            
        }
        else {
            rewrite(name, exp.get(0));
            rewrite(name, exp.get(1));
        }
        return exp; 
    }
     
    Exp rewriteVariable(Variable var, Exp exp) {
        if (var.getVariableProxy() == null) {
            var.setVariableProxy(getProxy(var));
        }
        stack.push(var);
        rewrite(var, exp);
        stack.pop();
        return exp;
    }
    
    Variable getProxy(Variable var){
        return Variable.create(var.getName().concat(PROXY));
    }
    
    Exp rewrite(Source exp) {
        if (distributeNamed) {
            // graph ?g { t1 t2 .. } -> { service <Si> { graph ?g { ti }} .. }
            // TODO: does not work properly if exp contains ?g in optional/minus/subquery           
            return rewriteNamed(exp.getSource(), exp.get(0));
        } else {
            Exp res = exp;
            // send named graph as is to remote servers
            if (ast.getDataset().hasNamed()) {
                Query q = query(BasicGraphPattern.create(exp));
                q.getAST().getDataset().setNamed(ast.getNamed());
                res = q;
            }
            return Service.create(ast.getServiceList(), res, false);
        }
    }
    
    Exp rewriteNamed(Atom name, Exp exp) {
        Exp res = rewrite(name, exp);
        if (name.isVariable()) {
            Variable var = name.getVariable();
            if (rewriteVariable.containsKey(var)) {
                // graph ?g where variable ?g has been rewritten as ?g_proxy
                // add filter (?g = ?g_proxy)
                Expression e = ast.createFunction(ast.createQName(Processor.COALESCE), 
                        Term.create(Term.SEQ, var, var.getVariableProxy()), 
                        Constant.create(true));
                //Triple filter = Triple.create(e);
                exp.add(e);
            }
        }
        return res;
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
            if (expandList.contains(exp)) {
                body.remove(i);
                for (Exp e : exp) {
                    body.add(i++, e);
                }
            } else {
                i++;
            }
        }
    }
    
    /**
     * Rewrite Triple t as: service <Si> { t } or: service <Si> { graph name { t
     * } } Add filters of body bound by t in the BGP, except exists filters.
     */
    Exp rewrite(Atom name, Triple t, Exp body) {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        if (name == null) {
            // service uri { triple } 
            bgp.add(t);
        } 
        else if (! stack.isEmpty()){ 
            rewriteVariable(t);
            bgp.add(Source.create(name, t));
        }
        else {
            bgp.add(Source.create(name, t)); 
        }
        filter(body, t, bgp);        
        Exp exp = from(name, bgp);
        Service s = Service.create(ast.getServiceList(), exp, false);
        return s;
    }
    
    void rewriteVariable(Triple t) {
        for (Variable var : stack) {
            if (t.bind(var)) {
                if (rewriteNamed){
                    // service uri { graph name { triple } } 
                    t.rewrite(var, var.getVariableProxy());
                    rewriteVariable.put(var, var);
                }
                else {
                    logger.warn(String.format("\n** Variable %s in %s is bound by named graph pattern in federated query mode.\nAn alternative consists in renaming this variable.", var, t));                    
                }
            }
        }
    }
    
  /** exp = BGP | graph name { BGP }
     * if from or from named, generate 
     * select * from  from named where { exp }
     */
    Exp from(Atom name, Exp exp){
        if (name == null){ 
            if (ast.getDataset().hasFrom()){
                  Query q = query(exp);
                  q.getAST().getDataset().setFrom(ast.getFrom());
                  return BasicGraphPattern.create(q);
            }   
        }
        else if (ast.getDataset().hasNamed()) {
            Query q = query(exp);
            q.getAST().getDataset().setNamed(ast.getNamed());
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
        if (name == null){
            rewrite(name, exp.getTerm().getExist().get(0));
        }
        else {
            if (name.isVariable()){
                // graph ?g { .. filter exists { BGP } }
                Variable var = name.getVariable();
                RewriteVariable save = rewriteVariable;
                rewriteVariable = rewriteVariable.copy();
                rewriteVariable.remove(var); 
                // same function as graph ?g { BGP }
                rewriteNamed(var, exp.getTerm().getExist().get(0));
                rewriteVariable = save;
            }
            else {
                rewriteNamed(name, exp.getTerm().getExist().get(0));
            }
        }
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
    public void visit(fr.inria.corese.kgram.core.Query query) {
    }
}
