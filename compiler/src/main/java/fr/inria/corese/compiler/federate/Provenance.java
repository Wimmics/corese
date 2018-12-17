package fr.inria.corese.compiler.federate;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Group;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Provenance {
    
    HashMap<Variable, List<Variable>> table;
    List<Variable> varList;
    Mappings map;
    
    
    Provenance(List<Service> serviceList, Mappings map) {
        this.map = map;
        table = new HashMap<>();
        varList = new ArrayList<>();
        init(serviceList);
    }
    
    void init(List<Service> serviceList) {
        for (Service serv : serviceList) {
            if (serv.getServiceName().isVariable()) {
                Variable var = serv.getServiceName().getVariable();
                varList.add(var);
                table.put(var, serv.getVariables());
            }
        }
    }
    
    HashMap<Variable, List<Variable>> getProvenance() {
        return table;
    }
    
    
    void display() {
        for (Mapping m : map) {
            for (Variable var : varList) {
                System.out.println(var + " " + m.getValue(var.getLabel()));
                for (Variable name : table.get(var)) {
                    if (!name.equals(var) && m.getValue(name.getLabel()) != null) {
                        System.out.println(name + " " + m.getValue(name.getLabel()));
                    }
                }
            }
            System.out.println("__");
        }
    }
    
    void aggregate() {
        Group group = Group.create(getNodeList());
        for (Mapping m : map) {
            group.add(m);
        }
        
        for (Mapping m : group.getTable().keySet()) {
            for (Variable var : varList) {
                System.out.println(m.getValue(var.getLabel()));
            }
            System.out.println("Number: " + group.getTable().get(m).size());
            System.out.println("__");
        }
    }
    
    List<Node> getNodeList() {
        List<Node> list = new ArrayList<Node>();
        for (Mapping m : map) {
            for (Variable var : varList) {
                Node node = m.getQueryNode(var.getLabel());
                if (node != null && ! list.contains(node)) {
                    list.add(node);
                }
            }
            if (list.size() == varList.size()) {
                return list;
            }
        }
        return list;
    }
    

}
