package fr.inria.acacia.corese.triple.parser;

import java.util.HashMap;

/**
 * Function definition function xt:fun(x) { exp }
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Function extends Statement {
    
    private static HashMap<String, Integer> annotation;
    
    static final int UNDEFINED  = -1;
    static final int TEST   = 0;
    static final int DEBUG  = 1;
    static final int TRACE  = 2;
    static final int PUBLIC = 3;
    
    private boolean isDebug = false;
    private boolean isTest = false;
    private boolean isTrace = false;
    
    static {
        initAnnotate();
    }
    
    static void initAnnotate(){
        annotation = new HashMap();
        annotation.put("@debug", DEBUG);
        annotation.put("@trace", TRACE);
        annotation.put("@test",  TEST);
        annotation.put("@export", PUBLIC);      
        annotation.put("@public", PUBLIC);      
    }

    Function(Term fun, Expression body) {
        super(Processor.FUNCTION, fun, body);
    }

    @Override
    public Term getFunction() {
        return getArg(0).getTerm();
    }

    @Override
    public Expression getBody() {
        return getArg(1);
    }

    public StringBuffer toString(StringBuffer sb) {
        sb.append(getLabel());
        sb.append(" ");
        getFunction().toString(sb);
        sb.append(" { ");
        sb.append(Term.NL);
        getBody().toString(sb);
        sb.append(" }");
        return sb;
    }
    
    int type(String a){
        Integer i = annotation.get(a);
        if (i == null){
            i = UNDEFINED;
        }
        return i;
    }
    
    void annotate(Metadata m){
        if (m == null){
            return;
        }
        for (String s : m){
            annotate(s);
        }
    }
    
    void annotate(String a) {
        switch (type(a)) {

            case DEBUG:
                setDebug(true);
                break;

            case TRACE:
               setTrace(true);
               break;

            case TEST:
                setTester(true);
                break;

            case PUBLIC:
                setExport(true);
                break;
        }
    }

    /**
     * @return the isDebug
     */
    public boolean isDebug() {
        return isDebug;
    }

    /**
     * @param isDebug the isDebug to set
     */
    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    /**
     * @return the isTest
     */
    public boolean isTester() {
        return isTest;
    }

    /**
     * @param isTest the isTest to set
     */
    public void setTester(boolean isTest) {
        this.isTest = isTest;
    }

    /**
     * @return the isTrace
     */
    public boolean isTrace() {
        return isTrace;
    }

    /**
     * @param isTrace the isTrace to set
     */
    public void setTrace(boolean isTrace) {
        this.isTrace = isTrace;
    }
}
