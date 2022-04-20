package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * This class has two instances and 
 * the first instance contains the second
 * a) uri2bgp:     uri -> (connected bgp) triple with one URI
 * b) uriList2bgp: uri -> (connected bgp) triple with several URI
 */
class URI2BGPList {
    // uri -> bgp list
    HashMap<String, List<BasicGraphPattern>> uri2bgp;
    // bgp -> uri list
    private BGP2URI bgp2uri;
    // list of triple in context a) or b)
    private ArrayList<Triple> tripleList;
    // service with one URI
    private ArrayList<Service> serviceList;
    // main URI2BGPList for triple with one URI
    // uriList2bgp for triple with several URI 
    private URI2BGPList uriList2bgp;
    
    class BGP2URI extends HashMap<BasicGraphPattern, List<String>> {
        
        // equivalent of get when different bgp object which have same content
        // must be same key
        BasicGraphPattern getKey(BasicGraphPattern bgp) {
            for (BasicGraphPattern exp : keySet()) {
                if (bgp.bgpEqual(exp)) {
                    return exp;
                }
            }
            return bgp;
        }
        
        List<BasicGraphPattern> keyList() {
            ArrayList<BasicGraphPattern> list = new ArrayList<>();
            list.addAll(keySet());
            return list;
        }
        
        List<BasicGraphPattern> sortKeyList() {
            return sort(keyList());
        }
        
        List<BasicGraphPattern> sort(List<BasicGraphPattern> list) {
            list.sort(new Comparator<>(){
                public int compare(BasicGraphPattern bgp1, BasicGraphPattern bgp2) {
                    return - Integer.compare(bgp1.size(), bgp2.size());
                }
            });
            return list;
        }
        
    }
    
    
    URI2BGPList() {
        uri2bgp = new HashMap<>();
        bgp2uri = new BGP2URI();
        tripleList = new ArrayList<>();
        serviceList = new ArrayList<>();
    }
    
    void complete() {
        bgp2uri();
        if (getUriList2bgp() !=null) {
            getUriList2bgp().bgp2uri();
        }
    }
    
    void bgp2uri() {        
        for (String uri : uri2bgp.keySet()) {
            for (BasicGraphPattern bgp : uri2bgp.get(uri)) {
                // different bgp object with same content 
                // must have same key bgp
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
    
    // assign triple to connected bgp of service uri
    // merge connected bgp
    void assignTripleToConnectedBGP(Triple triple, String uri) {
        List<BasicGraphPattern> bgpList = get(uri);
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
            // include it into former BGP
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
            // create new BGP because triple is connected to nobody
            bgpList.add(BasicGraphPattern.create(triple));
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

}
