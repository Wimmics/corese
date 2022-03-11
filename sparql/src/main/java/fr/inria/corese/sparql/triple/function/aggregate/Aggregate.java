package fr.inria.corese.sparql.triple.function.aggregate;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.aggregate.Distinct.TreeData;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Group;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.sparql.triple.function.script.LDScript;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Aggregate extends LDScript {
    static final String NL = System.getProperty("line.separator");
    static boolean compareIndex = false;
    Group group;
    TreeData tree;
    boolean isError;
    boolean isRunning = false;
    
    public Aggregate() {       
    }
    
    public Aggregate(String name) {
        super(name);
        setArity(1);
    } 
    
    @Override
    public Aggregate duplicate() {
        try {
            Aggregate agg = getClass().newInstance();
            fill(agg);
            return agg;
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Aggregate.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }
             
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        if (isRunning){
            // Use case: recursive query evaluate aggregate within aggregate
            return duplicate().eval(eval, b, env, p);
        }
        
        isRunning = true;
        isError = false;
        init(env);
        int n = 0;
        start();
        IDatatype dt;
        
        for (Mapping map : env.getAggregate()) {
            env.aggregate(map, n++);
            map.setEval(env.getEval());
            if (map.getBind() == null) {
                map.setBind(b);
            }
            // TODO: should we inherit Binding b for aggregate eval ?
            // (let (x = exp) { sum(z + x) } as ?sum)
            Binding bind = map.getBind();
            // eval aggregate exp
            dt = getBasicArg(0).eval(eval, bind, map, p);
            
            if (dt != null) {
                switch (oper()){
                    case ExprType.SAMPLE: 
                        isRunning = false;
                        return dt;
                    case ExprType.GROUPCONCAT:
                    case ExprType.STL_GROUPCONCAT:
                    case ExprType.STL_AGGREGATE:    
                        if (dt.isFuture()) {
                            Expression ee = (Expression) dt.getNodeObject();
                            // template ?out = future(concat(str, st:number(), str))
                            // eval(concat(str, st:number(), str))
                            dt = ee.eval(eval, bind, map, p);
                        }
                        // continue
                      
                    // process aggregate    
                    default: aggregate(dt);
                }
            }
        }
        
        isRunning = false;
        if (isError){
            return null;
        }
        return result();
    }
    
    void init(Environment env) {
        if (isDistinct()) {
            // use case: count(distinct exp)           
            if (arity() == 0) {
                // count(distinct *)
                List<Node> nodes = env.getQuery().selectNodesFromPattern();
                group = Group.create(nodes);
                group.setDistinct(true);
            }
            else {
                tree = Distinct.create();
            }
        }
    }

    public void aggregate(IDatatype dt) {
    }

    public void start() {       
    }

    public IDatatype result() {
        return null;
    }

    public boolean accept(IDatatype dt) {
        if (isDistinct()) {
            boolean b = tree.add(dt);
            return b;
        }
        return true;
    }
    
    public boolean accept(Mapping map) {
        if (isDistinct()) {
            boolean b = group.isDistinct(map);
            return b;
        }
        return true;
    }
    

    
}
