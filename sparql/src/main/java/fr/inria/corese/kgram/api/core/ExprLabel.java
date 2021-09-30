package fr.inria.corese.kgram.api.core;

/**
 *
 * @author corby
 */
public interface ExprLabel {
    
    public static final String SEPARATOR= "#";
    
    public static final String EQUAL        = "equal";
    public static final String DIFF         = "diff";
    public static final String LESS         = "less";
    public static final String LESS_EQUAL   = "lessEqual";
    public static final String GREATER      = "greater";
    public static final String GREATER_EQUAL= "greaterEqual";
    
    public static final String PLUS  = "plus";
    public static final String MINUS = "minus";
    public static final String MULT  = "mult";
    public static final String DIV   = "divis";
    
    public static final String COMPARE   = ExpType.KGRAM + "compare";
           
}
