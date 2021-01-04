package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Rewrite service <uri> { } as service <uri> { graph <g> { }}
 * Use case: agroportal store is structured with one named graph per ontology  
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class RewriteServiceGraph {
    
    FederateVisitor vis;
    int count = 0;
    boolean export = false;
    String name = "?_serv_";
    ArrayList<Variable> varList; 
    ArrayList<Service> serviceList;
    ASTQuery ast;
    ServiceGraph map;
    
    class ServiceGraph {
        
        HashMap <String, List<Constant>>  map;
        
        ServiceGraph(){
            map = new HashMap<>();
        }
        
        
        void declare(Constant service, Constant graph) {
            List<Constant> list = map.get(service.getLabel());
            if (list == null) {
                list = new ArrayList<>();
                map.put(service.getLabel(), list);
            }
            if (! list.contains(graph)) {
                list.add(graph);
            }
        }
        
        List<Constant> getGraphList(Constant service) {
            return map.get(service.getLabel());
        }
        
        Constant getGraph(Constant service) {
            List<Constant> list = map.get(service.getLabel());
            if (list == null) {
                return null;
            }
            return list.get(0);
        }
        
    }
            
    RewriteServiceGraph(FederateVisitor vis) {
        this.vis = vis;
        map = new ServiceGraph();
    }
    
    void process(ASTQuery ast) {
        this.ast = ast;
        init();
        rewrite(ast);
    }
    
    void init() {
        List<String> list = ast.getMetadata().getValues(Metadata.GRAPH);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                declare(list.get(i++), list.get(i));
            }
        }
    }
    
    void declare(String service, String graph) {
        map.declare(Constant.createResource(service), Constant.createResource(graph));
    }
    
    void rewrite(ASTQuery ast) {
        rewrite(ast.getBody());
    }
    
    void rewrite(Exp body) {
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isService()) {
                Service s = exp.getService();
                if (s.getServiceName().isConstant()) {
                    Constant graph = map.getGraph(s.getServiceName().getConstant());
                    if (graph != null) {
                        rewrite(s, graph);
                    }
                }
            }
            else if (exp.isFilter() || exp.isBind()) {
                rewrite(exp.getFilter());
            }
            else if (exp.isQuery()) {
                rewrite(exp.getAST());
            }
            else {
                rewrite(exp);
            }
        }
    }
    
    void rewrite(Expression exp){
        if (exp.isTermExist()) {
            rewrite(exp.getTerm().getExistBGP());
        }
        else if (exp.isTerm()) {
            for (Expression ee : exp.getArgs()) {
                rewrite(ee);
            }
        }
    }
    
    void rewrite(Service s, Constant graph) {
        s.setBodyExp(ast.bgp(ast.graph(graph, s.getBodyExp())));
    }
    
    List<Variable> getVarList() {
        return varList;
    }
    
    List<Service> getServiceList() {
        return serviceList;
    }
       

}
