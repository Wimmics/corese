package fr.inria.edelweiss.kgenv.eval;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Group;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgram.filter.Proxy;
import java.util.ArrayList;

/**
 * Interpreter that perfom aggregate over current group list of Mapping The eval
 * function is called by Mappings The eval() function can only compute
 * aggregates
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Walker extends Interpreter {

    static IDatatype ZERO = DatatypeMap.ZERO;
    static IDatatype TRUE = DatatypeMap.TRUE;
    static final StringBuilder EMPTY = new StringBuilder(0);

    /**
     * @return the compareIndex
     */
    public static boolean isCompareIndex() {
        return compareIndex;
    }

    /**
     * @param aCompareIndex the compareIndex to set
     */
    public static void setCompareIndex(boolean aCompareIndex) {
        compareIndex = aCompareIndex;
    }
    private Expr exp, function;
    Node qNode, tNode;
    IDatatype dtres;
    int num = 0, count = 0;
    boolean isError = false, first = true, and = true, isTemplateAgg = false;
    StringBuilder sb;
    String sep = " ";
    Group group;
    TreeData tree;
    Evaluator eval;
    private static final String NL = System.getProperty("line.separator");
    private static boolean compareIndex = false;
    private boolean hasLang = false;
    private String lang;
    private boolean ok = true;
    private boolean isString = true;
    boolean test = false;
    ArrayList<IDatatype> list;

    Walker(Expr exp, Node qNode, Proxy p, Environment env, Producer prod) {
        super(p);

        Query q = env.getQuery();
       
        eval = p.getEvaluator();
        this.qNode = qNode;
        
        switch (exp.oper()){
            case STL_AGGREGATE: 
                // final aggregate for sttl template to gather individual ?out results 
                // st:aggregate may be overloaded in st:profile  
                // function st:aggregate(?x) { aggregate(?x, us:my_agg) }
                // replace oper of st:agregate(?out) by oper of aggregate(?x, us:my_agg)
                Expr fun = getDefine(exp, env);
                if (fun == null){
                    // default is st:group_concat
                    exp.setOper(ExprType.STL_GROUPCONCAT);
                }
                else {
                    setDefinition(fun);
                }
                break;
                
        }
        
        setExp(exp);

        switch (exp.oper()){
            case STL_AGGREGATE:
                format(getDefinition().getBody(), env, prod); break;
            default: 
                format(getExp(), env, prod);
        }
           
        int size = env.getMappings().size();        
        sb = new StringBuilder();
        
        if (exp.isDistinct()) {
            // use case: count(distinct ?name)
            List<Node> nodes;
            if (exp.arity() == 0) {
                // count(distinct *)
                //nodes = env.getQuery().getNodes();
                nodes = env.getQuery().getSelectNodes();
            } else {
                nodes = env.getQuery().getAggNodes(exp.getFilter());
            }
            group = Group.create(nodes);
            group.setDistinct(true);
            tree = new TreeData();
        }
        
        switch (exp.oper()){
            case STL_AGGREGATE:
            case AGGLIST:
            case AGGREGATE:        
                list = new ArrayList();
                break;
        }
    }
    
    
    void format(Expr exp, Environment env, Producer prod) {
        if (exp.getArg() != null) {
            // separator as an evaluable expression: st:nl()
            IDatatype dt = (IDatatype) eval.eval(exp.getArg(), env, prod);
            sep = dt.getLabel();
        } else {
            if (exp.getModality() != null) {
                sep = exp.getModality();
            }

            if (env.getQuery().isTemplate()) {
                if (sep.equals("\n") || sep.equals("\n\n")) {
                    // get the indentation by evaluating a predefined st:nl()
                    // computed by PluginImpl/Transformer
                    // same as: separator = 'st:nl()'
                    Expr nl = env.getQuery().getTemplateNL().getFilter().getExp();
                    IDatatype dt = (IDatatype) eval.eval(nl, env, prod);
                    String str = dt.getLabel();
                    if (sep.equals("\n\n")) {
                        str = NL + str;
                    }
                    sep = str;
                }
            }
        }
    }

    Object getResult(Environment env, Producer p) {
        if (isError) {           
            return null;
        }
        return getResult(getExp(), env, p);
    }
        
    Object getResult(Expr exp, Environment env, Producer p){

        switch (exp.oper()) {

            case SAMPLE:
            case MIN:
            case MAX:
                return dtres;


            case SUM:
                if (dtres == null) {
                    return ZERO;
                } else {
                    return dtres;
                }

            case AVG:
                if (dtres == null) {
                    return ZERO;
                }

                try {
                    Object dt = dtres.div(DatatypeMap.newInstance(num));
                    return dt;
                } catch (java.lang.ArithmeticException e) {
                    return null;
                }


            case COUNT:
                return proxy.getValue(num);

            case GROUPCONCAT:
            case STL_GROUPCONCAT: 
                
               IDatatype res = result(sb, isString, (ok && lang != null)?lang:null);
               return res;

            case AGGAND:     
                return DatatypeMap.newInstance(and);
                
            case AGGLIST:                
                return DatatypeMap.createList(list);
                
            case AGGREGATE:
                // aggregate(?x) -> list of ?x
                // aggregate(?x, xt:mediane) ->
                // aggregate(?x, xt:mediane(?list))
                IDatatype dt = DatatypeMap.createList(list);
                if (exp.arity() == 2) {
                    return (IDatatype) eval.eval(exp.getExp(1).getLabel(), env, p, dt);                    
                }                        
                return dt;
                
            case STL_AGGREGATE: 
                return getResult(getDefinition().getBody(), env, p);

        }

        return null;
    }

    /**
     * if aggregate is distinct, check that map is distinct
     */
    boolean accept(Filter f, Mapping map) {
        if (f.getExp().isDistinct()) {
            boolean b = group.isDistinct(map);
            return b;
        }
        return true;
    }

    boolean accept(Filter f, IDatatype dt) {
        if (f.getExp().isDistinct()) {
            boolean b = tree.add(dt);
            return b;
        }
        return true;
    }
    
    void eval(Expr function, Environment env, Producer p, IDatatype dt) {
        Expr var = function.getFunction().getExp(0);
        env.set(function, var, dt);
        eval(function.getBody().getFilter(), env, p);
        env.unset(function, var, dt);
    }
    
    /**
     * map is a Mapping
     */
    @Override
    public Node eval(Filter f, Environment env, Producer p) {
        Mapping map = (Mapping) env;
        
        switch (f.getExp().oper()) {

            case GROUPCONCAT:
            case STL_GROUPCONCAT:
                return groupConcat(f, env, p);
                               
            case COUNT:
                if (f.getExp().arity() == 0) {
                    // count(*)
                    if (accept(f, map)) {                        
                        num++;
                    }
                    return null;
                }
                break;
                
            case AGGREGATE:
                if (f.getExp().arity() == 0) {
                    if (accept(f, map)) {                        
                        list.add(DatatypeMap.createObject(map));
                    }
                    return null;
                }  
                break;
                
        }


        if (f.getExp().arity() == 0) {
            return null;
        }

        Expr arg = f.getExp().getExp(0);
        Node node = null;  
        IDatatype dt = (IDatatype) eval.eval(arg, map, p);
        if (dt != null) {

            switch (f.getExp().oper()) {
                
                case STL_AGGREGATE:
                    eval(getDefinition(), env, p, dt);
//                    Expr var = getDefinition().getFunction().getExp(0);
//                    env.set(exp, var, dt);
//                    eval(getDefinition().getBody().getFilter(), env, p);
//                    env.unset(exp, var);
                    break;

                case MIN:
                case MAX:
                    if (dt.isBlank()) {
                        isError = true;
                        break;
                    }
                    if (dtres == null) {
                        dtres = dt;
                    } else {
                        try {
                            if (f.getExp().oper() == MIN) {
                                if (dt.less(dtres)) {
                                    dtres = dt;
                                }
                            } else if (dt.greater(dtres)) {
                                dtres = dt;
                            }
                        } catch (CoreseDatatypeException e) {
                        }
                    }
                    break;
               
                case COUNT:
                    if (accept(f, dt)) {
                        num++;
                    }
                    break;

                case SUM:
                case AVG:
                    if (!dt.isNumber()) {
                        isError = true;
                    } else if (accept(f, dt)) {
                        if (dtres == null) {
                            dtres = dt;
                        } else {
                            dtres = dtres.plus(dt);
                        }

                        num++;
                    }
                    break;

                case SAMPLE:
                    if (dtres == null && dt != null) {
                        dtres = dt;
                    }
                    break;

                case AGGAND:

                    if (dt == null) {
                        isError = true;
                    } else {
                        try {
                            boolean b = dt.isTrue();
                            and &= b;

                        } catch (CoreseDatatypeException ex) {
                            isError = true;
                        }
                    }

                    break;
                    
                case AGGLIST:
                case AGGREGATE:
                    if (dt == null) {
                        isError = true;
                    } 
                    else if (accept(f, dt)) {
                          list.add(dt);
                    }                                                                 
                    break;                    

            }
        }

        return null;
    }

    // with one argument
    Node groupConcat(Filter f, Environment map, Producer p) {
        IDatatype dt = (IDatatype) eval.eval(f.getExp().getExp(0), map, p);
        
        if (dt != null && dt.isFuture()) {
            Expr ee = (Expr) dt.getObject();
            // template ?out = future(concat(str, st:number(), str))
            // eval(concat(str, st:number(), str))
            dt = (IDatatype) eval.eval(ee, map, p);
        }
                                             
        if (accept(f, dt)) {
            if (dt != null) {
                
                if (count == 0 && dt.hasLang()) { // 0 vs 1
                    hasLang = true;
                    lang = dt.getLang();
                }
                
                if (ok) {
                    if (hasLang) {
                        if (!(dt.hasLang() && dt.getLang().equals(lang))) {
                            ok = false;
                        }
                    } else if (dt.hasLang()) {
                        ok = false;
                    }

                    if (!DatatypeMap.isString(dt)) {
                        isString = false;
                    }
                } 
                
                if (count++ > 0) {
                    sb.append(sep);
                }
                
                StringBuilder s = dt.getStringBuilder();
                if (s != null) {                   
                   sb.append(s);                  
                } else {
                    sb.append(dt.getLabel());
                }
            }
        }
        
        return null;
    }

     IDatatype result(StringBuilder sb, boolean isString, String lang){
        if (lang != null) {
            return DatatypeMap.createLiteral(sb.toString(), null, lang);
        } else if (isString) {
            return DatatypeMap.newStringBuilder(sb);
        } else {
            return DatatypeMap.newLiteral(sb.toString());
        }
    }
     
   

    /**
     * @return the exp
     */
    public Expr getExp() {
        return exp;
    }

    /**
     * @param exp the exp to set
     */
    public void setExp(Expr exp) {
        this.exp = exp;
    }

    /**
     * @return the function
     */
    public Expr getDefinition() {
        return function;
    }

    /**
     * @param function the function to set
     */
    public void setDefinition(Expr function) {
        this.function = function;
    }

    
    
    class TreeData extends TreeMap<IDatatype, IDatatype> {

        boolean hasNull = false;

        TreeData() {
            super(new Compare());
        }

        boolean add(IDatatype dt) {

            if (dt == null) {
                if (hasNull) {
                    return false;
                } else {
                    hasNull = true;
                    return true;
                }
            }

            if (containsKey(dt)) {
                return false;
            }
            put(dt, dt);
            return true;
        }
    }

    class Compare implements Comparator<IDatatype> {

        @Override
        public int compare(IDatatype dt1, IDatatype dt2) {
            if (compareIndex && dt1.getCode() != dt2.getCode()){
                // same value with different datatype considered different
                return Integer.compare(dt1.getCode(), dt2.getCode());
            }
            else {
               return dt1.compareTo(dt2);
            } 
        }
    }

}