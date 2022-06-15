package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.ASTSelector;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merge several service with same URI into one service
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Simplify extends Util {
    static Logger logger = LoggerFactory.getLogger(Simplify.class);
    private static boolean MERGE_EVEN_IF_NOT_CONNECTED = false;
    // heuristics to simplify service s bgp1 optional service (S) bgp2 
    // when all triple of bgp2
    // join with connected triple of bgp1 in s
    // and s memberOf S
    // heuristics similar to federate bgp, heuristics cover both arg of optional
    
    FederateVisitor visitor;
    private boolean debug = false;
    private boolean mergeEvenIfNotConnected = MERGE_EVEN_IF_NOT_CONNECTED;
    
    Simplify(FederateVisitor vis) {
        visitor = vis;
    }
    
    class ServiceList {
        HashMap<String, List<Service>> map;
        
        ServiceList() {
            map = new HashMap<>();
        }
        
        void add(Service serv) {
            List<Service> l = map.get(serv.getServiceName().getLabel());
            if (l == null) {
                l = new ArrayList<>();
                map.put(serv.getServiceName().getLabel(), l);
            }
            l.add(serv);
        }
        
        HashMap<String, List<Service>> getMap() {
            return map;
        }
    }
    
    // main function
    // exp is binary
    Exp simplify(Exp exp) {
        Exp res = simplifyBinary(exp);
        return res;
    }

    // main function
    Exp process(Exp bgp) {
        bgp = merge(bgp);
        if (visitor.isBounce()){
            // @deprecated
            bgp = bounce(bgp);
        }
        return bgp;
    }
    


        
    /**
     * BGP { service URI {EXP1} service URI {EXP2} EXP3 } ::= 
     * BGP { service URI {EXP1 EXP2} EXP3 }
     *
     * TODO: merge when there is only one URI ???
     */ 
    Exp merge(Exp bgp) {  
        return merge(bgp, isMergeEvenIfNotConnected());
    }

    Exp merge(Exp bgp, boolean mergeEvenIfNotConnected) {    
        ServiceList include = new ServiceList();
        ServiceList exclude = new ServiceList();        
        // create map: service URI -> list (Service)
        for (Exp exp : bgp) {
            if (exp.isService()){ 
                if (exp.getService().isFederate()) {
                    // several URI: skip
                } 
                else if (mergeEvenIfNotConnected) {
                    include.add(exp.getService());
                }
                else if (isTripleFilterOnly(exp.getBodyExp())) {
                    // do not merge basic BGP with same service URI
                    // because they are not connected 
                    exclude.add(exp.getService());
                }
                else {
                    include.add(exp.getService());
                }
            }
        }
              
        // group several services with same URI into one service
        for (List<Service> list : include.getMap().values()) {
            Service first = list.get(0);
            int i = 0;
            boolean mod = false;
            
            for (Service s : list) {
                if (i++ > 0) {
                    first.getBodyExp().include(s.getBodyExp());
                    bgp.getBody().remove(s);
                    mod = true;
                }
            }
            
            List<Service> alist = exclude.getMap().get(first.getServiceName().getLabel());
            if (alist != null) {
                for (Service s : alist) {
                    // TODO: take constant node into account for connect
                    if (first.getBodyExp().isConnect(s.getBodyExp())) {
                        first.getBodyExp().include(s.getBodyExp());
                        bgp.getBody().remove(s);
                        mod = true;
                    }
                }
            }

            if (true) { //  if (mod) {
                simplifyGraph2(first.getBodyExp());
                visitor.sort(first.getBodyExp());
            }
        }        
        bgp = move(bgp, exclude);       
        return bgp;
    }
    
    /**
     * @move rdfs:label
     * if there is a service s1 with several uri with a triple annotated as
     * @move and if there is a service s2 with one of the URI of s1, s1 can be
     * merged with s2
     */
    Exp move(Exp bgp, ServiceList serviceList) {
        if (! visitor.getAST().hasMetadata(Metadata.MOVE)) {
            return bgp;
        }
        boolean go = true;
        while (go) {
            go = false;
            ArrayList<Service> list = new ArrayList<>();
            for (Exp exp : bgp) {
                if (exp.isService() && isMoveable(exp.getService())) {
                    // exp moveable if connected with serv and exp has only one triple
                    Service serv = getCandidate(exp.getService(), serviceList);
                    if (serv != null) {
                        serv.getBodyExp().include(exp.getService().getBodyExp());
                        list.add(exp.getService());
                        go = true;
                    }
                }
            }

            for (Service s : list) {
                bgp.getBody().remove(s);
            }
        }
        
        return bgp;
    }
    
     Service getCandidate(Service serv, ServiceList map) {
        for (Atom name : serv.getServiceList()) {
            List<Service> list = map.getMap().get(name.getLabel());
            if (list != null) {
                Service res = list.get(0);
                if (serv.getBodyExp().isConnected(res.getBodyExp())) {
                    return res;
                }
            }
        }
        return null;
    }
     
    Service getCandidate2(Service serv, ServiceList map) {
        Service res = null;       
        for (Atom name : serv.getServiceList()) {
            List<Service> list = map.getMap().get(name.getLabel());
            if (list != null) {
                if (res == null) {
                    res = list.get(0);
                    if (! serv.getBodyExp().isConnect(res.getBodyExp())) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        return res;
    }
    
    boolean isMoveable(Service exp) {
        Exp body = exp.getBodyExp();
        if (!exp.isFederate()) {
            return false;
        }
        Triple t = getUniqueTriple(body);
        if (t == null) {
            return false;
        }
        return t.getPredicate().isConstant()
                && isMoveable(t.getPredicate().getConstant());
    }
    
    // TODO: there could be a bind  
    Triple getUniqueTriple(Exp body) {
        Triple t = null;
        for (Exp exp : body) {
            if (exp.isFilter()) {}
            else if (exp.isTriple()) {
                if (t == null) {
                    t = exp.getTriple();
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        return t;
    }
    
    boolean isMoveable(Constant predicate) {
        return visitor.getAST().hasMetadata(Metadata.MOVE)
                && (visitor.getAST().hasMetadataValue(Metadata.MOVE, predicate.getLabel())
                 || visitor.getAST().getMetadata().getValues(Metadata.MOVE) == null);
    }
    
    /**
     * BGP { service URI1 { EXP1 } service URI2 { EXP2 } }
     * if URI1 accept bouncing and EXP1.isConnected(EXP2)
     * ->
     * BGP { service URI1 { EXP1 service URI2 { EXP2 } } }
     */
    
    Exp bounce(Exp bgp) {
        HashMap<Service, Boolean> done   = new HashMap();
        HashMap<Service, Boolean> remove = new HashMap();
        
        for (int i = 0; i < bgp.size(); i++) {
            Exp e1 = bgp.get(i);
            if (done.get(e1) == null && e1.isService() && ! e1.getService().isFederate()) {
                Service s1 = e1.getService();
                
                for (int j = i + 1; j < bgp.size(); j++) {
                    Exp e2 = bgp.get(j);
                    if (done.get(e2) == null && e2.isService() && ! e2.getService().isFederate()) {
                        Service s2 = e2.getService();
                        
                        if (! s1.getServiceName().equals(s2.getServiceName())
                           && s1.getBodyExp().isConnected(s2.getBodyExp())) {
                            
                            if (bounce(s1)) {
                                s1.getBodyExp().add(s2);
                                done.put(s2, true);
                                remove.put(s2, true);
                                break;
                            } else if (bounce(s2)) {
                                s2.getBodyExp().add(s1);
                                done.put(s2, true);
                                remove.put(s1, true);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        for (Service exp : remove.keySet()) {
            bgp.getBody().remove(exp);
        }
        
        return bgp;
    }
    
    // @bounce <URI>
    boolean bounce(Service s) {
        return visitor.getAST().hasMetadataValue(Metadata.BOUNCE, s.getServiceName().getLabel());
    }
      
    /**
     * exp contains only triple and filter
     * TODO: accept bind (exp as var)
     */
    boolean isTripleFilterOnly(Exp exp) {
        for (Exp ee : exp) {
            if (ee.isFilter() || ee.isTriple()) {
                // ok
            }
            else {
                return false;
            }
        }
        return true;
    }
    
    boolean isUnionTripleOnly(Exp bgp) {
        if (bgp.size() == 1 && bgp.get(0).isUnion()) {
            Union union = bgp.get(0).getUnion();
            return isTripleFilterOnly(union.get(0)) && isTripleFilterOnly(union.get(1)) ;
        }
        return false;
    }
    
    boolean isUnionOrTripleOnly(Exp bgp) {
        return isUnionTripleOnly(bgp) || isTripleFilterOnly(bgp);
    }
    
    // bgp = filter exists { bgp }
    // in filter exists, merge services with same URI list 
    void simplifyFilterExist(Exp bgp) {
        ArrayList<Service> list = new ArrayList<>();
        for (int i = 0; i < bgp.size(); i++) {
            if (bgp.get(i).isService()) {
                Service s1 = bgp.get(i).getService();
                if (!list.contains(s1)) {
                    for (int j = i + 1; j < bgp.size(); j++) {
                        if (bgp.get(j).isService()) {
                            Service s2 = bgp.get(j).getService();
                            if (equalURI(s1.getServiceList(), s2.getServiceList())) {
                                s1.getBodyExp().addAll(s2.getBodyExp());
                                list.add(s2);
                            }
                        }
                    }
                }
            }
        }

        for (Service s : list) {
            bgp.getBody().remove(s);
        }
    }
    
    
    // second main function
    Exp simplifyBinary(Exp exp) {
        Exp simple = basicSimplify(exp);
        
        if (simple.isOptional() || simple.isMinus()) {
            Exp split = split(simple);
            if (visitor.isFederateUndefined()) {
                return skipUndefined(split);
            }
            return split;
        }

        if (visitor.isFederateUndefined()) {
            return skipUndefined(simple);
        }
        
        return simple;
    }
    
    // skip undefined service for union/optional/minus
    Exp skipUndefined(Exp exp) {
        if (exp.isUnion()) {
            return unionUndefined(exp);
        }
        else if (exp.isOptional()||exp.isMinus()) {
            return binaryUndefined(exp);
        }
        return exp;
    }
       
    
    /**
     * service s {e1} optional { service s {e2}}
     * ->
     * service s { e1 optional {e2} }
     * and simplifySelectFrom(exp)
     */
    Exp basicSimplify(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            if (e1.isService() && e2.isService()) {
                Service s1 = e1.getService();
                Service s2 = e2.getService();               
                return simplifyService(exp, s1, s2);
            }
        }
        return exp;
    }
    
    Exp simplifyService(Exp exp, Service s1, Service s2) {
        if (isSimplifyUnion(exp, s1, s2)) {
            return simplifyUnion(exp, s1, s2);
        }
        else if (!s1.isFederate() && !s2.isFederate()
          && s1.getServiceName().equals(s2.getServiceName())) {
            // both service with same uri            
            return merge(exp, s1, s2, s1.getServiceList());
        }
        else if (exp.isOptional() && visitor.isFederateOptional()) {
            return binary(exp);
        }
        else if (exp.isMinus()&& visitor.isFederateMinus()) {
            return binary(exp);
        }
        return exp;
    }
    
    // exp =  service S1 {bgp1} optional {service S2 {bgp2}}
    // return service S1 {bgp1 optional {bgp2}}
    Service merge(Exp exp, Service s1, Service s2, List<Atom> list) {
        exp.set(0, s1.getBodyExp());
        exp.set(1, s2.getBodyExp());
        Exp simple = simplifyGraph(exp);
        Service s = Service.create(list, BasicGraphPattern.create(simple));
        return s;
    }
    
    // service S1 {bgp1} optional {service S2 {bgp2}}
    // heuristics to simplify optional when triples in bgp2 
    // are present in service S1 inter S2
    // and for all t in bgp2, connected(t, bgp1) 
    // => join(t, bgp1, S1 inter S2) == true
    // -> service S1 {bgp1 optional {bgp2}}
    // @todo: generalize with several service in right exp
    Exp binary(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            
            if (e1.isService() && e2.isService()) {
                Service s1 = e1.getService();
                Service s2 = e2.getService();
                List<Atom> inter = intersection(s1, s2);

                if (! inter.isEmpty()) {
                    if (join(s1.bgp(), s2.bgp(), inter)) {
                        return merge(exp, s1, s2, s1.getServiceList());
                    }
                }
            }
        }
        return exp;
    }
    
    boolean join(BasicGraphPattern bgp1, BasicGraphPattern bgp2, List<Atom> list) {
        for (Atom uri : list) {
            if (!join(bgp1, bgp2, uri.getLabel())) {
                return false;
            }
        }
        return true;
    }

    
    // for all t in bgp2, connected(t, bgp1) => join(t, bgp1) == true
    // and at least one such t
    boolean join(BasicGraphPattern bgp1, BasicGraphPattern bgp2, String uri) {
        boolean join = false;
        for (Exp e : bgp2) {
            if (e.isTriple()) {
                Triple t = e.getTriple();
                if (bgp1.isConnected(t)) {
                    join = visitor.getAstSelector().join(bgp1, t, uri); 
                    if (!join) {
                        return false;
                    }
                }
            }
        }
        return join;
    }
    
    // case where there is undefined service in one branch
    Exp unionUndefined(Exp exp) {
        if (hasUndefinedService(exp.get(0))
                && !hasUndefinedService(exp.get(1))) {
            logger.info("Skip union exp:\n" + exp.get(0));
            return exp.get(1);
        } else if (hasUndefinedService(exp.get(1))
                && !hasUndefinedService(exp.get(0))) {
            logger.info("Skip union exp:\n" + exp.get(1));
            return exp.get(0);
        }
        return exp;
    }
    
    // optional/minus
    Exp binaryUndefined(Exp exp) {
        if (hasUndefinedService(exp.get(1)) &&
          ! hasUndefinedService(exp.get(0))) {
            logger.info("Skip minus|optional exp:\n" + exp.get(1));            
            return exp.get(0);
        }
        return exp;
    }
    
    boolean hasUndefinedService(Exp exp) {
        return exp.hasUndefinedService();
    } 
    
    ASTSelector getSelector() {
        return visitor.getAstSelector();
    }
    
    
    /**
     * {service S {t1}} union {service S {t2}}
     * ::=
     * service S { {t1} union {t2} }
     */
    Service simplifyUnion(Exp exp, Service s1, Service s2) {
        Union union = Union.create(s1.getBodyExp(), s2.getBodyExp());
        Service s = Service.create(s1.getServiceList(), BasicGraphPattern.create(union));
        return s;
    }
    
    boolean isSimplifyUnion(Exp exp, Service s1, Service s2) {
        return exp.isUnion() && s1.isFederate() && s2.isFederate() 
            && sameURIList(s1, s2)
            && isUnionOrTripleOnly(s1.getBodyExp()) && isUnionOrTripleOnly(s2.getBodyExp());
    }
    
    boolean sameURIList(Service s1, Service s2) {
        return same(s1.getServiceList(), s2.getServiceList());
    }
    
   boolean same(List<Atom> l1, List<Atom> l2) {
       if (l1.size() != l2.size()) {
           return false;
       }
       for (Atom s : l1) {
           if (! l2.contains(s)) {
               return false;
           }
       }
       return true;
   }
    
    Exp simplifyService2(Exp exp, Service s1, Service s2) {
        if (!s1.isFederate() && isMoveable(s2)
                && s2.getServiceList().contains(s1.getServiceName())) {
            exp.set(0, s1.getBodyExp());
            exp.set(1, s2.getBodyExp());
            Exp simple = simplifyGraph(exp);
            Service s = Service.create(s1.getServiceName(),
                            BasicGraphPattern.create(simple));
            return s;
        }
        return exp;
    }
    
    /**
     * select from  g { e1 } optional { select from  g { e2 } }
     * ->
     * select from  g { e1 optional { e2 } }
     */
    Exp simplifySelectFrom(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            if (e1.isQuery() && e2.isQuery()){
                ASTQuery ast1 = e1.getAST();
                ASTQuery ast2 = e2.getAST();                
               if (ast1.getFrom().size() == 1 && 
                    ast2.getFrom().size() == 1 &&
                    ast1.getFrom().get(0).equals(ast2.getFrom().get(0))
                        && ast1.isSelectAll() && ast2.isSelectAll()) {
                    exp.set(0, ast1.getBody());
                    exp.set(1, ast2.getBody());
                    Query q = visitor.getRewriteTriple().query(BasicGraphPattern.create(exp));
                    q.getAST().getDataset().setFrom(ast1.getFrom());
                    return q;
                }
            }
        }
        return exp;       
    }

     /**
     * graph g { e1 } optional { graph g { e2 } }
     * ->
     * graph g { e1 optional { e2 } }
     */
    Exp simplifyGraph(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            if (e1.isGraph() && e2.isGraph()){
                Source g1 = e1.getNamedGraph();
                Source g2 = e2.getNamedGraph();  
               if (//g1.getSource().isConstant() && 
                   g1.getSource().equals(g2.getSource())) {
                    exp.set(0, g1.getBodyExp());
                    exp.set(1, g2.getBodyExp());
                    Source g = Source.create(g1.getSource(), exp);
                    return g;
                }
            }
        }
        return exp;       
    }
    
    Exp simplifyGraph2(Exp exp) {
        int i = 0;
        ArrayList<Source> list = new ArrayList<>();
        for (Exp ee1 : exp) {
            if (ee1.isNamedGraph()) {
                for (int j = i + 1; j < exp.size(); j++) {
                    Exp ee2 = exp.get(j);
                    if (ee2.isNamedGraph()) {
                        Source g1 = ee1.getNamedGraph();
                        Source g2 = ee2.getNamedGraph();
                        if (//g1.getSource().isConstant() &&
                            g1.getSource().equals(g2.getSource())) {                            
                            Source g = g1.merge(g2);
                            exp.set(i, g);
                            list.add(g2);
                            ee1 = g;
                            visitor.sort(g.getBodyExp());
                        }
                    }
                }
            }
            i++;
        }
        for (Source g : list) {
            exp.getBody().remove(g);
        }
        return exp;
    }
    
    
    /**
     * 
     * service s1 {A} service s2 {B} optional { service s2 {C} }
     * with condition: x in var(C) & x not in B => x not in A
     * ->
     * service s1 {A} service s2 {B optional {C}}
     * 
     * service s1 {A} service s2 {B} minus { service s2 {C} }
     * with condition: x in var(C) & x not in B => x not in A
     * -> 
     * service s1 {A} service s2 {B minus {C}}
     */
    Exp split(Exp exp) {
        Exp fst = exp.get(0);
        Exp rst = exp.get(1);
        if (fst.size() == 2 && fst.get(0).isService() && fst.get(1).isService()
                && rst.size() == 1 && rst.get(0).isService()) {
            Service s1 = fst.get(0).getService();
            Service s2 = fst.get(1).getService();
            Service s3 = rst.get(0).getService();
            
            if (splitable(s1, s2, s3)) {
                return split(exp, s1, s2, s3);
            }
            else if (splitable(s2, s1, s3)) {
                return split(exp, s2, s1, s3);
            }
        }
        return exp;
    }
    
    boolean splitable(Service s1, Service s2, Service s3) {
        return !s2.isFederate() && !s3.isFederate()
                && s2.getServiceName().equals(s3.getServiceName())
                && gentle(s1, s2, s3);
    }
    
    Exp split(Exp exp, Service s1, Service s2, Service s3) {
        ASTQuery a = visitor.getAST();
        Service s = a.service(s2.getServiceName(), copy(a, exp, s2.getBodyExp(), s3.getBodyExp()));
        BasicGraphPattern bgp = a.bgp(s1, s);
        return bgp;
    }
    
    // condition: x in var(C) & x not in B => x not in A
    boolean gentle (Service s1, Service s2, Service s3) {
        return gentle(s1.getInscopeVariables(), s2.getInscopeVariables(), s3.getInscopeVariables());
    }
    
   
    boolean gentle(List<Variable> l1, List<Variable> l2, List<Variable> l3) {
        for (Variable var : l3) {
            if (! l2.contains(var) && l1.contains(var)) {
                return false;
            }
        }
        return true;
    }
    
    Exp copy(ASTQuery a, Exp exp, Exp e1, Exp e2) {
        if (exp.isMinus()) {
            return a.minus(e1, e2);
        } else {
            return a.optional(e1, e2);
        }
    }

    public boolean isMergeEvenIfNotConnected() {
        return mergeEvenIfNotConnected;
    }

    public void setMergeEvenIfNotConnected(boolean mergeEvenIfNotConnected) {
        this.mergeEvenIfNotConnected = mergeEvenIfNotConnected;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
