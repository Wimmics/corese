package fr.inria.corese.sparql.triple.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.exceptions.QuerySemanticException;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import java.util.List;

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
 * This class implements optional graph pattern, it may be recursive:<br>
 * optional ( A B optional ( C D ) )
 * <br>
 *
 * @author Olivier Corby
 */
public class Optional extends Binary {

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(Optional.class);
    // false: OPTION corese (unary)
    // true:  OPTIONAL SPARQL (binary)
    public static boolean isOptional = true;

    static int num = 0;

    public Optional() {
    }

    // PRAGMA: exp is BGP
    public Optional(Exp exp) {
        add(exp);
    }

    public Optional(Exp e1, Exp e2) {
        this(e1);
        add(e2);
    }
    
    public static Optional create(Exp e1, Exp e2) {
        return new Optional(e1, e2);
    }

    public static Optional create(Exp exp) {
        return new Optional(exp);
    }

    /**
     * (and t1 t2 (or (and t3) (and t4)))
     */
    @Override
    Bind validate(Bind env, int n) throws QuerySemanticException {
        return get(0).validate(env, n + 1);
    }

    String getOper() {
        return "option";
    }

    // corese option {}
    @Override
    public boolean isOption() {
        return !isOptional;
    }

    // sparql option {}
    @Override
    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public Optional getOptional() {
        return this;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {        
        toString(eget(0), sb);
        sb.nl().append(KeywordPP.OPTIONAL)
                .append(KeywordPP.SPACE);
        eget(1).pretty(sb);
        return sb;
    }
        
    void basicVariables(VariableScope sort, List<Variable> list) {
        if (size() > 0) {
            get(0).getVariables(sort, list);
        }
    }
    
    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
        switch (sort.getScope()) {
            case SUBSCOPE:  basicVariables(sort, list); break;
            default:  super.getVariables(sort, list); break;
        }
    }



    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
        boolean ok = true;
        for (Exp exp : getBody()) {
            boolean b = exp.validate(ast, exist);
            ok = ok && b;
        }
        return ok;
    }

}
