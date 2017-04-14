package fr.inria.corese.compiler.java;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTExtension;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Function;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Statement;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class JavaCompiler {

    private static Logger logger = LogManager.getLogger(JavaCompiler.class);
    static final String NL = System.getProperty("line.separator");
    static final String PATH =
            "/user/corby/home/NetBeansProjects/corese-github/kgtool/src/main/java/fr/inria/corese/extension/";
    static final String SPACE = " ";
    static final int STEP = 2;
    static final String IDATATYPE = "IDatatype";
    static final String PROXY_PACKAGE = "fr.inria.edelweiss.kgenv.eval.ProxyImpl";
    
    static final String importList = 
              "import fr.inria.acacia.corese.api.IDatatype;\n"
            + "import fr.inria.edelweiss.kgraph.query.PluginImpl;\n"
            + "import fr.inria.acacia.corese.cg.datatype.DatatypeMap;\n";
    
    
    StringBuilder sb;
    int margin = 0;
    Datatype dtcompiler;
    String name = "Extension";

    public JavaCompiler() {
        sb = new StringBuilder();
        dtcompiler = new Datatype();
    }
    
     public JavaCompiler(String name) {
         this();
         this.name = name;
     }

    @Override
    public String toString() {
        return sb.toString();
    }

    public JavaCompiler toJava(ASTQuery ast) throws IOException {
        toJava(ast.getDefine());
        return this;
    }

    public void toJava(ASTExtension ext) throws IOException {
        header(name);

        for (ASTExtension.ASTFunMap m : ext.getMaps()) {
            for (Function exp : m.values()) {
                toJava(exp);
                append(NL);
            }
        }

        trailer();
    }
    
    public void write() throws IOException {
         write(PATH);
    }

    public void write(String path) throws IOException {
        FileWriter fw = new FileWriter(String.format("%s%s.java", path, name));
        fw.write(sb.toString());
        fw.flush();
        fw.close();
    }

    void header(String name) {
        append("package fr.inria.corese.extension;");
        nl();
        nl();
        append(importList);      
        nl();
        nl();
        append(String.format("public class %s extends PluginImpl { ", name));
        nl();
        nl();
    }

    void trailer() {
        append("}");
        nl();
    }

    
    
    
    void toJava(Function exp) {
        functionDeclaration(exp.getFunction());
        append(" {");
        incrnl();
        toJava(exp.getBody());
        pv(exp.getBody());
        decrnl();
        append("}");
        nl();
    }

    public void toJava(Expression exp) {
        exp.toJava(this);
    }

    public void toJava(Variable var) {
        append(var.getSimpleName());
    }

    public void toJava(Constant cst) {
        append(dtcompiler.toJava(cst.getDatatypeValue()));
    }

    public void toJava(Term term) {
        if (term.getName().equals(Processor.SEQUENCE)) {
            sequence(term);
        } else if (term.isFunction()) {
            functionCall(term);
        } else {
            term(term);
        }
    }

    public void toJava(Statement term) {
        switch (term.oper()) {
            case ExprType.LET:
                let(term);
                return;
                
            case ExprType.FOR:
                loop(term);
                return;
        }
    }

    
    void functionDeclaration(Term term) {
        append("public").append(SPACE).append(IDATATYPE).append(SPACE);
        append(term.javaName()).append("(");
        int i = 0;
        for (Expression exp : term.getArgs()) {
            append(IDATATYPE).append(SPACE);
            toJava(exp.getVariable());
            if (i++ < term.arity() - 1) {
                append(", ");
            }
        }
        append(")");
    }

     void nl() {
        append(NL);
        for (int i = 0; i < margin; i++) {
            append(SPACE);
        }
    }

    void incr() {
        margin += STEP;
    }

    void incrnl() {
        incr();
        nl();
    }

    void decrnl() {
        decr();
        nl();
    }

    void decr() {
        margin -= STEP;
    }
    
    
    void pv(Expression exp) {
        if (isPV(exp)) {
            append(";");
        }
    }

    boolean isPV(Expression exp) {
        if (!exp.isTerm()) {
            return false;
        }
        switch (exp.oper()) {
            case ExprType.IF:
            case ExprType.FOR:
            case ExprType.SEQUENCE:

                return false;
        }
        return true;
    }

    StringBuilder append(String str) {
        sb.append(str);
        return sb;
    }
    
    void loop(Term term){
        append("for (IDatatype ");
        toJava(term.getVariable());
        append(" : ");
        toJava(term.getDefinition());
        append(".getValueList()) {");
        incrnl();
        toJava(term.getBody());
        pv(term.getBody());
        decrnl();
        append("}");
    }

    void let(Term term) {
        Expression decl = term.getArg(0);
        append(IDATATYPE).append(SPACE);
        toJava(decl.getArg(0));
        append(" = ");
        toJava(decl.getArg(1));
        append(";");
        nl();
        toJava(term.getArg(1));
    }

    void term(Term term) {
        switch (term.oper()) {
            case ExprType.NOT:
                not(term);
                return;
            case ExprType.AND:
                and(term);
                return;
            case ExprType.OR:
                or(term);
                return;

            case ExprType.IN:
                in(term);
                return;

        }

        toJava(term.getArg(0));
        append(".");
        append(getTermName(term)).append("(");
        toJava(term.getArg(1));
        append(")");
    }

    void in(Term term) {
        append("in(");
        toJava(term.getArg(0));
        append(", DatatypeMap.newList(");
        int i = 0;
        int size = term.getArg(1).arity() - 1;
        for (Expression exp : term.getArg(1).getArgs()) {
            toJava(exp);
            if (i++ < size) {
                append(", ");
            }
        }
        append("))");
    }

    void and(Term term) {
        append("and(");
        toJava(term.getArg(0));
        append(", ");
        toJava(term.getArg(1));
        append(")");
    }

    void or(Term term) {
        append("or(");
        toJava(term.getArg(0));
        append(", ");
        toJava(term.getArg(1));
        append(")");
    }

    void not(Term term) {
        append("not(");
        toJava(term.getArg(0));
        append(")");
    }

    String getTermName(Term term) {
        switch (term.oper()) {
            case ExprType.EQ:
                return "eq";
            case ExprType.NEQ:
                return "neq";
            case ExprType.LE:
                return "le";
            case ExprType.LT:
                return "lt";
            case ExprType.GE:
                return "ge";
            case ExprType.GT:
                return "gt";

            case ExprType.PLUS:
                return "plus";
            case ExprType.MINUS:
                return "minus";
            case ExprType.MULT:
                return "mult";
            case ExprType.DIV:
                return "div";

            case ExprType.AND:
                return "&&";
            case ExprType.OR:
                return "||";


        }
        return "undef";
    }

    void functionCall(Term term) {
        switch (term.oper()) {
            case ExprType.IF:
                ifthenelse(term);
                return;

            case ExprType.RETURN:
                append("return");
                append(SPACE);
                toJava(term.getArg(0));
                return;

            case ExprType.HASH:
                hash(term);
                return;
                
            case ExprType.CAST:
                cast(term);
                return;
        }

        call(term);
    }
        
    void cast(Term term){
        toJava(term.getArg(0));
        append(".cast(");
        toJava(term.getCName()); 
        append(")");
    }
    

    void hash(Term term) {
        append("hash(");
        append(dtcompiler.newInstance(term.getModality()));
        append(", ");
        toJava(term.getArg(0));
        append(")");
    }

    boolean isMethod(Term term) {
        String oper = term.getLabel();
        try {
            ClassLoader cl = getClass().getClassLoader();
            Class c = cl.loadClass(PROXY_PACKAGE);
            Class<IDatatype>[] aclasses = new Class[term.getArity()];
            for (int i = 0; i < aclasses.length; i++) {
                aclasses[i] = IDatatype.class;
            }
            c.getMethod(oper, aclasses);

            return true;

        } catch (ClassNotFoundException e) {
            logger.error(e);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
            logger.error(e);
        } catch (IllegalArgumentException e) {
        }
        return false;
    }

    void call(Term term) {
        append(term.javaName());
        append("(");
        int i = 0;
        for (Expression exp : term.getArgs()) {
            toJava(exp);
            if (i++ < term.arity() - 1) {
                append(", ");
            }
        }
        append(")");
    }

    void ifthenelse(Term term) {
        append("if (");
        toJava(term.getArg(0));
        append(".booleanValue()) {");
        incrnl();
        toStatement(term.getArg(1));
        pv(term.getArg(1));
        decrnl();
        append("}");
        nl();
        append("else ");
        if (term.getArg(2).oper() == ExprType.IF) {
            ifthenelse(term.getArg(2).getTerm());
        } else {
            append("{");
            incrnl();
            toStatement(term.getArg(2));
            pv(term.getArg(2));
            decrnl();
            append("}");
        }
    }

    void toStatement(Expression exp) {
        if (exp.isConstant() || exp.isVariable()) {
            append("self(");
            toJava(exp);
            append(");");
        } else {
            toJava(exp);
        }
    }

    void sequence(Term term) {
        for (Expression exp : term.getArgs()) {
            toJava(exp);
            pv(exp);
            nl();
        }
    }
}
