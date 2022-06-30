package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Binding;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.parser.ASTSelector;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Optional;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.visitor.ASTParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Ask remote endpoints if they contain predicates of the federated query
 * Build a table: triple | predicate -> ( uri )
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Selector {
    
    public static Logger logger = LoggerFactory.getLogger(Selector.class);
    private static final String SERVER_VAR = "?serv";
    private static final String BNODE_ID = "_:fbn";
    public static boolean SELECT_EXIST = true;
    //public static int NB_ENDPOINT = 10;
    
    private FederateVisitor vis;
    ASTQuery ast;
    private ASTSelector astSelector;
    HashMap<String, String> predicateVariable;
    HashMap<Triple, String> tripleVariable;
    HashMap<BasicGraphPattern, String> bgpVariable;
    private QuerySolver exec;
    // use case: reuse federated visitor source selection    
    private Mappings mappings;
    boolean sparql10 = false;
    boolean count = false;
    boolean trace = false;
    int bnode = 0;
    private int nbEndpoint = FederateVisitor.NB_ENDPOINT;
    private double nbSuccess = FederateVisitor.NB_SUCCESS;
    
    Selector(FederateVisitor vis, QuerySolver e, ASTQuery ast) {
        this.ast = ast;
        this.vis = vis;
        exec = e;
        init();
    }
    
    void init() {
        setAstSelector(new ASTSelector());
        predicateVariable = new HashMap<>();
        tripleVariable    = new HashMap<>();
        bgpVariable       = new HashMap<>();
        if (ast.hasMetadata(Metadata.SPARQL10)) {
            sparql10 = true;
        }
        
        count = ast.hasMetadata(Metadata.COUNT);
        trace = ast.hasMetadata(Metadata.TRACE);
    }
    

    boolean process() throws EngineException {
       return process11(getURIList());
    }
    
    List<Constant> getURIList() throws EngineException{
        List<Constant> list = ast.getServiceListConstant();        
        return list;
    }
    
    List<String> getIndexURIList() throws EngineException {
        return getIndexURIList(getIndexURL());
    }
    
    // @todo: nb endpoint sublist
    List<String> getIndexURIList(List<String> urlList) throws EngineException {
        if (urlList!=null && !urlList.isEmpty()) {
            ArrayList<String> list = new ArrayList<>();
            
            for (String url : urlList) {
                List<String> alist = getIndexURIList(url);
                
                for (String res : alist) {
                    if (!list.contains(res)) {
                        list.add(res);
                    }
                }
            }
            return list;
        }
        return getIndexURIList();
    }
    
    List<String> getIndexURIList(String url) throws EngineException {
        if (url==null) {
            url = getIndexURL();
        }
        List<String> list = getBasicIndexURIList(url);
        // @todo: remove exclude uri before sublist
        list = list.subList(0, Math.min(list.size(), getNbEndpoint()));
        return list;
    }
    
    // default index url
    String getIndexURL() {
        return SelectorIndex.INDEX_URL;
    }
    
    // query graph index for source discovery
    List<String> getBasicIndexURIList(String url) throws EngineException {
        Date d1 = new Date();
        // create query to discover relevant endpoint
        // who know ast federate query predicates
        ASTQuery a = new SelectorIndex(this, ast, url)
                .setNbSuccess(getNbSuccess())
                .process();
        Context ct = Context.create().setDiscovery(true);
        Mappings map = getQuerySolver().basicQuery(a, ct); 
        traceLog(map);
        getVisitor().setDiscovery(map);
        List<String> list = map.getStringValueList(SERVER_VAR); 
        
        log(url, a, map);
        logger.info("Source discovery:\n"+map);
        logger.info("Source discovery URL list:\n" + list);
        logger.info("Source discovery time: " + time(d1));
        
        return list;
    }
    
    void log(String url, ASTQuery a, Mappings map) {
        getQuerySolver().getLog().setASTIndex(a);
        getQuerySolver().getLog().setIndexMap(map);
        getQuerySolver().getLog().set(LogKey.INDEX, url);
        ast.getLog().setASTIndex(a);
        ast.getLog().setIndexMap(map);
        ast.getLog().set(LogKey.INDEX, url);
        //ast.getLog().setExceptionList(getQuerySolver().getLog().getExceptionList());
    }
    
    public static double time(Date d1, Date d2) {
        return (d2.getTime() - d1.getTime()) / 1000.0;
    }
    
    public static double time(Date d1) {
        return time(d1, new Date());
    }
        
    boolean process11(List<Constant> list) throws EngineException {
        if (list.isEmpty()) {
            logger.info("URL list is empty");
            return false;
        }
        Date d1 = new Date();
        ASTQuery aa = createSelector(list, false);
        metadata(aa);
        Mappings map;
        
        if (getMappings() == null) {
            // compute selection
            Context ct = Context.create().setSelection(true);
            // source selection inherit timeout if any as parameter sv:timeout=1000
            ct.inherit(ast.getContext());      
            if (getVisitor().isFederateIndex()) {
                ct.setFederateIndex(true);
            }            
            map = getQuerySolver().basicQuery(aa, ct);
            traceLog(map);
            getVisitor().setMappings(map);
        }
        else {
            // reuse selection
            map = getMappings();
        }
        
        getQuerySolver().getLog().setASTSelect(aa);
        getQuerySolver().getLog().setSelectMap(map);
        
        ast.getLog().setASTSelect(aa);
        ast.getLog().setSelectMap(map);
        ast.getLog().setExceptionList(getQuerySolver().getLog().getExceptionList());
        
        for (Mapping m : map) {
            IDatatype serv =  m.getValue(SERVER_VAR);
            
            // table: predicate -> exists boolean variable
            for (String pred : predicateVariable.keySet()) {
                String var = predicateVariable.get(pred);
                IDatatype val =  m.getValue(var);
                if (val != null && val.booleanValue()) {
                    getPredicateService().get(pred).add(Constant.create(serv));
                }
            }
            
            // table: triple -> exists boolean variable
            for (Triple t : tripleVariable.keySet()) {
                String var = tripleVariable.get(t);
                IDatatype val =  m.getValue(var);
                if (val != null && val.booleanValue()) {
                    getTripleService().get(t).add(Constant.create(serv));
                }
            }
            
            // table: bgp -> exists boolean variable
            // map: {t1 t2} -> (uri) list of uri where join(t1, t2) = true
            for (BasicGraphPattern bgp : bgpVariable.keySet()) {
                String var = bgpVariable.get(bgp);
                IDatatype val =  m.getValue(var);
                if (val != null && val.booleanValue()) {
                    getBgpService().get(bgp).add(Constant.create(serv));
                }
            }
        }
        Date d2 = new Date();
        boolean b = getAstSelector().complete();       
        trace(map, d1, d2);
        logger.info("Source Selection Join Test Success: " + b);
        // if selection join failure occurs in optional/minus/union/exists
        // do not fail, else fail
        // when return false below, query is considered as failing 
        // and will not be executed
        return b;
    }
    
    void traceLog(Mappings map) {
        for (List list : getQuerySolver().getLog().getLabelList("Server")) {
            logger.info(list.toString());
        }
    }

    void metadata(ASTQuery aa) {
        if (ast.hasMetadata(Metadata.SHOW)) {
            Metadata meta = new Metadata();
            meta.add(Metadata.SHOW);
            aa.setAnnotation(meta);
        }
        if (ast.hasMetadata(Metadata.REPORT)) {
            aa.getCreateMetadata().add(Metadata.REPORT);
            ASTParser walk = new ASTParser(aa).report();
            aa.process(walk);
        }
        if (ast.getMetaValue(Metadata.TIMEOUT)!=null) {
            aa.getCreateMetadata().add(Metadata.TIMEOUT, ast.getMetaValue(Metadata.TIMEOUT));
        }
    }
        
    

    
    List<Atom> getPredicateService(Constant pred) {
        return getPredicateService().get(pred.getLabel());
    }
    
    List<Atom> getPredicateService(Triple t) {
        List<Atom> list = getTripleService().get(t);
        if (list != null) {
            return list;
        }
        if (t.getPredicate().isVariable()) {
            return null; //ast.getServiceList();
        }
        return getPredicateService(t.getPredicate().getConstant());
    }
   
    void trace(Mappings map, Date d1, Date d2) {
        if (ast.hasMetadata(Metadata.TRACE)) {
            System.out.println("Selection Time: " + (d2.getTime() - d1.getTime()) / 1000.0);
        }
        if (ast.isDebug()) {
            System.out.println("Triple Selection");
            for (String pred : getPredicateService().keySet()) {
                System.out.println(pred + " " + getPredicateService().get(pred));
            }
            for (Triple t : getTripleService().keySet()) {
                System.out.println(t + " " + getTripleService().get(t));
            }
        }
        if (trace) {
            System.out.println(map);
        }
    }
    
    void declare(Constant p, Variable var) {
        predicateVariable.put(p.getLabel(), var.getLabel());
        if (! getPredicateService().containsKey(p.getLabel())) {
            getPredicateService().put(p.getLabel(), new ArrayList<>());
        }
    }
    
    void declare(Triple t, Variable var) {
        tripleVariable.put(t, var.getLabel());
        getTripleService().put(t, new ArrayList<>());
    }
    
    void declare(BasicGraphPattern bgp, Variable var) {
        bgpVariable.put(bgp, var.getLabel());
        getBgpService().put(bgp, new ArrayList<>());
    }
    
    /**
     * select * where {
     *  values ?serv {s1, sn}
     *  service ?serv {
     *      bind (exists { ?s predicate_i ?o } as ?b_i)
     *  }
     * }
     */     
    ASTQuery createSelector(List<Constant> list, boolean sparql10) {
        ASTQuery aa = ASTQuery.create();
        aa.setSelectAll(true);
        aa.setNSM(ast.getNSM()); 
        Variable serv = Variable.create(SERVER_VAR);
        
        // triple selector
        BasicGraphPattern bgp ;
        
//        if (getVisitor().isIndex()) {
//            //bgp = createBGPIndex(serv, aa);
//            ASTQuery a = new SelectorIndex(this, ast, 
//                    Constant.create(indexURI), list)
//                    .process(serv, aa);
//            return a;
//        }
//        else 
        if (sparql10) {
            bgp = createBGP10(aa);
        }
        else {
            bgp = createBGP(serv, aa);
        }              
        
        BasicGraphPattern body;
        if (getVisitor().isIndex()) {
            // @deprecated
            body = bgp;
        }
        else {
            Exp service;
            if (ast.hasMetadata(Metadata.GRAPH)) {
                service = Source.create(serv, bgp);
            }
            else {
                service = Service.create(serv, bgp);
            }                
            Values values   = Values.create(serv, list);        
            body = aa.bgp(values, service); 
        }
        
        aa.setBody(body);
        metadata(aa, ast);
        return aa;
    }
    
    // not used
    ASTQuery createDataset(List<Constant> list, boolean sparql10) {
        ASTQuery aa = ASTQuery.create();
        aa.setSelectAll(true);
        aa.setNSM(ast.getNSM()); 
              
        // selector for remote dataset named graph URI list
        BasicGraphPattern bgp = BasicGraphPattern.create(Query.create(namedGraph()));

        Variable serv   = Variable.create(SERVER_VAR);
        Service service = Service.create(serv, bgp);                
        Values values   = Values.create(serv, list);        
        
        BasicGraphPattern body = BasicGraphPattern.create(values, service);        
        aa.setBody(body);
        
        metadata(aa, ast);
        return aa;
    }
    
    /**
     * Compute remote dataset named graph URI list
     * select (group_concat(?g) as ?lg) where {
     * graph ?g { select * where { ?s ?p ?o } }
     * }
     */
    ASTQuery namedGraph() {
        ASTQuery aa = ASTQuery.create();
        
        Triple t = Triple.create(Variable.create("?s"), Variable.create("?p"), Variable.create("?o"));
        BasicGraphPattern ng = BasicGraphPattern.create(t);
        ASTQuery ang = aa.subCreate();
        ang.setBody(ng);
        ang.setSelectAll(true);
        ang.setLimit(1);
        
        
        BasicGraphPattern bgp = BasicGraphPattern.create(Query.create(ang));        
        Source src = Source.create(Variable.create("?g"), bgp);
        BasicGraphPattern body = BasicGraphPattern.create(src);
        aa.setBody(body);
        Term agg = Term.function("group_concat");
        agg.setModality(";");
        agg.add(Variable.create("?g"));
        aa.defSelect(Variable.create("?lg"), agg);
        return aa;
    }
    
    void metadata(ASTQuery aa, ASTQuery ast) {
        if (ast.hasMetadata(Metadata.EVENT) || ast.hasMetadata(Metadata.SEQUENCE) || ast.hasMetadata(Metadata.TRACE)) {
            Metadata m = new Metadata();
            aa.setMetadata(m);
        
            if (ast.hasMetadata(Metadata.EVENT)) {
                m.add(Metadata.EVENT);
                aa.setDefine(ast.getDefine());
                aa.setDefineLambda(ast.getDefineLambda());
            }
            
            if (ast.hasMetadata(Metadata.SEQUENCE)) {
                m.add(Metadata.SEQUENCE);
            }
            
            if (ast.hasMetadata(Metadata.TRACE)) {
                m.add(Metadata.TRACE);
            }
        }
    }
    
    
    BasicGraphPattern createBGP(Variable serv, ASTQuery aa) {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        int i = 0;        
        i = selectPredicate(aa, bgp, i);        
        selectTriple(aa, bgp, i);                
        return bgp;
    }
    
    int selectPredicate(ASTQuery aa, BasicGraphPattern bgp, int i) {
        for (Constant p : ast.getPredicateList()) {
            if (p.getLabel().equals(ASTQuery.getRootPropertyURI())) {
                // predicate with variable: skip it
            } else {
                Variable s = Variable.create("?s");
                Variable o = Variable.create("?o");
                Triple t = aa.triple(s, p, o);

                Variable var;
                if (count) {
                    var = count(aa, bgp, t, i);
                } else {
                    var = exist(aa, bgp, t, i);
                }
                declare(p, var);

                i++;
            }
        }
        return i;
    }
    
    // consider specific triple such as triple with constant value
    int selectTriple(ASTQuery aa, BasicGraphPattern bgp, int i) {
        if (getVisitor().isSelectFilter()) {
            i = selectTripleFilter(aa, bgp, i);

        } else {
            i = selectTripleBasic(aa, bgp, i);
        }

        if (getVisitor().isFederateJoin()) {
            // generate bind (exists {s p o . o q r} as ?b)
            // for each pair of connected triple
            // to test existence of join on each endpoint
            i = selectTripleJoin(aa, bgp, i);
        }
        
        return i;
    }

    /**
     * generate exists { s p o filter F } where F variables are bound by s p o
     * consider also triple with constant
     * by default filter limited to subset of filters: 
     * = regex strstarts strcontains
     */
    int selectTripleFilter(ASTQuery aa, BasicGraphPattern bgp, int i) {
        List<BasicGraphPattern> list = new SelectorFilter(getVisitor(), ast).process();
        HashMap<Triple, Triple> mapNoFilter = new HashMap<>();
        
        for (BasicGraphPattern exp : list) {
            Triple t = exp.get(0).getTriple();
            if (exp.size() > 1 || selectable(t)) {
                Variable var = null;
                String name = null;
                // if t already processed (same triple but not same java object)
                // if both t have no filter
                // no need to duplicate bind (exists {s p o} as ?b)
                // reuse former t variable

                if (exp.size() == 1) {
                    // no filter, just t
                    // former t must have no filter
                    name = getVariable(t, mapNoFilter);
                    mapNoFilter.put(t, t);
                }
                
                if (name !=null) {
                    // occurrence of same triple already processed with variable name
                    // do not duplicate exists clause on same triple
                    // reuse former variable name
                    var = new Variable(name);
                }                
                else if (count) {
                    var = count(aa, bgp, protect(exp), i++);
                } else {
                    var = exist(aa, bgp, protect(exp), i++);
                }
                
                declare(t, var);
            }
        }
        
        return i;
    }
    
    // rewrite bnode with unique bnode ID because 
    // different exists clause must not reuse same bnode !!!
    BasicGraphPattern protect(BasicGraphPattern bgp) {
        BasicGraphPattern exp = BasicGraphPattern.create();
        HashMap<Atom, Atom> map = new HashMap<>();
        
        for (Exp e : bgp) {
            if (e.isTriple()) {
                Triple t = e.getTriple();
                if (t.getSubject().isBlankNode() || t.getObject().isBlankNode()) {
                    Triple nt = protect(map, t); 
                    exp.add(nt);
                }
                else {
                    exp.add(t);
                }
            }
            else {
                exp.add(e);
            }
        }
        
        if (map.isEmpty()) {
            return bgp;
        }
        return exp;
    }
    
    Triple protect(HashMap<Atom, Atom> map, Triple t) {
        Triple nt = t.duplicate();
        if (t.getSubject().isBlankNode()) {
            nt.setSubject(bnode(map, t.getSubject()));
        }
        if (t.getObject().isBlankNode()) {
            nt.setObject(bnode(map, t.getObject()));
        }
        return nt;
    }
    
    Triple protect(Triple t) {
        return protect(new HashMap<>(), t);
    }
    
    Atom bnode(HashMap<Atom, Atom> map, Atom node) {
        Atom bn = map.get(node);
        if (bn == null) {
            bn = bnode();
            map.put(node, bn);
        }
        return bn;
    }
    
    Atom bnode() {
        return Constant.createBlank(BNODE_ID+bnode++);
    }
    
    
    // generate bind (exists {t1 . t2} as ?b)
    // for each pair of connected triple
    // to test existence of join on each endpoint
    // use case: determine if service uri {t1 t2} exists    
    int selectTripleJoin(ASTQuery aa, BasicGraphPattern bgp, int i) {
        SelectorFilter.JoinResult ares = new SelectorFilter(getVisitor(), ast).processJoin();
        List<BasicGraphPattern> list = ares.getBgpList();
        // join bgp should fail or not
        // map: bgp -> fail
        getAstSelector().setBgpFail(ares.getBgpFail());
        
        for (BasicGraphPattern exp : list) {
            // exp = {t1 . t2}
            Variable var;
            String name = getVariable(exp);
            
            if (name != null) {
                // {t1 . t2} already processed
                // do not duplicate exists {t1 . t2}
                // reuse variable
                var = new Variable(name);
            } else if (count) {
                var = count(aa, bgp, protect(exp), i++);
            } else {
                var = exist(aa, bgp, protect(exp), i++);
            }
            declare(exp, var);
        }
        return i;
    }

    int selectTripleBasic(ASTQuery aa, BasicGraphPattern bgp, int i) {
        for (Triple t : ast.getTripleList()) {
            if (selectable(t)) {
                // triple with constant

                Variable var;
                if (count) {
                    var = count(aa, bgp, protect(t), i);
                } else {
                    var = exist(aa, bgp, protect(t), i);
                }
                declare(t, var);

                i++;
            }
        }
        return i;
    }
    
    // if triple t1 is already processed with bind (exists {t1} as ?b)
    // return variable ?b
    // former occurrence t2 of t1 must have no filter
    String getVariable(Triple t1, HashMap<Triple, Triple> mapNoFilter) {
        for (Triple t2 : tripleVariable.keySet()) {
            if (t1.equals(t2) && mapNoFilter.containsKey(t2)) {
                return tripleVariable.get(t2);
            }
        }
        return null;
    }
            
    // retrieve occurrence of same bgp = {t1 . t2}
    // return variable if any
    String getVariable(BasicGraphPattern bgp) {
        for (BasicGraphPattern exp : bgpVariable.keySet()) {
            if (bgp.equalsTriple(exp)) {
                return bgpVariable.get(exp);
            }
        }
        return null;
    }
    
    boolean selectable(Triple t) {
        return (t.getSubject().isConstant() || t.getObject().isConstant());
    }
        
    Variable count(ASTQuery aa, BasicGraphPattern bgp, Triple t, int i) {
        return count(aa, bgp, aa.bgp(t), i);
    }
    
    Variable count(ASTQuery aa, BasicGraphPattern bgp, BasicGraphPattern bb, int i) {
        ASTQuery a = aa.subCreate();
        
        Term fun = Term.function(Processor.COUNT);
        Variable var = a.variable("?c_" + i);
        a.defSelect(var, fun);
        
        Term bound = Term.create(">", var, Constant.create(0));
        Variable varBound = a.variable("?v_" + i);
        aa.defSelect(varBound, bound);
        
        a.setBody(bb);
        
        bgp.add(a.bgp(Query.create(a)));
        
        return varBound;
    }
    
    Variable exist(ASTQuery aa, BasicGraphPattern bgp, Triple t, int i) {
        return exist(aa, bgp, aa.bgp(t), i);
    }
    
    Variable exist(ASTQuery aa, BasicGraphPattern bgp, BasicGraphPattern bb, int i) {
        if (SELECT_EXIST) {
            return selectExist(aa, bgp, bb, i);
        }
        else {
            return basicExist(aa, bgp, bb, i);
        }
    }
    
    Variable basicExist(ASTQuery aa, BasicGraphPattern bgp, BasicGraphPattern bb, int i) {
        Variable var = aa.variable("?b" + i++);
        Binding exist = Binding.create(aa.createExist(bb, false), var);
        bgp.add(exist);
        return var;
    }
    
    Variable selectExist(ASTQuery aa, BasicGraphPattern bgp, BasicGraphPattern bb, int i) {
        Variable var = aa.variable("?b" + i++);
        
        ASTQuery a = aa.subCreate();
        a.setSelectAll(true);
        a.setBody(bb);
        a.setLimit(1); 
        
        BasicGraphPattern exp = BasicGraphPattern.create(Query.create(a));
        Binding exist = Binding.create(aa.createExist(exp, false), var);
        bgp.add(exist);
        return var;
    }

    
    /**
     * for SPARQL 1.0 
     * select * where {
     * optional { si pi oi }
     * } limit 1
     * }
     * 
     */
    BasicGraphPattern createBGP10(ASTQuery aa) {
        ArrayList<Triple> tripleList = new ArrayList<>();
        int i = 0;
        for (Constant p : ast.getPredicateList()) {
            if (p.getLabel().equals(ASTQuery.getRootPropertyURI())) {

            } else {
                Variable s = Variable.create("?s" + i);
                Variable var = Variable.create("?o" + i++);
                Triple t = aa.triple(s, p, var);
                tripleList.add(t);
                declare(p, var);
            }
        }
        
        Exp option = optional(tripleList);
        
        ASTQuery a = aa.subCreate();
        a.setSelectAll(true);
        a.setBody(BasicGraphPattern.create(option));
        a.setLimit(1);
        
        BasicGraphPattern bgp = BasicGraphPattern.create(Query.create(a));
        
        return bgp;
    }
    
     /**
     * Create BGP to query graph index
     */
    BasicGraphPattern createBGPIndex(Variable serv, ASTQuery aa) {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        int i = 0;
        
        for (Constant p : ast.getPredicateList()) {
            if (p.getLabel().equals(ASTQuery.getRootPropertyURI())) {

            } else {
                Constant pns = aa.createQName("idx:namespace");
                Constant pdt = aa.createQName("idx:data");
                Constant ppr = aa.createQName("idx:predicate");
                
                Variable ns  = Variable.create("?ns");
                Variable dt  = Variable.create("?dt");
                Variable pr  = Variable.create("?pr");
                
                Triple t1 = aa.triple(serv, pns, ns);
                Triple t2 = aa.triple(ns, pdt, dt);
                Triple t3 = aa.triple(dt, ppr, p);
                
                // exists { ?serv idx:namespace/idx:data/idx:predicate predicate }
                Variable var = exist(aa, bgp, aa.bgp(t3, t2, t1), i);
                declare(p, var);
                                
                i++;
            }                      
        }
        
        selectTriple(aa, bgp, i);
                
        return bgp;
    }
    
    Exp optional(List<Triple> list) {
        Optional option = new Optional(BasicGraphPattern.create(), BasicGraphPattern.create(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            option = new Optional(BasicGraphPattern.create(option), BasicGraphPattern.create(list.get(i)));
        }
        return option;
    }

    public HashMap<String, List<Atom>> getPredicateService() {
        return getAstSelector().getPredicateService();
    }   

    public HashMap<Triple, List<Atom>> getTripleService() {
        return getAstSelector().getTripleService();
    }
    
    // map: bgp -> (uri)
    // where bgp is join of (two) triples
    public HashMap<BasicGraphPattern, List<Atom>> getBgpService() {
        return getAstSelector().getBgpService();
    }
    
    public ASTSelector getAstSelector() {
        return astSelector;
    }

    public void setAstSelector(ASTSelector astSelector) {
        this.astSelector = astSelector;
    }

    public Mappings getMappings() {
        return mappings;
    }

    public Selector setMappings(Mappings mappings) {
        this.mappings = mappings;
        return this;
    }

    public FederateVisitor getVisitor() {
        return vis;
    }

    public void setVisitor(FederateVisitor vis) {
        this.vis = vis;
    }

    public QuerySolver getQuerySolver() {
        return exec;
    }

    public void setQuerySolver(QuerySolver exec) {
        this.exec = exec;
    }
    
        /**
     * BUG when service 1.0 & service 1.1 and there are triples in 1.1
     * the triples of 1.0 are considered as absent although they are present as predicate
     */
    void process2() throws EngineException {
        if (sparql10) {
            process10(getServiceList(true));
            predicateVariable.clear();
            process11(getServiceList(false));
        }
        else {
            process11(ast.getServiceListConstant());
        }
    }
    void process10(List<Constant> list) throws EngineException {
        Date d1 = new Date();
        ASTQuery aa = createSelector(list, true);
        Mappings map = getQuerySolver().basicQuery(aa);
        if (ast.isDebug()) {
            System.out.println(map);
        }
        for (Mapping m : map) {
            IDatatype serv =  m.getValue(SERVER_VAR);
            for (String pred : predicateVariable.keySet()) {
                String var = predicateVariable.get(pred);
                IDatatype val = m.getValue(var);
                if (val != null) {
                    getPredicateService().get(pred).add(Constant.create(serv));
                }
            }
        }
        
        Date d2 = new Date();
        trace(map, d1, d2);
    }
    
        List<Constant> getServiceList(boolean sparql10) {
        List<Constant> list = ast.getServiceListConstant();
        List<Constant> res = new ArrayList<>();
        for (Constant serv : list) {
            if (sparql10){
                if (ast.hasMetadataValue(Metadata.SPARQL10, serv.getLabel())) {
                    res.add(serv);
                }
            }
            else {
                if (! ast.hasMetadataValue(Metadata.SPARQL10, serv.getLabel())) {
                    res.add(serv);
                }
            }
        }
        return res;
    }

    public int getNbEndpoint() {
        return nbEndpoint;
    }

    public Selector setNbEndpoint(int nbEndpoint) {
        this.nbEndpoint = nbEndpoint;
        return this;
    }

    public double getNbSuccess() {
        return nbSuccess;
    }

    public Selector setNbSuccess(double nbSuccess) {
        this.nbSuccess = nbSuccess;
        return this;
    }

}
