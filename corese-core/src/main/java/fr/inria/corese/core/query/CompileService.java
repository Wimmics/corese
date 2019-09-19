package fr.inria.corese.core.query;

import java.util.ArrayList;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.sparql.triple.parser.Processor;
import java.util.List;

public class CompileService {
    public static final String VALUES = NSManager.KGRAM + "values";
    public static final String FILTER = NSManager.KGRAM + "filter";

    Provider provider;
    List<List<Term>> termList;

    public CompileService(Provider p) {
        provider = p;
        termList = new ArrayList<>();
    }

    public CompileService() {
        termList = new ArrayList<>();
    }

    /**
     * Mappings map is result of preceding query pattern
     * Take map into account to evaluate service clause on remote endpoint
     * Generate relevant bindings for the service:
     * 
     * for each Mapping m in map : 
     * for each var in select clause of service : 
     * generate filter (var = m.value(var))
     */
    public boolean compile(Node serv, Query q, Mappings map, Environment env, int start, int limit) {
        complete(q);
        Query out = q.getOuterQuery();
        if (map == null || (map.size() == 1 && map.get(0).size() == 0)) {
            // lmap may contain one empty Mapping
            // use env because it may have bindings
            if (isValues(out)) {
                bindings(q, env);
            } else if (isFilter(out) || provider.isSparql0(serv)) {
                filter(q, env);
            } else {
                bindings(q, env);
            }
            return true;
        } else if (isValues(out)) {
            return bindings(q, map, start, limit);
        } else if (isFilter(out) || provider.isSparql0(serv)) {
            return filter(q, map, start, limit);
        } else {
            return bindings(q, map, start, limit);
        }
    }
    
    void complete(Query q) {
        Query out = q.getOuterQuery();
        ASTQuery ast = (ASTQuery) out.getAST();
        if (ast.hasMetadata(Metadata.LIMIT)) {
            ASTQuery aa = (ASTQuery) q.getAST();
            int limit = ast.getMetadata().getDatatypeValue(Metadata.LIMIT).intValue();
            if (limit < aa.getLimit()) {
                aa.setLimit(limit);
            }
        }
    }

   @Deprecated
    String getBind(Query q) {
        Transformer t = (Transformer) q.getTransformer();
        if (t != null) {
            Context c = t.getContext();
            IDatatype dt = c.get(Context.STL_BIND);
            if (dt != null) {
                return dt.getLabel();
            }
        }
        return null;
    }

    boolean isValues(Query q) {
        String str = getBind(q);
        return (str != null && str.equals(Context.STL_VALUES)) ||
                hasMetaData(q, VALUES);
    }

    boolean isFilter(Query q) {
        String str = getBind(q);
        return (str != null && str.equals(Context.STL_FILTER)) ||
                hasMetaData(q, FILTER);
    }
    
     boolean hasMetaData(Query q, String type) {
         ASTQuery ast = (ASTQuery) q.getAST();
         return ast.hasMetadata(Metadata.BIND, type);
    }
        
    public void prepare(Query q) {
//        Query g = q.getGlobalQuery();
//        ASTQuery ast = (ASTQuery) q.getAST();
//        ASTQuery ag = (ASTQuery) g.getAST();
        //ast.setPrefixExp(ag.getPrefixExp());
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
    void bindings(Query q, Environment env) {
        ASTQuery ast = (ASTQuery) q.getAST();
        ast.clearBindings();
        ArrayList<Variable> lvar = new ArrayList<Variable>();
        ArrayList<Constant> lval = new ArrayList<Constant>();

        //for (Node qv : q.getSelect()) {
        for (Node qv : q.getBody().getRecordInScopeNodesForService()) {
            String name = qv.getLabel();
            Variable var = ast.getSelectAllVar(name);
            if (var == null){
               var = Variable.create(name);
            }
            Node val = env.getNode(qv); //var.getProxyOrSelf());

            if (val != null && ! val.isBlank()) {
                lvar.add(var);
                IDatatype dt = (IDatatype) val.getValue();
                Constant cst = Constant.create(dt);
                lval.add(cst);
            }
        }
        
       Values values = Values.create(lvar, lval);

       setValues(ast, values);
    }

    /**
     * Generate bindings as bindings from Mappings
     */
    public boolean bindings(Query q, Mappings map, int start, int limit) {
        ASTQuery ast = (ASTQuery) q.getAST();
        ast.clearBindings();
        ArrayList<Variable> lvar = new ArrayList<Variable>();
        ArrayList<Constant> lval;
        Values values = Values.create();

        //for (Node qv : q.getSelect()) {
        for (Node qv : q.getBody().getRecordInScopeNodesForService()) {
            String name = qv.getLabel();
            Variable var = ast.getSelectAllVar(name);
            if (var == null){
                var = Variable.create(name);
            }
            lvar.add(var);
        }

        values.setVariables(lvar);

        for (int j = start; j < map.size() && j < limit; j++) {

            Mapping m = map.get(j);
            boolean ok = false;
            lval = new ArrayList<>();

            for (Node qnode : q.getBody().getRecordInScopeNodesForService()) {
                Node val = m.getNode(qnode);
                
                if (val != null && ! val.isBlank()) {
                    IDatatype dt = (IDatatype) val.getValue();
                    Constant cst = Constant.create(dt);
                    lval.add(cst);
                    ok = true;
                } else {
                    lval.add(null);
                }
            }

            if (ok) {
                values.addValues(lval);
            }
        }

       setValues(ast, values);
       return success(values);
    }
        
    boolean success(Values values) {
        return values.getVarList().size() > 0 && 
            values.getValues().size() > 0;
    }
    
    void setValues(ASTQuery ast, Values values) {
        if (ast.getSaveBody() == null) {
            ast.setSaveBody(ast.getBody());
        }
        BasicGraphPattern body = BasicGraphPattern.create();
        if (success(values)) {
            body.add(values);
        }
        for (Exp e : ast.getSaveBody()) {
            body.add(e);
        }
        ast.setBody(body);
    }
    

    /**
     * Search select variable of query that is bound in env Generate binding for
     * such variable as filters Set filters in ASTQuery
     */
    void filter(Query q, Environment env) {
        ASTQuery ast = (ASTQuery) q.getAST();
        ArrayList<Term> lt = new ArrayList<Term>();

        //for (Node qv : q.getSelect()) {
        for (Node qv : q.getBody().getRecordInScopeNodesForService()) {
            String var = qv.getLabel();
            Node val = env.getNode(var);

            if (val != null && ! val.isBlank()) {
                Variable v = Variable.create(var);
                IDatatype dt = (IDatatype) val.getValue();
                Constant cst = Constant.create(dt);
                Term t = Term.create(Term.SEQ, v, cst);
                lt.add(t);
            }
        }

        Term filter = null;
        if (lt.size() > 0) {
            filter = lt.get(0);
            for (int i = 1; i < lt.size(); i++) {
                filter = Term.create(Term.SEAND, filter, lt.get(i));
            }
        }

        setFilter(ast, filter);
    }

    /**
     * Generate bindings from Mappings as filter
     */
    public boolean filter(Query q, Mappings map, int start, int limit) {
        ASTQuery ast = (ASTQuery) q.getAST();
        Term filter = null;
        for (int j = start; j < map.size() && j < limit; j++) {
            Term f = getFilter(q, map.get(j));

            if (f != null) {                
                if (filter == null) {
                    filter = f;
                } else {
                    filter = Term.create(Term.SEOR, filter, f);
                }
            }
        }

        setFilter(ast, filter);
        return (filter != null);
    }
    
    Term getFilter(Query q, Mapping m) {
        ArrayList<Term> lt = new ArrayList<Term>();
        ASTQuery ast = (ASTQuery) q.getAST();

        for (Node varNode : q.getBody().getRecordInScopeNodesForService()) {
            String varName = varNode.getLabel();
            Node valNode = m.getNodeValue(varName);
            if (valNode != null) { // && ! valNode.isBlank()) {
                // do not send bnode because it will raise a syntax error
                // and it will not be available on another server because 
                // bnode are local
                // wish: select Mapping with unique(varNode, valNode)
                Term t = filter(ast, Variable.create(varName), (IDatatype) valNode.getDatatypeValue()); 
                lt.add(t);
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
        } else if (dt.isURI() && dt.getLabel().contains(" ")) {
            //ProviderImpl.logger.warn("URI with space: " + dt);
            return Term.create(Term.SEQ, var, Constant.create(dt.getLabel().replace(" ", "%20")));
        }
        return Term.create(Term.SEQ, var, Constant.create(dt));
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
    

    void setFilter(ASTQuery ast, Term f) {
        if (ast.getSaveBody() == null) {
            ast.setSaveBody(ast.getBody());
        }

        BasicGraphPattern body = BasicGraphPattern.create();

        for (Exp e : ast.getSaveBody()) {
            body.add(e);
        }
        if (f != null) {
            body.add(f);
        }
        ast.setBody(body);
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
}
