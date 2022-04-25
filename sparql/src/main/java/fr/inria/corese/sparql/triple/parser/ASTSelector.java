package fr.inria.corese.sparql.triple.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class ASTSelector {
    // Atom is Constant(url) url of service
    // String is predicate label
    private HashMap<String, List<Atom>> predicateService;
    private HashMap<Triple, List<Atom>> tripleService;
    // map: {t1 t2} -> (uri) where join(t1, t2) = true
    private HashMap<BasicGraphPattern, List<Atom>> bgpService;
    
    public ASTSelector() {
        predicateService = new HashMap<>();
        tripleService = new HashMap<>();
        bgpService = new HashMap<>();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPredicateService().toString());
        sb.append(System.getProperty("line.separator"));
        sb.append(getTripleService().toString());
        return sb.toString();
    }
    
   
    public void complete() {
        System.out.println("before:\n"+getTripleService());
        restrict();
        System.out.println("after:\n"+getTripleService());
    }
    
    void restrict() {
        for (BasicGraphPattern bgp : getBgpService().keySet()) {
            if (getBgpService().get(bgp).isEmpty()) {
                restrict(bgp);
            }
        }   
    }
    
    // when join bgp {t1 t2} -> ()
    // and t1 -> (u1 u2) and t2 -> (u1)
    // we know that t1 is not in u1 otherwise bgp uri list -> (u1)
    // in this case, modify (or set) map: t1 -> (u1 u2) into t1 -> (u2)    
    // @todo: triple with filter ?
    void restrict(BasicGraphPattern bgp) {
        Triple t1 = bgp.get(0).getTriple();
        Triple t2 = bgp.get(1).getTriple();
        List<Atom> l1 = getPredicateService(t1);
        List<Atom> l2 = getPredicateService(t2);
        
        if (l1!=null && l2!=null) {
            if (l1.size() == 1 && l2.size() > 1 && l2.contains(l1.get(0))) {
                restrict(t1, t2, l1, l2);
            }
            else if (l1.size() > 1 && l2.size() == 1 && l1.contains(l2.get(0))) {
                restrict(t2, t1, l2, l1);
            }
        }
    }
    
    void restrict(Triple t1, Triple t2, List<Atom> l1, List<Atom> l2) {
            List<Atom> list = getTripleService().get(t2);
            if (list == null) {
                list = new ArrayList<>();
                list.addAll(l2);
                getTripleService().put(t2, list);
            }
            list.remove(l1.get(0));
    }
    
    public List<Atom> getPredicateService(Constant pred) {
        return getPredicateService().get(pred.getLabel());
    }
    
    // search triple map
    // else search predicate map
    public List<Atom> getPredicateService(Triple t) {
        List<Atom> list = getTripleService().get(t);
        if (list != null) {
            return list;
        }
        if (t.getPredicate().isVariable()) {
            return null; //ast.getServiceList();
        }
        return getPredicateService(t.getPredicate().getConstant());
    }
    
    
    public ASTSelector copy(ASTQuery ast) {
        ASTSelector sel = new ASTSelector();
        sel.setPredicateService(getPredicateService());

        for (Triple t2 : ast.getTripleList()) {
            for (Triple t : getTripleService().keySet()) {
                if (t2.equals(t)) {
                    sel.getTripleService().put(t2, getTripleService().get(t));
                    break;
                }
            }
        }
        
        return sel;
    }
    
    public boolean join(BasicGraphPattern bgp, Triple t, String uri) {
        boolean suc = false;
        for (Exp exp : bgp) {
            if (exp.isTriple()) {
                if (join(exp.getTriple(), t, uri)) {
                    suc = true;
                }
                else {
                    return false;
                }
            }
        }
        return suc;
    }
    
    public boolean join(Triple t1, Triple t2, String uri) {
        for (BasicGraphPattern bgp : getBgpService().keySet()) {
            if (bgp.getBody().contains(t1) && bgp.getBody().contains(t2)) {
                for (Atom at : getBgpService().get(bgp)) {
                    if (at.getLabel().equals(uri)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
        
    public HashMap<String, List<Atom>> getPredicateService() {
        return predicateService;
    }

    public void setPredicateService(HashMap<String, List<Atom>> predicateService) {
        this.predicateService = predicateService;
    }

    public HashMap<Triple, List<Atom>> getTripleService() {
        return tripleService;
    }

    public void setTripleService(HashMap<Triple, List<Atom>> tripleService) {
        this.tripleService = tripleService;
    }

    public HashMap<BasicGraphPattern, List<Atom>> getBgpService() {
        return bgpService;
    }

    public void setBgpService(HashMap<BasicGraphPattern, List<Atom>> bgpService) {
        this.bgpService = bgpService;
    }
}
