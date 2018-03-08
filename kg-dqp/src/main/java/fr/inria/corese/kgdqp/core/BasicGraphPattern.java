package fr.inria.corese.kgdqp.core;

import fr.inria.corese.kgram.api.core.Edge;
import java.util.ArrayList;

/**
 * An helper class to handle connected basic graph patterns.
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class BasicGraphPattern {

    private ArrayList<String> vars;
    private ArrayList<Edge> edges;
    private boolean processed;

    public BasicGraphPattern() {
        vars = new ArrayList<String>();
        edges = new ArrayList<Edge>();
        processed = false;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public ArrayList<String> getVars() {
        return vars;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BasicGraphPattern other = (BasicGraphPattern) obj;

        for (String oV : other.getVars()) {
            if (!this.getVars().contains(oV)) {
                return false;
            }
        }

        for (Edge oE : other.getEdges()) {
            if (!this.getEdges().contains(oE)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.vars != null ? this.vars.hashCode() : 0);
        hash = 23 * hash + (this.edges != null ? this.edges.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        String sVars = "[";
        for (String s : vars) {
            sVars += s + " ";
        }
        sVars += "]";
        String res = "BasicGraphPattern{" + "vars=" + sVars + ", edges=\n";
        for (Edge e : edges) {
            res += "\t" + e.toString() + "\n";
        }
        res += "}";
        return res;
    }
}
