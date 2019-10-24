package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.compiler.java.JavaCompiler;

/**
 *
 * Toplevel of extended statements:
 * Function Let Loop
 * IfElse
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Statement  extends LDScript {
    int nbVariable = 0;
    
    public Statement(){}
    
    Statement(String name){
        super(name);
        setFunction(true);
    }
    
    Statement(String name, Expression fun, Expression body){
        super(name, fun, body);
        setFunction(true);
    }
    
    @Override
    public boolean isStatement(){
        return true;
    }
    
    /**
     * @return the nbVariable
     */
    @Override
    public int getNbVariable() {
        return nbVariable;
    }

    /**
     * @param nbVariable the nbVariable to set
     */
    @Override
    public void setNbVariable(int nbVariable) {
        this.nbVariable = nbVariable;
    }
    
    @Override
    public void toJava(JavaCompiler jc, boolean arg){
        jc.toJava(this, arg);
    }
}
