package fr.inria.corese.sparql.compiler.java;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.script.ForLoop;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.script.Let;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.function.script.Statement;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.kgram.api.core.ExprType;
import static fr.inria.corese.kgram.api.core.ExprType.AND;
import static fr.inria.corese.kgram.api.core.ExprType.DIV;
import static fr.inria.corese.kgram.api.core.ExprType.EQ;
import static fr.inria.corese.kgram.api.core.ExprType.FOR;
import static fr.inria.corese.kgram.api.core.ExprType.GE;
import static fr.inria.corese.kgram.api.core.ExprType.GT;
import static fr.inria.corese.kgram.api.core.ExprType.IF;
import static fr.inria.corese.kgram.api.core.ExprType.LE;
import static fr.inria.corese.kgram.api.core.ExprType.LET;
import static fr.inria.corese.kgram.api.core.ExprType.LT;
import static fr.inria.corese.kgram.api.core.ExprType.MINUS;
import static fr.inria.corese.kgram.api.core.ExprType.MULT;
import static fr.inria.corese.kgram.api.core.ExprType.NEQ;
import static fr.inria.corese.kgram.api.core.ExprType.OR;
import static fr.inria.corese.kgram.api.core.ExprType.PLUS;
import static fr.inria.corese.kgram.api.core.ExprType.RETURN;
import static fr.inria.corese.kgram.api.core.ExprType.SEQUENCE;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Java Compiler for LDScript Take an AST as input and compile the LDScript
 * function definitions in Java
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017-2019
 *
 */
public class JavaCompiler {

    private static Logger logger = LoggerFactory.getLogger(JavaCompiler.class);
    static final String NL = System.getProperty("line.separator");
    // where to write the Java code
    private String path =
      "/user/corby/home/NetBeansProjects/corese-github-v4/" +
      "corese-core/src/main/java/fr/inria/corese/core/extension/";
    static final String SPACE = " ";
    static final int STEP = 2;
    static final String IDATATYPE = "IDatatype";

    public static final String VAR_EXIST = "?_b";
    private String pack = "fr.inria.corese.core.extension";

    int margin = 0;
    int count = 0;
    int level = 0;
    String name = "Extension";

    StringBuilder sb;
    Header head;
    Datatype dtc;
    ASTQuery ast;
    private Function current;
    // current function processing
    private Function function;
    // stack of bound variables (function parameter, let)
    Stack stack;

    HashMap<String, Boolean> skip;
    HashMap<String, String> functionName, javaName;
    HashMap<Integer, String> termName;

    public JavaCompiler() {
        sb = new StringBuilder();
        dtc = new Datatype();
        stack = new Stack();
        head = new Header(this);
        skip = new HashMap<String, Boolean>();
        functionName = new HashMap<>();
        javaName = new HashMap<>();
        termName = new HashMap<>();
        init();
    }

    /**
     *
     * target Java class name
     */
    public JavaCompiler(String name) {
        this();
        record(name);
    }

  

    @Override
    public String toString() {
        return sb.toString();
    }

    /**
     * Main function to compile AST functions
     */
    public JavaCompiler compile(ASTQuery ast) throws IOException, EngineException {
        this.ast = ast;
        path(ast);
        head.process(getPackage(), name);
        compile(ast.getDefine());
        compile(ast.getDefineLambda());
        trailer();
        write();
        return this;
    }

    public JavaCompiler compile(Query q) throws IOException, EngineException {
        ASTQuery ast =  q.getAST();
        this.ast = ast;
        path(ast);
        head.process(getPackage(), name);
        //compile((ASTExtension) q.getExtension());
        //toJava(ast.getDefineLambda());
        trailer();
        write();
        return this;
    }

    public void compile(ASTExtension ext) throws IOException, EngineException {
        for (Function exp : ext.getFunctionList()) {
            //System.out.println(exp);
            if (!exp.hasMetadata(Metadata.SKIP)) {
                compile(exp);
                append(NL);
            }
        }
    }

    /**
     * Write result of compiling in a file
     */
    public void write() throws IOException {
        write(getPath());
    }

    public void write(String path) throws IOException {
        FileWriter fw = new FileWriter(String.format("%s%s.java", path, name));
        fw.write(head.getStringBuilder().toString());
        fw.write(dtc.getStringBuilder().toString());
        fw.write(dtc.getStringBuilderVar().toString());
        fw.write(NL);
        fw.write(sb.toString());
        fw.flush();
        fw.close();
    }

    /**
     * Compile one function
     */
    void compile(Function exp) throws EngineException {
        setCurrent(exp);
        stack.push(exp);
        functionDeclaration(exp);
        append(" {");
        incrnl();
        Rewrite rw = new Rewrite(ast, this);
        // rewrite body may add return statement when necessary
        Expression body = rw.process(exp.getBody());
        toJava(body);
        finish(body);
        stack.pop(exp);
        pv(body);
        decrnl();
        append("}");
        nl();
    }
    
    
    
    
    
    
    
 

    public void toJava(Expression exp) {
        exp.toJava(this, false);
    }

    // arg = true when exp is argument of a function
    public void toJava(Expression exp, boolean arg) {
        exp.toJava(this, arg);
    }

    public void toJava(Variable var, boolean arg) {
        append(name(var));
    }

    public void toJava(Constant cst, boolean arg) {
        append(dtc.toJava(cst.getDatatypeValue()));
    }

    public void toJava(Term term, boolean arg) {
        if (term.isExist()) {
            new Pattern(this).exist(term);
        } else if (term.getName().equals(Processor.SEQUENCE)) {
            sequence(term);
        } else if (term.isFunction()) {
            functionCall(term, arg);
        } else {
            term(term, arg);
        }
    }

    // lambda expression
    public void toJava(Function fun, boolean arg) {
        append(dtc.string(name(fun)));
    }

    public void toJava(Statement term, boolean arg) {
        switch (term.oper()) {
            case ExprType.LET:
                let(term.getLet());
                return;

            case ExprType.FOR:
                loop(term.getFor());
                return;
        }
    }
    
    
    
    
    
    
    
    



    void functionDeclaration(Function fun) {
        Term term = fun.getSignature();
        append("public").append(SPACE).append(IDATATYPE).append(SPACE);
        append(name(fun)).append("(");
        int i = 0;
        for (Expression exp : term.getArgs()) {
            append(IDATATYPE).append(SPACE);
            Variable var = exp.getVariable();
            toJava(var);
            if (i++ < term.arity() - 1) {
                append(", ");
            }
        }
        append(")");
    }

    String name(Function fun) {
        return javaName(fun.getFunction());
    }

    String javaName(Term term) {
        String name = term.getName();
        String str = NSManager.nstrip(name);
        if (str.equals(name) && name.contains(":")) {
            return name.replace(":", "_").replace("-", "_");
        }
        return str;
    }
    
    
    String name(Variable var) {
        return var.getSimpleName();
    }

    void loop(ForLoop term) {
        incrlevel();
        append("for (IDatatype ");
        toJava(term.getVariable());
        append(" : ");
        toJava(term.getDefinition(), true);
        append(".getValueList()) {");
        incrnl();
        stack.push(term);
        toJava(term.getBody());
        stack.pop(term);
        pv(term.getBody());
        decrnl();
        append("}");
        decrlevel();
    }

 

    void let(Let term) {
        incrlevel();
        boolean decr = true;
        for (Expression exp : term.getDeclaration()) {
            if (!stack.isBound(term.getVariable(exp))) {
                append(IDATATYPE).append(SPACE);
            }
            toJava(term.getVariable(exp));
            append(" = ");
            toJava(term.getDefinition(exp), true);
            pv();
            nl();
            stack.push(term.getVariable(exp));
        }

        stack.push(term);

        if (getCurrent().getBody() == term && isReturnable(term.getBody())) {
            // function body is one let
            sb.append("return ");
        }

        toJava(term.getBody());

        stack.pop(term);

        pv(term.getBody());

        if (decr) {
            decrlevel();
        }
    }


    void term(Term term, boolean arg) {
        switch (term.oper()) {
            case ExprType.NOT:
                not(term, arg);
                return;
            case ExprType.AND:
            case ExprType.OR:
                bool(term, arg);
                return;
            case ExprType.IN:
                in(term);
                return;
        }

        toJava(term.getArg(0));
        append(".");
        append(getTermName(term)).append("(");
        toJava(term.getArg(1), true);
        append(")");
    }

    void in(Term term) {
        append("in(");
        toJava(term.getArg(0));
        append(", ").append("DatatypeMap.newList").append("(");
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

    void bool(Term term, boolean arg) {
        String name = (term.oper() == AND) ? "and" : "or";
        append(name).append("(");
        toJava(term.getArg(0), arg);
        append(", ");
        toJava(term.getArg(1), arg);
        append(")");
    }

    void not(Term term, boolean arg) {
        append("not(");
        toJava(term.getArg(0), arg);
        append(")");
    }
    
   

    void functionCall(Term term, boolean arg) {
        switch (term.oper()) {
            case ExprType.IF:
                ifthenelse(term, arg);
                return;

            case ExprType.RETURN:
                append("return");
                append(SPACE);
                toJava(term.getArg(0), true);
                return;

            case ExprType.HASH:
                hash(term);
                return;

            case ExprType.CAST:
                cast(term);
                return;

            case ExprType.SET:
                set(term);
                return;

            case ExprType.MAP:
            case ExprType.MAPLIST:
            case ExprType.MAPMERGE:
                map(term);
                return;

            case ExprType.APPLY_TEMPLATES_WITH:
            case ExprType.APPLY_TEMPLATES_WITH_ALL:
                template(term);
                return;
        }

        call(term);
    }
    
    
    
    
    
     
    
    
    
    /**
     *
     * map(ex:fun(?x), ?list)
     */
    void map(Term term) {
        if (term.oper() == ExprType.MAPMERGE) {
            append(getFunctionName(Processor.XT_MERGE)).append("(");
        }

        append(mapName(term)).append("(");

        int i = 0;
        for (Expression exp : term.getArgs()) {
            toJava(exp);
            if (i++ < term.arity() - 1) {
                append(", ");
            }
        }
        append(")");

        if (term.oper() == ExprType.MAPMERGE) {
            append(")");
        }
    }

    String javaName(String name) {
        return NSManager.nstrip(name);
    }

    String mapName(Term term) {
        switch (term.oper()) {
            case ExprType.MAP:
                return "map";
            case ExprType.MAPLIST:
            case ExprType.MAPMERGE:
                return "maplist";
        }
        return "map";
    }

    void set(Term term) {
        Variable var = term.getArg(0).getVariable();
        if (!stack.isBound(var)) {
            dtc.declare(var);
        }
        toJava(var);
        append(" = ");
        toJava(term.getArg(1), true);
    }

    void cast(Term term) {
        toJava(term.getArg(0));
        append(".cast(");
        toJava(term.getCName());
        append(")");
    }

    void hash(Term term) {
        append("hash(");
        append(dtc.string(term.getModality()));
        append(", ");
        toJava(term.getArg(0));
        append(")");
    }

   
    /**
     * Generic function call
     */
    void call(Term term) {
        Method met = getMethod(term);
        if (met == null) {
            // function(dt)
            funcall(term);
        } else {
            // dt.method()
            methodcall(term, met);
        }
    }

    /**
     * generate dt.method()
     * 
     */
    void methodcall(Term term, Method met) {
        method(term, met.getReturnType() != IDatatype.class);
    }

   
    /**
     * Search IDatatype method 
     */
    Method getMethod(Term term) {
        if (term.getArgs().isEmpty()) {
            return null;
        }
        try {
            Class[] sig = new Class[term.getArgs().size() - 1];
            Arrays.fill(sig, IDatatype.class);
            //Method meth = IDatatype.class.getMethod(term.getName(), sig);
            Method meth = IDatatype.class.getMethod(getMethodName(term), sig);
            return meth;
        } catch (NoSuchMethodException | SecurityException ex) {
        }
        return null;
    }

    /**
     * Generate a function call
     */
    void funcall(Term term) {
        append(getMethodName(term));
        append("(");
        int i = 0;
        for (Expression exp : term.getArgs()) {
            toJava(exp, true);
            if (i++ < term.arity() - 1) {
                append(", ");
            }
        }
        append(")");
    }

    void method(Term term) {
        toJava(term.getArg(0), true);
        append(".");
        append(getMethodName(term));
        append("(");
        int i = 0;
        for (Expression exp : term.getArgs()) {
            // skip first arg
            if (i > 0) {
                toJava(exp, true);
                if (i++ < term.arity() - 1) {
                    append(", ");
                }
            } else {
                i++;
            }
        }
        append(")");
    }

    /**
     * Generate an IDatatype method call on first argument of term
     * dt.method()
     * wrap = true : cast method call into IDatatype 
     * because metodh return type is a Java type instead of a IDatatype
     * DatatypeMap.newInstance(dt.method())
     */
    void method(Term term, boolean wrap) {
        if (wrap) {
            append("DatatypeMap.newInstance").append("(");
        }
        method(term);
        if (wrap) {
            append(")");
        }
    }

    public String getMethodName(Term term) {
        String name = getFunctionName(term.getLabel());
        if (name == null) {
            return javaName(term);
        }
        return name;
    }

    String getFunctionName(String name) {
        return functionName.get(name);
    }
    
    
    
    

    void ifthenelse(Term term, boolean arg) {
        incrlevel();
        if (arg) {
            ifexp(term);
        } else {
            ifthenelse(term);
        }
        decrlevel();
    }

    void ifexp(Term term) {
        append("((");
        toJava(term.getArg(0));
        append(".booleanValue()");
        append(") ? ");
        incrnl();
        toJava(term.getArg(1), true);
        decrnl();

        append(" : ");

        incrnl();
        toJava(term.getArg(2), true);
        append(")");
        decrnl();
    }

    void ifthenelse(Term term) {
        append("if (");
        toJava(term.getArg(0), true);
        append(".booleanValue()");
        append(") {");
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
        int i = 1;
        for (Expression exp : term.getArgs()) {
            if (getLevel() == 0 && i++ == term.arity()) {
                if (isReturnable(exp)) {
                    // function call
                    sb.append("return ");
                }
            }

            toJava(exp);
            pv(exp);
            nl();
        }
    }
    
    
    
    /*
     * st:apply-templates-with(trans, var)
     */
    void template(Term term) {
        append("getPluginTransform().transform(");
        append(dtc.newInstance(isAll(term)));
        for (Expression exp : term.getArgs()) {
            append(", ");
            toJava(exp);
        }
        append(")");
    }

    boolean isAll(Term term) {
        switch (term.oper()) {
            case ExprType.APPLY_TEMPLATES_ALL:
            case ExprType.APPLY_TEMPLATES_WITH_ALL:
                return true;
        }
        return false;
    }

    
    
    boolean isReturnable(Expression exp) {
        switch (exp.oper()) {
            case RETURN:
            case LET:
            case FOR:
            case IF:
            case SEQUENCE:
                return false;
        }
        return true;
    }
    
    void define(int oper, String name) {
        termName.put(oper, name);
    }
    
    void defineTermName() {
        define(EQ, "eq");
        define(NEQ, "neq");
        define(LE, "le");
        define(LT, "lt");
        define(GT, "gt");
        define(GE, "ge");
        
        define(PLUS, "plus");
        define(MINUS, "minus");
        define(MULT, "mult");
        define(DIV, "div");
        
        define(AND, "&&");
        define(OR, "||");
    }
    
    /**
     * Java name in let (select where) in function
     * sh:path() -> jc:sh_path() 
     * 
     */
    public void setJavaName(String name, String java) {
        javaName.put(name, java);
    }
    
    public String getJavaName(String name) {
        return javaName.get(name);
    }
    
    void defineFunctionName() {
        functionName.put("isURI", "isURINode");
        functionName.put("isBlank", "isBlankNode");
        functionName.put("isLiteral", "isLiteralNode");

        functionName.put(Processor.XT_GEN_REST, "Rest.rest");
        functionName.put(Processor.XT_GEN_GET, "GetGen.gget");
        functionName.put(Processor.XT_GET, "Get.get");
        
        functionName.put(Processor.XT_EDGES, "edge");
        functionName.put(Processor.XT_SET, "set");
        functionName.put(Processor.XT_HAS, "has");
        functionName.put(Processor.XT_SIZE, "length");

        functionName.put(Processor.XT_MAP, "DatatypeMap.map");
        functionName.put(Processor.XT_LIST, "DatatypeMap.newList");
        functionName.put(Processor.XT_MEMBER, "DatatypeMap.member");
        functionName.put(Processor.XT_FIRST, "DatatypeMap.first");
        functionName.put(Processor.XT_REST, "DatatypeMap.rest");
        functionName.put(Processor.XT_ADD, "DatatypeMap.add");
        functionName.put(Processor.XT_CONS, "DatatypeMap.cons");
        functionName.put(Processor.XT_REVERSE, "DatatypeMap.reverse");
        functionName.put(Processor.XT_MERGE, "DatatypeMap.merge");

        functionName.put(Processor.STRLEN, "DatatypeMap.strlen");
        //functionName.put(Processor.STRDT, "DatatypeMap.newInstance");
    }

    String getTermName(Term term) {
        String name = termName.get(term.oper());
        if (name == null) {
            return "undef";
        }
        return name;
    }

    String getExistVar() {
        return VAR_EXIST + count++;
    }

    
    
    /**
     * name = DataShape or fr.inria.corese.core.extension.DataShape extract
     * package name if any
     */
    void record(String name) {
        this.name = name;
        int index = name.lastIndexOf(".");
        if (index != -1) {
            setPackage(name.substring(0, index));
            this.name = name.substring(index + 1);
        }
        System.out.println("package: " + this.getPackage());
        System.out.println("class: " + this.name);
    }

    void init() {
        skip.put(NSManager.SHAPE + "class", true);
        skip.put(NSManager.STL + "default", true);
        skip.put(NSManager.STL + "aggregate", true);
                
        defineFunctionName();
        defineTermName();
    }

    boolean skip(String name) {
        Boolean b = skip.get(name);
        return b != null && b;
    }

    void path(ASTQuery ast) {
        if (ast.hasMetadata(Metadata.PATH)) {
            setPath(ast.getMetadata().getValue(Metadata.PATH));
        }
        System.out.println("path: " + getPath());
    }

    void trailer() {
        append("}");
        nl();
    }

    void finish(Expression body) {
        switch (body.oper()) {
            case FOR:
                append(NL).append("return TRUE;");
                break;
            case SEQUENCE:
                Expression last = body.getArg(body.arity() - 1);
                if (last.oper() == FOR) {
                    append(NL).append("return TRUE;");
                }
                break;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    void incrlevel() {
        level++;
    }

    void decrlevel() {
        level--;
    }

    int getLevel() {
        return level;
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
            pv();
        }
    }

    void pv() {
        append(";");
    }

    boolean isPV(Expression exp) {
        if (!exp.isTerm()) {
            return false;
        }
        switch (exp.oper()) {
            case ExprType.IF:
            case ExprType.FOR:
            case ExprType.LET:
            case ExprType.SEQUENCE:

                return false;
        }
        return true;
    }

    StringBuilder append(String str) {
        sb.append(str);
        return sb;
    }

    /**
     * @return the function
     */
    public Function getFunction() {
        return function;
    }

    /**
     * @param function the function to set
     */
    public void setFunction(Function function) {
        this.function = function;
    }

    /**
     * @return the current
     */
    public Function getCurrent() {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(Function current) {
        this.current = current;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the pack
     */
    public String getPackage() {
        return pack;
    }

    /**
     * @param pack the pack to set
     */
    public void setPackage(String pack) {
        this.pack = pack;
    }
}
