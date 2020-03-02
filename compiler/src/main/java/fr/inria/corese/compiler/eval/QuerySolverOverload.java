package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class QuerySolverOverload {

    public static final String ERROR    = "@error"; 
    public static final String EQ = "@eq";
    public static final String NE = "@ne";
    public static final String LE = "@le";
    public static final String LT = "@lt";
    public static final String GE = "@ge";
    public static final String GT = "@gt";
    
    public static final String PLUS  = "@plus";
    public static final String MINUS = "@minus";
    public static final String MULT  = "@mult";
    public static final String DIVIS = "@divis";


    public static final String US = NSManager.USER;
    public static final String MERROR = US + "error";
    
    public static final String MEQ = US + "eq";
    public static final String MNE = US + "ne";
    public static final String MLE = US + "le";
    public static final String MLT = US + "lt";
    public static final String MGE = US + "ge";
    public static final String MGT = US + "gt";

    public static final String MPLUS  = US + "plus";
    public static final String MMINUS = US + "minus";
    public static final String MMULT  = US + "mult";
    public static final String MDIVIS = US + "divis";

    public static final String MCOMPARE = US + "compare";
    
    static final IDatatype ERROR_DATATYPE = DatatypeMap.newResource(IDatatype.ERROR_DATATYPE);
    

    private boolean overload = true;
    QuerySolverVisitorBasic visitor;
    
    
    QuerySolverOverload(QuerySolverVisitorBasic vis) {
        visitor = vis;
    }
    
    public void setOverload(boolean b) {
        overload = b;
    }
    
    public boolean isOverload() {
        return overload;
    }
    

    boolean overload(Expr exp, IDatatype res, IDatatype dt1, IDatatype dt2) {
        return overload && isOverload(dt1, dt2);
    }
    
     boolean overload(IDatatype dt1, IDatatype dt2) {
        return overload && isOverload(dt1, dt2);
    }

    boolean isOverload(IDatatype dt1, IDatatype dt2) {
        return datatypeOverload(dt1, dt2) || bnodeOverload(dt1, dt2);
    }
    
    boolean bnodeOverload(IDatatype dt1, IDatatype dt2) {
        return dt1.isBlank() && dt2.isBlank();
    }
    
    boolean datatypeOverload(IDatatype dt1, IDatatype dt2) {
        return dt1.isGeneralized()&& dt2.isGeneralized()
            && (dt1.getDatatypeURI().equals(dt2.getDatatypeURI()) || compatible(dt1, dt2));
    }
    
    // placeholder to determine if datatypes are compatible
    // length in km and length in m are compatible
    // use case: xt:datatype(us:km, us:length)
    // use case: xt:datatype(us:m,  us:length)
    boolean compatible(IDatatype dt1, IDatatype dt2) {
        String t1 = visitor.getSuperType(dt1.getDatatype());
        String t2 = visitor.getSuperType(dt2.getDatatype());  
        return t1 != null && t2 != null && t1.equals(t2);
    }


    public IDatatype error(Eval eval, Expr exp, IDatatype[] args) {
        return overloadErrorMethod(eval, exp, args);
    }

    /**
     * Return error() if res == null && overload == null
     */
    IDatatype overload(Eval eval, Expr exp, IDatatype res, IDatatype[] param) {
        // 1) @type us:length function us:eq(?e, ?a, ?b) where datatype(?a) == us:length
        // 2) function us:eq(?e, ?a, ?b)
        // 3) if res == null process as an error
        IDatatype dt = overloadMethod(eval, exp, param);
        if (dt == null) {
            if (res == null) {
                return error(eval, exp, param);
            }
            // std result
            return  res;
        }
        return dt;
    }
    
    int compare(Eval eval, int res, IDatatype... param) {
        IDatatype dt =  visitor.methodBasic(eval, MCOMPARE,  param);
        if (dt == null) {
            return res;
        }
        return dt.intValue();
    }
    
    
    /**
     * a = b return null 
     * 1) try      @type dt:error function us:eq(?e, ?a, ?b) 
     * 2) then try @type dt:error function us:error(?e, a, ?b)
     */
    IDatatype overloadErrorMethod(Eval eval, Expr exp, IDatatype[] param) {
        String name = getMethodName(exp);
        IDatatype[] values = toArray(param, exp);
        if (name == null) {
            return visitor.methodBasic(eval, MERROR, ERROR_DATATYPE, values);
        }
        IDatatype val = visitor.methodBasic(eval, name, ERROR_DATATYPE, values);
        if (val == null) {
            val = visitor.methodBasic(eval, MERROR, ERROR_DATATYPE, values);
        }
        return val;
    }

    /**
     * a = b return null 1) try @eq function us:feq(?e, ?a, ?b) 2) otherwise try
     * @error function us:error(?e, a, ?b)
     */
    IDatatype overloadError(Eval eval, Expr exp, IDatatype[] param) {
        String name = getName(exp, param);
        IDatatype[] values = toArray(param, exp);
        if (name == null) {
            return visitor.callbackBasic(eval, ERROR, values);
        }
        IDatatype val = visitor.callbackBasic(eval, name, values);
        if (val == null) {
            val = visitor.callbackBasic(eval, ERROR, values);
        }
        return val;
    }

    IDatatype[] toArray(IDatatype[] args, Object... lobj) {
        IDatatype[] param = new IDatatype[args.length + lobj.length];
        int i = 0;
        for (Object obj : lobj) {
            param[i++] = DatatypeMap.getValue(obj);
        }
        for (IDatatype val : args) {
            param[i++] = val;
        }
        return param;
    }

    IDatatype overloadFunction(Eval eval, Expr exp, IDatatype[] param) {
        String name = getName(exp, param);
        if (name == null) {
            return null;
        }
        return visitor.callback(eval, name, param);
    }

    IDatatype overloadMethod(Eval eval, Expr exp, IDatatype[] param) {
        String name = getMethodName(exp);
        if (name == null) {
            return null;
        }
        return visitor.method(eval, name,  param);
    }
    
    IDatatype kind(IDatatype dt) {
       IDatatype kind = DatatypeMap.kind(dt);
       return kind;
    }

    String getMethodName(Expr exp) {
        switch (exp.oper()) {
            case ExprType.EQ:
                return MEQ;
            case ExprType.NEQ:
                return MNE;

            case ExprType.LE:
                return MLE;
            case ExprType.LT:
                return MLT;
            case ExprType.GE:
                return MGE;
            case ExprType.GT:
                return MGT;

            case ExprType.PLUS:
                return MPLUS;
            case ExprType.MINUS:
                return MMINUS;
            case ExprType.MULT:
                return MMULT;
            case ExprType.DIV:
                return MDIVIS;
                
            default:
                // concat() -> us:concat()
                return US.concat(exp.getLabel());
        }
        //return null;
    }
   
    String getName(Expr exp, IDatatype[] param) {
        switch (exp.oper()) {
            case ExprType.EQ:
                return EQ;
            case ExprType.NEQ:
                return NE;
            case ExprType.LE:
                return LE;
            case ExprType.LT:
                return LT;
            case ExprType.GE:
                return GE;
            case ExprType.GT:
                return GT;
                
            case ExprType.PLUS:
                return PLUS;
            case ExprType.MINUS:
                return MINUS;
            case ExprType.MULT:
                return MULT;
            case ExprType.DIV:
                return DIVIS;    
        }
        return null;
    }

}
