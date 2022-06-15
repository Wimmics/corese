package fr.inria.corese.compiler.federate;

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
    private static boolean MERGE_EVEN_IF_NOT_CONNECTED = true;
    
    private FederateVisitor visitor;
    // connected bgp of triple with one uri (deprecated)
    URI2BGPList uri2bgp;
    // connected bgp of triple with several uri (current)
    URI2BGPList uriList2bgp;
    BGP2URI bgp2uri;
    private boolean mergeEvenIfNotConnected = MERGE_EVEN_IF_NOT_CONNECTED;
    
    RewriteBGPList(FederateVisitor vis, URI2BGPList map) {
        visitor = vis;
        uri2bgp = map;
        uriList2bgp = map.getUriList2bgp();
        bgp2uri = uriList2bgp.getBgp2uri();
    }
    
    // for connected bgp with |bgp|>1 
    // generate service (uriList) {bgp}
    // complete with other triples
    // return union
    Exp process(Atom namedGraph, Exp body, ArrayList<Exp> filterList) {
        ArrayList<Exp> list = new ArrayList<>();
        // list of all candidate connected bgp with |bgp|>1 
        // sorted by greater size first
        List<BasicGraphPattern> sortedList = bgp2uri.sortKeyList();
        
        if (BGP_LIST) {
            // new version
            // compute list of partition of connected bgp
            // partition means cover body with partition of triple
            // complete each partition with missing triple from body
            List<List<BasicGraphPattern>> partitionList = partition(sortedList);
            trace(sortedList, partitionList);
            
            for (List<BasicGraphPattern> bgpList : partitionList) {
                // rewrite each partition of bgp as service {bgp} 
                // and complete with missing triples 
                BasicGraphPattern exp
                        = process(namedGraph, body, bgpList, filterList);
                list.add(exp);
            }          
        } else {
            // former deprecated version
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
                
        if (list.isEmpty() || getVisitor().isFederateComplete()) {
            // complete with all lonely triples
            // process with a fake empty bgp to get all triples
            Exp exp = process(namedGraph, body, new ArrayList<>(), filterList);
            
            if (exp.size()==0) {
                if (list.isEmpty()) {
                    return null;
                }
            }
            else {
                list.add(exp);
            }
        }
        
        // clean filter from body
        for (Exp exp : filterList) {
            body.getBody().remove(exp);
        }
        
        Exp union = union(list, 0);               
        return union;
    }
    
    // rewrite one bgpList partition of connected bgp as list of service {bgp}
    // complete with missing triples
    // return one bgp with result of rewrite
    BasicGraphPattern process
        (Atom namedGraph, Exp body, List<BasicGraphPattern> bgpList, ArrayList<Exp> filterList) {
        BasicGraphPattern exp = BasicGraphPattern.create();
        
        if (bgpList.size() > 0) {
            for (BasicGraphPattern bgp : bgpList) {
                // list of uri for the bgp.
                List<String> uriList = bgp2uri.get(bgp);
                // insert relevant filter from body into bgp
                // @todo: record filter that is inserted in bgp
                // and remove it from body
                getVisitor().filter(body, bgp, filterList);
                Service service = getVisitor().getRewriteTriple()
                        .rewrite(namedGraph, bgp, getVisitor().getAtomList(uriList));
                exp.add(service);
            }
        }

        // complete with missing triples if any
        for (Triple triple : uriList2bgp.getTripleList()) {
            if (! contains(bgpList, triple)) {
                Service service = getVisitor().getRewriteTriple()
                 .rewriteTripleWithSeveralURI(namedGraph, triple, body, filterList);
               // getVisitor().filter(body, service.getBodyExp());
                exp.add(service);
            }
        }
        
        if (!visitor.isFederateBGP()) {
            // complete with missing triple with one URI, not in bgp
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
        ctrace("before simplify: %s", exp);
        getVisitor().getSimplify().merge(exp, isMergeEvenIfNotConnected());
        // @todo: filter
        ctrace("after simplify: %s", exp);
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
    
  
        
    // bgpList = list of all connected bgp (including when |bgp|=1) of all uri
    // list sorted by decreasing size
    // return list partition of connected bgp where |bgp|>1
    // (bgp1 bgp2) (bgp1 bgp3) (bgp2) (bgp3)
    // each partition is not necessarily complete wrt triple list (hence not a set partition)
    // each partition will be completed with missing triple from uriList2bgp.getTripleList()
    List<List<BasicGraphPattern>> partition(List<BasicGraphPattern> bgpList) {
        List<List<BasicGraphPattern>> res = new ArrayList<>();
        
        if (!bgpList.isEmpty() && getVisitor().isFederatePartition()) {
            BasicGraphPattern bgp = bgpList.get(0);
            if (bgp.size() == uriList2bgp.getTripleList().size()) {
                // first bgp contains all triple
                // return it directly
                res.add(List.of(bgp));
                return res;
            }
        }
        
        // natural partition with |bgp|>1 and |uri|=1, if any
        List<BasicGraphPattern> partition = bgp2uri.partition();
        ctrace("natural partition:\n%s", partition);
        // remove natural partition from candidate bgpList, if any
        List<BasicGraphPattern> subList = substract(bgpList, partition); 
        ctrace("start bgpList:\n%s", subList);
        // start rec computing with natural partition, if any
        rec(subList, partition, res, 0); 
        ctrace("list: %s", uriList2bgp.getTripleList());
        ctrace("map: %s", bgp2uri);
        return res;
    }
    
    // remove natural partition sub from candidate main, if any
    List<BasicGraphPattern> substract(List<BasicGraphPattern> main, List<BasicGraphPattern> sub) {
        if (sub.isEmpty()) {
            return main;
        }
        ArrayList<BasicGraphPattern> list = new ArrayList<>();
        for (BasicGraphPattern bgp : main) {
            if (! sub.contains(bgp)) {
                list.add(bgp);
            }
        }
        return list;
    }

    // rec compute combinaisons of disjoint bgp of bgpList where |bgp| > 1 
    void rec(List<BasicGraphPattern> bgpList, List<BasicGraphPattern> res, List<List<BasicGraphPattern>> resList, int i) {
        boolean rec = false;
        
        for (int j = i; j<bgpList.size(); j++) {
            // pick one bgp
            BasicGraphPattern bgp = bgpList.get(j);
            if (bgp.size()>1 && ! bgp.hasIntersection(res)) {
                // bgp has no triple in common with current solution
                rec = true;
                // add bgp to current solution
                res.add(bgp);
                // recurse on the rest of bgpList
                rec(bgpList, res, resList, j+1);
                // remove current bgp and consider next bgp
                res.remove(res.size()-1);
            }
        }
        
        if (!rec && !res.isEmpty()) {
            // no more recursive call, there is a solution: record it
            resList.add(List.copyOf(res));
        }
    }   
    
    void ctrace(String mes, Object... obj) {
        if (TRACE_BGP_LIST) {
            trace(mes, obj);
        }
    }
    
    void trace(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
    }
        
    void trace(List<BasicGraphPattern> sortedList, List<List<BasicGraphPattern>> alist) {
        if (TRACE_BGP_LIST) {
            for (BasicGraphPattern bgp : sortedList) {
                trace("bgp: %s", bgp);
                trace("");
            }
            for (List<BasicGraphPattern> ll : alist) {
                trace("partition: %s",ll);
                trace("");
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
    
        // @todo:
    // when a partition is complete on triples and each part has only one uri
    // generate this partition only
    // (u1 -> t1 t2)(u2 -> t3 t4)
    // no need to generate (u2 -> t3 t4) alone because the completed first part will be the same    
    void select(List<List<BasicGraphPattern>> list) {       
        int size = uriList2bgp.getTripleList().size();
        
        for (List<BasicGraphPattern> alist : list) {
            int count = 0;
            int nbUri = 0;
            for (BasicGraphPattern bgp : alist) {
                count += bgp.size();
                List<String> uriList = bgp2uri.get(bgp);
                if (uriList.size()>nbUri) {
                    nbUri = uriList.size();
                }
                trace("%s\n%s", uriList, bgp);
            }
            if (count==size && nbUri==1) {
                trace("complete");
            }
            trace("__");
        }
    }

    public boolean isMergeEvenIfNotConnected() {
        return mergeEvenIfNotConnected;
    }

    public void setMergeEvenIfNotConnected(boolean mergeEvenIfNotConnected) {
        this.mergeEvenIfNotConnected = mergeEvenIfNotConnected;
    }
    
    
}
