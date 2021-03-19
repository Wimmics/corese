package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rewrite service (uri) { } as values ?serv { (uri) } service ?serv { }
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class RewriteService {
    
    FederateVisitor vis;
    int count = 0;
    boolean export = false;
    String name = Service.SERVER_SEED;
    ArrayList<Variable> varList; 
    ArrayList<Service> serviceList;
    
    RewriteService(FederateVisitor vis) {
        this.vis = vis;
        varList = new ArrayList<>();
        serviceList = new ArrayList<>();
    }
    
    void process(ASTQuery ast) {
        if (ast.hasMetadata(Metadata.PUBLIC)) {
            export = true;
        }
        process(ast.getBody());
    }
    
    void process(Exp body) {
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isService()) {
                Service s = exp.getService();
                if (! s.getServiceName().isVariable()) {
                    Variable var = new Variable(name + count++);
                    varList.add(var);
                    serviceList.add(s);                  
                    s.setServiceName(var);
                    if (true) {
                        // bind service variable with values
                        Values values = Values.create(var, s.getServiceConstantList());
                        s.clearServiceList();
                        body.add(i, values);
                        i++;
                    }
                    else {
                      // service has variable and server URI list
                       s.setGenerated(true);
                    }
                }
            }
            else {
                process(exp);
            }
        }
    }
    
    List<Variable> getVarList() {
        return varList;
    }
    
    List<Service> getServiceList() {
        return serviceList;
    }
       

}
