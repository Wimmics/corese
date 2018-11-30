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
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Option;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Ask remote endpoints if they contain predicates of the federated query
 * Build a table: predicate -> { Service_1, Service_n }
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Selector {
    
    private static final String SERVER_VAR = "?serv";
    
    ASTQuery ast;
    HashMap<String, List<Atom>> predicateService;
    HashMap<String, String> predicateVariable;
    HashMap<Triple, List<Atom>> tripleService;
    HashMap<Triple, String> tripleVariable;
    QuerySolver exec;
    boolean sparql10 = false;
    
    Selector(QuerySolver e, ASTQuery ast) {
        this.ast = ast;
        exec = e;
        predicateService  = new HashMap<>();
        predicateVariable = new HashMap<>();
        tripleVariable    = new HashMap<>();
        tripleService     = new HashMap<>();
        
        if (ast.hasMetadata(Metadata.SPARQL10)){
            sparql10 = true;
        }
    }
    
    void process() {
        if (sparql10) {
            process10(getServiceList(true));
            predicateVariable.clear();
            process11(getServiceList(false));
        }
        else {
            process11(getServiceList());
        }
    }
    
    
    List<Constant> getServiceList(boolean sparql10) {
        List<Constant> list = getServiceList();
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
    
    void process11(List<Constant> list) {
        ASTQuery aa = createSelector(list, false);
        Mappings map = exec.basicQuery(aa);

        for (Mapping m : map) {
            IDatatype serv = (IDatatype) m.getValue(SERVER_VAR);
            
            for (String pred : predicateVariable.keySet()) {
                String var = predicateVariable.get(pred);
                IDatatype val = (IDatatype) m.getValue(var);
                if (val != null && val.booleanValue()) {
                    predicateService.get(pred).add(Constant.create(serv));
                }
            }
            
            for (Triple t : tripleVariable.keySet()) {
                String var = tripleVariable.get(t);
                IDatatype val = (IDatatype) m.getValue(var);
                if (val != null && val.booleanValue()) {
                    tripleService.get(t).add(Constant.create(serv));
                }
            }
            
        }

        trace();
    }
          
    void process10(List<Constant> list) {
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
        
        trace();
    }
    
    List<Atom> getPredicateService(Constant pred) {
        return predicateService.get(pred.getLabel());
    }
    
    List<Atom> getPredicateService(Triple t) {
        List<Atom> list = tripleService.get(t);
        if (list != null && !list.isEmpty()) {
            return list;
        }
        return predicateService.get(t.getPredicate().getLabel());
    }
   
    void trace() {
        if (ast.isDebug()) {
            System.out.println("Triple Selection");
            for (String pred : predicateService.keySet()) {
                System.out.println(pred + " " + predicateService.get(pred));
            }
            for (Triple t : tripleService.keySet()) {
                System.out.println(t + " " + tripleService.get(t));
            }
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
        
        // triple selector
        BasicGraphPattern bgp = (sparql10) ? createBGP10(aa) : createBGP(aa); 
        
        Variable serv   = Variable.create(SERVER_VAR);
        Service service = Service.create(serv, bgp);                
        Values values   = Values.create(serv, list);        
        
        BasicGraphPattern body = BasicGraphPattern.create(values, service);        
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
        if (ast.hasMetadata(Metadata.EVENT) || ast.hasMetadata(Metadata.NEW)) {
            Metadata m = new Metadata();
            aa.setMetadata(m);
        
            if (ast.hasMetadata(Metadata.EVENT)) {
                m.add(Metadata.EVENT);
                aa.setDefine(ast.getDefine());
                aa.setDefineLambda(ast.getDefineLambda());
            }
            
            if (ast.hasMetadata(Metadata.NEW)) {
                m.add(Metadata.NEW);
            }
        }
    }
    
    
    BasicGraphPattern createBGP(ASTQuery aa) {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        int i = 0;
        
        for (Constant p : ast.getPredicateList()) {
            if (p.getLabel().equals(ASTQuery.getRootPropertyURI())) {

            } else {
                Variable s = Variable.create("?s");
                Variable o = Variable.create("?o");
                Triple t = aa.triple(s, p, o);
                BasicGraphPattern bb = BasicGraphPattern.create(t);
                Variable var = Variable.create("?b" + i++);
                Binding exist = Binding.create(aa.createExist(bb, false), var);
                bgp.add(exist);
                declare(p, var);
            }
        }
        
        for (Triple t : ast.getTripleList()) {
            if (selectable(t)) {
                // triple with constant
                BasicGraphPattern bb = BasicGraphPattern.create(t);
                Variable var = Variable.create("?b" + i++);
                Binding exist = Binding.create(aa.createExist(bb, false), var);
                bgp.add(exist);
                declare(t, var);
            }
        }
        
        return bgp;
    }
    
    boolean selectable(Triple t) {
        return t.getPredicate().isConstant() 
                && (t.getSubject().isConstant() || t.getObject().isConstant());
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
        Option option = new Option(BasicGraphPattern.create(), BasicGraphPattern.create(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            option = new Option(BasicGraphPattern.create(option), BasicGraphPattern.create(list.get(i)));
        }
        return option;
    }
    
    
    ArrayList<Constant> getServiceList() {
        ArrayList<Constant> list = new ArrayList<>();
        for (Atom at : ast.getServiceList()) {
            list.add(at.getConstant());
        }
        return list;
    }

}
