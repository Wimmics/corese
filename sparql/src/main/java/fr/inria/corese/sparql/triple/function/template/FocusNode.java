package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.triple.parser.ASTQuery;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class FocusNode extends TemplateFunction {  
        
    public FocusNode(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) {
            return null;
        }
        String name = ASTQuery.IN;
        if (param.length == 1) {
            name = param[0].getLabel();
        }
        Node node = env.getNode(name);
        if (node == null) {
            return null;
        }
        return  node.getValue();
    }
   
}

