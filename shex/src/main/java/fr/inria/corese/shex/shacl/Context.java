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
    private boolean disjoint = false;
    
    
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
        if (isInOneOfLoop() && isOneOf()) {
            // current triple has no card because card is card of surrounding oneOf 
            return 1;
        }
        return getCardinality().min;
    }
    
    int getMax() {
        if (getRepeatedExpr() == null) {
            return 1;
        }
        if (isInOneOfLoop() && isOneOf()) {
            // current triple has no card because card is card of surrounding oneOf 
            return 1;
        }
        return getCardinality().max;
    }
    
//    int getMinOld() {
//        if (getRepeatedExpr() == null) {
//            return 1;
//        }
//        if (isInOneOfLoop()) {
//            if (isExpCardinality()) {
//                if (isOneOf()) {
//                    // current triple has no card because card is card of surrounding oneOf 
//                    return 1;
//                } // else current constraint has min; return real min below
//            } else {
//                return 0;
//            }
//        }
//        
//        return getCardinality().min;
//    }
//    
//    int getMaxOld() {
//        if (getRepeatedExpr() == null) {
//            return 1;
//        }
//        if (isInOneOfLoop()) {
//            if (isExpCardinality()) {
//                if (isOneOf()) {
//                    // current triple has no card because card is card of surrounding oneOf 
//                    return 1;
//                }
//                // else current constraint has max; return real max below
//            } else {
//                return 1;
//            }
//        }
//
//        return getCardinality().max;
//    }
    
    public boolean isLoop() {
        return getCardinality() != null
                && (getCardinality() == Interval.STAR
                ||  getCardinality() == Interval.PLUS);
    }

    
    // (A|B)*
    public boolean isStar() {
        return getCardinality() != null
                && getCardinality() == Interval.STAR;
    }
    
    public boolean isPositive() {
        return getCardinality() != null
                && (getCardinality() == Interval.PLUS ||
                getCardinality().min>0
                );
    }
    
    // (A|B)*
    public boolean isPlus() {
        return getCardinality() != null
                && getCardinality() == Interval.PLUS;
    }
    
    // (A|B)*
    public boolean isOneOf() {
        return getRepeatedExpr() != null && 
                getRepeatedExpr().getSubExpression() instanceof OneOf;
    }
    
    Interval getCardinality() {
        if (getRepeatedExpr() == null) {
            return null;
        }
        return getRepeatedExpr().getCardinality();
    }
    
    boolean hasCardinality() {
        return getRepeatedExpr() != null;
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
    public boolean isExpCardinality() {
        return optional;
    }

    /**
     * @param optional the optional to set
     */
    public void setExpCardinality(boolean optional) {
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
    public Context setInOneOfLoop(boolean inOneOfStar) {
        this.inOneOfLoop = inOneOfStar;
        return this;
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

    /**
     * @return the disjoint
     */
    public boolean isDisjoint() {
        return disjoint;
    }

    /**
     * @param disjoint the disjoint to set
     */
    public void setDisjoint(boolean disjoint) {
        this.disjoint = disjoint;
    }
    
}
