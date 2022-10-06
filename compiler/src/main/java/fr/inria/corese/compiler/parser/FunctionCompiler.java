package fr.inria.corese.compiler.parser;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.datatype.DatatypeHierarchy;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.exceptions.UndefinedExpressionException;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @import <url>
 * dereference URL at compile time
 * LinkedFunction: 
 * 1) ff:foo() undefined extension function:         dereference  URL at compile time
 * 2) funcall (ff:foo) undefined extension function: dereference  URL at runtime 
 * 
 * LinkedFunction : Access.setLinkedFunction(true);
 * whereas @import is granted
 * 
 * Accept/reject namespace for import and LinkedFunction:
 * AccessNamespace.define(namespace, true|false)
 * 
 * @author Olivier Corby, INRIA I3S 2020
 */
public class FunctionCompiler {

    private static String NL = System.getProperty("line.separator");
    private static Logger logger = LoggerFactory.getLogger(FunctionCompiler.class);
    private static HashMap<String, String> loaded;
    HashMap<String, String> imported;
    Transformer transformer;

    static {
        setLoaded(new HashMap<>());
    }

    FunctionCompiler(Transformer t) {
        transformer = t;
        imported = new HashMap<>();
    }

    void compile(Query q, ASTQuery ast) throws EngineException {
        imports(q, ast);
        compileFunction(q, ast);
        compileLambda(q, ast);
    }

    void compileFunction(Query q, ASTQuery ast) throws EngineException {
        compile(q, ast, ast.getDefine());
        define(q, ast);
    }

    void compileLambda(Query q, ASTQuery ast) throws EngineException {
        compile(q, ast, ast.getDefineLambda());
        define(ast, ast.getDefineLambda(), q);
    }

    /**
     * defined functions use case: transformation profile PRAGMA: expressions
     * have declared local variables (see ASTQuery Processor)
     */
    void define(Query q, ASTQuery ast) throws SafetyException {
        if (ast.getDefine() == null || ast.getDefine().isEmpty()) {
            return;
        }
        if (Access.reject(Access.Feature.LDSCRIPT, ast.getLevel())
         || Access.reject(Access.Feature.DEFINE_FUNCTION, ast.getLevel())) { 
            throw new SafetyException(TermEval.FUNCTION_DEFINITION_MESS);
        }

        define(ast, ast.getDefine(), q);
    }

    void compile(Query q, ASTQuery ast, ASTExtension ext) throws EngineException {
        if (ext.isCompiled()) {
            // recursion from subquery in function: do nothing
        } else {
            ext.setCompiled(true);
            for (Function fun : ext.getFunList()) {
                compile(q, ast, fun);
            }
            ext.setCompiled(false);
        }
    }

    void compile(Query q, ASTQuery ast, Function fun) throws EngineException {
        if (fun.getMetadata() != null) {
            basicImports(q, ast, fun.getMetadata());
        }
        fun.compile(ast);
        transformer.compileExist(fun, false);
        q.defineFunction(fun);
    }

    // @import <uri> select where 
    void imports(Query q, ASTQuery ast) throws EngineException {
        if (ast.hasMetadata(Metadata.IMPORT)) {
            basicImports(q, ast, ast.getMetadata());
        }
    }
    
    void basicImports(Query q, ASTQuery ast, Metadata m) throws EngineException {
        if (m.hasMetadata(Metadata.IMPORT)) {
            for (String path : m.getValues(Metadata.IMPORT)) {
                imports(q, ast, path);
            }
        }
    }
    
    void imports(Query q, ASTQuery ast, String path) throws EngineException {
        if (Access.accept(Feature.IMPORT_FUNCTION, ast.getLevel(), path)) {
            basicImports(q, ast, path);
        }
        else {
            throw new SafetyException(TermEval.IMPORT_MESS, path);
        }
    }
    
    
    
    
    /**
     * After compiling query, there are undefined functions
     * If authorized, load LinkedFunction
     * If still undefined, throw Undefined Exception
     */     
    public void undefinedFunction(Query q, ASTQuery ast, Level level) throws EngineException {
        ArrayList<Expression> list = new ArrayList<>();

        for (Expression exp : ast.getUndefined().values()) {
            boolean ok = getExtension().isDefined(exp) || q.getExtension().isDefined(exp);
            if (ok) {
            } else {
                if (acceptLinkedFunction(level, exp.getLabel())) {
                    ok = getLinkedFunctionBasic(ast, exp);
                }
                if (!ok) {
                    list.add(exp);
                }
            }
        }

        if (list.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Expression exp : list) {
            sb.append(exp.toString()).append(NL);
        }
        if (Access.UNDEFINED_EXPRESSION_EXCEPTION) {
            throw new UndefinedExpressionException(TermEval.UNDEFINED_EXPRESSION_MESS + NL + sb.toString());
        }
        else {
            logger.error(TermEval.UNDEFINED_EXPRESSION_MESS);
            logger.error(sb.toString());
        }
    }
    
    boolean acceptLinkedFunction(Level level, String ns) {
        return Access.accept(Feature.LINKED_FUNCTION, level, ns);
    }
    
    boolean getLinkedFunctionBasic(ASTQuery ast, Expression exp) throws EngineException {
        boolean b = getLinkedFunctionBasic(exp.getLabel(), ast.getLevel());
        if (b) {
            return getExtension().isDefined(exp);
        }
        return false;
    }
    
    boolean getLinkedFunction(String label) throws EngineException {
        if (acceptLinkedFunction(Level.USER_DEFAULT, label)) { 
            return getLinkedFunctionBasic(label);
        }
        return false;
    }
    
    // @import <path>
    void basicImports(Query q, ASTQuery ast, String path) throws EngineException {
        if (imported.containsKey(path)) {
            return;
        }
        if (ast.isDebug()) {
            logger.info("Import: " + path);
        }
        imported.put(path, path);
        ASTQuery ast2 = transformer.getSPARQLEngine().parse(path, ast.getLevel());        
        compile(q, ast, ast2.getDefine());
        define(ast, ast2.getDefine(), q);
        compile(q, ast, ast2.getDefineLambda());
        define(ast, ast2.getDefineLambda(), q);
    }
    
    boolean getLinkedFunctionBasic(String label) throws EngineException {
        return getLinkedFunctionBasic(label, Level.USER_DEFAULT);
    }
    
    boolean getLinkedFunctionBasic(String label, Level level) throws EngineException {
        String path = NSManager.namespace(label);
        if (getLoaded().containsKey(path)) {
            return true;
        }
        logger.info("Load Linked Function: " + label);
        getLoaded().put(path, path);
        Query imp = transformer.getSPARQLEngine().parseQuery(path, level);
        if (imp != null && imp.hasDefinition()) {
            // loaded functions are exported in Interpreter  
            definePublic(imp.getExtension(), imp);
            return true;
        }
        return false;
    }

    static void removeLinkedFunction() {
        for (String name : getLoaded().values()) {
            getExtension().removeNamespace(name);
        }
        getLoaded().clear();
    }

    /**
     * Define function into Extension Export into Interpreter
     */
    void define(ASTQuery ast, ASTExtension aext, Query q) {
        ASTExtension ext = q.getCreateExtension();
        DatatypeHierarchy dh = new DatatypeHierarchy();
        if (q.isDebug()) {
            dh.setDebug(true);
        }
        ext.setHierarchy(dh);
        boolean pub = ast.hasMetadata(Metadata.PUBLIC);
        for (Function exp : aext.getFunList()) { 
            ext.define(exp);
            if (pub || exp.isPublic()) {
                definePublic(exp, q);
            }
            if (exp.hasMetadata(Metadata.UPDATE)) {
                // @update event function => detail mode for Construct insert/delete
                q.setDetail(true);
            }
        }
    }

    // TODO: check isSystem() because it is exported
    /**
     * ext is loaded function definitions define them as public
     *
     * @param ext
     * @param q
     */
    void definePublic(ASTExtension ext, Query q) {
        definePublic(ext, q, true);
    }

    /**
     * isDefine = true means export to Interpreter Use case: Transformation
     * st:profile does not export to Interpreter hence it uses isDefine = false
     */
     public void definePublic(ASTExtension ext, Query q, boolean isDefine) {
        for (Expr exp : ext.getFunctionList()) {
            Function e = (Function) exp;
            definePublic(e, q, isDefine);
        }
    }

    void definePublic(Function fun, Query q) {
        definePublic(fun, q, true);
    }

    void definePublic(Function fun, Query q, boolean isDefine) {
        if (isDefine) {
            if (getExtension().getHierarchy() == null) {
                getExtension().setHierarchy(new DatatypeHierarchy());
            }
            getExtension().define(fun);
        }
        fun.setPublic(true);
        if (fun.isSystem()) {
            // export function with exists {} 
            fun.getTerm().setPattern(q);
        }
    }
    
    public static void clean() {
        getLoaded().clear();;
    }

    public static HashMap<String, String> getLoaded() {
        return loaded;
    }

    public static void setLoaded(HashMap<String, String> aLoaded) {
        loaded = aLoaded;
    }
    
    
    static ASTExtension getExtension() {
        //return Interpreter.getExtension();
        return ASTExtension.getSingleton();
    }


}
