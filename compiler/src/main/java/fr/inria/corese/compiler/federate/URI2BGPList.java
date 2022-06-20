package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTSelector;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class has two instances 
 * the main instance contains the second one
 * a) main instance: uri -> (connected bgp) triple with one URI (deprecated)
 * b) uriList2bgp:   uri -> (connected bgp) triple with several URI
 */
public class URI2BGPList {
    public static boolean TRACE_SKIP = false;
    // uri -> bgp list
    HashMap<String, List<BasicGraphPattern>> uri2bgp;
    // bgp -> uri list
    private BGP2URI bgp2uri;
    // list of all triples in both instances
    private ArrayList<Triple> tripleList;
    // service with one URI
    private ArrayList<Service> serviceList;
    // uriList2bgp instance for triple with several URI 
    private URI2BGPList uriList2bgp;
    private FederateVisitor visitor;
    
    // optional does not
    private boolean join = true;
    
    
    URI2BGPList(FederateVisitor vis) {
        uri2bgp = new HashMap<>();
        bgp2uri = new BGP2URI();
        tripleList = new ArrayList<>();
        serviceList = new ArrayList<>();
        visitor = vis;
    }
    
    void complete() {
        bgp2uri();
        if (getUriList2bgp() !=null) {
            getUriList2bgp().bgp2uri();
        }
    }
    
    /**
     * Create bgp2uri map: bgp -> list uri
     * When several physical occurrences of same (equals) bgp, 
     * pick one key bgp that represents all of them
     */
    void bgp2uri() {        
        for (String uri : uri2bgp.keySet()) {
            for (BasicGraphPattern bgp : uri2bgp.get(uri)) {
                // retrieve key bgp for this bgp
                // use case: several similar bgp objects represented by 
                // one bgp object
                BasicGraphPattern key = getBgp2uri().getKey(bgp);
                List<String> uriList  = getBgp2uri().get(key);
                if (uriList == null) {
                    uriList = new ArrayList<>();
                    getBgp2uri().put(key, uriList);
                }
                uriList.add(uri);
            }
        }
    }

    List<BasicGraphPattern> get(String label) {
        List<BasicGraphPattern> bgpList = uri2bgp.get(label);
        if (bgpList == null) {
            bgpList = new ArrayList<>();
            uri2bgp.put(label, bgpList);
        }
        return bgpList;
    }

    void add(String label, BasicGraphPattern bgp) {
        List<BasicGraphPattern> bgpList = uri2bgp.get(label);
        if (bgpList == null) {
            bgpList = new ArrayList<>();
            uri2bgp.put(label, bgpList);
        }
        bgpList.add(bgp);
    }

    HashMap<String, List<BasicGraphPattern>> getMap() {
        return uri2bgp;
    }
    
    ASTSelector getSelector() {
        return getVisitor().getAST().getAstSelector();
    }
    
    // does triple t join with each connected triple in bgp
    boolean join(BasicGraphPattern bgp, Triple t, String uri) {
        if (getVisitor().acceptWithoutJoinTest(t)) {
            return true;
        }
        return getSelector().join(bgp, t, uri);        
    }
    
    // do every pair of connected triple in bgp1 and bgp2 join ?
    boolean join(BasicGraphPattern bgp1, BasicGraphPattern bgp2, String uri) {
        for (Exp e : bgp2) {
            if (e.isTriple()) {
                if (getVisitor().acceptWithoutJoinTest(e.getTriple())) {
                    return true;
                }
                if (!join(bgp1, e.getTriple(), uri)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // assign triple to one bgp of uri, connected with triple 
    // and merge all connected bgp
    // otherwise create new bgp
    void assignTripleToConnectedBGP(Triple triple, String uri) {
        List<BasicGraphPattern> bgpList = get(uri);
        int i = insert(bgpList, triple, uri);

        if (i>=0) {
            // triple inserted in ith bgp
            // merge other connected bgp if any
            merge(bgpList, triple, uri, i);
        } 
        else {
            // create new bgp because triple is connected to no existing bgp
            // or it does not join
            bgpList.add(BasicGraphPattern.create(triple));
        }
    }
    
    // find connected bgp where triple join: insert triple in bgp
    int insert(List<BasicGraphPattern> bgpList, Triple triple, String uri) {
        int i = 0;
        for (BasicGraphPattern bgp : bgpList) {
            if (bgp.isConnected(triple)) {
                if (isJoin()) {
                    if (join(bgp, triple, uri)) {
                        // source selection confirm that triple join with bgp
                        bgp.add(triple);
                        return i;
                    } else {
                        // source selection confirm that triple does not join with bgp
                        if (TRACE_SKIP) {
                            trace("bgp join skip connection: %s %s", triple, uri);
                            trace("%s", bgp);
                        }
                    }
                } else {
                    bgp.add(triple);
                    return i;
                }
            }
            i++;
        }
        // no connected bgp with join
        return -1;
    }
    
    // if bgp2 is connected to triple, include it into former ith bgp1    
    void merge(List<BasicGraphPattern> bgpList, Triple triple, String uri, int i) {
        BasicGraphPattern bgp1 = bgpList.get(i);
        ArrayList<BasicGraphPattern> toRemove = new ArrayList<>();
 
        for (int j = i + 1; j < bgpList.size(); j++) {
            BasicGraphPattern bgp2 = bgpList.get(j);
            if (bgp2.isConnected(triple)) {
                if (isJoin()) {
                    if (join(bgp1, bgp2, uri)) {
                        // merge only if source selection confirm join
                        // in connected bgp
                        bgp1.include(bgp2);
                        toRemove.add(bgp2);
                    }
                } else {
                    bgp1.include(bgp2);
                    toRemove.add(bgp2);
                }
            }
        }

        // remove bgp that have been merged
        for (BasicGraphPattern bgp : toRemove) {
            bgpList.remove(bgp);
        }
    }
    

    // for every URI: merge list of BGP into one BGP
    // use case: bgp with one URI
    void merge() {
        for (String uri : uri2bgp.keySet()) {
            List<BasicGraphPattern> list = uri2bgp.get(uri);
            BasicGraphPattern bgp = list.get(0);
            for (BasicGraphPattern exp : list) {
                if (exp != bgp) {
                    bgp.include(exp);
                }
            }
            for (int i = 1; i < list.size(); i++) {
                list.remove(i);
            }
        }
    }

    /**
     * merge two service bgp with same URI if there is a filter with variables
     * and each service bind some (but not all) of the variables
     */
    void merge(List<Expression> filterList) {
        for (String uri : uri2bgp.keySet()) {
            List<BasicGraphPattern> list = uri2bgp.get(uri);
            merge(list, filterList);
        }
    }

    void merge(List<BasicGraphPattern> list, List<Expression> filterList) {
        if (list.size() == 2) {
            List<Variable> l1 = list.get(0).getSubscopeVariables();
            List<Variable> l2 = list.get(1).getSubscopeVariables();
            for (Expression filter : filterList) {
                List<Variable> varList = filter.getInscopeVariables();
                if (gentle(l1, l2, varList)) {
                    list.get(0).include(list.get(1));
                    list.remove(1);
                    return;
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
        sb.append("triple: ").append(getTripleList()).append("\n");
        for (String uri : uri2bgp.keySet()) {
            sb.append(uri).append(": ");
            for (BasicGraphPattern bgp : uri2bgp.get(uri)) {
                sb.append(bgp).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    void trace() {
        // uri -> bgp list, triple with one uri
        trace("uri\n%s", this);
        // uri -> bgp list, triple with several uri
        trace("uri list\n%s", getUriList2bgp());
        trace("bgp2uri:\n");
        
        for (BasicGraphPattern bgp
                : getUriList2bgp().getBgp2uri().keySet()) {
            trace("%s: %s", bgp,
                    getUriList2bgp().getBgp2uri().get(bgp));
        }
    }
    
    void trace(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
    }
    
    void add(Triple t) {
        getTripleList().add(t);
    }

    public ArrayList<Triple> getTripleList() {
        return tripleList;
    }

    public void setTripleList(ArrayList<Triple> tripleList) {
        this.tripleList = tripleList;
    }

    public URI2BGPList getUriList2bgp() {
        return uriList2bgp;
    }

    public void setUriList2bgp(URI2BGPList uriList2bgp) {
        this.uriList2bgp = uriList2bgp;
    }

    public BGP2URI getBgp2uri() {
        return bgp2uri;
    }

    public void setBgp2uri(BGP2URI bgp2uri) {
        this.bgp2uri = bgp2uri;
    }
    
    void addServiceWithOneURI(Service s) {
        getServiceList().add(s);
    }

    public ArrayList<Service> getServiceList() {
        return serviceList;
    }

    public void setServiceList(ArrayList<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public FederateVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(FederateVisitor visitor) {
        this.visitor = visitor;
    }

    public boolean isJoin() {
        return join;
    }

    public URI2BGPList setJoin(boolean join) {
        this.join = join;
        return this;
    }

}
