package fr.inria.corese.core.query;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.SERVICE_LIMIT;
import java.util.ArrayList;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.parser.VariableLocal;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompileService implements URLParam {
    static Logger logger = LoggerFactory.getLogger(CompileService.class);
    public static final String KG_VALUES = NSManager.KGRAM + "values";
    public static final String KG_FILTER = NSManager.KGRAM + "filter";
    // default binding
    public static boolean BINDING_VALUES = true;

    private ProviderService provider;
    List<List<Term>> termList;
    private Environment env;

    public CompileService(ProviderService p, Environment env) {
        provider = p;
        termList = new ArrayList<>();
        setEnv(env);
    }

    public CompileService() {
        termList = new ArrayList<>();
    }
    
    public static void setBinding(String value) {
        if (value.equals("values")) {
            setBinding(true);
        }
        else if (value.equals("filter")) {
            setBinding(false);
        }
    }
    
    public static void setBinding(boolean value) {
        BINDING_VALUES = value;
    }

    /**
     * Mappings map is result of preceding query pattern
     * Take map into account to evaluate service clause on remote endpoint
     * Generate relevant variable bindings for the service:
     * 
     * for each Mapping m in map : 
     * for each var in select clause of service : 
     * generate filter (var = m.value(var)) or values clause
     * When no map, use env
     * Create a copy of ast with bindings if any, otherwise return ast as is.
     */
    public ASTQuery compile(URLServer serv, Query q, Mappings map, CompileServiceResult res, int start, int limit) {
        ASTQuery ast = q.getAST();
        complete(serv, q, ast);
        Query out = q.getOuterQuery();
        boolean isValues = getIsValues(serv, out);
        if (map == null || (map.size() == 1 && map.get(0).size() == 0)) {
            // lmap may contain one empty Mapping
            // use env because it may have bindings
            if (isValues) {
                return bindings(q, getEnvironment());
            } else  {
                return filter(q, getEnvironment());
            } 
        } else if (isValues) {
            return bindings(serv, q, map, res, getEnvironment(), start, limit);
        } else {
            return filter(serv, q, map, res, start, limit);
        } 
    }
       
    // q is service query
    // ast is query ast
    void complete(URLServer serv, Query q, ASTQuery ast) {
        if (!ast.hasLimit()) {
            ASTQuery gast = q.getGlobalQuery().getAST();
            IDatatype dt  = gast.getMetaValue(Metadata.LIMIT);
            int myLimit   = serv.intValue(LIMIT);
            
            if (myLimit >= 0) {
                // service URL parameter limit=n
                ast.setLimit(myLimit);
            } else if (dt != null) {
                // limit of outer query of service (if any)
                int limit = q.getOuterQuery().getAST().getLimit();
                // @limit of global query
                if (dt != null) {
                    limit = dt.intValue();
                }
                // select lower limit
                ast.setLimit(Math.min(limit, ast.getLimit()));
            } else {
                Integer limit = Property.intValue(SERVICE_LIMIT);
                if (limit != null) {
                    ast.setLimit(limit);
                }
            }
        }
        //logger.info("Limit: " + ast.getLimit());
    }

    boolean getIsValues(URLServer serv, Query q) {
        if (serv.hasParameter(BINDING, VALUES)) {
            return true;
        } 
        else if (serv.hasParameter(BINDING, FILTER)) {
            return false;
        }
        else if (hasRecMetaData(q, Metadata.BINDING, KG_VALUES)) {
            return true;
        }
        else if (hasRecMetaData(q, Metadata.BINDING, KG_FILTER)) {
            return false;
        }
        else {
            return BINDING_VALUES;
        }
    }

    boolean isValues(URLServer serv, Query q) {
        return serv.hasParameter(BINDING, VALUES) || hasRecMetaData(q, Metadata.BINDING, KG_VALUES);
    }

    boolean isFilter(URLServer serv, Query q) {
        return serv.hasParameter(BINDING, FILTER) || hasRecMetaData(q, Metadata.BINDING, KG_FILTER);
    }
    
    boolean hasMetaData(Query q, int meta, String type) {
         ASTQuery ast =  getAST(q.getGlobalQuery());
         return ast.hasMetadata(meta, type);
    }
    
    boolean hasRecMetaData(Query q, int meta, String type) {
         if (getAST(q).hasMetadata(meta, type)) {
             return true;
         }
         if (q.getOuterQuery() == null || q.getOuterQuery() == q) {
             return false;
         }
         return hasRecMetaData(q.getOuterQuery(), meta, type);
    }
    
    ASTQuery getAST(Query q) {
        return  q.getAST();
    }
        
    public void prepare(Query q) {
    }

    public int slice(Query q) {
        Query g = q.getOuterQuery();
        return g.getSlice();
    }

    boolean isMap(Query q) {
        Query g = q.getOuterQuery();
        return g.isMap();
    }

    /**
     * Search select variable of query that is bound in env Generate binding for
     * such variable Set bindings in ASTQuery
     */
    ASTQuery bindings(Query q, Environment env) {
        ASTQuery ast =  q.getAST();
        ArrayList<Variable> lvar = new ArrayList<>();
        ArrayList<Constant> lval = new ArrayList<>();

        for (Node qv : q.getBody().getRecordInScopeNodesForService()) {
            String name = qv.getLabel();
            Variable var = ast.getSelectAllVar(name);
            if (var == null){
               var = Variable.create(name);
            }
            Node val = env.getNode(qv); 

            if (val != null && ! val.isBlank()) {
                lvar.add(var);
                IDatatype dt =  val.getValue();
                Constant cst = Constant.create(dt);
                lval.add(cst);
            }
        }
        
       Values values = Values.create(lvar, lval);
       return setValues(ast, values);
    }

    /**
     * Generate bindings as bindings from Mappings
     */
    ASTQuery bindings(URLServer url, Query q, Mappings map, CompileServiceResult res, Environment env, int start, int limit) {
        ASTQuery ast =  q.getAST();        
        // in-scope variables
        List<Variable> varList = getVariables(url, q, ast, map);
        // in-scope bound variables
        List<Variable> lvar = new ArrayList<>();
        
        // determine in-scope variables that are bound at least in one Mapping
        for (Variable var : varList) {
            for (int j = start; j < map.size() && j < limit; j++) {
                Node val = map.get(j).getNodeValue(var.getLabel());
                if (val != null) {
                    if (val.isBlank()) {
                        res.setBnode(true);
                    } else {
                        lvar.add(var);
                        break;
                    }
                }
            }
        }
        
        Values values = Values.create();        
        if (!lvar.isEmpty()) {
            for (int j = start; j < map.size() && j < limit; j++) {
                Mapping m = map.get(j);
                // list of values for one result
                ArrayList<Constant> list = new ArrayList<>();

                boolean ok = false, blank = false;

                for (Variable var : lvar) {
                    Node val = m.getNodeValue(var.getLabel());
                    if (val == null) {
                        list.add(null);
                    } 
                    else if (val.isBlank()) {
                        list.add(null);
                        blank = true;
                        res.setBnode(true);
                    } 
                    else {
                        IDatatype dt =  val.getValue();
                        Constant cst = create(ast, dt);
                        list.add(cst);
                        ok = true;
                    }
                }
                if (ok) {
                    // at least one variable is bound in this result
                    values.addValues(list);
                }
                else if (blank) {
                    // there is a binding but it is blank: we skip it
                    // hence we may lose a join between services 
                    // in particular when services concern the same endpoint
                    getLog().incr(url.getLogURLNumber(), LogKey.BNODE, 1);
                }
            }
        }

       values.setVariables(lvar);      
       return setValues(ast, values);
    }
    
    /**
     * DRAFT for testing
     * service </sparql?filter=?_tmp_> {
     *   bind (regex(?x, ns:) as ?_tmp_)
     * }
     * select candidate variable binding passing for ?x where filter succeed
     * var=dt is a candidate variable binding
     * if var is filter variable ?x: eval filter with ?x=dt
     * manage ?x as a global variable
     */
    boolean eval(Expression filter, Environment env, Binding b, Variable var, IDatatype dt) {
        if (filter.isTerm() && filter.arity() >= 2 && 
            filter.getArg(0).isVariable() && filter.getArg(0).equals(var)) {
            b.setGlobalVariable(var.getLabel(), dt);            
            try {
                IDatatype res = filter.eval((Interpreter)env.getEval().getEvaluator(), b, env, env.getEval().getProducer());
                if (res == null) {
                    return false;
                }
                return res.booleanValue();
            } catch (EngineException ex) {
                System.out.println("filter: " + var + " " + dt + " " + "error");
                return false;
            }
        }
        return true;
    }
    
    Expression getFilter(ASTQuery ast, URLServer url) {
        String name = url.getParameter(FILTER);
        if (name == null) {
            return null;
        }
        Expression exp = getFilter(ast, name);
        if (exp != null && exp.arity()>=2 && exp.getArg(0).isVariable()) {
            Variable nvar = new VariableLocal(exp.getArg(0).getLabel());
            nvar.setIndex(Binding.UNBOUND);
            exp.getTerm().setArg(0, nvar);
        }
        return exp;
    }
    
    Expression getFilter(ASTQuery ast, String name) {
        for (Exp exp : ast.getBody()) {
            if (exp.isBind() && exp.getBind().getVariable().getLabel().equals(name)) {
                return exp.getBind().getFilter();
            }
        }
        return null;
    }
    
    
    List<Variable> getVariables(URLServer url, Query q, ASTQuery ast, Mappings map) {
        ArrayList<Variable> lvar = new ArrayList<>();
        // determine list of in-scope variables in service
        for (Node qv : q.getBody().getRecordInScopeNodesForService()) {
            String name = qv.getLabel();
            if (url.accept(name)) {
                // variable not in skip and in focus -- if any
                Variable var = ast.getSelectAllVar(name);
                if (var == null) {
                    var = Variable.create(name);
                }
                lvar.add(var);
            }
        }
        return lvar;
    }
        
    boolean success(Values values) {
        return values.getVarList().size() > 0 && 
            values.getValues().size() > 0;
    }
    
    /**
     * Return a copy of ast with values if any or ast itself
     */
    ASTQuery setValues(ASTQuery ast, Values values) {
        if (success(values)) {
            ASTQuery astCopy = ast.copy();           
            BasicGraphPattern body = BasicGraphPattern.create();
            body.add(values);
            for (Exp e : astCopy.getBody()) {
                if (accept(e)) {
                   body.add(e);
                }
            }
            astCopy.setBody(body);
            return astCopy;
        } else {
            return ast;
        }
    }
    
    // remove values var { undef }
    boolean accept(Exp exp) {
        if (exp.isValues()) {
            return exp.getValuesExp().isDefined();
        }
        return true;
    }
    
    
    
    /**
     * Return a copy of ast with filter if any or ast itself
     */
    ASTQuery setFilter(ASTQuery aa, Term f) {
        if (f != null) {
            ASTQuery ast = aa.copy();
            BasicGraphPattern body = BasicGraphPattern.create();
            for (Exp e : ast.getBody()) {
                if (accept(e)) {
                    body.add(e);
                }
            }
            body.add(f);
            ast.setBody(body);
            return ast;
        } else {
            return aa;
        }
    }

    /**
     * Search select variable of query that is bound in env Generate binding for
     * such variable as filters Set filters in ASTQuery
     */
    ASTQuery filter(Query q, Environment env) {
        ASTQuery ast =  q.getAST();
        ArrayList<Term> lt = new ArrayList<>();

        for (Node qv : q.getBody().getRecordInScopeNodesForService()) {
            String var = qv.getLabel();
            Node val = env.getNode(var);

            if (val != null) {
                if (val.isBlank()) {
                
                }
                else {
                    Variable v = Variable.create(var);
                    IDatatype dt =  val.getValue();
                    Constant cst = Constant.create(dt);
                    Term t = Term.create(Term.SEQ, v, cst);
                    lt.add(t);
                }
            }
        }

        Term filter = null;
        if (lt.size() > 0) {
            filter = lt.get(0);
            for (int i = 1; i < lt.size(); i++) {
                filter = Term.create(Term.SEAND, filter, lt.get(i));
            }
        }

        return setFilter(ast, filter);
    }

    /**
     * Generate bindings from Mappings as filter
     */
    public ASTQuery filter(URLServer url, Query q, Mappings map, int start, int limit) {
        return filter(url, q, map, new CompileServiceResult(), start, limit);
    }
    
    public ASTQuery filter(URLServer url, Query q, Mappings map, CompileServiceResult res, int start, int limit) {
        ASTQuery ast =  q.getAST();
        Term filter = null;
        List<Variable> lvar = getVariables(url, q, ast, map);
        
        for (int j = start; j < map.size() && j < limit; j++) {
            Term f = getFilter(url, q, ast, map.get(j), res, lvar);

            if (f != null) {                
                if (filter == null) {
                    filter = f;
                } else {
                    filter = Term.create(Term.SEOR, filter, f);
                }
            }
        }

        return setFilter(ast, filter);
    }
    
    Term getFilter(URLServer url, Query q, ASTQuery ast, Mapping m, CompileServiceResult res, List<Variable> lvar) {
        ArrayList<Term> lt = new ArrayList<>();
        boolean blank = false;
        
        for (Variable var : lvar) {
            Node valNode = m.getNodeValue(var.getLabel());
            if (valNode != null) {
                if (valNode.isBlank()) {
                    // do not send bnode because it will raise a syntax error
                    // and it will not be available on another server because 
                    // bnode are local
                    blank = true;
                    res.setBnode(true);
                }
                else {                    
                    Term t = filter(ast, var,  valNode.getDatatypeValue());
                    lt.add(t);
                }
            }
        }
        
        if (lt.size() > 0) { 
            submit(lt);
            Term f = lt.get(0);

            for (int i = 1; i < lt.size(); i++) {
                f = Term.create(Term.SEAND, f, lt.get(i));
            }
            return f;
        }
        else if (blank) {
            getLog().incr(url.getLogURLNumber(), LogKey.BNODE, 1);
        }
        return null;
    }
        
    /**
     * Generate filter var = val
     * except when val is a bnode, in this case: isBlank(var)
     * because there is no mean to retrieve a specific bnode in a remote server
     * hence, the service query will get all bnodes (!) and the local join will do the job
     * of selecting the right bnodes according to current partial solution where we have the bnode val
     */
    Term filter(ASTQuery ast, Variable var, IDatatype dt) {
        if (dt.isBlank()) {
            return Term.function(Processor.ISBLANK, var);
        } else  {    
            return Term.create(Term.SEQ, var, create(ast, dt));
        }
    }
    
    Constant create(ASTQuery ast, IDatatype dt) {
        if (dt.isURI()) {
            if (dt.getLabel().contains(" ")) {
                return Constant.create(dt.getLabel().replace(" ", "%20"));
            } else {
                return Constant.create(dt.getLabel());
            }
        }
        else {
            return Constant.create(dt);
        }
    }
    
    /**
     * Check that two successive filter lists are different
     */
    boolean accept(List<Term> lt) {
        if (termList.isEmpty()) {
            return true;
        }
        return ! equal(lt, termList.get(termList.size() - 1));
    }
    
    boolean equal(List<Term> l1, List<Term> l2) {
        if (l1.size() != l2.size()) {return false;}
        for (int i = 0; i<l1.size(); i++) {
            if (! equal(l1.get(i), l2.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    boolean equal(Term t1, Term t2) {
        return t1.getArg(0).getLabel().equals(t2.getArg(0).getLabel())
            && t1.getArg(1).getDatatypeValue().equals(t2.getArg(1).getDatatypeValue()) ;
    }
    
    void submit(List<Term> lt) {
        //termList.add(lt);
    }
      

    /**
     * Generate bindings as a string values () {()} syntax
     */
    StringBuffer strBindings(Query q, Mappings map) {
//                if (group == null){
//                    group =  Group.instance(q.getSelectFun());
//                }

        String SPACE = " ";
        StringBuffer sb = new StringBuffer();

        sb.append("values (");

        for (Node qv : q.getSelect()) {
            sb.append(qv.getLabel());
            sb.append(SPACE);
        }
        sb.append("){");

        for (Mapping m : map) {
                       
            sb.append("(");

            for (Node var : q.getSelect()) {
                Node val = m.getNode(var);
                if (val == null) {
                    sb.append("UNDEF");
                } else {
                    sb.append(val.getValue().toString());
                }
                sb.append(SPACE);
            }

            sb.append(")");
        }

        sb.append("}");
        return sb;

    }

    /**
     * @return the env
     */
    public Environment getEnvironment() {
        return env;
    }

    /**
     * @param env the env to set
     */
    public void setEnv(Environment env) {
        this.env = env;
    }

    public ContextLog getLog() {
        return getProvider().getLog();
    }

    public ProviderService getProvider() {
        return provider;
    }

    public void setProvider(ProviderService provider) {
        this.provider = provider;
    }
}
