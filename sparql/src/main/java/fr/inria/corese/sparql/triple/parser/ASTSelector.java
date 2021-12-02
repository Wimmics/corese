package fr.inria.corese.sparql.triple.parser;

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
    
    public ASTSelector() {
        predicateService = new HashMap<>();
        tripleService = new HashMap<>();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPredicateService().toString());
        sb.append(System.getProperty("line.separator"));
        sb.append(getTripleService().toString());
        return sb.toString();
    }
    
    public List<Atom> getPredicateService(Constant pred) {
        return getPredicateService().get(pred.getLabel());
    }
    
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
}
