package fr.inria.corese.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.NSManager;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.json.JSONObject;

/**
 *
 */
public class GraphDistance {
    public static int DISTANCE = 2;
    private Graph graph;
    private JSONObject json;
    private NSManager nsm;
    public enum Mode {URI, NAME, DEFAULT};
    private Mode mode = Mode.DEFAULT;
    
    public GraphDistance(Graph g) {
        graph = g;
        setJson(new JSONObject());
    }
    
    public GraphDistance(Graph g, Mode m) {
        this(g);
        setMode(m);
    }
    
    public JSONObject match(ASTQuery ast) {
        return match(ast, DISTANCE);
    }
   
    public JSONObject match(ASTQuery ast, int distance) {
        setNsm(ast.getNSM());

        for (Constant node : ast.getConstantNodeList()) {            
            if (node.isURI() && getGraph().getNode(node.getDatatypeValue()) == null) {
                match(getGraph().getNodes(), node.getDatatypeValue(), distance);
            }                        
        }

        for (Constant name : ast.getPredicateList()) {
            if (getGraph().getPropertyNode(name.getLabel()) == null) {
                match(getGraph().getProperties(), name.getDatatypeValue(), distance);
            }
        }
                
        for (Constant name : ast.getConstantGraphList()) {
            if (getGraph().getGraphNode(name.getLabel()) == null) {
                match(getGraph().getGraphNodes(), name.getDatatypeValue(), distance);
            }
        }
        
        return getJson();
    }
    
    
    void match(Iterable<Node> it, IDatatype dt, int distance) {
        String label = dt.getLabel();
        String name  = getNsm().nstrip(label);

        int minLabel = Integer.MAX_VALUE;
        double minName  = Double.MAX_VALUE;
        String closeLabel = label;
        String closeName  = label;
        
        for (var node : it) {
            int dist = urlDistance(label, node.getLabel());
            
            if (dist == 0) {
                return;
            }
            
            if (dist < minLabel) {
                minLabel = dist;
                closeLabel = node.getLabel();
            }
            
            String name2 = getNsm().nstrip(node.getLabel());
            double dist2 = nameDistance(name, name2);
            
            if (dist2 < minName) {
                minName = dist2;
                closeName = node.getLabel();
            } 
        }
        
        switch (getMode()) {
            case NAME: 
                if (minName <= distance) {
                    getJson().put(label, closeName);
                }
                break;
                
            case URI:
                if (minLabel <= distance) {
                    getJson().put(label, closeLabel);
                }
                break;
                
            case DEFAULT:
                if (minName < minLabel) {
                    getJson().put(label, closeName);
                } 
                else if (!closeLabel.equals(label)) {
                    getJson().put(label, closeLabel);
                }                                                
        }              
    }

    public int distance (String l1, String l2) {
        return LevenshteinDistance.getDefaultInstance().apply(l1, l2);
    }  

    // levenshtein distance
    public int urlDistance (String l1, String l2) {
        return distance(l1, l2);
    } 
    
    boolean containWithoutCase(String l1, String l2) {
        return containWithCase(l1.toLowerCase(), l2.toLowerCase());
    }
    
    boolean containWithCase(String l1, String l2) {
        return l1.contains(l2) || l2.contains(l1);
    }
    
    // ameliorated levenshtein distance
    public double nameDistance (String l1, String l2) {
        if (l1.equals(l2)) {
            return 0;
        }
        // same name without case: better than any other distance
        if (l1.toLowerCase().equals(l2.toLowerCase())) {
            return 0.3;
        }
        // distance when one name contain other name is less than
        // same distance when no one contain the other
        // prefLabel: label considered better than prepare 
        if (containWithoutCase(l1, l2)) {
            return distance(l1.toLowerCase(), l2.toLowerCase()) - 0.3;
        }
        return distance(l1, l2);
    } 
    
    
   
    public JSONObject cardinality(ASTQuery ast) {
        JSONObject json = new JSONObject();
        
        for (Constant name : ast.getPredicateList()) {
            Node pred = getGraph().getPropertyNode(name.getLabel());
            if (pred == null) {
                json.put(name.getLabel(), 0);
            }
            else {
                json.put(name.getLabel(), getGraph().getIndex().size(pred));
            }
        }
        
        return json;
    }
    

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public NSManager getNsm() {
        return nsm;
    }

    public void setNsm(NSManager nsm) {
        this.nsm = nsm;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
}
