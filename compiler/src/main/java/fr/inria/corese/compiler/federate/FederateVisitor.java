package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.util.ArrayList;
import java.util.HashMap;
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
public class FederateVisitor implements QueryVisitor, URLParam {

    private static Logger logger = LoggerFactory.getLogger(FederateVisitor.class);
    static final String UNDEF = "?undef_serv";
    
    public static final String PROXY = "_proxy_";
    private static HashMap<String, List<Atom>> federation;
    
    // false: evaluate named graph pattern as a whole on each server 
    // true:  evaluate the triples of the named graph pattern on the merge of 
    // the named graphs of the servers
    boolean distributeNamed = true;
    boolean rewriteNamed = false;
    // same in case of select from where
    boolean distributeDefault = false;
    // service selection for triples
    boolean select = true;
    // consider filter in source selection
    private boolean selectFilter = true;
    // group connected triples with same service into connected service s { BGP }
    boolean group = true;
    // in optional/minus/union: group every triples with same service into one service s { BGP }
    private boolean merge = true;
    // factorize unique service in optional/minus/union
    boolean simplify  = true;
    boolean exist = true;
    private boolean bounce = false;
    boolean verbose = false;
    boolean variable = false;
    boolean aggregate = false;
    boolean provenance = false;
    private boolean index = false;
    private boolean sparql = false;

    ASTQuery ast;
    Stack stack;
    Selector selector;
    QuerySolver exec;
    RewriteBGP rew;
    RewriteTriple rwt;
    RewriteService rs;
    Simplify sim;
    List<Atom> empty;
    private URLServer url;
    
    static {
        federation = new HashMap<>();
    }
    
    public FederateVisitor(QuerySolver e){
        stack = new Stack();
        exec = e;
        rew = new RewriteBGP(this);
        rwt = new RewriteTriple(this);
        sim = new Simplify(this);
        empty = new ArrayList<>(0);
    }
    
    /**
     * Query Visitor just before compiling AST
     */
    @Override
    public void visit(ASTQuery ast) {
        process(ast);
    }
    
    @Override
    public void visit(fr.inria.corese.kgram.core.Query query) {
        query.setFederate(true);
        ASTQuery ast = (ASTQuery) query.getAST();
        ast.getLog().setAST(ast);
        exec.getLog().setAST(ast);
    }
    
    @Override
    public void before(fr.inria.corese.kgram.core.Query q) {
        
    }
    
    @Override
    public void after(Mappings map) {
        if (provenance) {
            Provenance prov = getProvenance(map);
            System.out.println(prov);        
        }
    }
    
    void process(ASTQuery ast) {
        this.ast = ast;
        if (! init()) {
            return;
        }
        rew.setDebug(ast.isDebug());
        option();
        
        if (isSparql()) {
            // select where { BGP } -> select where { service URLs { BGP } }
            if (verbose) {
                System.out.println("\nbefore:");
                System.out.println(ast.getBody());
            }
            sparql(ast);
        }
        else {
        
            if (ast.getContext() != null) {
                ast.setServiceList(tune(ast.getContext(), ast.getServiceList()));
            }
            
            if (select) {
                try {
                    selector = new Selector(this, exec, ast);
                    selector.process();
                } catch (EngineException ex) {
                    logger.error(ex.getMessage());
                }
            }

            if (verbose) {
                System.out.println("\nbefore:");
                System.out.println(ast.getBody());
            }

            rewrite(ast);
        }
        if (verbose) {
            System.out.println("\nafter:");
            System.out.println(ast.getBody());
            System.out.println();
        }
    }
       
    boolean init() {
        if (ast.hasMetadata(Metadata.FEDERATION)) {
            List<String> list = ast.getMetadata().getValues(Metadata.FEDERATION);
            List<Atom> serviceList;
            if (list.isEmpty()) {
                return false;
            }
            else if (list.size() == 1) {
                // refer to federation
                serviceList = getFederation().get(list.get(0));
                setURL(new URLServer(list.get(0)));
                if (serviceList == null) {
                    logger.error("Undefined federation: " + list.get(0));
                    return false;
                }
            } else {
                // define federation
                serviceList = new ArrayList<>();
                for (int i = 1; i < list.size(); i++) {
                    serviceList.add(Constant.createResource(list.get(i)));
                }
                defFederation(list.get(0), serviceList);
            }
            ast.setServiceList(serviceList);
            // same as Transformer federate()
            // use case: come here from corese server federate mode
            ast.defService((String)null);
        }

        return true;
    }
    
    

    
    public Provenance getProvenance(Mappings map) {
        Provenance prov = new Provenance(rs.getServiceList(), map);
        map.setProvenance(prov);
        return prov;
    }
    
    
    /**
     * Metadata: 
     * default is true:
     * @skip kg:select kg:group kg:simplify kg:distributeNamed
     * default is false:
     * @type kg:exist kg:verbose
     */
    void option()  {
        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.VERBOSE)) {
            verbose = true;
        }
        if (skip(Metadata.DISTRIBUTE_NAMED)) {
            distributeNamed = false;
        }
        if (skip(Metadata.SELECT)) {
            select = false;
        }
        if (skip(Metadata.SELECT_FILTER)) {
            setSelectFilter(false);
        }
        if (skip(Metadata.GROUP)) {
            group = false;
        }
        if (skip(Metadata.MERGE)) {
            setMerge(false);
        }
        if (skip(Metadata.SIMPLIFY)) {
            simplify = false;
        }
        if (skip(Metadata.EXIST)) {
            exist = false;
        }
        if (ast.hasMetadata(Metadata.BOUNCE)) {
            bounce = true;
        }
        if (ast.hasMetadata(Metadata.VARIABLE) || ast.hasMetadata(Metadata.PUBLIC)) {
            variable = true;
        }
        if (ast.hasMetadata(Metadata.SERVER)) {
            variable = true;
            aggregate = true;
        }
        if (ast.hasMetadata(Metadata.PROVENANCE)) {
            variable = true;
            provenance = true;
        }
        if (ast.hasMetadata(Metadata.INDEX)) {
            setIndex(true);
        }
        if (ast.hasMetadata(Metadata.SPARQL)) {
            setSparql(true);
        }
        
    }
    
    boolean skip(String name) {
        return ast.hasMetadataValue(Metadata.SKIP, name);
    }
    
    boolean isExist() {
        return exist;
    }
    
    /**
     * Rewrite query body with one service clause with federation URLs
     */
    void sparql(ASTQuery ast) {
        Exp body = ast.getBody();
        List<Atom> list = ast.getServiceList();
        if (ast.getContext() != null) {
            list = tune(ast.getContext(), list);
        }
        Service serv = Service.create(list, body);
        ast.setBody(ast.bgp(serv));
        // TODO: check inherit limit ??? offset ???
        complete(ast);        
        // include external values clause inside body
        prepare(ast);
        variable(ast);
        finish(ast);       
    }
    
    /**
     * Complete URL of SPARQL endpoints of federation with information from context
     * For example: mode=share&mode=debug
     */
    List<Atom> tune(Context c, List<Atom> list) {
        if (isShareable(c)) {
            List<Atom> alist = new ArrayList<>();
            for (Atom at : list) {
                String uri = at.getConstant().getLongName();
                if (c.accept(uri)) {
                    uri = c.tune(uri);
                    System.out.println("Fed tune: " + uri);
                    alist.add(Constant.createResource(uri));
                }
            }
            return alist;
        }
        return list;
    }
    
    boolean isShareable(Context c) {
        return (c.hasValue(MODE) && c.hasValue(SHARE))
                || c.hasValue(ACCEPT) || c.hasValue(REJECT) || c.hasValue(EXPORT);
    }
       
    /**
     * Main rewrite function 
     */
    void rewrite(ASTQuery ast) {
        prepare(ast);
        rewrite(null, ast);
        graph(ast);
        complete(ast);
        variable(ast);
        finish(ast);
    }
    
    void finish(ASTQuery ast) {
        ast.getVisitorList().add(this);
    }
    
    void prepare(ASTQuery ast) {
        if (ast.getValues() != null) {
            ast.where().add(0, ast.getValues());
            ast.setValues(null);
        }
    }
    
    void complete(ASTQuery ast) {
        setLimit(ast);
    }
    
    void graph(ASTQuery ast) {
        new RewriteServiceGraph(this).process(ast);
    }
    
    /**
     * Unique service inherits query limit if any
     */
    void setLimit(ASTQuery ast) {
        if (ast.hasLimit()) {
            Exp body = ast.getBody();
            if (body.size() == 1 && body.get(0).isService()) {
                Service s = body.get(0).getService();
                ASTQuery aa = ast.getSetSubQuery(s);
                if (!aa.hasLimit()) {
                    aa.setLimit(ast.getLimit());
                }
            }
        }
    }
    
    // @variable
    // rewrite service (uri) {} as values ?serv { (uri) } service ?serv {}
    void variable(ASTQuery ast) {
        if (variable) {
            rs = new RewriteService(this);
            rs.process(ast);
            if (aggregate) {
                aggregate(ast, rs.getVarList());
            }
        }
    }
    
    /**
     * select ?serv_1  ?serv_n (count(*) as ?count)
     * where {}
     * group by ?serv_1  ?serv_n.
     */
    void aggregate(ASTQuery ast, List<Variable> valList) {
        ast.setGroup(valList);
        ast.cleanSelect();
        ast.setSelect(valList);
        Term fun = Term.function(Processor.COUNT);
        Variable var = Variable.create("?count");
        ast.defSelect(var, fun);
    }
    
    /**
     * ast is global or subquery
     * name is embedding named graph if any
     */
    void rewrite(Atom name, ASTQuery ast) {
        for (Expression exp : ast.getModifierExpressions()) {
            rewriteFilter(name, exp);
        }       
        rewrite(name, ast.getBody());
    }
    
    /**
     * Core rewrite function
     * Recursively rewrite triple t as: service <s1> <s2> { t } Add
     * filters that are bound by the triple (except some exists {} which must stay
     * local)
     */
    Exp rewrite(Atom name, Exp body) {
        return rewrite(name, body, body);
    }
    
    Exp rewrite(Atom name, Exp main, Exp body) {
        ArrayList<Exp> filterList = new ArrayList<>();
        
        if (group && body.isBGP()) {
            // BGP body may be modified
            // replace triples by service URI { t1 t2 }
            // possibly several service with same URI with connected BGP 
            // these service clauses will not be rewritten afterward
            // triple with several URI not rewritten yet
            // filterList is list of filter/bind that have been copied in service
            rew.groupTripleInServiceWithOneURI(name, main, body, filterList);
        }
        
        ArrayList<Exp> expandList = new ArrayList<> ();
        
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);

            if (exp.isQuery()) {
                // TODO: graph name ??
                rewrite(name, exp.getAST());
            } else if (exp.isService() || exp.isValues()) {
                // keep it
            } else if (exp.isFilter() || exp.isBind()) {
                // rewrite exists { }
                if (! filterList.contains(exp)){
                    // not already processed
                    // rewrite filter exists BGP and bind (exists BGP as var) as service
                    rewriteFilter(name, exp.getFilter());
                }
            } else if (exp.isTriple()) {
                // remaining triple with several services
                // triple t -> service (<Si>) { t }
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
                // recursively rewrite arguments
                exp = rewrite(name, exp);
                if (simplify) {
                    exp = sim.simplify(exp);
                } 
                i = insert(body, exp, i);

            } else {
                // BGP
                rewrite(name, body, exp);
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
            bind(body);
            filter(body);
            new Sorter().process(body);
        }
                
        return body;
    }
    
    
    int insert(Exp body, Exp exp, int i) {
        if (exp.isBGP()) {
            // in some case:
            //     service s1 {A} service s2 {B} optional/minus {service s2 C}
            // -> {service s1 {A} . service s2 {B optional/minus C}}
            body.set(i, exp.get(0));     // s1
            body.add(i + 1, exp.get(1)); // s2
            i++;
        } else {
            body.set(i, exp);
        }
        return i;
    }
    
      /**
     * Move bind (exp as var) into appropriate service uri { } if any
     */
    void bind(Exp body) {
        ArrayList<Exp> list = new ArrayList<>();
        for (Exp exp : body) {
            if (exp.isBind() && ! exp.getFilter().isTermExistRec()) {
                boolean b = rew.move(exp, body);
                if (b) {
                    list.add(exp);
                }
            }
        }
        for (Exp exp : list) {
            body.getBody().remove(exp);
        }
    }
    
    
    /**
     * Move filter  into appropriate service 
     */
    void filter(Exp body) {
        ArrayList<Exp> list = new ArrayList<>();
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (exp.getFilter().isTermExistRec()) {
                    boolean b = rew.filterExist(exp, body);
                    if (b) {
                        list.add(exp);
                    }
                }
                else {
                   boolean b = rew.move(exp, body);
                    if (b) {
                        list.add(exp);
                    } 
                }
            }
        }
        for (Exp exp : list) {
            body.getBody().remove(exp);
        }
    }
    
    /**
     * Filter may be copied into additional service
     */
    void filter(Exp body, List<Exp> filterList) {
        for (Exp exp : filterList) {
            if (exp.isFilter() && !exp.getFilter().isTermExistRec()) {
                boolean b = rew.move(exp, body);               
            }
        }
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
            return Union.create(list.get(n), union(list, n+1));
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
        return getDefaultServiceList();
    }
    
    List<Atom> getServiceListTriple(Triple t) {     
        if (select) {
            List<Atom> list = selector.getPredicateService(t);
            if (list != null && ! list.isEmpty()) {
                return list;
            }
        }
        return getDefaultServiceList();
    }
    
    // when there is no service for a triple
    List<Atom> getDefaultServiceList() {
        if (select) {
            return undefinedService();
        }
        return ast.getServiceList();
        //return empty;
    }
    
    List<Atom> undefinedService() {
        ArrayList<Atom> list = new ArrayList<>();
        list.add(Variable.create(UNDEF));
        return list;      
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
        return f.getFilter().isTermExistRec();
    }
    
    boolean isExist(Exp f) {
        return f.getFilter().isTermExist();
    }

    boolean isNotExist(Exp f) {
        return f.getFilter().isNotTermExist() ;
    }
    
      /**
     * @return the bounce
     */
    public boolean isBounce() {
        return bounce;
    }
    
    public static void defineFederation(String name, List<String> list) {
        List<Atom> serviceList = new ArrayList<>();
        for (String url : list) {
            serviceList.add(Constant.createResource(url));
        }
        defFederation(name, serviceList);
    }
    
    public static void declareFederation(String name, List<IDatatype> list) {
        List<Atom> serviceList = new ArrayList<>();
        for (IDatatype url : list) {
            serviceList.add(Constant.create(url));
        }
        defFederation(name, serviceList);
    }
    
    public static void defFederation(String name, List<Atom> list) {
        getFederation().put(name, list);
    }
    
    /**
     * @return the federation
     */
    public static HashMap<String, List<Atom>> getFederation() {
        return federation;
    }
    
    public static List<Atom> getFederation(String name) {
        return getFederation().get(name);
    }
    
    public List<Atom> getFederationFilter(String name) {
        List<Atom> list = getFederation().get(name);
        if (list == null) {
            return null;
        }
        return list;
    }

    /**
     * @param aFederation the federation to set
     */
    public static void setFederation(HashMap<String, List<Atom>> aFederation) {
        federation = aFederation;
    }

    /**
     * @return the merge
     */
    public boolean isMerge() {
        return merge;
    }

    /**
     * @param merge the merge to set
     */
    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    /**
     * @return the selectFilter
     */
    public boolean isSelectFilter() {
        return selectFilter;
    }

    /**
     * @param selectFilter the selectFilter to set
     */
    public void setSelectFilter(boolean selectFilter) {
        this.selectFilter = selectFilter;
    }

    /**
     * @return the index
     */
    public boolean isIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(boolean index) {
        this.index = index;
    }

    /**
     * @return the sparql
     */
    public boolean isSparql() {
        return sparql;
    }

    /**
     * @param sparql the sparql to set
     */
    public void setSparql(boolean sparql) {
        this.sparql = sparql;
    }

    /**
     * @return the url
     */
    public URLServer getURL() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setURL(URLServer url) {
        this.url = url;
    }

   
}
