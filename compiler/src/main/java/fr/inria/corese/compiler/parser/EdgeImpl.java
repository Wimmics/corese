package fr.inria.corese.compiler.parser;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.ArrayList;

import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.core.PointerObject;

public class EdgeImpl extends PointerObject implements Edge {

    public static String TOP = RDFS.RootPropertyURI;
    ArrayList<Node> nodes;
    Node edgeNode, edgeVariable, mySelf;
    String label;
    Triple triple;
    int index = -1;
    boolean matchArity = false;

    public EdgeImpl() {
        this(TOP);
    }

    public EdgeImpl(String label) {
        this.label = label;
        nodes = new ArrayList<Node>();
    }

    public EdgeImpl(Triple t) {
        label = t.getProperty().getLongName();
        triple = t;
        nodes = new ArrayList<Node>();
        setMatchArity(t.isMatchArity());
    }

    public static EdgeImpl create(String label, Node sub, Node obj) {
        EdgeImpl edge = new EdgeImpl(label);
        edge.add(sub);
        edge.add(obj);
        return edge;
    }

    public static EdgeImpl create(Node prop, Node sub, Node obj) {
        String name = TOP;
        if (prop.isConstant()) {
            name = prop.getLabel();
        }
        EdgeImpl edge = new EdgeImpl(name);
        edge.add(sub);
        edge.add(obj);
        if (prop.isVariable()) {
            //edge.setEdgeNode(prop);
            edge.setEdgeVariable(prop);
        } else {
            edge.setEdgeNode(prop);
        }
        return edge;
    }

    @Override
    public String toString() {
        String str = "";
        String name = label;
        if (getEdgeVariable() != null) {
            name = getEdgeVariable().toString();
        } else if (triple != null) {
            name = triple.getProperty().getName();
        }
        str += getNode(0) + " " + name;
        for (int i = 1; i < nodes.size(); i++) {
            str += " " + getNode(i);
        }
        return str;
    }

    @Override
    public Iterable<DatatypeValue> getLoop() {
        ArrayList<DatatypeValue> list = new ArrayList();
        for (int i = 0; i <= nodes.size(); i++) {
            list.add(getValue(null, i));
        }
        return list;
    }
    
    @Override
    public String getDatatypeLabel() {
        return toString();
    }

    @Override
    public DatatypeValue getValue(String var, int n) {
        switch (n) {
            case 0:
                return getNode(0).getDatatypeValue();
            case 1:
                return getPredicate().getDatatypeValue();
            default:
                if (n <= nodes.size()) {
                    return getNode(n - 1).getDatatypeValue();
                }
        }
        return null;
    }
    
    @Override
    public boolean isMatchArity() {
        return matchArity;
    }
    
    public void setMatchArity(boolean b) {
        matchArity = b;
    }

    public Node getPredicateNode() {
        Node var = getEdgeVariable();
        return (var == null) ? getEdgeNode() : var;
    }

    public Triple getTriple() {
        if (triple == null) {
            triple = triple();
        }
        return triple;
    }

    Triple triple() {
        Atom subject = ((NodeImpl) nodes.get(0)).getAtom();
        Atom object = ((NodeImpl) nodes.get(1)).getAtom();
        Constant property = getName();
        Variable variable = getVariable();
        Triple triple = Triple.create(subject, property, variable, object);
        return triple;
    }

    Constant getName() {
        Atom name;
        if (edgeNode != null) {
            name = ((NodeImpl) edgeNode).getAtom();
            return name.getConstant();
        }
        return Constant.create(label);
    }

    Variable getVariable() {
        if (edgeVariable != null) {
            return ((NodeImpl) edgeVariable).getAtom().getVariable();
        }
        return null;
    }

    public void add(Node node) {
        nodes.add(node);
    }

    /**
     *
     * Query edge node is stored only if it is a variable otherwise it is
     * useless and may lead to a pb when match subproperty
     */
    public void setEdgeNode(Node n) {
        edgeNode = n;
    }

    @Override
    public boolean contains(Node n) {
        // TODO Auto-generated method stub
        return nodes.contains(n);
    }

    @Override
    public Node getEdgeNode() {
        // TODO Auto-generated method stub
        return edgeNode;
    }

    @Override
    public Node getEdgeVariable() {
        // TODO Auto-generated method stub
        return edgeVariable;
    }

    @Override
    public Node getPredicate() {
        if (edgeVariable == null) {
            return edgeNode;
        }
        return edgeVariable;
    }

    public void setEdgeVariable(Node n) {
        edgeVariable = n;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Node getNode(int n) {
        return nodes.get(n);
    }

    @Override
    public int nbNode() {
        return nodes.size();
    }

    @Override
    public int nbGraphNode() {
        return nodes.size();
    }

    @Override
    public void setIndex(int n) {
        index = n;
    }

    @Override
    public Edge getEdge() {
        return this;
    }

    @Override
    public Node getGraph() {
        return null;
    }

    @Override
    public Node getNode() {
        if (mySelf == null) {
            mySelf = DatatypeMap.createObject(this.toString(), this);
        }
        return mySelf;
    }

    @Override
    public Object getProvenance() {
        return null;
    }

    public void setProvenance(Object obj) {

    }

    @Override
    public PointerType pointerType() {
        return PointerType.TRIPLE;
    }

}
