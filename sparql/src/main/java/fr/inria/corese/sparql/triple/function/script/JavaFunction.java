/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Context;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class JavaFunction extends TermEval {

    JavaFunction() {}

    JavaFunction(String name) {
        super(name);
    }
    
    boolean reject(Binding b, Environment env) {
        //Context c  = (Context) env.getQuery().getContext();
        //Access.reject(Feature.READ_WRITE_JAVA, c.getLevel());
        return Access.reject(Feature.READ_WRITE_JAVA);
    }
    
}
