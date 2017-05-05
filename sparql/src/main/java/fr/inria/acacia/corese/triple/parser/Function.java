package fr.inria.acacia.corese.triple.parser;

import fr.inria.corese.compiler.java.JavaCompiler;

/**
 * Function definition function xt:fun(x) { exp }
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Function extends Statement {
    private boolean isDebug = false;
    private boolean isTest = false;
    private boolean isTrace = false;
    private boolean isPublic = false;
    
    Metadata annot;
    
   

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

    @Override
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
    
    @Override
    public void toJava(JavaCompiler jc){
        jc.toJava(this);
    }
    
    
    Metadata getMetadata(){
        return annot;
    }
    
    boolean hasMetadata(){
        return annot != null;
    }
    
    public boolean hasMetadata(int type) {
        return annot != null && annot.hasMetadata(type);
    }
    
    void annotate(Metadata m){
        if (m == null){
            return;
        }
        set(m);
        for (String s : m){
            annotate(s);
        }
    }
    
    void set(Metadata m){
        if (annot == null){
            // function annotation
            annot = m;
        }
        else {
            // package annotation 
            annot.add(m);
        }
    }
     
    void annotate(String a) {
        switch (annot.type(a)) {

            case Metadata.DEBUG:
                setDebug(true);
                break;

            case Metadata.TRACE:
               setTrace(true);
               break;

            case Metadata.TEST:
                setTester(true);
                break;

            case Metadata.PUBLIC:
                setPublic(true);
                break;
        }
    }

    /**
     * @return the isDebug
     */
    @Override
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
    @Override
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
    @Override
    public boolean isTrace() {
        return isTrace;
    }

    /**
     * @param isTrace the isTrace to set
     */
    public void setTrace(boolean isTrace) {
        this.isTrace = isTrace;
    }
    
        /**
     * @return the isExport
     */
        @Override
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * @param isExport the isExport to set
     */
        @Override
    public void setPublic(boolean isExport) {
        this.isPublic = isExport;
    }

}
