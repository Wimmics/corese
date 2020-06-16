package fr.inria.corese.shex.shacl;

import fr.inria.lille.shexjava.schema.abstrsynt.NodeConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.RepeatedTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.Shape;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class Context {
    
    private RepeatedTripleExpression repeatedExpr;
    private List<NodeConstraint> nodeConstraintList;
    // process sh:qualifiedValueShape [ qualifiedExpr ]
    private ShapeExpr qualifiedExpr;
    // Embedding shape to get EXTRA if any
    private Shape shape;  
    // qualified expr => sh:qualifiedValueShape
    private HashMap<TripleExpr, Boolean> qualified;
    
    
    Context(RepeatedTripleExpression e) {
        setRepeatedExpr((RepeatedTripleExpression) e);
    }
    
    Context(NodeConstraint... cst) {
          this(Arrays.asList(cst));
    }
    
    Context(List<NodeConstraint> list) {
        setNodeConstraintList(list);
    }
    
    boolean isQualified(TripleExpr exp) {
        if (getQualified() == null) {
            return false;
        }
        Boolean b = getQualified().get(exp);
        return b != null && b;
    }
    
    HashMap<TripleExpr, Boolean> qualified() {
        if (getQualified() == null) {
            setQualified(new HashMap<>());
        }
        return getQualified();
    }
    
    void qualify(TripleExpr exp) {
        qualified().put(exp, true);
    }
    
    int getMin() {
        if (getRepeatedExpr() == null) {
            return 1;
        }
        return getRepeatedExpr().getCardinality().min;
    }
    
    int getMax() {
        if (getRepeatedExpr() == null) {
            return 1;
        }
        return getRepeatedExpr().getCardinality().max;
    }

    /**
     * @return the repeatedExpr
     */
    public RepeatedTripleExpression getRepeatedExpr() {
        return repeatedExpr;
    }

    /**
     * @param repeatedExpr the repeatedExpr to set
     */
    public Context setRepeatedExpr(RepeatedTripleExpression repeatedExpr) {
        this.repeatedExpr = repeatedExpr;
        return this;
    }

    /**
     * @return the nodeConstraintList
     */
    public List<NodeConstraint> getNodeConstraintList() {
        return nodeConstraintList;
    }

    /**
     * @param nodeConstraintList the nodeConstraintList to set
     */
    public void setNodeConstraintList(List<NodeConstraint> nodeConstraintList) {
        this.nodeConstraintList = nodeConstraintList;
    }

    /**
     * @return the qualifiedExpr
     */
    public ShapeExpr getQualifiedExpr() {
        return qualifiedExpr;
    }

    /**
     * @param qualifiedExpr the qualifiedExpr to set
     */
    public Context setQualifiedExpr(ShapeExpr qualifiedExpr) {
        this.qualifiedExpr = qualifiedExpr;
        return this;
    }
    
    boolean isQualified() {
        return getQualifiedExpr() != null;
    }

    /**
     * @return the shape
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * @param shape the shape to set
     */
    public Context setShape(Shape shape) {
        this.shape = shape;
        return this;
    }

    /**
     * @return the qualified
     */
    public HashMap<TripleExpr, Boolean> getQualified() {
        return qualified;
    }

    /**
     * @param qualified the qualified to set
     */
    public void setQualified(HashMap<TripleExpr, Boolean> qualified) {
        this.qualified = qualified;
    }
    
}
