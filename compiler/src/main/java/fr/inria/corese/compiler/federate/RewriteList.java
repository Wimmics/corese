package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Union;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RewriteList {
      
    FederateVisitor visitor;
    
    RewriteList(FederateVisitor vis){
        visitor = vis;
    }
    
    // group rdf list in specific service bgp
    // group connected triple with bnode variable in specific service bgp
    // modify body
    // return false when one bgp has no service
    boolean process(Exp body) {
        boolean suc = true;
        if (visitor.isProcessList()) {
            List<BasicGraphPattern> list = new ArrayList<>();
            body.getRDFList(list);
            body.getBGPWithBnodeVariable(list);
            suc = bgp2service(body, list);
        }
        return suc;
    }
        
    boolean bgp2service(Exp body, List<BasicGraphPattern> list) {
        boolean suc = true;
        for (BasicGraphPattern exp : list) {
            Exp service = bgp2service(exp);
            if (service == null) {
                // no service handle list
                visitor.logger.info("No service for exp: " + exp);
                suc = false;
            } else {
                replace(body, exp, service);
            }
        }
        return suc;
    }
    
    void replace(Exp body, BasicGraphPattern bgp, Exp serviceExp) {
        for (Exp triple : bgp) {
            body.getBody().remove(triple);
        }
        body.add(serviceExp);
    }
    

    // rewrite rdf list as service (S) { bgp }
    // where all rdf list triple are in all s in S
    Exp bgp2service(BasicGraphPattern bgp) {
        List<Atom> uriList = new ArrayList<>();
        int count = 0;
        for (Exp triple : bgp) {
            List<Atom> list = visitor.getServiceList(triple.getTriple());
            if (count++ == 0) {
                uriList = list;
            }
            else {
                uriList = intersection(uriList, list);            
            }
        }
        
        if (uriList.isEmpty()) {
            return null;
        }
        Service s = Service.create(uriList, bgp);
        return s;
    }
    
    ArrayList<Atom> intersection(List<Atom> l1, List<Atom> l2) {
        ArrayList<Atom> uriList = new ArrayList<>(); 
        for (Atom uri : l1) {
            if (l2.contains(uri)) {
                uriList.add(uri);
            }
        }
        return uriList;
    }

   
    Exp union(List<Service> list, int n) {
        if (n == list.size()-1) {
            return list.get(n);
        }
        else {
            return Union.create(list.get(n), union(list, n+1));
        }
    }
    
    
}
