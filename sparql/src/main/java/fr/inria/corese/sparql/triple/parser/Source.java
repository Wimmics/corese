package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.cst.KeywordPP;

/**
 * <p>
 * Title: Corese</p>
 * <p>
 * Description: A Semantic Search Engine</p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Acacia</p>
 * <br>
 * This class implements graph ?src { PATTERN }
 * <br>
 *
 * @author Olivier Corby
 */
public class Source extends SourceExp {

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;

    boolean state = false;
    boolean leaf = false;
    private boolean isRec = false;

    public Source() {
    }

    /**
     * Model the scope of graph ?src { pattern }
     *
     */
    public Source(Atom src, Exp exp) {
        super(src, exp);
    }

    public static Source create(Atom src, Exp exp) {
        if (!exp.isAnd()) {
            exp = new BasicGraphPattern(exp);
        }
        Source s = new Source(src, exp);
        return s;
    }

    @Override
    public Source copy() {
        Source exp = super.copy().getNamedGraph();
        exp.setSource(getSource());
        return exp;
    }

    @Override
    public Source getNamedGraph() {
        return this;
    }

    @Override
    public Exp getBodyExp() {
        return get(0);
    }

    public void setState(boolean s) {
        state = s;
    }

    public void setLeaf(boolean s) {
        leaf = s;
    }

    boolean isState() {
        return state;
    }

    @Override
    public boolean isGraph() {
        return true;
    }

    @Override
    public void setRec(boolean b) {
        isRec = b;
    }

    public boolean isRec() {
        return isRec;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        sb.append(KeywordPP.GRAPH + KeywordPP.SPACE);
        sb.append(getSource());
        sb.append(KeywordPP.SPACE);
        getBodyExp().pretty(sb);
        return sb;
    }

    @Override
    public boolean validateData(ASTQuery ast) {
        if (getSource().isVariable()) {
            return false;
        }

        Exp ee = this;
        if (size() == 1 && get(0) instanceof And) {
            // dive into {}
            ee = get(0);
        }
        
        for (Exp exp : ee.getBody()) {
            if (!(exp.isTriple() && exp.validateData(ast))) {
                return false;
            }
        }
        return true;
    }
    
    public Source merge(Source exp) {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        bgp.addAll(getBodyExp());
        bgp.addAll(exp.getBodyExp());
        return Source.create(getSource(), bgp);
    }

}
