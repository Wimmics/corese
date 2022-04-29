package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.FederateMerge;
import java.util.List;

/**
 *
 */
public class ExtractList implements FederateMerge {
    
     // create and return bgp list of RDF list if any
     public List<BasicGraphPattern> getRDFList(Exp exp, List<BasicGraphPattern> list) {        
        for (Exp ee : exp) {
            if (ee.isTriple() && ee.getTriple().isList()) {
               recordListTriple(list, ee.getTriple(), true);
            }
        }
        
        merge(list, true);
        sort(list);
        return list;
    }
          
     
     // create and return list of connected bgp made of triple connected by bnode variable:
     // ?s :p [ :q :a ; :r :b ]
    public List<BasicGraphPattern> getBGPWithBnodeVariable(Exp exp, List<BasicGraphPattern> list) {
        return getBGPWithBnodeVariable(exp, list, this);
    }

    // merge triple on specific criteria
    public List<BasicGraphPattern> getBGPWithBnodeVariable(Exp exp, List<BasicGraphPattern> list, FederateMerge fm) {
        bgpMerge(exp, list, fm);
        merge(list, false);
        return list;
    }
    
    List<BasicGraphPattern> bgpMerge(Exp exp, List<BasicGraphPattern> list, FederateMerge fm) {
        for (Exp ee : exp) {
            if (ee.isTriple() && fm.merge(ee.getTriple())) {
                recordListTriple(list, ee.getTriple(), false);
            }
        }
        return list;
    }
    
    // @todo
    // s p [ q r ] . s q v
    // bgp = s p [q r]
    // merge s q v in bgp because it share variable s with bgp
    List<BasicGraphPattern> bgpComplete(Exp exp, List<BasicGraphPattern> list, FederateMerge fm) {
        for (Exp ee : exp) {
            if (ee.isTriple()) {
            }
        }
        return list;
     }
    
     @Override
    public boolean merge(Triple t) {
        return hasBlank(t);
    }
          
    boolean hasBlank(Triple t) {
        return t.getSubject().isBlankNode() ||
               t.getObject().isBlankNode();
    }
    
    void recordListTriple(List<BasicGraphPattern> list, Triple t, boolean blist) {
        for (BasicGraphPattern exp : list) {
            if (connected(exp, t, blist)) {
                if (! exp.getBody().contains(t)) {
                    exp.add(t);
                }
                return;
            }
        }
        list.add(BasicGraphPattern.create(t));
    }
    
    boolean connected(BasicGraphPattern bgp, Triple t1, boolean list) {
        for (Exp ee : bgp) {
            Triple t2 = ee.getTriple();
            if (connected(t1, t2, list)) {
                return true;
            }
        }
        return false;
    }
    
    boolean connected(Triple t1, Triple t2, boolean list) {
        boolean b =  t1.getSubject().equals(t2.getSubject())
             || t1.getSubject().equals(t2.getObject())
             || t1.getObject().equals(t2.getSubject()) ;
        if (list || b) {
            return b;
        }
        return t1.getObject().equals(t2.getObject()) ;
    }
    
    boolean connected(BasicGraphPattern bgp1, BasicGraphPattern bgp2, boolean list) {
        for (Exp ee : bgp1) {
            if (connected(bgp2, ee.getTriple(), list)) {
                return true;
            }
        }
        return false;
    }
    
    void merge(List<BasicGraphPattern> list, boolean blist) {
        boolean run = true;
        while (run) {
            run = false;
            for (int i = 0; i < list.size(); i++) {
                BasicGraphPattern bgp1 = list.get(i);
                for (int j = i + 1; j < list.size();) {
                    BasicGraphPattern bgp2 = list.get(j);
                    if (connected(bgp1, bgp2, blist)) {
                        bgp1.addDistinct(bgp2);
                        list.remove(bgp2);
                        run = true;
                    } else {
                        j++;
                    }
                }
            }
        }
    }
    
    void sort(List<BasicGraphPattern> list) {
        for (BasicGraphPattern bgp : list) {
            sort(bgp);
        }
    }
    
    // pust first two triples of list  at begining of bgp
    void sort(BasicGraphPattern rdfList) {
        Exp fst;
        for (int i = 0; i<rdfList.size();i++) {
            Triple t1 = rdfList.get(i).getTriple();
            boolean find = false;
            Triple t2 = null;
            for (int j = 0; j<rdfList.size();j++) {
                t2 = rdfList.get(j).getTriple();
                if (t1.getSubject().equals(t2.getObject())) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                rdfList.getBody().remove(t1);
                rdfList.add(0, t1);
            }
        }
    }
}
