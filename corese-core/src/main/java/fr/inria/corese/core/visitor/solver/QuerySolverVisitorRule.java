package fr.inria.corese.core.visitor.solver;

import fr.inria.corese.compiler.eval.QuerySolverVisitorBasic;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class QuerySolverVisitorRule extends QuerySolverVisitorBasic {

  
    private static Logger logger = LoggerFactory.getLogger(QuerySolverVisitorRule.class);
    
    private RuleEngine re;
    // should rule engine call entailment 
    private boolean entailment = false;
    private static String visitorName;
    
    public QuerySolverVisitorRule() {
    }

    public QuerySolverVisitorRule(Eval e) {
        super(e);
    }
    
    public QuerySolverVisitorRule(RuleEngine re, Eval e) {
        super(e);
        this.re = re;
    }
    
    @Override
    public IDatatype init() {
        setEntailment(define(ENTAILMENT, 4));
        IDatatype dt = callback(INIT, toArray(getRuleEngine()));
        return dt;
    }

    @Override
    public IDatatype beforeEntailment(IDatatype path) {
        IDatatype dt = callback(BEFORE_ENTAIL, toArray(getRuleEngine(), path));
        return dt;
    }
    
    @Override
    public IDatatype afterEntailment(IDatatype path) {
        return callback(AFTER_ENTAIL, toArray(getRuleEngine(), path));
    }
    
    @Override
    public IDatatype prepareEntailment(IDatatype path) {
        IDatatype dt = callback(PREPARE_ENTAIL, toArray(getRuleEngine(), path));
        return dt;
    }  
    
    @Override
    public IDatatype beforeUpdate(Query q) {
        IDatatype dt = callback(BEFORE_UPDATE, toArray(q));
        return dt;
    } 
    
    @Override
    public IDatatype afterUpdate(Mappings map) {
        IDatatype dt = callback(AFTER_UPDATE, toArray(map));
        return dt;
    } 
    
    @Override
    public IDatatype update(Query q, List<Edge> delete, List<Edge> insert) {
        return callback(UPDATE, toArray(q, toDatatype(delete), toDatatype(insert)));
    } 
    
    @Override
    public IDatatype loopEntailment(IDatatype path) {
        IDatatype dt = callback(LOOP_ENTAIL, toArray(getRuleEngine(), path));
        return dt;
    }      
      
    @Override
    public IDatatype beforeRule(Query q) {
        IDatatype dt = callback(BEFORE_RULE, toArray(getRuleEngine(), q));
        return dt;
    }

    // res: Mappings or List<Edge>
    @Override
    public IDatatype afterRule(Query q, Object res) {
        return callback(AFTER_RULE, toArray(getRuleEngine(), q, res));
    }
    
    // res: Mappings or List<Edge>
    @Override
    public IDatatype constraintRule(Query q, Object res, IDatatype success) {
        return callback(CONSTRAINT_RULE, toArray(getRuleEngine(), q, res, success));
    }
    
    // rule engine call entailment only when ldscript function entailment below this one is defined
    @Override
    public boolean entailment() {
        return isEntailment();
    }
    
    @Override
    public IDatatype entailment(Query rule, List<Edge> construct, List<Edge> where) { 
        return callback(ENTAILMENT, toArray(getRuleEngine(), rule, toDatatype(construct), toDatatype(where)));
    }

    /**
     * @return the entailment
     */
    public boolean isEntailment() {
        return entailment;
    }

    /**
     * @param entailment the entailment to set
     */
    public void setEntailment(boolean entailment) {
        this.entailment = entailment;
    }
    
    public static QuerySolverVisitorRule create(RuleEngine re, Eval eval) {
        if (getVisitorName() == null) {
            return new QuerySolverVisitorRule(re, eval);
        }
        QuerySolverVisitorRule vis = create(re, eval, getVisitorName());
        if (vis == null) {
            return new QuerySolverVisitorRule(re, eval);
        }
        return vis;
    }

    static QuerySolverVisitorRule create(RuleEngine re, Eval eval, String name) {
        try {
            Class visClass = Class.forName(name);
            Object obj = visClass.getDeclaredConstructor(RuleEngine.class, Eval.class).newInstance(re, eval);
            if (obj instanceof QuerySolverVisitorRule) {
                return (QuerySolverVisitorRule) obj;
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
    
      /**
     * @return the re
     */
    public RuleEngine getRuleEngine() {
        return re;
    }

    /**
     * @param re the re to set
     */
    public void setRuleEngine(RuleEngine re) {
        this.re = re;
    }


}
