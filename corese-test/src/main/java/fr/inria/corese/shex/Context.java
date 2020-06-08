package fr.inria.corese.shex;

import fr.inria.lille.shexjava.schema.abstrsynt.RepeatedTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;

/**
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class Context {
    
    private ShapeExpr exp;
    private TripleExpr tripleExp;
    
    Context(ShapeExpr e) {
        exp = e;
    }
    
    Context(TripleExpr e) {
        tripleExp = e;
    }
    
    RepeatedTripleExpression getRepeatedTripleExp() {
        if (getTripleExp() == null ||  !(getTripleExp() instanceof RepeatedTripleExpression)) {
            return null;
        }
        return (RepeatedTripleExpression) getTripleExp();
    }

    /**
     * @return the exp
     */
    public ShapeExpr getExp() {
        return exp;
    }

    /**
     * @param exp the exp to set
     */
    public void setExp(ShapeExpr exp) {
        this.exp = exp;
    }

    /**
     * @return the tripleExp
     */
    public TripleExpr getTripleExp() {
        return tripleExp;
    }

    /**
     * @param tripleExp the tripleExp to set
     */
    public void setTripleExp(TripleExpr tripleExp) {
        this.tripleExp = tripleExp;
    }
    
}
