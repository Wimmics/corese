package fr.inria.corese.kgenv.federate;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Binding;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Service;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Values;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
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
    
    Selector(QuerySolver e, ASTQuery ast) {
        this.ast = ast;
        exec = e;
        predicateService  = new HashMap<>();
        predicateVariable = new HashMap<>();
    }
    
    void process() {
        ASTQuery aa = create();
        Mappings map = exec.basicQuery(aa);
        
        for (Mapping m : map) {
            IDatatype serv = (IDatatype) m.getValue(SERVER_VAR);
            for (String pred : predicateVariable.keySet()) {
                String var = predicateVariable.get(pred);
                IDatatype val = (IDatatype) m.getValue(var);
                if (val.booleanValue()) {
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
        predicateService.put(p.getLabel(), new ArrayList<Atom>());
    }
    
    /**
     * select * where {
     *  values ?serv {s1, sn}
     *  service ?serv {
     *      bind (exists { ?s predicate_i ?o } as ?b_i)
     *  }
     * }
     */
    ASTQuery create() {
        ASTQuery aa = ASTQuery.create();
        aa.setSelectAll(true);
        aa.setNSM(ast.getNSM());
        BasicGraphPattern bgp = BasicGraphPattern.create();
        int i = 0;
        
        for (Constant p : ast.getPredicateList()) {
            Variable s = Variable.create("?s");
            Variable o = Variable.create("?o");
            Triple t   = aa.triple(s, p, o);
            BasicGraphPattern bb = BasicGraphPattern.create(t);
            Variable var = Variable.create("?b" + i++);      
            Binding exist = Binding.create(aa.createExist(bb, false), var);
            bgp.add(exist);
            declare(p, var);
        }
                
        Variable serv = Variable.create(SERVER_VAR);
        Values values = Values.create(serv, getServiceList());
        
        Service service = Service.create(serv, bgp);
        aa.setBody(BasicGraphPattern.create(values, service));
        return aa;
    }
    
    ArrayList<Constant> getServiceList() {
        ArrayList<Constant> list = new ArrayList<>();
        for (Atom at : ast.getServiceList()) {
            list.add(at.getConstant());
        }
        return list;
    }

}
