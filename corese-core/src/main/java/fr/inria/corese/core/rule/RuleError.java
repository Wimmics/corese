package fr.inria.corese.core.rule;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Mappings;
import java.util.List;

/**
 *
 */
public class RuleError {  
    private Rule rule;
    private List<Edge> edgeList;
    private Mappings map;
    private boolean edge = true;
    
    RuleError(Rule r, List<Edge> list) {
        rule = r;
        edgeList = list;
        edge = true;
    }
    
    RuleError(Rule r, Mappings m) {
        rule = r;
        map = m;
        edge = false;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getRule().getName()).append("\n");
        
        if (isEdge()){
            for (Edge edge : getEdgeList()) {
                sb.append(String.format("%s %s\n", edge.getProperty(), edge.getNode(1)));
            }
            sb.append("\n");
        }
        else {
            sb.append(getMap());
        }
        return sb.toString();
    }
    
    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Mappings getMap() {
        return map;
    }

    public void setMap(Mappings map) {
        this.map = map;
    }

    public boolean isEdge() {
        return edge;
    }

    public void setEdge(boolean edge) {
        this.edge = edge;
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

    public void setEdgeList(List<Edge> edgeList) {
        this.edgeList = edgeList;
    }
}
