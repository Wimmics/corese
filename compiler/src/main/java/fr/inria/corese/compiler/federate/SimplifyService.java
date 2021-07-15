package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Heuristics:
 * Group and restrict federated services on the intersection of their URL list
 * 
 */
public class SimplifyService {
    
    
    Exp simplify(Exp bgp) {
        Service current = null;
        List<Atom> inter = null;
        List<Service> serviceList = new ArrayList<>();
        
        for (Exp exp : bgp) {
            
            if (exp.isService()) {
                Service s = exp.getService();
                List<Atom> alist = s.getServiceList();
                
                if (current == null) {
                    current = s;
                    inter = alist;
                }
                else {
                    inter = intersection(inter, alist);s.isConnected(exp);
                    if (inter.isEmpty() || ! current.getBodyExp().isConnect(s.getBodyExp())) {
                        // former service intersection do not share any URL with s
                        // or they are disconnected
                        // start new intersection 
                        current = s;
                        inter = alist;
                    } else {
                        // merge current and s
                        current.setURLList(inter);
                        current.getBodyExp().include(s.getBodyExp());
                        serviceList.add(s);
                    }
                }
            }
        }
        
        for (Service s : serviceList) {
            bgp.getBody().remove(s);
        }
        
        return bgp;
    }
    
    List<Atom> myIntersection(List<Atom> l1, List<Atom> l2) {
        if (l1 == null) {
            return l2;
        }
        return intersection(l1, l2);
    }

    
    List<Atom> intersection(List<Atom> l1, List<Atom> l2) {
        List<Atom> list = new ArrayList<Atom>();
        for (Atom at : l1) {
            if (l2.contains(at)) {
                list.add(at);
            }
        }
        return list;
    }
    
}
