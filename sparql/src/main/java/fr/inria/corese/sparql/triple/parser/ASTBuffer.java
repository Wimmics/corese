package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import java.util.HashMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class ASTBuffer  {
    
    static final String NL = System.getProperty("line.separator");
    static final String SPACE = " ";
    private HashMap<Triple, Triple> done;
    private boolean service = false;
    
    int count = 0;
    
    StringBuffer sb;
    private JavaCompiler javacompiler;
    
    public ASTBuffer() {
        sb = new StringBuffer();
        done = new HashMap<>();
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

    public HashMap<Triple, Triple> getDone() {
        return done;
    }

    public void setDone(HashMap<Triple, Triple> done) {
        this.done = done;
    }
    
    /**
     * Do not print (nested) rdf star triple twice
     * @param exp
     * @return 
     */
    public boolean accept(Exp exp) {
        if (exp.isTriple()) {
            return ! getDone().containsKey(exp.getTriple());
        }
        return true;
    }

    public boolean isService() {
        return service;
    }

    public ASTBuffer setService(boolean service) {
        this.service = service;
        return this;
    }

}
