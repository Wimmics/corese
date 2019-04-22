package fr.inria.corese.compiler.parser;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.TripleStore;
import fr.inria.corese.kgram.path.Path;

public class NodeImpl implements Node {

    /**
     * @return the matchCardinality
     */
    public boolean isMatchCardinality() {
        return matchCardinality;
    }

    /**
     * @param matchCardinality the matchCardinality to set
     */
    public void setMatchCardinality(boolean matchCardinality) {
        this.matchCardinality = matchCardinality;
    }

    /**
     * @return the matchNodeList
     */
    @Override
    public boolean isMatchNodeList() {
        return matchNodeList;
    }

    /**
     * @param matchNodeList the matchNodeList to set
     */
    public void setMatchNodeList(boolean matchNodeList) {
        this.matchNodeList = matchNodeList;
    }

    Atom atom;
    int index = -1;
    private boolean matchNodeList = false;
    private boolean matchCardinality = false;

    public NodeImpl(Atom at) {
        atom = at;
    }

    public static NodeImpl createNode(Atom at) {
        return new NodeImpl(at);
    }

    public static NodeImpl createVariable(String name) {
        return new NodeImpl(Variable.create(name));
    }

    public static NodeImpl createResource(String name) {
        return new NodeImpl(Constant.create(name));
    }

    public static NodeImpl createConstant(String name) {
        return new NodeImpl(Constant.create(name, RDFS.xsdstring));
    }

    public static NodeImpl createConstant(String name, String datatype) {
        return new NodeImpl(Constant.create(name, datatype));
    }

    public static NodeImpl createConstant(String name, String datatype, String lang) {
        return new NodeImpl(Constant.create(name, null, lang));
    }

    public Atom getAtom() {
        return atom;
    }

    @Override
    public IDatatype getValue() {
        return atom.getDatatypeValue();
    }
    
    public IDatatype getValue(Node n) {
        return (IDatatype) n.getValue();    
    }

    @Override
    public IDatatype getDatatypeValue() {
        return atom.getDatatypeValue();
    }
    
    @Override
    public void setDatatypeValue(DatatypeValue dt) {
        if (dt instanceof IDatatype) {
            setDatatypeValue((IDatatype)dt);
        }
    }
    
    public void setDatatypeValue(IDatatype dt) {
        atom = Constant.create(dt);
    }
    
    @Override
    public Node getGraph() {
        return null;
    }
    
    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public String toString() {
        return atom.toSparql(); // + "[" + getIndex() +"]";
    }

    @Override
    public int compare(Node node) {
        if (node.getValue() instanceof IDatatype) {
            return getValue().compareTo(getValue(node));
        }
        return getLabel().compareTo(node.getLabel());
    }

    @Override
    public int getIndex() {
        // TODO Auto-generated method stub
        return index;
    }

    @Override
    public String getLabel() {
        // TODO Auto-generated method stub
        if (atom.isResource()) {
            return atom.getLongName();
        }
        return atom.getName();
    }

    @Override
    public boolean isConstant() {
        // TODO Auto-generated method stub
        return atom.isConstant();
    }

    @Override
    public boolean isVariable() {
        // TODO Auto-generated method stub
        return atom.isVariable(); //&& ! atom.getVariable().isBlankNode();
    }

    @Override
    public boolean isBlank() {
        // TODO Auto-generated method stub
        return atom.isBlank() || (isVariable() && atom.getVariable().isBlankNode());
    }

    @Override
    public boolean isFuture() {
        return isConstant() && atom.getConstant().getDatatypeValue().isFuture();
    }

    @Override
    public boolean same(Node n) {
        if (isVariable() || n.isVariable()) {
            return sameVariable(n);
        }       
        return getValue().sameTerm(getValue(n));
        //return compare(n) == 0;
    }
    
    boolean sameVariable(Node n){      
        return isVariable() && n.isVariable() && getLabel().equals(n.getLabel());
    }

    @Override
    public boolean match(Node n) {
       if (isVariable() || n.isVariable()) {
            return sameVariable(n);
        }
        return getValue().match(getValue(n));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            return equals((Node) o); // was same
        }
        return false;
    }
    
     public boolean equals(Node n) {
        if (isVariable() || n.isVariable()) {
            return sameVariable(n);
        }
        return getValue().equals(getValue(n));
     }

    @Override
    public void setIndex(int n) {
        // TODO Auto-generated method stub
        index = n;
    }

    @Override
    public Object getObject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setObject(Object o) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public Path getPath() {       
        return atom.getDatatypeValue().getPath();
    }

    @Override
    public Object getProperty(int p) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProperty(int p, Object o) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getKey() {
        return INITKEY;
    }

    @Override
    public void setKey(String str) {
    }

    @Override
    public TripleStore getTripleStore() {
        return null;
    }
}
