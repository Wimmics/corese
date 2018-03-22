package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exist;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Rewrite {
   
    FederateVisitor visitor;
    private boolean debug = false;
    
    Rewrite(FederateVisitor vis) {
        visitor = vis;
    }
    
     class ServiceBGP {
        HashMap<String, BasicGraphPattern> map = new HashMap<>();
        
        BasicGraphPattern get(String label){
            BasicGraphPattern bgp = map.get(label);
            if (bgp == null) {
                bgp = BasicGraphPattern.create();
                map.put(label, bgp);
            }
            return bgp;
        }
        
        HashMap<String, BasicGraphPattern> getMap() {
            return map;
        }      
       
    }
    
    
    
    /**
     * group triple patterns that share the same service URI
     * into one service URI
     * modify the body (replace triples by BGPs)
     * filterList: list to be filled with filters that are copied into service
     */
    void prepare(Atom name, Exp body, List<Exp> filterList) {
        ServiceBGP map = new ServiceBGP();
        
        // create BGPs for triples that share same service URI
        // create a table service -> BGP
        for (int i = 0; i<body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isTriple() && ! exp.isFilter()) {
                List<Atom> list = visitor.getServiceList(exp.getTriple());
                if (list.size() == 1) {
                   BasicGraphPattern bgp = map.get(list.get(0).getLabel());
                   bgp.add(exp);
                }
            }
        }
               
        HashMap<Triple, BasicGraphPattern>  table = new HashMap<>();
        HashMap<BasicGraphPattern, Service> done = new HashMap<>();
        
        // record triples that are member of created BGPs
        for (BasicGraphPattern bgp : map.getMap().values()) {
            if (bgp.size() > 1) {
                for (Exp exp : bgp) {
                    table.put(exp.getTriple(), bgp);
                }
            }
        }
        
        // for each triple member of created BGP
        // replace first triple by service s { BGP }
        // move simple filters into BGP
        for (int i = 0; i<body.size(); i++) {
            if (body.get(i).isTriple()) { 
                Triple t = body.get(i).getTriple();
                if (table.containsKey(t)){
                    BasicGraphPattern bgp = table.get(t);
                    if (done.get(bgp) == null ) {                       
                        // service s { bgp }
                        Service serv = visitor.rewrite(name, bgp, visitor.getServiceList(t));
                        // do it once for first triple of this BGP
                        done.put(bgp, serv);
                        // replace first triple of BGP by service BGP
                        body.set(i, serv);
                    } 
                }
            }
        }
        
        // remove triples member of a created BGP
        for (Triple t : table.keySet()) {
            body.getBody().remove(t);
        }
        
        filter(name, body, done, filterList);
    }
    
    
    void filter(Atom name, Exp body, HashMap<BasicGraphPattern, Service> bgpList, List<Exp> filterList) {
        // copy filters from body into BGP who bind all filter variables
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (visitor.isRecExist(exp)) {
                    if (visitor.isExist() && 
                            (visitor.isExist(exp) || visitor.isNotExist(exp))) {
                        // draft for testing
                        filterExist(name, body, bgpList, filterList, exp);                       
                    }
                }
                else {
                    for (BasicGraphPattern bgp : bgpList.keySet()) {
                        List<Variable> varList = bgp.getVariables();
                        if (exp.getFilter().isBound(varList)) {
                            bgp.add(exp);
                            if (!filterList.contains(exp)) {
                                filterList.add(exp);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Draft for testing
     * TODO: test conditions to move exists clause into service
     * 
     * body = 
     * service URI { BGP }  filter exists { EXP } 
     * ->
     * service URI { BGP }  filter exists { service URI { EXP } }
     * ->
     * service URI { BGP  filter exists { EXP } } 
     * 
     * TODO: 
     * does not work if there were existing services before rewrite
     * because they are not in bgpMap
     */
    void filterExist(Atom name, Exp body, HashMap<BasicGraphPattern, Service> bgpMap, List<Exp> filterList, Exp filterExist) {
        boolean isNotExist = visitor.isNotExist(filterExist);
        Expression filter = filterExist.getFilter();
        visitor.rewriteFilter(name, filter);
        Exist exist;
        if (isNotExist) {
            exist = filter.getTerm().getArg(0).getTerm().getExistPattern();
        }
        else {
            exist = filter.getTerm().getExistPattern();            
        }
        Exp bgpExist = exist.get(0);
        
        if (bgpExist.size() == 1 && bgpExist.get(0).isService()) {
            Service servExist = bgpExist.get(0).getService();
            if (servExist.getServiceList().size() == 1) {

               List<BasicGraphPattern> bgpList = new ArrayList<>();
               List<Variable> existVarList = bgpExist.getVariables();
               List<Variable> intersection = null;
               
                // select relevant BGP with same URI as exists
                // If other BGP bind some variables of exists, 
                // they must bind the same variables as the relevant one
                for (BasicGraphPattern bgp : bgpMap.keySet()) {

                    Service servBGP = bgpMap.get(bgp);
                    List<Variable> bgpVarList = bgp.getVariables();                  
                    List<Variable> inter = intersection(existVarList, bgpVarList);
                    
                    if (isDebug()) {
                        System.out.println("R: " + existVarList + " " + bgpVarList);
                        System.out.println("Intersection: " + inter);
                    }
                    
                    if (servExist.getServiceName().equals(servBGP.getServiceName())) {
                        if (intersection == null) {
                             intersection = inter;
                        }
                        
                        if (equal(intersection, inter)) {
                            bgpList.add(bgp);
                        } else {
                            // two BGP intersect differently the exists clause
                            return;
                        }
                    } 
                    else // service with another URI
                    if (!inter.isEmpty()) {
                        if (intersection == null) {
                            intersection = inter;
                        } else if (!equal(intersection, inter)) {
                            // two BGP intersect differently the exists clause
                            return;
                        }
                    }                   
                }

                if (bgpList.size() == 1) {
                    // remove service from exists
                    exist.set(0, servExist.get(0));
                    // move exist into relevant service 
                    bgpList.get(0).add(filterExist);
                    if (!filterList.contains(filterExist)) {
                        filterList.add(filterExist);
                    }
                }
                
            }
        }
    }
    
    boolean equal(List<Variable> list1, List<Variable> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (Variable var : list1) {
            if (! list2.contains(var)) {
                return false;
            }
        }
        return true;
    }
    
    boolean hasIntersection(List<Variable> list1, List<Variable> list2) {
       return ! intersection(list1, list2).isEmpty();              
    }
    
    List<Variable> intersection(List<Variable> list1, List<Variable> list2) {
        ArrayList<Variable> list = new ArrayList<>();
         for (Variable var : list1) {
            if (list2.contains(var) && ! list.contains(var)) {
                list.add(var);
            }
        }
        return list;
    }
    
    
    boolean includedIn(List<Variable> list1, List<Variable> list2) {
        for (Variable var : list1) {
            if (! list2.contains(var)) {
                return false;
            }
        }
        return true;
    }
    
     /**
     * service s {e1} optional { service s {e2}}
     * ->
     * service s { e1 optional {e2} }
     * and simplifySelectFrom(exp)
     */
    Exp simplify(Exp exp) {
        if (exp.get(0).size() == 1 && exp.get(1).size() == 1) {
            Exp e1 = exp.get(0).get(0);
            Exp e2 = exp.get(1).get(0);
            if (e1.isService() && e2.isService()) {
                Service s1 = e1.getService();
                Service s2 = e2.getService();
                if (s1.getServiceList().size() == 1
                        && s2.getServiceList().size() == 1
                        && s1.getServiceName().equals(s2.getServiceName())) {
                    exp.set(0, s1.get(0));
                    exp.set(1, s2.get(0));
                    Exp simple = simplifySelectFrom(exp);
                    Service s = 
                        Service.create(s1.getServiceName(), 
                            BasicGraphPattern.create(simple));
                    return s;
                }
            }
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
                ASTQuery ast1 = e1.getQuery();
                ASTQuery ast2 = e2.getQuery();                
               if (ast1.getFrom().size() == 1 && 
                    ast2.getFrom().size() == 1 &&
                    ast1.getFrom().get(0).equals(ast2.getFrom().get(0))
                        && ast1.isSelectAll() && ast2.isSelectAll()) {
                    exp.set(0, ast1.getBody());
                    exp.set(1, ast2.getBody());
                    Query q = visitor.query(BasicGraphPattern.create(exp));
                    q.getAST().getDataset().setFrom(ast1.getFrom());
                    return q;
                }
            }
        }
        return exp;       
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
