/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;

/**
 *
 * @author corby
 */
public class FunctionDefined extends LDScript {

    public FunctionDefined() {}
    
    public FunctionDefined(String name) {
        super(name);
        setArity(2);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype name = getBasicArg(0).eval(eval, b, env, p);
        IDatatype nb = getBasicArg(1).eval(eval, b, env, p);

        if (name == null || nb == null) {
            return  null;
        }
            
        Function function = getDefine(env, name.stringValue(), nb.intValue());
      
        if (function == null) {
            return FALSE;
        }
        return TRUE;
    }

}