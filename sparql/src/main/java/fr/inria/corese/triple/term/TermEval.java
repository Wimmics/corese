/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.triple.term;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TermEval extends Term {
    
    public TermEval(String name, Expression e1, Expression e2, Expression e3) {
        super(name, e1, e2);
        add(e3);
    }

    public TermEval(String name, Expression e1, Expression e2) {
        super(name, e1, e2);
    }
    
    public TermEval(String name, Expression e){
        super(name, e);
    }
    
    public TermEval(String name){
        super(name);
    }

    public boolean isTrue(IDatatype dt) {
        try {
            return dt.isTrue();
        } catch (CoreseDatatypeException e) {
            return false;
        }
    }
    
    public IDatatype value(boolean value){
        return (value) ? DatatypeMap.TRUE : DatatypeMap.FALSE;
    }
    
    public IDatatype[] eval(Computer eval, Binding b, Environment env, Producer p, int start) {
        IDatatype[] args = new IDatatype[arity() - start];
        int i = 0;
        for (int j = start; j < arity(); j++) {
            args[i] = getArg(j).eval(eval, b, env, p);
            if (args[i] == null) {                
                return null;
            }
            i++;
        }
        return args;
    }
    
     public boolean isReturn(IDatatype dt){
        return dt == null || DatatypeMap.isResult(dt);
    }
    
}
