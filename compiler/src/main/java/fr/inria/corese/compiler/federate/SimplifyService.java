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
public class SimplifyService extends Util {
    
    
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
       
}
