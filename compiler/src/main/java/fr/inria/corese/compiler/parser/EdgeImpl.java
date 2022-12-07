package fr.inria.corese.compiler.parser;

import java.util.ArrayList;
import java.util.Objects;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.core.PointerObject;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;

public class EdgeImpl extends PointerObject implements Edge {

    public static String TOP = RDFS.RootPropertyURI;
    ArrayList<Node> nodes;
    Node edgeNode, edgeVariable, mySelf;
    String label;
    Triple triple;
    int index = -1;
    boolean matchArity = false;
    private boolean nested = false;
    // true for values ?t { <<s p o>> } see CompilerKgram
    private boolean created = false;

    public EdgeImpl() {
        this(TOP);
    }

    public EdgeImpl(String label) {
        this.label = label;
        nodes = new ArrayList<>();
    }

    public EdgeImpl(Triple t) {
        label = t.getProperty().getLongName();
        triple = t;
        nodes = new ArrayList<>();
        setNested(t.isNested());
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
            // edge.setEdgeNode(prop);
            edge.setEdgeVariable(prop);
        } else {
            edge.setEdgeNode(prop);
        }
        return edge;
    }

    @Override
    public String toString() {
        if (isNested()) {
            return nestedTriple();
        }
        return basicTriple();
    }

    String basicTriple() {    
        String str = "";
        String name = label;
        
        if (getTriple()!=null && getTriple().getRegex()!=null) {
            name = getTriple().getRegex().toString();
        }
        else if (getEdgeVariable() != null) {
            name = getEdgeVariable().toString();
        } else if (getTriple() != null) {
            name = getTriple().getProperty().getName();
        }
        
        str += getNode(0);        
        str += " " + name;
        for (int i = 1; i < nodes.size(); i++) {
            str += " " + getNode(i);           
        }
        return str;
    }
    
    String nestedTriple() {
        return nestedTripleBasic();
    }
    
    String nestedTripleDebug() {
        String str = String.format("<<%s[%s] %s %s[%s]>> [%s]", 
                getNode(0), getNode(0).isVariable()?getNode(0).getLabel():"",
                getPredicateNode(), 
                getNode(1), getNode(1).isVariable()?getNode(1).getLabel():"",
                getNode(Edge.REF_INDEX).getLabel());        
        return str;
    }
    
    String nestedTripleBasic() {
        String str = String.format("<<%s %s %s>> [%s]", 
                getNode(0), getPredicateNode(), getNode(1), 
                getNode(Edge.REF_INDEX).getLabel());        
        return str;
    }

    @Override
    public Iterable<IDatatype> getLoop() {
        ArrayList<IDatatype> list = new ArrayList();
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
    public IDatatype getValue(String var, int n) {
        switch (n) {
            case 0:
                return getNode(0).getDatatypeValue();
            case 1:
                return getProperty().getDatatypeValue();
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
     * Query edge node is stored only if it is a variable otherwise it is useless
     * and may lead to a pb when match subproperty
     */
    public void setEdgeNode(Node n) {
        edgeNode = n;
    }

    @Override
    public boolean contains(Node n) {
        return nodes.contains(n);
    }

    @Override
    public Node getEdgeNode() {
        return edgeNode;
    }

    @Override
    public Node getEdgeVariable() {
        return edgeVariable;
    }

    @Override
    public Node getProperty() {
        if (edgeVariable == null) {
            return edgeNode;
        }
        return edgeVariable;
    }

    public void setEdgeVariable(Node n) {
        edgeVariable = n;
    }

    @Override
    public int getEdgeIndex() {
        return index;
    }

    @Override
    public String getEdgeLabel() {
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
    public void setEdgeIndex(int n) {
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

    @Override
    public void setProvenance(Object obj) {

    }

    @Override
    public PointerType pointerType() {
        return PointerType.TRIPLE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSubjectValue(), this.getPredicateValue(), this.getObjectValue(), this.getGraphValue());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EdgeImpl)) {
            return false;
        }

        EdgeImpl other = (EdgeImpl) o;

        return Objects.equals(this.getSubjectValue(), other.getSubjectValue())
                && Objects.equals(this.getPredicateValue(), other.getPredicateValue())
                && Objects.equals(this.getObjectValue(), other.getObjectValue())
                && Objects.equals(this.getGraphValue(), other.getGraphValue());
    }

    @Override
    public boolean isNested() {
        return nested;
    }

    @Override
    public void setNested(boolean nested) {
        this.nested = nested;
    }

    @Override
    public boolean isCreated() {
        return created;
    }

    @Override
    public void setCreated(boolean created) {
        this.created = created;
    }

}
