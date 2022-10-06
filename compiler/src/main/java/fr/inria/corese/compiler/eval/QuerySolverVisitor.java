package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback manager for LDScript functions with specific annotations Eval SPARQL
 * processor calls before() and after()
 *
 * @before      function us:before(?q) {}
 * @after       function us:after(?m) {}
 * @produce     function us:produce(?q) {}
 * @candidate   function us:candidate(?q, ?e) {}
 * @result      function us:result(?m) {}
 * @solution    function us:solution(?m) {}

 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class QuerySolverVisitor extends QuerySolverVisitorBasic {

    private static Logger logger = LoggerFactory.getLogger(QuerySolverVisitor.class);
    
     public QuerySolverVisitor() {}


    public QuerySolverVisitor(Eval e) {
        super(e);
        overload = new QuerySolverOverload(this);
    }

    @Override
    public IDatatype prepare() {
        callback(PREPARE, toArray());
        return DatatypeMap.TRUE;
    }
    
    @Override
    public IDatatype init(Query q) {
        // Visitor may be reused by let (?g = construct where)
        if (query == null) {
            query = q;
            ast =  q.getAST();
            setSelect();
            initialize();
            execInit(q);
        }
        return DatatypeMap.TRUE;
    }
    
    void execInit(Query q) {
        // authorize possible update whereas we start select where
        boolean b = getEval().getSPARQLEngine().isSynchronized();
        getEval().getSPARQLEngine().setSynchronized(true);
        callback(INIT, toArray(q));
        getEval().getSPARQLEngine().setSynchronized(b);
        getEval().getProducer().start(q);
    }

    @Override
    public IDatatype init() {
        return callback(INIT, toArray());        
    }
    
    @Override
    public IDatatype initParam() {
        return callback(INIT_PARAM, toArray());        
    }
    
    // called by corese gui -param processing by GraphEngine
    public IDatatype initServer(String uri) {
        return callback(INIT_SERVER, toArray(uri));
    } 

    
    @Override
    public IDatatype before(Query q) {
        if (query == q) {
            return callback(BEFORE, toArray(q));
        }
        // subquery
        return start(q);
    }
    
    @Override
    public IDatatype after(Mappings map) {
        if (map.getQuery() == query) {
            return callback(AFTER, toArray(map));
        }
        // subquery
        return finish(map);
    }
    
    @Override
    public IDatatype construct(Mappings map) {
        return callback(CONSTRUCT, toArray(map.getGraph()));
    }
    
    @Override
    public IDatatype beforeUpdate(Query q) {
        return callback(BEFORE_UPDATE, toArray(q));
    }

    @Override
    public IDatatype afterUpdate(Mappings map) {
        return callback(AFTER_UPDATE, toArray(map));
    }
    
    @Override
    public IDatatype beforeLoad(IDatatype path) {
        return callback(BEFORE_LOAD, toArray(path));
    }

    @Override
    public IDatatype afterLoad(IDatatype path) {
        return callback(AFTER_LOAD, toArray(path));
    }
    
    @Override
    public IDatatype start(Query q) {
        return callback(START, toArray(q));
    }

    @Override
    public IDatatype finish(Mappings map) {
        return callback(FINISH, toArray(map));
    }
    
    @Override
    public IDatatype insert(IDatatype path, Edge edge) {
        return callback(INSERT, toArray(path, edge));
    }
    
    @Override
    public IDatatype delete(Edge edge) {
        return callback(DELETE, toArray(edge));
    }
    
    @Override
    public IDatatype update(Query q, List<Edge> delete, List<Edge> insert) { 
        return callback(UPDATE, toArray(q, toDatatype(delete), toDatatype(insert)));
    }
    
    @Override
    public IDatatype orderby(Mappings map) {
        return sort(getEval(), ORDERBY, toArray(map));
    }
    
    @Override
    public boolean distinct(Eval eval, Query q, Mapping map) {
        IDatatype key = callback(eval, DISTINCT, toArray(q, map));
        if (key == null) {
            return true;
        }       
        return distinct(eval, q, key);
    }
    
    boolean distinct(Eval eval, Query q, IDatatype key) {
        IDatatype res = getDistinct(eval, q).get(key);
        if (res == null) {
            getDistinct(eval, q).set(key, key);
            return true;
        }
        return false;
    }
    
    /**
     * Query and subquery must have different table for distinct
     * As they have different environment, we assign the table to the environment
     */
    IDatatype getDistinct(Eval eval, Query q) {
        IDatatype dt = distinct.get(eval.getEnvironment());
        if (dt == null) {
            dt = DatatypeMap.map();
            distinct.put(eval.getEnvironment(), dt);
        }
        return dt;
    }
    
    @Override
    public boolean limit(Mappings map) {
        IDatatype dt = callback(LIMIT, toArray(map));
        return dt == null || dt.booleanValue();
    }
    
    @Override
    public int timeout(Node serv) {
        IDatatype dt = callback(TIMEOUT, toArray(serv));
        if (dt == null) {
            return 0;
        }
        return dt.intValue();
    }
    
    @Override
    public int slice(Node serv, Mappings map) {
        IDatatype dt = callback(SLICE, toArray(serv, map));
        if (dt == null) {
            return SLICE_DEFAULT;
        }
        return dt.intValue();
    }
    
        
    @Override
    public IDatatype produce(Eval eval, Node g, Edge q) {  
        return produce1(eval, g, q);
    }
    
    public IDatatype produce1(Eval eval, Node g, Edge q) {  
        return callback(eval, PRODUCE, toArray(g, q));
    }
      
   @Override
    public IDatatype candidate(Eval eval, Node  g, Edge q, Edge e) {  
        return callback(eval, CANDIDATE, toArray(g, q, e));
    }
            
    @Override
    public boolean result(Eval eval, Mappings map, Mapping m) {       
        return result(callback(eval, RESULT, toArray(map, m)));       
    }
    
    boolean result(IDatatype dt) {
        if (dt == null) {
            return true;
        }
        return dt.booleanValue();
    }
    
    @Override
    public IDatatype statement(Eval eval, Node g, Exp e) { 
        return callback(eval, STATEMENT, toArray(g, e));
    }
       
    @Override
    public IDatatype path(Eval eval, Node g, Edge q, Path p, Node s, Node o) {       
        return callback(eval, PATH, toArray(g, q, p, s, o));
    }
    
    @Override
    public boolean step(Eval eval, Node g, Edge q, Path p, Node s, Node o) {  
         return result(callback(eval, STEP, toArray(g, q, p, s, o)));         
    }
       
    @Override
    public IDatatype values(Eval eval, Node g, Exp e, Mappings m) { 
        return callback(eval, VALUES, toArray(g, e, m));    
    }  
    
    @Override
    public IDatatype bind(Eval eval, Node g, Exp e, IDatatype dt) { 
        return callback(eval, BIND, toArray(g, e, dt));    
    } 
    
    @Override
    public IDatatype bgp(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, BGP, toArray(g, e, m));
    }
    
    @Override
    public IDatatype join(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, JOIN, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype optional(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, OPTIONAL, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype minus(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, MINUS, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype union(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, UNION, toArray(g, e, m1, m2));
    }
    
     @Override
    public IDatatype graph(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, GRAPH, toArray(g, e, m));
    }
    
    
    @Override
    public IDatatype service(Eval eval, Node s, Exp e, Mappings m) {       
        return callback(eval, SERVICE, toArray(s, e, m));
    }
    
    @Override
    public IDatatype query(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, QUERY, toArray(g, e, m));
    }
    
    @Override
    public boolean filter() {      
        Expr exp = getDefineMetadata(getEnvironment(), FILTER, 3);
        return (exp != null);
    } 
    
    @Override
    public boolean filter(Eval eval, Node g, Expr e, boolean b) {       
        IDatatype dt = callback(eval, FILTER, toArray(g, e, DatatypeMap.newInstance(b)));
        if (dt == null) {
            return b;
        }
        return dt.booleanValue();
    }
    
    @Override
    public IDatatype function(Eval eval, Expr funcall, Expr fundef) {  
        if (isFunction()) {
            IDatatype dt = callback(eval, FUNCTION, toArray(funcall, fundef));       
            return dt;
        }
        return null;
    }
    
    
    @Override
    public boolean having(Eval eval, Expr e, boolean b) {       
        IDatatype dt = callback(eval, HAVING, toArray(e, DatatypeMap.newInstance(b)));
        if (dt == null) {
            return b;
        }
        return dt.booleanValue();
    }
    
    @Override
    public IDatatype select(Eval eval, Expr e, IDatatype dt) {       
        IDatatype val = callback(eval, SELECT, toArray(e, dt));
        return dt;
    }
    
    @Override
    public IDatatype aggregate(Eval eval, Expr e, IDatatype dt) {       
        IDatatype val = callback(eval, AGGREGATE, toArray(e, dt));
        return dt;
    }
   

    
    @Override
    public IDatatype error(Eval eval, Expr exp, IDatatype[] args) {
        return  overload.error(eval, exp, (IDatatype[]) args);
    }
    
    @Override
    public boolean overload(Expr exp, IDatatype res, IDatatype dt1, IDatatype dt2) {
        // prevent overload within overload
        return ! isRunning() && overload.overload(exp, res, dt1, dt2);
    }
       
   @Override
    public IDatatype overload(Eval eval, Expr exp, IDatatype res, IDatatype[] args) {
        return overload.overload(eval, exp,  res, (IDatatype[]) args);
    }   
    
    @Override
    public int compare(Eval eval, int res, IDatatype dt1, IDatatype dt2) {
        if (! isRunning() && overload.overload(dt1, dt2)) {
            return overload.compare(eval, res, dt1, dt2);
        }
        return res;
    }
    
    @Override
    public boolean produce() {
        return accept(PRODUCE) && define(PRODUCE, 2);
    }
    
    @Override
    public boolean candidate() {
        return accept(CANDIDATE) && define(CANDIDATE, 3);
    }
    
    @Override
    public boolean statement() {
        return accept(STATEMENT) && define(STATEMENT, 2);
    }
    

}
