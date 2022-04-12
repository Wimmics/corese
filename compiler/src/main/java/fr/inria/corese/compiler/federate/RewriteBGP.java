package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.parser.VariableScope;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Group triple patterns that share a unique service URI into one service URI
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class RewriteBGP {

    static final String FAKE_URI = "http://ns.inria.fr/_fake_";


    FederateVisitor visitor;
    private boolean debug = false;
    //RewriteBGPSeveralURI rewrite;

    RewriteBGP(FederateVisitor vis) {
        visitor = vis;
        //rewrite = new RewriteBGPSeveralURI(vis);
    }

   

    /**
     * group triple patterns that share the same service URI into one or several 
     * connected service URI 
     * modify the body (replace triples by BGPs) 
     * copy bind (exp as var) if possible and copy filters if possible
     * filterList: list to be filled with bind & filters that are copied into service
     * TODO: bind (exp as var) could also be assignToConnectedBGP
     * @return BGP list of connected triples with several service URI
     */
    URI2BGPList rewriteTripleWithOneURI
        (Atom name, Exp main, Exp body, List<Exp> filterList) {
            
        URI2BGPList uri2bgpList     = new URI2BGPList();
        URI2BGPList uriList2bgpList = new URI2BGPList();
        List<Expression> localFilterList = new ArrayList<>();
        
        // create map: URI -> (BGP1 .. BGPn)        
        // create BGP for triples that share same service URI
        // group connected triples with one URI in uri2bgpList 
        // (triple with several URI in FAKE_URI URI here)
        // group connected triples with several URI in uriList2bgpList 
        assign(body, uri2bgpList, uriList2bgpList, localFilterList);
        
        // merge BGP of arguments of optional/minus/union
        // merge BGP when filter need it
        merge(main, uri2bgpList, localFilterList);

        // create map: triple -> BGP  with one URI
        HashMap<Triple, BasicGraphPattern> triple2bgp = record(uri2bgpList);
        
        // create map: BGP -> Service with one URI
        // for each triple member of BGP with one URI
        // replace triple in body by service URI { BGP }
        // remove all such triples
        HashMap<BasicGraphPattern, Service> bgp2Service = 
                triple2ServiceWithOneURI(name, body, triple2bgp);
                      
        // create local fake service for triple with several URI
        // just for processing filter below
        List<BasicGraphPattern> bgpListWithSeveralURI = 
                uri2bgpList.get(FAKE_URI);
        
        for (BasicGraphPattern bgp : bgpListWithSeveralURI) {
            Service serv = Service.create(Constant.createResource(FAKE_URI), bgp);
            bgp2Service.put(bgp, serv);
        }
        
        boolean tripleFilter = ! uri2bgpList.getMap().isEmpty();
        // push bind (exp as var) into appropriate service clause where exp variables are bound
        // succcess means that no bind() stay outside service clauses
        boolean success = bind(name, body, bgp2Service, filterList, tripleFilter);
        // push filter into appropriate service clause where filter variables are bound
        filter(name, body, bgp2Service, filterList, success && tripleFilter);
        
        //rewrite.tripleWithSeveralURI(body, uriList2bgpList);
        return uriList2bgpList;
    }
        

        
    // create map URI -> (BGP1 .. BGPn)        
    // group connected triples with one URI in uri2bgpList 
    // (triple with several URI in FAKE_URI URI here)
    // group connected triples with several URI in uriList2bgpList         
    void assign(Exp body, URI2BGPList uri2bgpList, URI2BGPList uriList2bgpList, List<Expression> localFilterList) {
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isFilter()) {
                localFilterList.add(exp.getFilter());
            } else if (exp.isValues() || exp.isBind()) {
                // do nothing
            } else if (exp.isTriple()) {
                Triple triple = exp.getTriple();
                List<Atom> list = visitor.getServiceList(triple);
                
                if (list.size() == 1) {
                    // list of connected BGP of triple with one URI
                    assignTripleToConnectedBGP(uri2bgpList, triple, list);
                    uri2bgpList.add(triple);
                } else {
                    // list of connected BGP of triple with several URI
                    assignTripleToConnectedBGP(uri2bgpList, triple, FAKE_URI);
                    assignTripleToConnectedBGP(uriList2bgpList, triple, list);
                    uriList2bgpList.add(triple);
                }
            }
        }
    }
    
    
    // create map triple -> BGP 
    HashMap<Triple, BasicGraphPattern> record(URI2BGPList uri2bgpList) {
        HashMap<Triple, BasicGraphPattern> triple2bgp   = new HashMap<>();

        for (String uri : uri2bgpList.getMap().keySet()) {
            if (uri.equals(FAKE_URI)) {
                // several URI
            }
            else {
                // one URI
                List<BasicGraphPattern> list = uri2bgpList.get(uri);
                for (BasicGraphPattern bgp : list) {
                    for (Exp exp : bgp) {
                        triple2bgp.put(exp.getTriple(), bgp);
                    }
                }
            }
        }
        
        return triple2bgp;
    }
    
    // for each triple member of BGP with one URI
    // replace first triple by service URI { BGP }
    HashMap<BasicGraphPattern, Service> triple2ServiceWithOneURI
        (Atom name, Exp body, HashMap<Triple, BasicGraphPattern> triple2bgp) {
        HashMap<BasicGraphPattern, Service> bgp2Service = new HashMap<>();

        for (int i = 0; i < body.size(); i++) {
            if (body.get(i).isTriple()) {
                Triple t = body.get(i).getTriple();
                if (triple2bgp.containsKey(t)) {
                    BasicGraphPattern bgp = triple2bgp.get(t);
                    if (bgp2Service.get(bgp) == null) {
                        // service s { bgp }
                        Service serv = visitor.getRewriteTriple().rewrite(name, bgp, visitor.getServiceList(t));
                        // do it once for first triple of this BGP
                        bgp2Service.put(bgp, serv);
                        // replace first triple of BGP by service BGP
                        // other such triple will be removed as they are in BGP
                        body.set(i, serv);
                    }
                }
            }
        }
        
        // remove triples member of a created service with one URI
        for (Triple t : triple2bgp.keySet()) {
            body.getBody().remove(t);
        }
        
        return bgp2Service;
    }
    
    void merge(Exp main, URI2BGPList uri2bgpList, List<Expression> localFilterList) {
        if (visitor.isMerge()) {
            if (main.isBinaryExp()) {
                // optional|minus|union
                // in each branch: merge BGP with same URI into one BGP
                // in order to prepare simplify
                uri2bgpList.merge();
            } else if (!localFilterList.isEmpty()) {
                // merge BGP with same URI when they share filter
                uri2bgpList.merge(localFilterList);
            }
        }
    }

    /**
     * Add triple in appropriate BGP in serv -> (BGP1 , BGPn) where triple is
     * connected to BGP 
     * merge BGPs that are connected with triple.
     */
    void assignTripleToConnectedBGP(URI2BGPList map, Triple triple, List<Atom> list) {
        for (Atom at : list) {
            assignTripleToConnectedBGP(map, triple, at.getLabel());
        }
    }
        
    void assignTripleToConnectedBGP(URI2BGPList map, Triple triple, String serv) {
        List<BasicGraphPattern> bgpList = map.get(serv);
        boolean isConnected = false;
        int i = 0;

        // find a connected BGP
        for (BasicGraphPattern bgp : bgpList) {
            if (bgp.isConnected(triple)) {
                bgp.add(triple);
                isConnected = true;
                break;
            }
            i++;
        }

        if (isConnected) {
            ArrayList<BasicGraphPattern> toRemove = new ArrayList<>();

            // if  another BGP is connected to triple
            // include it into the first BGP
            for (int j = i + 1; j < bgpList.size(); j++) {
                if (bgpList.get(j).isConnected(triple)) {
                    bgpList.get(i).include(bgpList.get(j));
                    toRemove.add(bgpList.get(j));
                }
            }

            // remove BGPs that have been included
            for (BasicGraphPattern bgp : toRemove) {
                bgpList.remove(bgp);
            }
        } else {
            // new BGP because triple is connected to nobody
            bgpList.add(BasicGraphPattern.create(triple));
        }
    }

    void filter(Atom name, Exp body, HashMap<BasicGraphPattern, Service> bgpList, List<Exp> filterList, boolean tripleFilter) {
        // copy filters from body into BGP who bind all filter variables
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (visitor.isRecExist(exp)) {
                    if (visitor.isExist() && tripleFilter && accept(exp)) {
                        filterExist(name, body, bgpList, filterList, exp);
                    }
                } else {
                    move(exp, bgpList, filterList);
                }
            }
        }
    }

    boolean accept(Exp exp) {
        return visitor.isExist(exp) || visitor.isNotExist(exp);
    }

    /**
     * return true if there is no bind() or if every bind has been inserted into
     * a service clause in such a way that no bind() stay outside a service
     */
    boolean bind(Atom name, Exp body, HashMap<BasicGraphPattern, Service> bgpList, List<Exp> filterList, boolean tripleFilter) {
        boolean success = true;
        // copy bind(exp as var) from body into BGP who bind all exp variables
        for (Exp exp : body) {
            if (exp.isBind()) {
//                if (visitor.isRecExist(exp)) {
//                    success = false;
//                } 
                if (visitor.isRecExist(exp)) {
                    if (visitor.isExist() && tripleFilter && accept(exp)) {
                        boolean move = filterExist(name, body, bgpList, filterList, exp);
                        success &= move;
                    }
                    else {
                        success = false;
                    }
                } else {
                    boolean move = move(exp, bgpList, filterList);
                    success &= move;
                }
            }
        }
        return success;
    }

    /**
     * exp is filter or bind(exp as var) try to move it in service clause where
     * its variables are bound
     */
    boolean move(Exp exp, HashMap<BasicGraphPattern, Service> bgpList, List<Exp> filterList) {
        boolean move = false;
        for (BasicGraphPattern bgp : bgpList.keySet()) {
            Service serv = bgpList.get(bgp);
            List<Variable> varList = bgp.getSubscopeVariables();
            if (exp.getFilter().isBound(varList)) {
                if (serv.getServiceName().getLabel().equals(FAKE_URI)) {
                    // service with several URI
                }
                else {
                    // service with one URI
                    bgp.add(exp);
                    move = true;
                    if (!filterList.contains(exp)) {
                        filterList.add(exp);
                    }
                }
            }
        }
        return move;
    }
    
 

    /**
     * First phase: service with one URI
     * Copy filter exists into appropriate service URI, first phase (service with one URI)
     * filter exists service with one URI can be merged into bgp service with same URI
     *
     * body = service URI { BGP } filter exists { EXP } -> 
     * service URI { BGP } filter exists { service URI { EXP } } -> 
     * service URI { BGP filter exists { EXP } }
     *
     * TODO: does not work if there were existing service before rewrite because
     * they are not in bgpMap In this case, this function is not called TODO:
     * does not work if some triples are not yet in a service clause because we
     * are in the prepare phase
     */
    boolean filterExist(Atom name, Exp body, HashMap<BasicGraphPattern, Service> bgpMap, List<Exp> filterList, Exp filterExist) {
        Expression filter = filterExist.getFilter();
        // rewrite exists {} with service clause inside
        visitor.rewriteFilter(name, filter);
        Term termExist = filterExist.getFilter().getTermExist();        
        Exp bgpExist = termExist.getExistBGP();

        if (bgpExist.size() == 1 && bgpExist.get(0).isService()) {
            // exists { service S {} }
            // filter exists body is one single service
            Service servExist = bgpExist.get(0).getService();
            if (servExist.getServiceList().size() == 1) {
                // filter exists service with one URI
                List<BasicGraphPattern> bgpList = new ArrayList<>();
                List<Variable> existVarList = bgpExist.getVariables(VariableScope.inscope().setFilter(true));
                List<Variable> previousIntersection = null;

                // select relevant BGP/Service with same URI as exists
                // If other BGP bind some variables of exists, 
                // they must bind the same variables as the relevant one
                for (BasicGraphPattern bgp : bgpMap.keySet()) {

                    Service servBGP = bgpMap.get(bgp);
                    List<Variable> bgpVarList = bgp.getInscopeVariables();
                    // intersection of BGP and exists variables
                    List<Variable> currentIntersection = intersection(existVarList, bgpVarList);

                    if (isDebug()) {
                        System.out.println("R: bgp: " + bgpVarList  + " exist: " + existVarList);
                        System.out.println("Intersection: " + currentIntersection);
                    }

                    if (servExist.getServiceName().equals(servBGP.getServiceName())) {
                        // bgp service and exists service with same URI
                        if (equal(currentIntersection, existVarList)) {
                            // bgp bind exactly exists variables
                            bgpList.add(bgp);
                        } else {
                            if (previousIntersection == null) {
                                previousIntersection = currentIntersection;
                            }
                            if (equal(previousIntersection, currentIntersection)) {
                                // same intersection of new bgp variables with exists variables
                                bgpList.add(bgp);
                            } else if (currentIntersection.isEmpty()) {
                                // no intersection, do nothing its ok
                            } else {
                                // two BGP intersect differently the exists clause
                                // filter exists { service {} } cannot be merged to existing bgp
                                // filter stands on its own
                                return false;
                            }
                        }
                    } // service with another URI
                    else if (!currentIntersection.isEmpty()) {
                        if (previousIntersection == null) {
                            previousIntersection = currentIntersection;
                        } else if (!equal(previousIntersection, currentIntersection)) {
                            // two BGP intersect differently the exists clause
                            return false;
                        }
                    }
                }

                if (bgpList.size() == 1) {
                    // there is one bgp service with same URI as filter exists service
                    // merge filter exists into bgp with same service URI
                    if (isDebug()) System.out.println("move1: " + filterExist);
                    // remove service from filter exists
                    termExist.setExistBGP(servExist.getBodyExp());
                    // move filter exist into relevant bgp service 
                    bgpList.get(0).add(filterExist);
                    if (!filterList.contains(filterExist)) {
                        filterList.add(filterExist);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    
    /**
     * Second phase: 
     * the whole content of a BGP have been rewritten
     * Move bind/filter when there is *only one* service which binds it with *inscope* variables
     * We are not sure that it succeeds but there is one service that can do it
     */
    boolean move(Exp filter, Exp body) {
        boolean move = false;
        boolean severalURI = false;
        List<Exp> list = new ArrayList<>();
        
        for (Exp exp : body) {
            if (exp.isService()) {
                Service serv = exp.getService();
                Exp bgp = serv.getBodyExp();
                if (! bgp.getBody().contains(filter)) {
                    List<Variable> varList = bgp.getInscopeVariables();
                    if (filter.getFilter().isBound(varList)) {
                        // severalURI = true for service with several URI
                        severalURI = serv.getServiceName().getLabel().equals(FAKE_URI);
                        list.add(bgp);
                    }
                }
            }
        }
        if (list.size() == 1 && !severalURI) {
            list.get(0).add(filter);
            move = true;
        }
        return move;
    }
    

    /**
     * Second phase
     * Copy filter exists { service (URI) { exp }} into appropriate bgp service (URI) { exp }
     * when both have same URI list and filter exists variable are bound properly by service 
     * the whole content of a BGP have been rewritten, some filter exists remain : try again
     * The difference with first phase is that here the service where to copy a filter exists 
     * may contain optional, union, etc which may have been simplified and hence merge may be possible now
     * Heuristics: 
     * when filter exists has several URI, test it on the same endpoint as its outer BGP
     */
    boolean filterExist(Exp filterExist, Exp body) {
        if (!(visitor.isExist() && accept(filterExist))) {
            return false;
        }
        Term termExist = filterExist.getFilter().getTermExist();        
        Exp bgpExist = termExist.getExistBGP();
        
        if (bgpExist.size() == 1 && bgpExist.get(0).isService()) {
            Service servExist = bgpExist.get(0).getService();

            if (servExist.getServiceList().size() > 0) { //== 1) {
                // filter exists {service uri {}}
                List<Service> serviceList = new ArrayList<>();
                List<Variable> existVarList = bgpExist.getVariables(VariableScope.inscope().setFilter(true));
                List<Variable> previousIntersection = null;

                // select relevant BGP with same URI as exists
                // If other BGP bind some variables of exists, 
                // they must bind the same variables as the relevant one
                for (Exp exp : body) {
                    if (exp.isFilter()) {
                        // skip
                    } 
                    else if (exp.isService()) {
                        Service service = exp.getService();
                        List<Variable> bgpVarList = service.getInscopeVariables();
                        List<Variable> currentIntersection = intersection(existVarList, bgpVarList);

                        if (isDebug()) {
                            System.out.println("R: " + existVarList + " " + bgpVarList);
                            System.out.println("Intersection: " + currentIntersection);
                        }
                        
                        //if (servExist.getServiceName().equals(servBGP.getServiceName())) {
                        if (equalURI(servExist.getServiceList(), service.getServiceList())) {
                            if (equal(currentIntersection, existVarList)) {
                                serviceList.add(service);
                            } else {
                                if (previousIntersection == null) {
                                    previousIntersection = currentIntersection;
                                }
                                if (equal(previousIntersection, currentIntersection)) {
                                    serviceList.add(service);
                                } else if (currentIntersection.isEmpty()) {
                                    // do nothing, its ok
                                } else {
                                    // two BGP intersect differently the exists clause
                                    return false;
                                }
                            }
                        } // service with another URI
                        else if (!currentIntersection.isEmpty()) {
                            if (previousIntersection == null) {
                                previousIntersection = currentIntersection;
                            } else if (!equal(previousIntersection, currentIntersection)) {
                                // two BGP intersect differently the exists clause
                                return false;
                            }
                        }
                    }
                    else {
                        return false;
                    }
                }
                
                if (serviceList.size() >0) { //== 1) {
                    if (isDebug()) System.out.println("move2: " + filterExist);
                    // remove service clause from exists (keep service bgp)
                    termExist.setExistBGP(servExist.getBodyExp());
                    // move exist into relevant service 
                    Service serv = serviceList.get(0);
                    serv.insert(filterExist);
                    return true;
                }
            }
        }
        return false;
    }
    
    boolean equalURI(List<Atom> list1, List<Atom> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (Atom var : list1) {
            if (!list2.contains(var)) {
                return false;
            }
        }
        return true;
    }


    boolean equal(List<Variable> list1, List<Variable> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (Variable var : list1) {
            if (!list2.contains(var)) {
                return false;
            }
        }
        return true;
    }

    boolean hasIntersection(List<Variable> list1, List<Variable> list2) {
        return !intersection(list1, list2).isEmpty();
    }

    List<Variable> intersection(List<Variable> list1, List<Variable> list2) {
        ArrayList<Variable> list = new ArrayList<>();
        for (Variable var : list1) {
            if (list2.contains(var) && !list.contains(var)) {
                list.add(var);
            }
        }
        return list;
    }

    boolean includedIn(List<Variable> list1, List<Variable> list2) {
        for (Variable var : list1) {
            if (!list2.contains(var)) {
                return false;
            }
        }
        return true;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
