package fr.inria.corese.compiler.federate;

import static fr.inria.corese.compiler.federate.util.RewriteErrorMessage.NO_SERVICE;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.api.FederateMerge;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Union;
import java.util.ArrayList;
import java.util.List;

/**
 * Rewrite rdf list into one service bgp 
 * Rewrite bgp with bnode into one service bgp: ?s :p [ :q ?v ]
 * @focus "?var" -> merge triple with variable var in bgp with bnode and var
 * @focus "?s"
 * ?s p [q u] . ?s p v
 * -> add ?s p v in bgp because it share variable ?s
 */
public class RewriteList implements FederateMerge {
      
    private FederateVisitor visitor;
    IDatatype focus;
    
    RewriteList(FederateVisitor vis){
        visitor = vis;
    }
    
    // group rdf list in specific service bgp
    // group connected triple with bnode variable in specific service bgp
    // modify body
    // return false when one bgp has no service
    // @focus "var" -> merge triple with variable var in the same way as bnode variable
    boolean process(Exp body) {
        focus = visitor.getAST().getMetadataDatatypeValue(Metadata.FOCUS);
        boolean suc = true;
        if (getVisitor().isProcessList()) {
            List<BasicGraphPattern> list = new ArrayList<>();
            body.getRDFList(list);
            body.getBGPWithBnodeVariable(list, this);
            suc = bgp2service(body, list);
        }
        return suc;
    }
    
    @Override
    public boolean merge(Triple t) {
        return hasBlank(t) || submerge(t);
    }
    
    boolean hasBlank(Triple t) {
        return t.getSubject().isBlankNode() ||
               t.getObject().isBlankNode();
    }
    
    boolean submerge(Triple t) {       
        if (focus == null) {
             return false;   
        }
        return hasVariable(t, focus.getLabel());
    }
    
    boolean hasVariable(Triple t, String var) {
        return hasVariable(t.getSubject(), var) ||
               hasVariable(t.getObject(), var);
    }
    
    boolean hasVariable(Atom at, String var) {
        if (at.isVariable()) {
            return var.contains(at.getLabel());
        }
        return false;
    }
    

        
    boolean bgp2service(Exp body, List<BasicGraphPattern> list) {
        boolean suc = true;
        for (BasicGraphPattern exp : list) {
            Exp service = bgp2service(exp);
            if (service == null) {
                // no service handle list
                getVisitor().getErrorManager().add(NO_SERVICE, exp);
                suc = false;
            } else {
                getVisitor().filter(body, exp);
                replace(body, exp, service);
            }
        }
        return suc;
    }
    
    void replace(Exp body, BasicGraphPattern bgp, Exp serviceExp) {
        for (Exp exp : bgp) {
            if (exp.isTriple()) {
                body.getBody().remove(exp);
            }
        }
        body.add(serviceExp);
    }
    

    // rewrite rdf list as service (S) { bgp }
    // where all rdf list triple are in all s in S
    Exp bgp2service(BasicGraphPattern bgp) {
        List<Atom> uriList = new ArrayList<>();
        int count = 0;
        for (Exp triple : bgp) {
            List<Atom> list = getVisitor().getServiceList(triple.getTriple());
            if (list.isEmpty()) {
                getVisitor().error(triple.getTriple(), "rdf list 2 service");
            }
            if (count++ == 0) {
                uriList = list;
            }
            else {
                uriList = intersection(uriList, list);            
            }
        }
        
        if (uriList.isEmpty()) {
            return null;
        }
        Service s = Service.create(uriList, bgp);
        return s;
    }
    
    ArrayList<Atom> intersection(List<Atom> l1, List<Atom> l2) {
        ArrayList<Atom> uriList = new ArrayList<>(); 
        for (Atom uri : l1) {
            if (l2.contains(uri)) {
                uriList.add(uri);
            }
        }
        return uriList;
    }

   
    Exp union(List<Service> list, int n) {
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
