package fr.inria.corese.compiler.federate;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Group;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Provenance {
    final static String NL = System.getProperty("line.separator");
    
    HashMap<Variable, List<Variable>> table;
    List<Variable> varList;
    Mappings map;
    Group group;
    
    
    Provenance(List<Service> serviceList, Mappings map) {
        this.map = map;
        table = new HashMap<>();
        varList = new ArrayList<>();
        init(serviceList);
        aggregate();
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
        group = Group.create(getNodeList());
        for (Mapping m : map) {
            group.add(m);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Mapping m : getServers()) {
            for (Variable var : getVariables()) {
                DatatypeValue res = m.getValue(var.getLabel());
                if (res != null) {
                    sb.append(res.stringValue()).append(NL);
                }
            }
            sb.append("Number: ").append(getMappings(m).size()).append(NL);
            sb.append("__").append(NL);
        }
        return sb.toString();   
    }
    
    public List<Variable> getVariables() {
        return varList;
    }
    
    public Collection<Mapping> getServers() {
        return group.getTable().keySet();
    }
    
    public Mappings getMappings(Mapping m) {
        return group.getTable().get(m);
    }
    
    public List<Node> getServerNames(Mapping m) {
        ArrayList<Node> list = new ArrayList<>();
        for (Variable var : getVariables()) {
            Node n = m.getNode(var.getLabel());
            if (n != null) {
                list.add(n);
            }
        }
        return list;
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
