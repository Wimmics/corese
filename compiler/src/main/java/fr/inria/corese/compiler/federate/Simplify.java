package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Union;
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
            map = new HashMap<>();
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
    
    
    Exp simplifyBGP(Exp bgp) {
        bgp = merge(bgp);
        if (visitor.isBounce()){
            bgp = bounce(bgp);
        }
        return bgp;
    }
        
    /**
     * BGP { service URI {EXP1} service URI {EXP2} EXP3 } ::= 
     * BGP { service URI {EXP1 EXP2} EXP3 }
     *
     * TODO: merge when there is only one URI ???
     */ 
    Exp merge(Exp bgp) {    
        ServiceList map1 = new ServiceList();
        ServiceList map2 = new ServiceList();        
        // create map: service URI -> list (Service)
        for (Exp exp : bgp) {
            if (exp.isService()){ 
                if (exp.getService().isFederate()) {
                    // skip
                }               
                else if (isTripleOnly(exp.getBodyExp())) {
                    // do not merge basic BGP with same service URI
                    // because they are not connected 
                    map2.add(exp.getService());
                }
                else {
                    map1.add(exp.getService());
                }
            }
        }
              
        // group several services with same URI into one service
        for (List<Service> list : map1.getMap().values()) {
            if (list.size() > 1) {
                int i = 0;
                Service first = list.get(0);
                
                for (Service s : list) {
                    if (i++ > 0) {
                        first.getBodyExp().include(s.getBodyExp());                       
                        bgp.getBody().remove(s);
                    }
                }
            }
        }
        
        // @local rdfs:label
        /**
         * if there is a service s1 with several uri with  a triple 
         *  annotated as @local and if there is a service s2 with 
         * one of the URI of s1, s1 can be merged with s2
         */
        ArrayList<Service> list = new ArrayList<>();
        for (Exp exp : bgp) {
            if (exp.isService() && isLocalizable(exp.getService())) {
                Service serv = getCandidate(exp.getService(), map2);
                if (serv != null) {
                    serv.getBodyExp().include(exp.getService().getBodyExp());
                    list.add(exp.getService());
                }               
            }
        }
  
        for (Service s : list) {
            bgp.getBody().remove(s);
        }
               
        return bgp;
    }
    
    
    Service getCandidate(Service serv, ServiceList map) {
        Service res = null;       
        for (Atom name : serv.getServiceList()) {
            List<Service> list = map.getMap().get(name.getLabel());
            if (list != null) {
                if (res == null) {
                    res = list.get(0);
                    if (! serv.getBodyExp().isConnected(res.getBodyExp())) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        return res;
    }
    
    boolean isLocalizable(Service exp) {
        Exp body = exp.getBodyExp();
        return exp.isFederate()
                && body.size() == 1 && body.get(0).isTriple()
                && body.get(0).getTriple().getPredicate().isConstant()
                && visitor.getAST().hasMetadata(Metadata.LOCAL, body.get(0).getTriple().getPredicate().getLabel());
    }
    
    /**
     * BGP { service URI1 { EXP1 } service URI2 { EXP2 } }
     * if URI1 accept bouncing and EXP1.isConnected(EXP2)
     * ->
     * BGP { service URI1 { EXP1 service URI2 { EXP2 } } }
     */
    
    Exp bounce(Exp bgp) {
        HashMap<Service, Boolean> done   = new HashMap();
        HashMap<Service, Boolean> remove = new HashMap();
        
        for (int i = 0; i < bgp.size(); i++) {
            Exp e1 = bgp.get(i);
            if (done.get(e1) == null && e1.isService() && ! e1.getService().isFederate()) {
                Service s1 = e1.getService();
                
                for (int j = i + 1; j < bgp.size(); j++) {
                    Exp e2 = bgp.get(j);
                    if (done.get(e2) == null && e2.isService() && ! e2.getService().isFederate()) {
                        Service s2 = e2.getService();
                        
                        if (! s1.getServiceName().equals(s2.getServiceName())
                           && s1.getBodyExp().isConnected(s2.getBodyExp())) {
                            
                            if (bounce(s1)) {
                                s1.getBodyExp().add(s2);
                                done.put(s2, true);
                                remove.put(s2, true);
                                break;
                            } else if (bounce(s2)) {
                                s2.getBodyExp().add(s1);
                                done.put(s2, true);
                                remove.put(s1, true);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        for (Service exp : remove.keySet()) {
            bgp.getBody().remove(exp);
        }
        
        return bgp;
    }
    
    // @bounce <URI>
    boolean bounce(Service s) {
        return visitor.getAST().hasMetadataValue(Metadata.BOUNCE, s.getServiceName().getLabel());
    }
       
    boolean isTripleOnly(Exp exp) {
        for (Exp ee : exp) {
            if (! ee.isTriple()) {
                return false;
            }
        }
        return true;
    }
    
    boolean isUnionTripleOnly(Exp bgp) {
        if (bgp.size() == 1 && bgp.get(0).isUnion()) {
            Union union = bgp.get(0).getUnion();
            return isTripleOnly(union.get(0)) && isTripleOnly(union.get(1)) ;
        }
        return false;
    }
    
    boolean isUnionOrTripleOnly(Exp bgp) {
        return isUnionTripleOnly(bgp) || isTripleOnly(bgp);
    }
    
      /**
     * service s {e1} optional { service s {e2}}
     * ->
     * service s { e1 optional {e2} }
     * and simplifySelectFrom(exp)
     */
    Exp simplifyStatement(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            if (e1.isService() && e2.isService()) {
                Service s1 = e1.getService();
                Service s2 = e2.getService();               
                return simplifyService(exp, s1, s2);
            }
        }
        return exp;
    }
    
    Exp simplifyService(Exp exp, Service s1, Service s2) {
        if (isSimplifyUnion(exp, s1, s2)) {
            return simplifyUnion(exp, s1, s2);
        }
        if (!s1.isFederate() && !s2.isFederate()
                && s1.getServiceName().equals(s2.getServiceName())) {
            exp.set(0, s1.getBodyExp());
            exp.set(1, s2.getBodyExp());
            Exp simple = simplifyGraph(exp);
            Service s = Service.create(s1.getServiceName(),
                            BasicGraphPattern.create(simple));
            return s;
        }
        return simplifyService2(exp, s1, s2);
    }
    
    /**
     * {service S {t1}} union {service S {t2}}
     * ::=
     * service S { {t1} union {t2} }
     */
    Service simplifyUnion(Exp exp, Service s1, Service s2) {
        Union union = Union.create(s1.getBodyExp(), s2.getBodyExp());
        Service s = Service.create(s1.getServiceList(), BasicGraphPattern.create(union));
        return s;
    }
    
    boolean isSimplifyUnion(Exp exp, Service s1, Service s2) {
        return exp.isUnion() && s1.isFederate() && s2.isFederate() 
            && sameURIList(s1, s2)
            && isUnionOrTripleOnly(s1.getBodyExp()) && isUnionOrTripleOnly(s2.getBodyExp());
    }
    
    boolean sameURIList(Service s1, Service s2) {
        return same(s1.getServiceList(), s2.getServiceList());
    }
    
   boolean same(List<Atom> l1, List<Atom> l2) {
       if (l1.size() != l2.size()) {
           return false;
       }
       for (Atom s : l1) {
           if (! l2.contains(s)) {
               return false;
           }
       }
       return true;
   }
    
    Exp simplifyService2(Exp exp, Service s1, Service s2) {
        if (!s1.isFederate() && isLocalizable(s2)
                && s2.getServiceList().contains(s1.getServiceName())) {
            exp.set(0, s1.getBodyExp());
            exp.set(1, s2.getBodyExp());
            Exp simple = simplifyGraph(exp);
            Service s = Service.create(s1.getServiceName(),
                            BasicGraphPattern.create(simple));
            return s;
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
                    exp.set(0, g1.getBodyExp());
                    exp.set(1, g2.getBodyExp());
                    Source g = Source.create(g1.getSource(), exp);
                    return g;
                }
            }
        }
        return exp;       
    }
    
}
