package fr.inria.corese.shex.shacl;

import fr.inria.lille.shexjava.schema.abstrsynt.NodeConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.OneOf;
import fr.inria.lille.shexjava.schema.abstrsynt.RepeatedTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.Shape;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeNot;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;
import fr.inria.lille.shexjava.util.Interval;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class Context {
    // cardinality: min max
    private RepeatedTripleExpression repeatedExpr;
    // (A|B) *
    private Interval oneOfCardinality;
    // top level constraints: ex:test URI pattern { exp }
    private List<NodeConstraint> nodeConstraintList;
    // process sh:qualifiedValueShape [ qualifiedExpr ]
    private ShapeExpr qualifiedExpr;
    // top level NOT: ex:test NOT { exp }
    private ShapeNot notExpr;
    // Embedding shape to get EXTRA if any
    private Shape shape;  
    // qualified expr => sh:qualifiedValueShape
    private HashMap<TripleExpr, Boolean> qualified;
    private HashMap<String, String> forward;
    private HashMap<String, String> backward;
    private boolean optional = false;
    private boolean inOneOfLoop = false;
    
    
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
        if (isInOneOfLoop() && ! isOptional()) {
            // (A|B)* -> A? B?
            return 0;
        }
        return getRepeatedExpr().getCardinality().min;
    }
    
    int getMax() {
        if (getRepeatedExpr() == null) {
            return 1;
        }
        if (isInOneOfLoop()) {
            if (isOptional()) {
                // (A|B)* -> A? B?
                if (isLoop()) {
                    return 1;
                } // else return real max below
            }
            else {
                 // (A|B)* -> A? B?
                return 1;
            }
        }
        return getRepeatedExpr().getCardinality().max;
    }
    
//    public boolean isOneOfStar() {
//        return getRepeatedExpr() != null
//                && getRepeatedExpr().getSubExpression() instanceof OneOf
//                && getRepeatedExpr().getCardinality() == Interval.STAR;
//    }
//    
    
    public boolean isLoop() {
        return getRepeatedExpr() != null
                && (getRepeatedExpr().getCardinality() == Interval.STAR
                ||  getRepeatedExpr().getCardinality() == Interval.PLUS);
    }

    
    // (A|B)*
    public boolean isStar() {
        return getRepeatedExpr() != null
                && getRepeatedExpr().getCardinality() == Interval.STAR;
    }
    
    // (A|B)*
    public boolean isPlus() {
        return getRepeatedExpr() != null
                && getRepeatedExpr().getCardinality() == Interval.PLUS;
    }
    
    // (A|B)*
    public boolean isOneOf() {
        return getRepeatedExpr() != null && 
                getRepeatedExpr().getSubExpression() instanceof OneOf;
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

    /**
     * @return the notExpr
     */
    public ShapeNot getNotExpr() {
        return notExpr;
    }

    /**
     * @param notExpr the notExpr to set
     */
    public Context setNotExpr(ShapeNot notExpr) {
        this.notExpr = notExpr;
        return this;
    }

    /**
     * @return the oneOfInterval
     */
    public Interval getOneOfCardinality() {
        return oneOfCardinality;
    }

    /**
     * @param oneOfInterval the oneOfInterval to set
     */
    public void setOneOfCardinality(Interval oneOfCardinality) {
        this.oneOfCardinality = oneOfCardinality;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @param optional the optional to set
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * @return the inOneOfStar
     */
    public boolean isInOneOfLoop() {
        return inOneOfLoop;
    }

    /**
     * @param inOneOfStar the inOneOfStar to set
     */
    public void setInOneOfLoop(boolean inOneOfStar) {
        this.inOneOfLoop = inOneOfStar;
    }

    /**
     * @return the forward
     */
    public HashMap<String, String> getForward() {
        return forward;
    }

    /**
     * @param forward the forward to set
     */
    public void setForward(HashMap<String, String> forward) {
        this.forward = forward;
    }

    /**
     * @return the backward
     */
    public HashMap<String, String> getBackward() {
        return backward;
    }

    /**
     * @param backward the backward to set
     */
    public void setBackward(HashMap<String, String> backward) {
        this.backward = backward;
    }
    
}
