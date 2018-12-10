package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Exp;
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
    String name = "?_serv_";
    ArrayList<Variable> varList; 
    
    RewriteService(FederateVisitor vis) {
        this.vis = vis;
        varList = new ArrayList<>();
    }
    
    void process(ASTQuery ast) {
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
                    Values values = Values.create(var, s.getServiceConstantList());
                    s.setServiceName(var);
                    s.clearServiceList();
                    body.add(i, values);
                    i++;
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
       

}
