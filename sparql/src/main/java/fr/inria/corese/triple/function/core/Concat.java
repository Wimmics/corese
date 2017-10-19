package fr.inria.corese.triple.function.core;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.Expr;
import static fr.inria.edelweiss.kgram.api.core.ExprType.CONCAT;
import static fr.inria.edelweiss.kgram.api.core.ExprType.STL_CONCAT;
import static fr.inria.edelweiss.kgram.api.core.ExprType.STL_NUMBER;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Concat extends TermEval {

    public Concat(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        String str = "";
        String lang = null;

        if (arity() == 0) {
            return DatatypeMap.EMPTY_STRING;
        }
        int length = 0;


        // when template st:concat()
        // st:number() is not evaluated now
        // it will be evaluated by template group_concat aggregate
        // return future(concat(str, st:number(), str))
        boolean isSTLConcat = oper() == STL_CONCAT;

        StringBuilder sb = new StringBuilder();
        ArrayList<Object> list = null;
        boolean ok = true, hasLang = false, isString = true;
        IDatatype dt = null;
        int i = 0;
        List<Expression> argList = getArgs();
        boolean isCompliant = eval.isCompliant();
        for (int j = 0; j < ((length > 0) ? length : argList.size());) {

            Expression ee = argList.get(j);

            if (isSTLConcat && isFuture(ee)) {
                // create a future
                if (list == null) {
                    list = new ArrayList<Object>();
                }
                if (sb.length() > 0) {
                    list.add(result(env, sb, isString, (ok && lang != null) ? lang : null, isSTLConcat));
                    sb = new StringBuilder();
                }
                list.add(ee);
                // Do not touch to j++ (see below int k = j;)   
                j++;
                continue;
            }
            dt = ee.eval(eval, b, env, p);

            // Do not touch to j++ (see below int k = j;)             
            j++;

            if (dt == null) {
                return null;
            }

            if (isSTLConcat && dt.isFuture()) {
                // result of ee is a Future
                // use case: ee = box { e1 st:number() e2 }
                // ee = st:concat(e1, st:number(), e2) 
                // dt = Future(concat(e1, st:number(), e2))
                // insert Future arg list (e1, st:number(), e2) into current argList
                // arg list is inserted after ee (indice j is already  set to j++)
                ArrayList<Expression> el = new ArrayList(argList.size());
                el.addAll(argList);
                Expression future = (Expression) dt.getObject();
                int k = j;

                for (Expression arg : future.getArgs()) {
                    el.add(k++, arg);
                }
                argList = el;
                continue;
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

        if (list != null) {
            // return ?out = future(concat(str, st:number(), str)
            // will be evaluated by template group_concat(?out) aggregate
            if (sb.length() > 0) {
                list.add(result(env, sb, isString, (ok && lang != null) ? lang : null, isSTLConcat));
            }
            Expr e = createFunction(Processor.CONCAT, list, env);
            IDatatype res = DatatypeMap.createFuture(e);
            return res;
        }

        return result(env, sb, isString, (ok && lang != null) ? lang : null, isSTLConcat);
    }
    
    Expr createFunction(String name, List<Object> args, Environment env){
        Term t = Term.function(name);
        for (Object arg : args){
            if (arg instanceof IDatatype){
                // str: arg is a StringBuilder, keep it as is
                Constant cst = Constant.create("Future", null, null);
                cst.setDatatypeValue((IDatatype) arg);
                t.add(cst);
            }
            else {
                // st:number()
               t.add((Expression) arg);
            }
        }
        t.compile((ASTQuery)env.getQuery().getAST());
        return t;
    }

    boolean isFuture(Expr e) {
        if (e.oper() == STL_NUMBER //|| e.oper() == STL_FUTURE
                ) {
            return true;
        }
        if (e.oper() == CONCAT || e.oper() == STL_CONCAT) {
            // use case:  group { st:number() } box { st:number() }
            return false;
        }
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
}
