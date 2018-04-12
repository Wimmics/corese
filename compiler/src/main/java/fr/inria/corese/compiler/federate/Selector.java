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
    QuerySolver exec;
    boolean sparql10 = false;
    
    Selector(QuerySolver e, ASTQuery ast) {
        this.ast = ast;
        exec = e;
        predicateService  = new HashMap<>();
        predicateVariable = new HashMap<>();
        
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
        ASTQuery aa = create(list, false);
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
        }

        trace();
    }
    
    void process10(List<Constant> list) {
        ASTQuery aa = create(list, true);
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
    
    void trace() {
        if (ast.isDebug()) {
            System.out.println("Triple Selection");
            for (String pred : predicateService.keySet()) {
                System.out.println(pred + " " + predicateService.get(pred));
            }
        }
    }
    
    void declare(Constant p, Variable var) {
        predicateVariable.put(p.getLabel(), var.getLabel());
        if (! predicateService.containsKey(p.getLabel())) {
            predicateService.put(p.getLabel(), new ArrayList<Atom>());
        }
    }
    
    /**
     * select * where {
     *  values ?serv {s1, sn}
     *  service ?serv {
     *      bind (exists { ?s predicate_i ?o } as ?b_i)
     *  }
     * }
     */
    ASTQuery create(List<Constant> list, boolean sparql10) {
        ASTQuery aa = ASTQuery.create();
        aa.setSelectAll(true);
        aa.setNSM(ast.getNSM());        
        BasicGraphPattern bgp = (sparql10) ? createBGP10(aa) : createBGP(aa);
                        
        Variable serv = Variable.create(SERVER_VAR);
        Values values = Values.create(serv, list);
        
        Service service = Service.create(serv, bgp);
        aa.setBody(BasicGraphPattern.create(values, service));
        return aa;
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
        return bgp;
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
