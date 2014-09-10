package fr.inria.edelweiss.kgram.sorter.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic triple pattern edge used to connect two triple pattern nodes
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 27 juin 2014
 */
public class BPGEdge {

    private BPGNode n1;
    private BPGNode n2;
    private double weight = -1;
    private boolean directed = false;
    private List<String> variables = null;

    public BPGEdge(BPGNode n1, BPGNode n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    public BPGEdge(BPGNode n1, BPGNode n2, boolean directed) {
        this(n1, n2);
        this.directed = directed;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public List<String> getVariables() {
        return variables;
    }

    
    /**
     * Get one of the nodes in the edge
     * 
     * @param in
     * @return 
     */
    public BPGNode get(int in) {
        return in == 0 ? n1 : (in == 1 ? n2 : null);
    }

    /**
     * Get the other node in the edge
     * 
     * @param n
     * @return 
     */
    public BPGNode get(BPGNode n) {
        return n1.equals(n) ? n2 : n1;
    }

    /**
     * Get the two nodes in a list
     * 
     * @return 
     */
    public List<BPGNode> getNodes() {
        List l = new ArrayList();
        l.add(n1);
        l.add(n2);
        return l;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    @Override
    public String toString() {
        return "{ " + n1 + ", " + n2 + ", " + weight + ", " + directed + '}';
    }
}
