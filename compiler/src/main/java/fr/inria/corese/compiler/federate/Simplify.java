package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Simplify {
    
    FederateVisitor visitor;
    private boolean debug = false;
    
    Simplify(FederateVisitor vis) {
        visitor = vis;
    }
    
    class ServiceList {
        HashMap<String, List<Service>> map;
        
        ServiceList() {
            map = new HashMap<String, List<Service>>();
        }
        
        void add(Service serv) {
            List<Service> l = map.get(serv.getServiceName().getLabel());
            if (l == null) {
                l = new ArrayList<>();
                map.put(serv.getServiceName().getLabel(), l);
            }
            l.add(serv);
        }
        
        HashMap<String, List<Service>> getMap() {
            return map;
        }
    }
    
    /**
     * BGP { service URI {EXP1} service URI {EXP2} EXP3 }
     * ::= 
     * BGP { service URI {EXP1 EXP2} EXP3 }
     */
    Exp simplifyBGP(Exp bgp) {
        ServiceList map = new ServiceList();
        
        for (Exp exp : bgp) {
            if (exp.isService() && ! exp.getService().isFederate()) {
                if (isTripleOnly(exp.get(0))) {
                    // do not merge basic BGP with same service URI
                    // because they are not connected 
                }
                else {
                    map.add(exp.getService());
                }
            }
        }
        
        for (List<Service> list : map.getMap().values()) {
            if (list.size() > 1) {
                int i = 0;
                Service first = list.get(0);
                
                for (Service s : list) {
                    if (i++ > 0) {
                        for (Exp ee : s.get(0)) {
                            first.get(0).add(ee);
                        }
                        bgp.getBody().remove(s);
                    }
                }
            }
        }
               
        return bgp;
    }
    
    boolean isTripleOnly(Exp exp) {
        for (Exp ee : exp) {
            if (! ee.isTriple()) {
                return false;
            }
        }
        return true;
    }
    
      /**
     * service s {e1} optional { service s {e2}}
     * ->
     * service s { e1 optional {e2} }
     * and simplifySelectFrom(exp)
     */
    Exp simplify(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            if (e1.isService() && e2.isService()) {
                Service s1 = e1.getService();
                Service s2 = e2.getService();
                if (s1.getServiceList().size() == 1
                        && s2.getServiceList().size() == 1
                        && s1.getServiceName().equals(s2.getServiceName())) {
                    exp.set(0, s1.get(0));
                    exp.set(1, s2.get(0));
                    Exp simple = simplifyGraph(exp);
                    Service s = 
                        Service.create(s1.getServiceName(), 
                            BasicGraphPattern.create(simple));
                    return s;
                }
            }
        }
        return exp;
    }
    
    /**
     * select from  g { e1 } optional { select from  g { e2 } }
     * ->
     * select from  g { e1 optional { e2 } }
     */
    Exp simplifySelectFrom(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            if (e1.isQuery() && e2.isQuery()){
                ASTQuery ast1 = e1.getQuery();
                ASTQuery ast2 = e2.getQuery();                
               if (ast1.getFrom().size() == 1 && 
                    ast2.getFrom().size() == 1 &&
                    ast1.getFrom().get(0).equals(ast2.getFrom().get(0))
                        && ast1.isSelectAll() && ast2.isSelectAll()) {
                    exp.set(0, ast1.getBody());
                    exp.set(1, ast2.getBody());
                    Query q = visitor.getRewriteTriple().query(BasicGraphPattern.create(exp));
                    q.getAST().getDataset().setFrom(ast1.getFrom());
                    return q;
                }
            }
        }
        return exp;       
    }

     /**
     * graph g { e1 } optional { graph g { e2 } }
     * ->
     * graph g { e1 optional { e2 } }
     */
    Exp simplifyGraph(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            if (e1.isGraph() && e2.isGraph()){
                Source g1 = e1.getNamedGraph();
                Source g2 = e2.getNamedGraph();                
               if (g1.getSource().isConstant() && g1.getSource().equals(g2.getSource())) {
                    exp.set(0, g1.get(0));
                    exp.set(1, g2.get(0));
                    Source g = Source.create(g1.getSource(), exp);
                    return g;
                }
            }
        }
        return exp;       
    }
    
}
