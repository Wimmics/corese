package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Access.Feature;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class JavaFunction extends LDScript {
    static final String GLOBAL_VALUE = "ds:getPublicDatatypeValue";
    private boolean reject = false;

    JavaFunction() {}

    JavaFunction(String name) {
        super(name);
//        switch (name) {
//            case GLOBAL_VALUE: break;
//            default: setReject(Access.reject(Feature.JAVA_FUNCTION));
//        }
    }
    
    /**
     * @param reject the reject to set
     */
    public void setReject(boolean reject) {
        this.reject = reject;
    }
    
}
