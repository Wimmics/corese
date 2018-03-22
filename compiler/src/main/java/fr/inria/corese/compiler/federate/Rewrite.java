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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Rewrite {

    FederateVisitor visitor;
    
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
                    if (visitor.isExist() && visitor.isExist(exp)) {
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
     */
    void filterExist(Atom name, Exp body, HashMap<BasicGraphPattern, Service> bgpList, List<Exp> filterList, Exp filterExist) {
        Expression filter = filterExist.getFilter();
        List<String> list = filter.getVariables();

        visitor.rewriteFilter(name, filter);
        Exist exist = filter.getTerm().getExistPattern();
        Exp bgpExist = exist.get(0);

        if (bgpExist.size() == 1 && bgpExist.get(0).isService()) {
            Service servExist = bgpExist.get(0).getService();
            if (servExist.getServiceList().size() == 1) {

                for (BasicGraphPattern bgp : bgpList.keySet()) {
                    Service servBGP = bgpList.get(bgp);
                    if (servExist.getServiceName().equals(servBGP.getServiceName())) {
                        // remove the exist service clause, 
                        exist.set(0, servExist.get(0));
                        // move the exist BGP into the outer service BGP
                        bgp.add(filterExist);
                        System.out.println("R: " + servBGP);
                        if (!filterList.contains(filterExist)) {
                            filterList.add(filterExist);
                        }
                    }
                }
            }
        }
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
    
    
    
}
