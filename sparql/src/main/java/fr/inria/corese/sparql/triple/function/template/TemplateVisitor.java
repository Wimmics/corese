package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.TransformVisitor;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class TemplateVisitor extends TemplateFunction {

    private static final String VISIT_DEFAULT_NAME = NSManager.STL + "default";
    private static final IDatatype VISIT_DEFAULT = DatatypeMap.newResource(VISIT_DEFAULT_NAME);

    public TemplateVisitor(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) {
            return null;
        }

        TransformVisitor visitor = eval.getVisitor(b, env, p);  
        
        switch (oper()) {
            case ExprType.STL_VISITED:
                switch (param.length) {
                    case 0:
                        return DatatypeMap.createList(visitor.visited());
                    default:
                        return (visitor.isVisited(param[0])) ? TRUE : FALSE;
                }

            case ExprType.STL_VISIT:
                switch (param.length) {
                    case 1:  visitor.visit(VISIT_DEFAULT, param[0], null);
                    return TRUE;
                    case 2: visitor.visit(param[0], param[1], null);
                    return TRUE;
                    case 3: visitor.visit(param[0], param[1], param[2]);
                    return TRUE;
                }
                
            case ExprType.STL_VGET: return visitor.get(param[0], param[1]);
            
            case ExprType.STL_VSET: return visitor.set(param[0], param[1], param[2]);
            
            case ExprType.STL_ERRORS: return DatatypeMap.createList(visitor.getErrors(param[0]));
            
            case ExprType.STL_ERROR_MAP: return visitor.errors();
            
            case ExprType.STL_VISITED_GRAPH: return visitor.visitedGraphNode();
                

            default:
                return null;
        }
    }

}
