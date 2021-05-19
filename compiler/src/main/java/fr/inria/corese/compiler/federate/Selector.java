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
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Optional;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Ask remote endpoints if they contain predicates of the federated query
 * Build a table: triple | predicate -> { Service_1, Service_n }
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Selector {
    
    private static final String SERVER_VAR = "?serv";
    
    FederateVisitor vis;
    ASTQuery ast;
    HashMap<String, List<Atom>> predicateService;
    HashMap<String, String> predicateVariable;
    HashMap<Triple, List<Atom>> tripleService;
    HashMap<Triple, String> tripleVariable;
    QuerySolver exec;
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
        predicateService  = new HashMap<>();
        predicateVariable = new HashMap<>();
        tripleVariable    = new HashMap<>();
        tripleService     = new HashMap<>();

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
        Mappings map = exec.basicQuery(aa);
        
        exec.getLog().setASTSelect(aa);
        exec.getLog().setSelectMap(map);
        ast.getLog().setASTSelect(aa);
        ast.getLog().setSelectMap(map);
        
        for (Mapping m : map) {
            IDatatype serv = (IDatatype) m.getValue(SERVER_VAR);
            
            // table: predicate -> exists boolean variable
            for (String pred : predicateVariable.keySet()) {
                String var = predicateVariable.get(pred);
                IDatatype val = (IDatatype) m.getValue(var);
                if (val != null && val.booleanValue()) {
                    predicateService.get(pred).add(Constant.create(serv));
                }
            }
            
            // table: triple -> exists boolean variable
            for (Triple t : tripleVariable.keySet()) {
                String var = tripleVariable.get(t);
                IDatatype val = (IDatatype) m.getValue(var);
                if (val != null && val.booleanValue()) {
                    tripleService.get(t).add(Constant.create(serv));
                }
            }
            
        }
        Date d2 = new Date();
        trace(map, d1, d2);
    }
    
    void metadata(ASTQuery aa) {
        if (ast.hasMetadata(Metadata.SHOW)) {
            Metadata meta = new Metadata();
            meta.add(Metadata.SHOW);
            aa.setAnnotation(meta);
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
            IDatatype serv = (IDatatype) m.getValue(SERVER_VAR);
            for (String pred : predicateVariable.keySet()) {
                String var = predicateVariable.get(pred);
                IDatatype val = (IDatatype) m.getValue(var);
                if (val != null) {
                    predicateService.get(pred).add(Constant.create(serv));
                }
            }
        }
        
        Date d2 = new Date();
        trace(map, d1, d2);
    }
    
    List<Atom> getPredicateService(Constant pred) {
        return predicateService.get(pred.getLabel());
    }
    
    List<Atom> getPredicateService(Triple t) {
        List<Atom> list = tripleService.get(t);
        if (list != null) {
            return list;
        }
        if (t.getPredicate().isVariable()) {
            return ast.getServiceList();
        }
        return getPredicateService(t.getPredicate().getConstant());
    }
   
    void trace(Mappings map, Date d1, Date d2) {
        if (ast.hasMetadata(Metadata.TRACE)) {
            System.out.println("Selection Time: " + (d2.getTime() - d1.getTime()) / 1000.0);
        }
        if (ast.isDebug()) {
            System.out.println("Triple Selection");
            for (String pred : predicateService.keySet()) {
                System.out.println(pred + " " + predicateService.get(pred));
            }
            for (Triple t : tripleService.keySet()) {
                System.out.println(t + " " + tripleService.get(t));
            }
        }
        if (trace) {
            System.out.println(map);
        }
    }
    
    void declare(Constant p, Variable var) {
        predicateVariable.put(p.getLabel(), var.getLabel());
        if (! predicateService.containsKey(p.getLabel())) {
            predicateService.put(p.getLabel(), new ArrayList<Atom>());
        }
    }
    
    void declare(Triple t, Variable var) {
        tripleVariable.put(t, var.getLabel());
        tripleService.put(t, new ArrayList<Atom>());
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
        
        if (vis.isIndex()) {
            bgp = createBGPIndex(serv, aa);
        }
        else if (sparql10) {
            bgp = createBGP10(aa);
        }
        else {
            bgp = createBGP(serv, aa);
        }              
        
        BasicGraphPattern body;
        if (vis.isIndex()) {
            bgp.add(0,Values.create(serv, list));
            body = bgp;
        }
        else {
            Service service = Service.create(serv, bgp);                
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
        
        for (Constant p : ast.getPredicateList()) {
            if (p.getLabel().equals(ASTQuery.getRootPropertyURI())) {

            } else {
                Variable s = Variable.create("?s");
                Variable o = Variable.create("?o");
                Triple t   = aa.triple(s, p, o);
                
                Variable var;
                if (count) {
                    var = count(aa, bgp, t, i);
                }
                else {
                    var = exist(aa, bgp, t, i);
                }
                declare(p, var);
                                
                i++;
            }                      
        }
        
        selectTriple(aa, bgp, i);
                
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
    
    void selectTriple(ASTQuery aa, BasicGraphPattern bgp, int i) {
        if (vis.isSelectFilter()) {
            selectTripleFilter(aa, bgp, i);
        }
        else {
            selectTripleBasic(aa, bgp, i);
        }
    }

    /**
     * generate exists { s p o filter F } where F variables are bound by s p o
     * by default filter limited to x = y 
     */
    void selectTripleFilter(ASTQuery aa, BasicGraphPattern bgp, int i) {
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
    
    
    boolean selectable(Triple t) {
        return (t.getSubject().isConstant() || t.getObject().isConstant());
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
    
    Exp optional(List<Triple> list) {
        Optional option = new Optional(BasicGraphPattern.create(), BasicGraphPattern.create(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            option = new Optional(BasicGraphPattern.create(option), BasicGraphPattern.create(list.get(i)));
        }
        return option;
    }

}
