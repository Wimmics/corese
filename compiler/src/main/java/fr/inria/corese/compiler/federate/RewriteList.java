package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
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
    
    // map: uri -> ({t1 t2} {t3 t4})
    // list of connected bgp where triples have several uri
    // hence triple occur several times in the map
    // @pragma: triple with one uri are rewritten as service uri {t} in body
    // triple with several uri are as is in body
    void process(Exp body) {
        if (visitor.isProcessList()) {
            processList(body);
        }
    }
    
    
    
    void processList(Exp body) {
        List<Exp> alist = new ArrayList<>();
        List<BasicGraphPattern> list = body.getRDFList();
        if (list.isEmpty()) {
            // no rdf list
        } else {
            for (BasicGraphPattern rdfList : list) {
                Exp serviceExp = processRDFList(rdfList);
                if (serviceExp == null) {
                    // no service handle list
                    visitor.logger.info("No service for list: " + rdfList);
                } else {
                    replace(body, rdfList, serviceExp);
                }
            }
        }
    }
    
    // replace list as triple in body
    // by service
    void replace(Exp body, BasicGraphPattern rdfList, Exp serviceExp) {
        for (Exp triple : rdfList) {
            body.getBody().remove(triple);
        }
        Triple t = rdfList.get(0).getTriple();
        ArrayList<Exp> list = new ArrayList<>();
        
        for (Exp exp : body) {
            if (exp.isTriple() && 
                    exp.getTriple().getObject().equals(t.getSubject())) {
                list.add(exp);
            }
        }
        
        for (Exp exp : list) {
            serviceExp.getBodyExp().add(0, exp);
            body.getBody().remove(exp);
        }
        body.add(serviceExp);
    }
    
    Exp processRDFList(BasicGraphPattern rdfList) {
        List<Atom> uriList = new ArrayList<>();
        int count = 0;
        for (Exp triple : rdfList) {
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
        Service s = Service.create(uriList, rdfList);
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
