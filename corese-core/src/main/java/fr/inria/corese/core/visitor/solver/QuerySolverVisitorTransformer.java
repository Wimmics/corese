package fr.inria.corese.core.visitor.solver;

import fr.inria.corese.compiler.eval.QuerySolverVisitorBasic;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.workflow.Data;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Context;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class QuerySolverVisitorTransformer extends QuerySolverVisitorBasic {

   
    private static Logger logger = LoggerFactory.getLogger(QuerySolverVisitorTransformer.class);
    
    private static String visitorName;
    
    private Transformer transformer;

    public QuerySolverVisitorTransformer() {}
    
    public QuerySolverVisitorTransformer(Eval e) {
        super(e);
    }
   
    public QuerySolverVisitorTransformer(Transformer t, Eval e) {
        super(e);
        setTransformer(t);
    }
   

    public IDatatype beforeTransformer(String uri) {
        //System.out.println("beforeTransformer: " + uri);
        return callback(BEFORE_TRANSFORMER, toArray(getTransformer(), uri));
    }

    public IDatatype afterTransformer(String uri, String res) {
//        System.out.println("afterTransformer: " + uri);
//        System.out.println(res);
        return callback(AFTER_TRANSFORMER, toArray(getTransformer(), uri, res));
    }
    
    
    
    public IDatatype beforeWorkflow(Context ctx, Data data) {
        //System.out.println("beforeWorkflow:");
        return callback(BEFORE_WORKFLOW, toArray(
            ctx, data.getGraph()));
    }
    
    
    public IDatatype afterWorkflow(Context ctx, Data data) {
        //System.out.println("beforeWorkflow: " + data);
        return callback(AFTER_WORKFLOW, toArray(ctx, data));
    }
    
    
    public static QuerySolverVisitorTransformer create(Eval eval) {
        if (getVisitorName() == null) {
            return new QuerySolverVisitorTransformer(eval);
        }
        QuerySolverVisitorTransformer vis = create(eval, getVisitorName());
        if (vis == null) {
            return new QuerySolverVisitorTransformer(eval);
        }
        return vis;
    }

    static QuerySolverVisitorTransformer create(Eval eval, String name) {
        try {
            Class visClass = Class.forName(name);
            Object obj = visClass.getDeclaredConstructor(Eval.class).newInstance(eval);
            if (obj instanceof QuerySolverVisitorTransformer) {
                return (QuerySolverVisitorTransformer) obj;
            } else {
                logger.error("Uncorrect Visitor: " + name);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            java.util.logging.Logger.getLogger(QueryProcess.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            logger.error("Undefined Visitor: " + name);
        }

        return null;
    }
    
    
    public static QuerySolverVisitorTransformer create(Transformer t, Eval eval) {
        if (getVisitorName() == null) {
            return new QuerySolverVisitorTransformer(t, eval);
        }
        QuerySolverVisitorTransformer vis = create(t, eval, getVisitorName());
        if (vis == null) {
            return new QuerySolverVisitorTransformer(t, eval);
        }
        return vis;
    }

    static QuerySolverVisitorTransformer create(Transformer t, Eval eval, String name) {
        try {
            Class visClass = Class.forName(name);
            Object obj = visClass.getDeclaredConstructor(Transformer.class, Eval.class).newInstance(t, eval);
            if (obj instanceof QuerySolverVisitorTransformer) {
                return (QuerySolverVisitorTransformer) obj;
            } else {
                logger.error("Uncorrect Visitor: " + name);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            java.util.logging.Logger.getLogger(QueryProcess.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            logger.error("Undefined Visitor: " + name);
        }

        return null;
    }
    
     /**
     * @return the transformer
     */
    public Transformer getTransformer() {
        return transformer;
    }

    /**
     * @param transformer the transformer to set
     */
    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }
    

    /**
     * @return the visitorName
     */
    public static String getVisitorName() {
        return visitorName;
    }

    /**
     * @param aVisitorName the visitorName to set
     */
    public static void setVisitorName(String aVisitorName) {
        visitorName = aVisitorName;
    }
}
