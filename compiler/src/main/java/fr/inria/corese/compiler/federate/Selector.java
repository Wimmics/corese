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
import fr.inria.corese.sparql.triple.parser.ASTSelector;
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

/**
 *
 * Ask remote endpoints if they contain predicates of the federated query
 * Build a table: triple | predicate -> ( uri )
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Selector {
    
    private static final String SERVER_VAR = "?serv";
    public static boolean TEST_JOIN = false;
    
    private FederateVisitor vis;
    ASTQuery ast;
    private ASTSelector astSelector;
    HashMap<String, String> predicateVariable;
    HashMap<Triple, String> tripleVariable;
    HashMap<BasicGraphPattern, String> bgpVariable;
    QuerySolver exec;
    // use case: reuse federated visitor source selection    
    private Mappings mappings;
    boolean sparql10 = false;
    boolean count = false;
    boolean trace = false;
    
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
    
    /**
     * BUG when service 1.0 & service 1.1 and there are triples in 1.1
     * the triples of 1.0 are considered as absent although they are present as predicate
     */
    void process() throws EngineException {
        if (sparql10) {
            process10(getServiceList(true));
            predicateVariable.clear();
            process11(getServiceList(false));
        }
        else {
            process11(ast.getServiceListConstant());
        }
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
    
    void process11(List<Constant> list) throws EngineException {
        if (list.isEmpty()) {
            return;
        }
        Date d1 = new Date();
        ASTQuery aa = createSelector(list, false);
        metadata(aa);
        Mappings map;
        
        if (getMappings() == null) {
            // compute selection
            map = exec.basicQuery(aa);
            getVisitor().setMappings(map);
        }
        else {
            // reuse selection
            map = getMappings();
        }
        
        exec.getLog().setASTSelect(aa);
        exec.getLog().setSelectMap(map);
        
        ast.getLog().setASTSelect(aa);
        ast.getLog().setSelectMap(map);
        ast.getLog().setExceptionList(exec.getLog().getExceptionList());
        
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
        getAstSelector().complete();       
        trace(map, d1, d2);
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
    }
          
    void process10(List<Constant> list) throws EngineException {
        Date d1 = new Date();
        ASTQuery aa = createSelector(list, true);
        Mappings map = exec.basicQuery(aa);
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
        
        if (getVisitor().isIndex()) {
            bgp = createBGPIndex(serv, aa);
        }
        else if (sparql10) {
            bgp = createBGP10(aa);
        }
        else {
            bgp = createBGP(serv, aa);
        }              
        
        BasicGraphPattern body;
        if (getVisitor().isIndex()) {
            bgp.add(0,Values.create(serv, list));
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
    void selectTriple(ASTQuery aa, BasicGraphPattern bgp, int i) {
        if (getVisitor().isSelectFilter()) {
            i = selectTripleFilter(aa, bgp, i);
            if (TEST_JOIN) {
                selectTripleJoin(aa, bgp, i);
            }
        }
        else {
            selectTripleBasic(aa, bgp, i);
        }
    }

    /**
     * generate exists { s p o filter F } where F variables are bound by s p o
     * consider also triple with constant
     * by default filter limited to subset of filters: 
     * = regex strstarts strcontains
     */
    int selectTripleFilter(ASTQuery aa, BasicGraphPattern bgp, int i) {
        List<BasicGraphPattern> list = new SelectorFilter(ast).process();
        for (BasicGraphPattern exp : list) {
            Triple t = exp.get(0).getTriple();
            if (exp.size() > 1 || selectable(t)) {
                Variable var;
                if (count) {
                    var = count(aa, bgp, exp, i);
                } else {
                    var = exist(aa, bgp, exp, i);
                }
                declare(t, var);
                i++;
            }
        }
        return i;
    }
    
    void selectTripleJoin(ASTQuery aa, BasicGraphPattern bgp, int i) {
        List<BasicGraphPattern> list = new SelectorFilter(ast).processJoin();
        for (BasicGraphPattern exp : list) {
            // @todo: check exp not already processed
            Variable var;
            if (count) {
                var = count(aa, bgp, exp, i);
            } else {
                var = exist(aa, bgp, exp, i);
            }
            declare(exp, var);
            i++;
        }
    }
    
    boolean selectable(Triple t) {
        return (t.getSubject().isConstant() || t.getObject().isConstant());
    }

    
    void selectTripleBasic(ASTQuery aa, BasicGraphPattern bgp, int i) {
        for (Triple t : ast.getTripleList()) {
            if (selectable(t)) {
                // triple with constant

                Variable var;
                if (count) {
                    var = count(aa, bgp, t, i);
                } else {
                    var = exist(aa, bgp, t, i);
                }
                declare(t, var);

                i++;
            }
        }
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
        Variable var = aa.variable("?b" + i++);
        Binding exist = Binding.create(aa.createExist(bb, false), var);
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

}
