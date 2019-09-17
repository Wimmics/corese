package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Binding;
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
 * Group triple patterns that share a unique service URI into one service URI
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class RewriteBGP {

    static final String FAKE_SERVER = "http://ns.inria.fr/_fake_";

    FederateVisitor visitor;
    private boolean debug = false;

    RewriteBGP(FederateVisitor vis) {
        visitor = vis;
    }

    /**
     * service -> List of connected BGP
     */
    class ServiceBGP {

        HashMap<String, List<BasicGraphPattern>> map = new HashMap<>();

        List<BasicGraphPattern> get(String label) {
            List<BasicGraphPattern> bgpList = map.get(label);
            if (bgpList == null) {
                bgpList = new ArrayList<>();
                map.put(label, bgpList);
            }
            return bgpList;
        }

        void add(String label, BasicGraphPattern bgp) {
            List<BasicGraphPattern> bgpList = map.get(label);
            if (bgpList == null) {
                bgpList = new ArrayList<>();
                map.put(label, bgpList);
            }
            bgpList.add(bgp);
        }

        HashMap<String, List<BasicGraphPattern>> getMap() {
            return map;
        }
        
        void merge() {
            for (String uri : map.keySet()) {
                List<BasicGraphPattern> list = map.get(uri);
                BasicGraphPattern bgp = list.get(0);
                for (BasicGraphPattern exp : list) {
                    if (exp != bgp) {
                        bgp.include(exp);
                    }
                }
                for (int i = 1; i<list.size(); i++) {
                    list.remove(i);
                }
            }
        }
        
        /**
         * merge two service bgp with same URI if there is a filter with variables
         * and each service bind some (but not all) of the variables
         */
        void merge(List<Expression> filterList) {
            for (String uri : map.keySet()) {
                List<BasicGraphPattern> list = map.get(uri);
                if (list.size() == 2) {
                    List<Variable> l1 = list.get(0).getSubscopeVariables();
                    List<Variable> l2 = list.get(1).getSubscopeVariables();
                    for (Expression filter : filterList) {
                        List<Variable> varList = filter.getInscopeVariables();
                        if (gentle(l1, l2, varList)) {
                            list.get(0).include(list.get(1));
                            list.remove(1);
                        }
                    }
                }
            }
        }
          
        // every var in l3 in l1 or l2
        // some  var in l3 not in l1 and in l2
        boolean gentle(List<Variable> l1, List<Variable> l2, List<Variable> l3) {
            for (Variable var : l3) {
                if (!(l1.contains(var) || l2.contains(var))) {
                    return false;
                }
            }
            for (Variable var : l3) {
                if (!l1.contains(var) || !l2.contains(var)) {
                    return true;
                }
            }
            return false;
        }

        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String uri : map.keySet()) {
                sb.append(uri).append(": ");
                for (BasicGraphPattern bgp : map.get(uri)) {
                    sb.append(bgp).append(" ");
                }
                sb.append("\n");
            }
            return sb.toString();
        }

    }

    /**
     * group triple patterns that share the same service URI into one or several 
     * connected service URI 
     * modify the body (replace triples by BGPs) 
     * copy bind (exp as var) if possible and copy filters if possible
     * filterList: list to be filled with bind & filters that are copied into service
     * TODO: bind (exp as var) could also be assignToConnectedBGP
     */
    void groupTripleInServiceWithOneURI(Atom name, Exp main, Exp body, List<Exp> filterList) {
        ServiceBGP map = new ServiceBGP();
        boolean tripleFilter = true;
        // create BGPs for triples that share same service URI
        // create a map serviceURI -> (BGP1 .. BGPn)
        List<Expression> flist = new ArrayList<>();
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isFilter() ) {
                flist.add(exp.getFilter());
            } 
            else if (exp.isValues() ||  exp.isBind()) {
                // do nothing
            }
            else if (exp.isTriple()) {
                Triple triple = exp.getTriple();
                List<Atom> list = visitor.getServiceList(triple);
                if (list.size() == 1) {
                    assignTripleToConnectedBGP(map, triple, list.get(0).getLabel());
                } else {
                    //tripleFilter = false;
                    assignTripleToConnectedBGP(map, triple, FAKE_SERVER);
                }
            } else {
                tripleFilter = false;
            }
        }
        
        if (visitor.isMerge()) {
            if (main.isBinaryExp()) {
                map.merge();
            }
            else if (! flist.isEmpty()) {
                map.merge(flist);
            }
        }
        
        HashMap<Triple, BasicGraphPattern> table = new HashMap<>();
        HashMap<BasicGraphPattern, Service> done = new HashMap<>();

        // record triples that are member of created BGPs
        for (String uri : map.getMap().keySet()) {
            if (!uri.equals(FAKE_SERVER)) {
                List<BasicGraphPattern> list = map.get(uri);
                for (BasicGraphPattern bgp : list) {
                    for (Exp exp : bgp) {
                        table.put(exp.getTriple(), bgp);
                    }
                }
            }
        }

        // for each triple member of created BGP
        // replace first triple by service s { BGP }
        // move simple filters into BGP
        for (int i = 0; i < body.size(); i++) {
            if (body.get(i).isTriple()) {
                Triple t = body.get(i).getTriple();
                if (table.containsKey(t)) {
                    BasicGraphPattern bgp = table.get(t);
                    if (done.get(bgp) == null) {
                        // service s { bgp }
                        Service serv = visitor.getRewriteTriple().rewrite(name, bgp, visitor.getServiceList(t));
                        // do it once for first triple of this BGP
                        done.put(bgp, serv);
                        // replace first triple of BGP by service BGP
                        body.set(i, serv);
                    }
                }
            }
        }

        // create local fake service for triple with several URI
        // just for testing filter below
        List<BasicGraphPattern> list = map.get(FAKE_SERVER);
        for (BasicGraphPattern bgp : list) {
            Service serv = Service.create(Constant.createResource(FAKE_SERVER), bgp);
            done.put(bgp, serv);
        }

        // remove triples member of a created service 
        for (Triple t : table.keySet()) {
            body.getBody().remove(t);
        }

        // push bind (exp as var) into appropriate service clause where exp variables are bound
        // succcess means that no bind() stay outside service clauses
        boolean success = bind(name, body, done, filterList, tripleFilter);
        // push filter into appropriate service clause where filter variables are bound
        filter(name, body, done, filterList, success && tripleFilter);
    }

    /**
     * Add triple in appropriate BGP in serv -> (BGP1 , BGPn) where triple is
     * connected to BGP merge BGPs that are connected with triple.
     */
    void assignTripleToConnectedBGP(ServiceBGP map, Triple triple, String serv) {
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
            if (!serv.getServiceName().getLabel().equals(FAKE_SERVER)) {
                List<Variable> varList = bgp.getSubscopeVariables();
                if (exp.getFilter().isBound(varList)) {
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
     * Draft for testing 
     * Copy filter exists into appropriate service URI, first phase
     *
     * body = service URI { BGP } filter exists { EXP } -> service URI { BGP }
     * filter exists { service URI { EXP } } -> service URI { BGP filter exists
     * { EXP } }
     *
     * TODO: does not work if there were existing service before rewrite because
     * they are not in bgpMap In this case, this function is not called TODO:
     * does not work if some triples are not yet in a service clause because we
     * are in the prepare phase
     */
    boolean filterExist(Atom name, Exp body, HashMap<BasicGraphPattern, Service> bgpMap, List<Exp> filterList, Exp filterExist) {
        Expression filter = filterExist.getFilter();
        visitor.rewriteFilter(name, filter);
        Term termExist = filterExist.getFilter().getTermExist();        
        Exp bgpExist = termExist.getExistBGP();

        if (bgpExist.size() == 1 && bgpExist.get(0).isService()) {
            Service servExist = bgpExist.get(0).getService();
            if (servExist.getServiceList().size() == 1) {

                List<BasicGraphPattern> bgpList = new ArrayList<>();
                //List<Variable> existVarList = bgpExist.getInscopeVariables();
                List<Variable> existVarList = bgpExist.getVariables(VariableScope.inscope().setFilter(true));
                List<Variable> previousIntersection = null;

                // select relevant BGP with same URI as exists
                // If other BGP bind some variables of exists, 
                // they must bind the same variables as the relevant one
                for (BasicGraphPattern bgp : bgpMap.keySet()) {

                    Service servBGP = bgpMap.get(bgp);
                    List<Variable> bgpVarList = bgp.getInscopeVariables();
                    List<Variable> currentIntersection = intersection(existVarList, bgpVarList);

                    if (isDebug()) {
                        System.out.println("R: bgp: " + bgpVarList  + " exist: " + existVarList);
                        System.out.println("Intersection: " + currentIntersection);
                    }

                    if (servExist.getServiceName().equals(servBGP.getServiceName())) {
                        if (equal(currentIntersection, existVarList)) {
                            bgpList.add(bgp);
                        } else {
                            if (previousIntersection == null) {
                                previousIntersection = currentIntersection;
                            }
                            if (equal(previousIntersection, currentIntersection)) {
                                bgpList.add(bgp);
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

                if (bgpList.size() == 1) {
                    if (isDebug()) System.out.println("move1: " + filterExist);
                    // remove service from exists
                    termExist.setExistBGP(servExist.getBodyExp());
                    // move exist into relevant service 
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
     * second phase: the whole content of a BGP have been rewritten
     * Move bind/filter when there is *only one* service which binds it with *inscope* variables
     * We are not sure that it succeeds but there is one service that can do it
     */
    boolean move(Exp current, Exp body) {
        boolean move = false;
        boolean fake = false;
        List<Exp> list = new ArrayList<>();
        for (Exp exp : body) {
            if (exp.isService()) {
                Service serv = exp.getService();
                Exp bgp = serv.getBodyExp();
                if (! bgp.getBody().contains(current)) {
                    List<Variable> varList = bgp.getInscopeVariables();
                    if (current.getFilter().isBound(varList)) {
                        fake = serv.getServiceName().getLabel().equals(FAKE_SERVER);
                        list.add(bgp);
                    }
                }
            }
        }
        if (list.size() == 1 && !fake) {
            list.get(0).add(current);
            move = true;
        }
        return move;
    }
    

    /**
     * 
     * Copy filter exists { service uri { exp }} into appropriate service uri { exp }
     * second phase: the whole content of a BGP have been rewritten, some filter exists remain : try again
     * The difference with first phase is that here the service where to copy a filter exists may contain optional, union, etc.
     */
    boolean filterExist(Exp filterExist, Exp body) {
        if (!(visitor.isExist() && accept(filterExist))) {
            return false;
        }
        Term termExist = filterExist.getFilter().getTermExist();        
        Exp bgpExist = termExist.getExistBGP();
        if (bgpExist.size() == 1 && bgpExist.get(0).isService()) {
            Service servExist = bgpExist.get(0).getService();
            if (servExist.getServiceList().size() == 1) {

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
                        Service servBGP = exp.getService();
                        List<Variable> bgpVarList = servBGP.getInscopeVariables();
                        List<Variable> currentIntersection = intersection(existVarList, bgpVarList);

                        if (isDebug()) {
                            System.out.println("R: " + existVarList + " " + bgpVarList);
                            System.out.println("Intersection: " + currentIntersection);
                        }

                        if (servExist.getServiceName().equals(servBGP.getServiceName())) {
                            if (equal(currentIntersection, existVarList)) {
                                serviceList.add(servBGP);
                            } else {
                                if (previousIntersection == null) {
                                    previousIntersection = currentIntersection;
                                }
                                if (equal(previousIntersection, currentIntersection)) {
                                    serviceList.add(servBGP);
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

                if (serviceList.size() == 1) {
                    if (isDebug()) System.out.println("move2: " + filterExist);
                    // remove service from exists
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
