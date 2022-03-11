package fr.inria.corese.sparql.triple.function.core;


import fr.inria.corese.kgram.api.core.Expr;
import static fr.inria.corese.kgram.api.core.ExprType.CONCAT;
import static fr.inria.corese.kgram.api.core.ExprType.FORMAT;
import static fr.inria.corese.kgram.api.core.ExprType.STL_CONCAT;
import static fr.inria.corese.kgram.api.core.ExprType.STL_FORMAT;
import static fr.inria.corese.kgram.api.core.ExprType.STL_NUMBER;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Concat extends TermEval {

    public Concat() {
    }

    public Concat(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        if (arity() == 0) {
            return DatatypeMap.EMPTY_STRING;
        }

        if (oper() == STL_CONCAT) {
            return stlconcat(eval, b, env, p);
        } else {
            return concat(eval, b, env, p);
        }
    }

    IDatatype stlconcat(Computer eval, Binding b, Environment env, Producer p) throws EngineException {

        StringBuilder sb = new StringBuilder();
        ArrayList<Expression> list = null;
        boolean ok = true, hasLang = false, isString = true;
        String str = "";
        String lang = null;
        IDatatype dt = null;
        int i = 0;
        boolean isCompliant = eval.isCompliant();

        for (Expression ee : getArgs()) {

            Expression exp = null;

            if (isFuture(ee)) {
                exp = ee;
            } else {
                dt = ee.eval(eval, b, env, p);

                if (dt == null) {
                    return null;
                } else if (dt.isFuture()) {
                    exp = (Expression) dt.getNodeObject();
                }
            }

            if (exp == null) {

                if (i == 0 && dt.hasLang()) {
                    hasLang = true;
                    lang = dt.getLang();
                }
                i++;

                if (isCompliant && !isStringLiteral(dt)) {
                    return null;
                }

                if (dt.getStringBuilder() != null) {
                    sb.append(dt.getStringBuilder());
                } else {
                    sb.append(dt.getLabel());
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
            } else {
                // result of ee is a Future
                // use case: ee = box { e1 st:number() e2 }
                // ee = st:concat(e1, st:number(), e2) 
                // dt = Future(concat(str1, st:number(), str2))              
                if (list == null) {
                    list = new ArrayList<>();
                }
                if (sb.length() > 0) {
                    list.add(constant(result(env, sb, isString, (ok && lang != null) ? lang : null, true)));
                    sb = new StringBuilder();
                }
                list.add(exp);
            }

        }

        if (list != null) {
            // return ?out = future(concat(str, st:number(), str)
            // will be evaluated by template group_concat(?out) aggregate
            if (sb.length() > 0) {
                list.add(constant(result(env, sb, isString, (ok && lang != null) ? lang : null, true)));
            }
            Expr e = createFunction(Processor.CONCAT, list, env);
            if (e == null) {
                return null;
            }
            IDatatype res = DatatypeMap.createFuture(e);
            return res;
        }

        return result(env, sb, isString, (ok && lang != null) ? lang : null, true);
    }

    

    IDatatype concat(Computer eval, Binding b, Environment env, Producer p) throws EngineException {

        StringBuilder sb = new StringBuilder();
        ArrayList<Object> list = null;
        boolean ok = true, hasLang = false, isString = true;
        String str = "";
        String lang = null;
        int i = 0;
        List<Expression> argList = getArgs();
        boolean isCompliant = eval.isCompliant();

        for (Expression ee : getArgs()) {

            IDatatype dt = ee.eval(eval, b, env, p);

            if (dt == null) {
                return null;
            }
            else if (dt.isFuture()) {
                // group { format { st:number() }}
                // compiled as group_concat(concat(st:format(st:number()))
                // st:format freeze number: evaluate it now
                Expression exp = (Expression) dt.getNodeObject();
                dt = exp.eval(eval, b, env, p);
                if (dt == null) {
                    return null;
                }
            }

            if (i == 0 && dt.hasLang()) {
                hasLang = true;
                lang = dt.getLang();
            }
            i++;

            if (isCompliant && !isStringLiteral(dt)) {
                return null;
            }

            if (dt.getStringBuilder() != null) {
                sb.append(dt.getStringBuilder());
            } else {
                sb.append(dt.getLabel());
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

        }

        return result(env, sb, isString, (ok && lang != null) ? lang : null, false);
    }

    Expr createFunction(String name, ArrayList<Expression> args, Environment env) {
        ASTQuery ast =  env.getQuery().getAST();
        try {
            return ast.createFunction(name, args);
        } catch (EngineException ex) {
            log(ex.getMessage());
            return null;
        }
    }

    /**
     * Create a fake Constant to hold a IDatatype that is the result of a part
     * of concat; This Constant will be argument of another concat.
     */
    Constant constant(IDatatype dt) {
        Constant cst = Constant.create("Future", null, null);
        cst.setDatatypeValue(dt);
        return cst;
    }
   
    boolean isFuture(Expr e) {
        switch (e.oper()){
            // freeze st:number()
            case STL_NUMBER: return true;
            // use case:  group { st:number() } box { st:number() }
            // evaluate arguments but freeze st:number()
            // return IDatatype Future with st:number inside
            case STL_CONCAT:
            case STL_FORMAT:
            // does not freeze st:number
            case CONCAT:
            case FORMAT: return false;
        }
        
        // use case: if (test, e1, st:number())
        // freeze exp
        if (e.arity() > 0) {
            for (Expr a : e.getExpList()) {
                if (isFuture(a)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     */
    IDatatype result(Environment env, StringBuilder sb, boolean isString, String lang, boolean isSTL) {
        if (lang != null) {
            return DatatypeMap.createLiteral(sb.toString(), null, lang);
        } else if (isString) {
//            if (isSTL) {
//                return plugin.getBufferedValue(sb, env);
//            } else 
            //{
            return DatatypeMap.newStringBuilder(sb);
            //}
        } else {
            return DatatypeMap.newLiteral(sb.toString());
        }
    }
    
    
    // when template st:concat()
    // st:number() is not evaluated now
    // it will be evaluated by template group_concat aggregate
    // return future(concat(str, st:number(), str))
//    IDatatype stlconcat2(Computer eval, Binding b, Environment env, Producer p) {
//
//        StringBuilder sb = new StringBuilder();
//        ArrayList<Expression> list = null;
//        boolean ok = true, hasLang = false, isString = true;
//        String str = "";
//        String lang = null;
//        IDatatype dt = null;
//        int i = 0;
//        List<Expression> argList = getArgs();
//        boolean isCompliant = eval.isCompliant();
//
//        for (int j = 0; j < argList.size();) {
//
//            Expression ee = argList.get(j);
//
//            if (isFuture(ee)) {
//                // create a future
//                if (list == null) {
//                    list = new ArrayList<>();
//                }
//                if (sb.length() > 0) {
//                    list.add(constant(result(env, sb, isString, (ok && lang != null) ? lang : null, true)));
//                    sb = new StringBuilder();
//                }
//                list.add(ee);
//                // Do not touch to j++ (see below int k = j;)   
//                j++;
//                continue;
//            }
//
//            dt = ee.eval(eval, b, env, p);
//
//            // Do not touch to j++ (see below int k = j;)             
//            j++;
//
//            if (dt == null) {
//                return null;
//            }
//
//            if (dt.isFuture()) {
//                // result of ee is a Future
//                // use case: ee = box { e1 st:number() e2 }
//                // ee = st:concat(e1, st:number(), e2) 
//                // dt = Future(concat(e1, st:number(), e2))
//                // insert Future arg list (e1, st:number(), e2) into current argList
//                // arg list is inserted after ee (indice j is already  set to j++)
//                ArrayList<Expression> el = new ArrayList(argList.size());
//                el.addAll(argList);
//                Expression future = (Expression) dt.getObject();
//                int k = j;
//
//                for (Expression arg : future.getArgs()) {
//                    el.add(k++, arg);
//                }
//                argList = el;
//                continue;
//            }
//
//            if (i == 0 && dt.hasLang()) {
//                hasLang = true;
//                lang = dt.getLang();
//            }
//            i++;
//
//            if (isCompliant && !isStringLiteral(dt)) {
//                return null;
//            }
//
//            if (dt.getStringBuilder() != null) {
//                sb.append(dt.getStringBuilder());
//            } else {
//                sb.append(dt.getLabel());
//            }
//
//            if (ok) {
//                if (hasLang) {
//                    if (!(dt.hasLang() && dt.getLang().equals(lang))) {
//                        ok = false;
//                    }
//                } else if (dt.hasLang()) {
//                    ok = false;
//                }
//
//                if (!DatatypeMap.isString(dt)) {
//                    isString = false;
//                }
//            }
//
//        }
//
//        if (list != null) {
//            // return ?out = future(concat(str, st:number(), str)
//            // will be evaluated by template group_concat(?out) aggregate
//            if (sb.length() > 0) {
//                list.add(constant(result(env, sb, isString, (ok && lang != null) ? lang : null, true)));
//            }
//            Expr e = createFunction(Processor.CONCAT, list, env);
//            IDatatype res = DatatypeMap.createFuture(e);
//            return res;
//        }
//
//        return result(env, sb, isString, (ok && lang != null) ? lang : null, true);
//    }
    
}
