package fr.inria.corese.kgram.api.core;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * Interface for Producer iterator that encapsulate Edge or Node with its Graph
 * Node
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Edge extends Pointerable {

    // rdf star reference node index: index of t = 2 in tuple(s p o t)
    int REF_INDEX = 2;

    // nb nodes to consider in sparql query processing
    default int nbNode() {
        return 2;
    }
    
    // nb nodes to consider in graph index
    // edge triple node (g, t = (s p o)) has 2 nodes s,o for index and 3 nodes s,o,t for sparql
    default int nbNodeIndex() {
        return nbNode();
    }

    /**
     * nodes that are vertex of the graph use case: metadata node is not a graph
     * vertex
     */
    default int nbGraphNode() {
        return nbNode();
    }
    
    Node getNode(int i);

    default void setNode(int i, Node n) {}

   
    default Node getEdgeNode() {
        return getProperty();
    }
    
    default Node getEdgeVariable() {
        return null;
    }

    // edge variable or edge node
    Node getProperty();

    default void setProperty(Node node) {}
    
    
    /**
     * Is node returned by getNode()
     *
     * @param n
     * @return
     */
    default boolean contains(Node node) {return false;}

   
    String getEdgeLabel();

   
    default int getEdgeIndex() { return -1;}
    default void setEdgeIndex(int n) {}

    // manage access right
    default byte getLevel() {
        return -1;
    }

    default Edge setLevel(byte b) {
        return this;
    }

    
    // use case: internal index edge
    default boolean isInternal() {
        return nbNode() == 2 && ! isTripleNode();
    }

    @Override
    Node getNode();

    Node getGraph();

    default void setGraph(Node n) {
    }

    @Override
    Edge getEdge();

    default Object getProvenance() { return null;};

    default void setProvenance(Object obj) {}

    default boolean isMatchArity() {
        return false;
    }

    // nested rdf star triple <<s p o>>
    default boolean isNested() {
        return false;
    }

    default boolean isAsserted() {
        return !isNested();
    }

    default void setNested(boolean b) {
    }

    default void setAsserted(boolean b) {
        setNested(!b);
    }
    
    // edge created as nested triple expression
    // bind (<<s p o>> as ?t)
    // values ?t { <<s p o>> }
    default boolean isCreated() {
        return false;
    }
    
    default void setCreated(boolean b) {
    }

    default Node getGraphNode() {
        return getGraph();
    }

    default Node getSubjectNode() {
        return getNode(0);
    }

    default Node getPropertyNode() {
        return getProperty();
    }

    default Node getObjectNode() {
        return getNode(1);
    }

    default IDatatype getGraphValue() {
        Node node = getGraph();
        if (node == null) {
            return null;
        }
        return node.getDatatypeValue();
    }

    default IDatatype getSubjectValue() {
        return getNode(0).getDatatypeValue();
    }

    default IDatatype getPropertyValue() {
        return getPredicateValue();
    }

    default IDatatype getPredicateValue() {
        if (getProperty() == null) {
            return null;
        }
        return getProperty().getDatatypeValue();
    }

    default IDatatype getObjectValue() {
        return getNode(1).getDatatypeValue();
    }

    // for rdf star only
    default boolean hasReferenceNode() {
        return nbNode() > REF_INDEX && getReferenceNode().getDatatypeValue().isTriple();
    }
    
    default Node getReferenceNode() {
        if (nbNode() <= REF_INDEX) {
            return null;
        }
        return getNode(REF_INDEX);
    }
    
    default void setReferenceNode(Node node) {
        setNode(REF_INDEX, node);
    }
    
    default boolean isTripleNode() {
        return false;
    }
    
    default Node getTripleNode() {
        return null;
    }
    
    default void setTripleNode(Node node) {}

    default boolean sameTerm(Edge e) {
        return sameTermWithoutGraph(e)
                && (getGraphValue() == null || e.getGraphValue() == null
                ? getGraphValue() == e.getGraphValue()
                : getGraphValue().sameTerm(e.getGraphValue()));
    }
    
    default boolean sameTermWithoutGraph(Edge e) {
        return getSubjectValue().sameTerm(e.getSubjectValue())
                && getPredicateValue().sameTerm(e.getPredicateValue())
                && getObjectValue().sameTerm(e.getObjectValue());
    }

    default boolean equals(Edge e) {
        return equalsWithoutGraph(e)
                && (getGraphValue() == null || e.getGraphValue() == null
                ? getGraphValue() == e.getGraphValue()
                : getGraphValue().equals(e.getGraphValue()));
    }
    
    default boolean equalsWithoutGraph(Edge e) {
        return getObjectValue().equals(e.getObjectValue())
                && getSubjectValue().equals(e.getSubjectValue())
                && getPredicateValue().equals(e.getPredicateValue());
    }
    
    default int compareWithoutGraph(Edge e) throws CoreseDatatypeException {
        int res = getSubjectValue().compare(e.getSubjectValue());
        if (res == 0) {
            res = getPredicateValue().compare(e.getPredicateValue());
        }
        if (res == 0) {
            res = getObjectValue().compare(e.getObjectValue());
        }
        return res;
    }
    
}
