package fr.inria.corese.compiler.federate;

import static fr.inria.corese.compiler.federate.util.RewriteErrorMessage.FILTER_EXISTS;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.parser.VariableScope;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Group connected triple in bgp wrt service url
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class PrepareBGP extends Util {

    static final String FAKE_URI = "http://ns.inria.fr/_fake_";

    private FederateVisitor visitor;
    private boolean debug = false;
    private boolean skipSameAs = true;

    PrepareBGP(FederateVisitor vis) {
        visitor = vis;
    }

   
    // create map: URI -> (BGP1 .. BGPn)        
    // create BGP for triples that share same service URI
    // group connected triples with one URI in uri2bgpList 
    // (triple with several URI in FAKE_URI URI here)
    // group connected triples with several URI in uriList2bgpList 
    /**
     * modify the body (replace triples by BGPs) 
     * copy bind (exp as var) if possible and copy filters if possible
     * filterList: list to be filled with bind & filters that are copied into service
     * TODO: bind (exp as var) could also be assignToConnectedBGP
     * @return BGP list of connected triples with several service URI
     */
    URI2BGPList process
        (Atom name, Exp main, Exp body, List<Exp> filterList) {
            
        URI2BGPList uri2bgpList     = new URI2BGPList(getVisitor())
                .setJoin(join(main, body));
        
        URI2BGPList uriList2bgpList = new URI2BGPList(getVisitor())
                .setJoin(join(main, body));
        
        uri2bgpList.setUriList2bgp(uriList2bgpList);
        List<Expression> localFilterList = new ArrayList<>();
        
        // create map: URI -> (BGP1 .. BGPn)        
        // create BGP for triples that share same service URI
        // new version:
        // group connected triples with several URI in uriList2bgpList
        // former version:
        // group connected triples with one URI in uri2bgpList 
        // (triple with several URI in FAKE_URI URI here)
        assign(body, uri2bgpList, uriList2bgpList, localFilterList);
        
        // PRAGMA: the rest of the function process former case
        // where we distinguished one/several uri
        // not used for new case with bgp and several uri
        
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
                triple2ServiceWithOneURI(name, body, triple2bgp, uri2bgpList);
                      
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
        
        uri2bgpList.complete();
        return uri2bgpList;
    }
        
    // right exp of optional/minus do not use select join to create bgp list
    // it means that connected triples remain in same bgp even if they do not join
    // this is a heuristics in favor of simplification of A optional/minus B
    boolean join(Exp main, Exp body) {
        if ((main.isOptional()|| main.isMinus()) && body == main.get(1)) {
            return false;
        }
        return getVisitor().USE_JOIN;
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
                List<Atom> list = getVisitor().getServiceList(triple);
                if (list.isEmpty()) {
                    //getVisitor().error(triple, "triple 2 bgp");
                }
                if (getVisitor().isFederateBGP()) {
                    // do not distinguish one vs several URI
                    // list of connected BGP of triple with any nb URI
                    assignTripleToConnectedBGP(uriList2bgpList, triple, list);
                    uriList2bgpList.add(triple);
                } else {
                    // distinguish one vs several URI
                    if (list.size() == 1) {
                        // list of connected BGP of triple with one URI
                        assignTripleToConnectedBGP(uri2bgpList, triple, list);
                        uri2bgpList.add(triple);
                    } else {
                        // list of connected BGP of triple with several URI
                        uri2bgpList.assignTripleToConnectedBGP(triple, FAKE_URI);
                        assignTripleToConnectedBGP(uriList2bgpList, triple, list);
                        uriList2bgpList.add(triple);
                    }
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
        (Atom name, Exp body, 
                HashMap<Triple, BasicGraphPattern> triple2bgp,
                URI2BGPList uri2bgpList) {
            
        HashMap<BasicGraphPattern, Service> bgp2Service = new HashMap<>();

        for (int i = 0; i < body.size(); i++) {
            if (body.get(i).isTriple()) {
                Triple t = body.get(i).getTriple();
                if (triple2bgp.containsKey(t)) {
                    BasicGraphPattern bgp = triple2bgp.get(t);
                    if (bgp2Service.get(bgp) == null) {
                        // service s { bgp }
                        List<Atom> uriList = getVisitor().getServiceList(t);
                        if (uriList.isEmpty()) {
                            getVisitor().error(t, "triple 2 service");
                        }
                        Service serv = getVisitor().getRewriteTriple().rewrite(name, bgp, uriList);
                        // do it once for first triple of this BGP
                        bgp2Service.put(bgp, serv);
                        uri2bgpList.addServiceWithOneURI(serv);
                        
                        if (getVisitor().isFederateBGP()) {
                            // do nothing yet, 
                            // RewriteBGPList will complete with serv
                        }
                        else {
                            // replace first triple of BGP by service BGP
                            // other such triple are removed below as they are in BGP
                            body.set(i, serv);
                        }
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
        if (getVisitor().isMerge()) {
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
    void assignTripleToConnectedBGP(URI2BGPList map, Triple triple, List<Atom> uriList) {
        if (getVisitor().isSplit(triple)) {
            // owl:sameAs will be processed later as a lonely missing triple
            // in order not to screw up bgp connectivity building
        }
        else {
            basicAssignTripleToConnectedBGP(map, triple, uriList);
        }
    }
    
    void basicAssignTripleToConnectedBGP(URI2BGPList map, Triple triple, List<Atom> uriList) {
        for (Atom uri : uriList) {
            map.assignTripleToConnectedBGP(triple, uri.getLabel());
        }
    }
        

    void filter(Atom name, Exp body, HashMap<BasicGraphPattern, Service> bgpList, List<Exp> filterList, boolean tripleFilter) {
        // copy filters from body into BGP who bind all filter variables
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (getVisitor().isRecExist(exp)) {
                    if (getVisitor().isExist() && tripleFilter && accept(exp)) {
                        filterExist(name, body, bgpList, filterList, exp);
                    }
                } else {
                    move(exp, bgpList, filterList);
                }
            }
        }
    }

    boolean accept(Exp exp) {
        return getVisitor().isExist(exp) || getVisitor().isNotExist(exp);
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
                if (getVisitor().isRecExist(exp)) {
                    if (getVisitor().isExist() && tripleFilter && accept(exp)) {
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
     * exp is filter or bind (exp as var) 
     * try to move it in service clause where
     * its variables are bound
     */
    boolean move(Exp filterExp, HashMap<BasicGraphPattern, Service> bgpList, List<Exp> filterList) {
        boolean move = false;
        for (BasicGraphPattern bgp : bgpList.keySet()) {
            Service serv = bgpList.get(bgp);
            List<Variable> varList = bgp.getSubscopeVariables();
            if (isBound(filterExp, varList)) {
                if (serv.getServiceName().getLabel().equals(FAKE_URI)) {
                    // service with several URI
                }
                else {
                    // service with one URI
                    bgp.add(filterExp);
                    move = true;
                    if (!filterList.contains(filterExp)) {
                        filterList.add(filterExp);
                    }
                }
            }
        }
        return move;
    }
    
    boolean isBound(Exp filterExp, List<Variable> varList) {
        if (filterExp.getFilter().isBound(varList)) {
            if (filterExp.isBind()) {
                return varList.contains(filterExp.getBind().getVariable());
            }
            else {
                return true;
            }
        }
        return false;
    }
    
  void trace(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
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
        getVisitor().rewriteFilter(name, filter);
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
                    List<Variable> currentIntersection = intersectionVariable(existVarList, bgpVarList);

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
     * Move filter into appropriate service 
     */
    boolean moveFilter(Exp body) {
        boolean suc = true;
        ArrayList<Exp> list = new ArrayList<>();
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (exp.getFilter().isTermExistRec()) {
                    boolean b = filterExist(exp, body);
                    // move filter exists { service uri {}} into 
                    // service uri {} where filter variables are bound appropriately
                    if (b) {
                        list.add(exp);
                    }
                    else {
                        // filter exists with service clause remain
                        // on its own
                        suc = false;
                        getVisitor().getErrorManager()
                                .add(FILTER_EXISTS, exp);
                    }
                }
                else {
                   boolean b = move(exp, body);
                    if (b) {
                        list.add(exp);
                    } 
                }
            }
        }
        for (Exp exp : list) {
            body.getBody().remove(exp);
        }
        return suc;
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
                        List<Variable> currentIntersection = intersectionVariable(existVarList, bgpVarList);

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

    public FederateVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(FederateVisitor visitor) {
        this.visitor = visitor;
    }

    public boolean isSkipSameAs() {
        return skipSameAs;
    }

    public void setSkipSameAs(boolean skipSameAs) {
        this.skipSameAs = skipSameAs;
    }

}
