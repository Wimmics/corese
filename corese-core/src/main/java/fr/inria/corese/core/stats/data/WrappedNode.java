package fr.inria.corese.core.stats.data;

import fr.inria.corese.kgram.api.core.Node;

/**
 * mainly for overriding the hashCode and equals methods of Node, same object if
 * with same label, that is all
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 4 juil. 2014
 */
public class WrappedNode {

    private Node node = null;
    private String label = null;

    public WrappedNode(Node n) {
        this.node = n;
    }

    public WrappedNode(String label) {
        this.label = label;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.node != null ? this.node.getLabel().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WrappedNode other = (WrappedNode) obj;
        if (this.node != other.node && (this.node == null || !this.node.getLabel().equals(other.node.getLabel()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return node==null?label:node.toString();
    }
}
