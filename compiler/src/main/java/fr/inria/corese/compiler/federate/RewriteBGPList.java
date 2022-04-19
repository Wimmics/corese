package fr.inria.corese.compiler.federate;

import fr.inria.corese.compiler.federate.URI2BGPList.BGP2URI;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rewrite BGP of triple with several URI
 * input:  
 * map: uri -> bgpList
 * create:
 * map: bgp -> uriList where bgp are connected
 * 
 * for each |bgp| > 1
 * create: exp = service (uriList) {bgp}
 * complete exp with every triple not in bgp: 
 * 1) with several uri
 * 2) with one uri
 * return union of exp above
 * 
 * if there is no |bgp|>1
 * generate service (S) {triple} for every triple
 * 
 */
public class RewriteBGPList {
    
    FederateVisitor visitor;
    // connected bgp of triple with one uri
    URI2BGPList uri2bgp;
    // connected bgp of triple with several uri
    URI2BGPList uriList2bgp;
    BGP2URI bgp2uri;
    
    RewriteBGPList(FederateVisitor vis, URI2BGPList map) {
        visitor = vis;
        uri2bgp = map;
        uriList2bgp = map.getUriList2bgp();
        bgp2uri = uriList2bgp.getBgp2uri();
    }
    
    // for each connected bgp with |bgp|>1 
    // generate service (uriList) {bgp}
    // complete with other triples
    // return union
    Exp process(Atom namedGraph, Exp body, ArrayList<Exp> filterList) {
        ArrayList<Exp> list = new ArrayList<>();
        // rewrite connected bgp where triple have several uri
        for (BasicGraphPattern bgp : bgp2uri.keySet()) {
            if (bgp.size()>1) {
                // rewrite bgp and complete with triples not in bgp
                BasicGraphPattern exp = 
                        process(namedGraph, body, bgp, filterList);
                list.add(exp);
            }
        }
        
        if (list.isEmpty()) {
            // no bgp with several uri and size > 1
            // process with one empty bgp to get all triples
            Exp exp = process(namedGraph, body, BasicGraphPattern.create(), filterList);
            if (exp.size()==0) {
                return null;
            }
            return exp;
        }
        Exp union = union(list, 0);
        return union;
    }
    
    // find two bgp with no triple intersection
    void bgpIntersection() {
        List<BasicGraphPattern> bgpList = bgp2uri.keyList();
        
        for (int i = 0; i < bgpList.size(); i++) {
            BasicGraphPattern bgp1 = bgpList.get(i);
            if (bgp1.size() > 1) {
                for (int j = i + 1; j < bgpList.size(); j++) {
                    BasicGraphPattern bgp2 = bgpList.get(j);
                    if (bgp2.size()>1) {
                        trace("bgp1: %s\nbgp2: %s\ninter:%s", 
                        bgp1, bgp2, bgp1.intersectionTriple(bgp2));
                    }
                }
            }
        }
    }
    
    // rewrite one connected bgp where triple have several uri
    BasicGraphPattern process
        (Atom namedGraph, Exp body, BasicGraphPattern bgp, ArrayList<Exp> filterList) {

        BasicGraphPattern exp = BasicGraphPattern.create();
        
        if (bgp.size() > 0) {
            // list of uri for the bgp.
            List<String> uriList = bgp2uri.get(bgp);
            // insert relevant filter from body into bgp
            filter(body, bgp);
            Service service = Service.newInstance(uriList, bgp);
            exp.add(service);
        }

        // complete with other triple (with several uri) not in bgp
        for (Triple triple : uriList2bgp.getTripleList()) {
            if (!bgp.getBody().contains(triple)) {
                Service service = visitor.getRewriteTriple()
                 .rewriteTripleWithSeveralURI(namedGraph, triple, bgp, filterList);
                filter(body, service.getBodyExp());
                exp.add(service);
            }
        }
        
        if (!visitor.isTestFederateNew()) {
            // complete with triple with one URI, not in bgp
            // these triple are already rewritten as service
            for (Service srv : uri2bgp.getServiceList()) {
                exp.add(srv);
            }
        }
        
        // merge service uri {A} service uri {B} as service uri {A B}
        // @todo: should we merge when A and B are not connected ?
        // use case: uri A . uri2 C . uri B
        // where A connected to C and C connected to B
        // @hint: 
        // merge(true)  merge all bgp
        // merge(false) merge connected bgp only
        visitor.getSimplify().merge(exp, true);
        // @todo: filter

        return exp;
    }
        
    void trace(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
    }
        
    // copy relevant filter from body into bgp
    void filter(Exp body, Exp bgp) {
        List<Variable> varList = bgp.getInscopeVariables();
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (exp.getFilter().isTermExistRec()) {
                    // skip
                }
                else if (exp.getFilter().isBound(varList) && 
                        !bgp.getBody().contains(exp)) {
                    bgp.add(exp);
                }               
            }
        }
    }
    
    Exp union(List<Exp> list, int n) {
        if (n == list.size()-1) {
            return list.get(n);
        }
        else {
            return Union.create(list.get(n), union(list, n+1));
        }
    }
    
    
}
