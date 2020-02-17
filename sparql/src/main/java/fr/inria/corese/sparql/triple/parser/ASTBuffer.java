package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.compiler.java.JavaCompiler;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class ASTBuffer  {
    
    static final String NL = System.getProperty("line.separator");
    static final String SPACE = " ";
    
    int count = 0;
    
    StringBuffer sb;
    private JavaCompiler javacompiler;
    
    public ASTBuffer() {
        sb = new StringBuffer();
    }
    
    public ASTBuffer append(Object obj) {
        sb.append(obj);
        return this;
    }
    
     public ASTBuffer kw(String obj) {
        sb.append(obj).append(SPACE);
        return this;
    }
    
    public ASTBuffer append(Object... lobj) {
        for (Object o : lobj) {
           sb.append(o);
        }
         return this;
    }
    
    public int length () {
        return sb.length();
    }
    
    @Override
    public String toString() {
        return sb.toString();
    }
    
    public ASTBuffer incr() {
        count += 1;
        return this;
    }
    
    public ASTBuffer decr() {
        count -= 1;
        return this;
    }
    
    public ASTBuffer nl() {
        append(NL);
        indent();
        return this;
    }
    
    public ASTBuffer nlincr() {
        incr();
        return nl();
    }
    
    public ASTBuffer nldecr() {
        decr();
        return nl();
    }
    
    public void indent() {
        for (int i=0; i<count; i++) {
            append("  ");
        }
    }

    /**
     * @return the javacompiler
     */
    public JavaCompiler getCompiler() {
        return javacompiler;
    }
    
    public boolean hasCompiler() {
        return getCompiler() != null;
    }

    /**
     * @param javacompiler the javacompiler to set
     */
    public void setCompiler(JavaCompiler javacompiler) {
        this.javacompiler = javacompiler;
    }

}
