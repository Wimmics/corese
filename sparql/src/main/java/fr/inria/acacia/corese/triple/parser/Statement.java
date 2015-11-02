package fr.inria.acacia.corese.triple.parser;

/**
 *
 * Toplevel of extended statements:
 * Function Let Loop
 * IfElse
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Statement  extends Term {
    
    Statement(){}
    
    Statement(String name, Expression fun, Expression body){
        super(name, fun, body);
        setFunction(true);
    }
}
