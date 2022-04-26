package fr.inria.corese.compiler.federate;

import fr.inria.corese.compiler.federate.URI2BGPList.BGP2URI;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Union;
import java.util.ArrayList;
import java.util.List;

/**
 * Rewrite BGP of triple with several URI
 * input:  
 * map: uri -> bgpList
 * create:
 * map: bgp -> uriList where bgp are connected
 * 
 * when BGP_LIST = true
 * generate list or partition of connected bgp  with |bgp|>1
 * generate service S {bgp}
 * and complete each partition with missing triples
 * 
 * when BGP_LIST = false
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
    public static boolean BGP_LIST = true;
    public static boolean TRACE_BGP_LIST = false;
    
    private FederateVisitor visitor;
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
        // rewrite connected bgp 
        List<BasicGraphPattern> sortedList = bgp2uri.sortKeyList();
        
        if (BGP_LIST) {
            // compute list of partition of connected bgp
            // and complete each partition with missing triple
            List<List<BasicGraphPattern>> partitionList = partition(sortedList);
            trace(sortedList, partitionList);
            
            for (List<BasicGraphPattern> bgpList : partitionList) {
                // rewrite each partition and complete with missing triples 
                BasicGraphPattern exp
                        = process(namedGraph, body, bgpList, filterList);
                list.add(exp);
            }

        } else {
            // consider each bgp and complete with missing triple
            for (BasicGraphPattern bgp : sortedList) {
                if (bgp.size() > 1) {
                    // rewrite bgp and complete with triples not in bgp
                    BasicGraphPattern exp
                            = process(namedGraph, body, List.of(bgp), filterList);
                    list.add(exp);
                }
            }
        }
                
        if (list.isEmpty()) {
            // no bgp (with several uri) and size > 1
            // process with one empty bgp to get all triples
            Exp exp = process(namedGraph, body, new ArrayList<>(), filterList);
            if (exp.size()==0) {
                return null;
            }
            return exp;
        }
        Exp union = union(list, 0);
        return union;
    }
    
    // rewrite bgpList partition of connected bgp list
    // complete with other triple
    BasicGraphPattern process
        (Atom namedGraph, Exp body, List<BasicGraphPattern> bgpList, ArrayList<Exp> filterList) {

        BasicGraphPattern exp = BasicGraphPattern.create();
        
        if (bgpList.size() > 0) {
            for (BasicGraphPattern bgp : bgpList) {
                // list of uri for the bgp.
                List<String> uriList = bgp2uri.get(bgp);
                // insert relevant filter from body into bgp
                getVisitor().filter(body, bgp);
                //Service service = Service.newInstance(uriList, bgp);
                Service service = getVisitor().getRewriteTriple()
                        .rewrite(namedGraph, bgp, getVisitor().getAtomList(uriList));
                exp.add(service);
            }
        }

        // complete with other triple (with several uri) not in bgp
        for (Triple triple : uriList2bgp.getTripleList()) {
            if (! contains(bgpList, triple)) {
                Service service = getVisitor().getRewriteTriple()
                 .rewriteTripleWithSeveralURI(namedGraph, triple, body, filterList);
               // getVisitor().filter(body, service.getBodyExp());
                exp.add(service);
            }
        }
        
        if (!visitor.isFederateBGP()) {
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
        getVisitor().getSimplify().merge(exp, true);
        // @todo: filter

        return exp;
    }
        
    boolean contains(List<BasicGraphPattern> list, Triple t) {
        for (BasicGraphPattern bgp : list) {
            if (bgp.getBody().contains(t)) {
                return true;
            }
        }
        return false;
    }
        
    // return list of partition of connected bgp
    List<List<BasicGraphPattern>> partition(List<BasicGraphPattern> bgpList) {
        List<List<BasicGraphPattern>> res  = new ArrayList<>();
        rec(bgpList, new ArrayList<>(), res, 0);        
        return res;
    }

    // compute all combinaisons of disjoint bgp of in where |bgp| > 1 
    void rec(List<BasicGraphPattern> bgpList, List<BasicGraphPattern> res, List<List<BasicGraphPattern>> resList, int i) {
        boolean suc = false;
        
        for (int j = i; j<bgpList.size(); j++) {
            BasicGraphPattern bgp = bgpList.get(j);
            if (bgp.size()>1 && ! bgp.hasIntersection(res)) {
                suc = true;
                res.add(bgp);
                rec(bgpList, res, resList, j+1);
                res.remove(res.size()-1);
            }
        }
        
        if (!suc && !res.isEmpty()) {
            resList.add(List.copyOf(res));
        }
    }   
    

    void trace(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
    }
        
    void trace(List<BasicGraphPattern> sortedList, List<List<BasicGraphPattern>> alist) {
        if (TRACE_BGP_LIST) {
            for (BasicGraphPattern bgp : sortedList) {
                System.out.println("bgp: " + bgp);
                System.out.println("");
            }
            for (List<BasicGraphPattern> ll : alist) {
                System.out.println("partition: " + ll);
                System.out.println("");
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

    public FederateVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(FederateVisitor visitor) {
        this.visitor = visitor;
    }
    
    
}
