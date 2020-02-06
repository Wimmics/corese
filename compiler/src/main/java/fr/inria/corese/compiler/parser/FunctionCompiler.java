package fr.inria.corese.compiler.parser;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.datatype.DatatypeHierarchy;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author corby
 */
public class FunctionCompiler {

    private static Logger logger = LoggerFactory.getLogger(FunctionCompiler.class);
    static HashMap<String, String> loaded;
    HashMap<String, String> imported;
    Transformer transformer;

    static {
        loaded = new HashMap<>();
    }

    FunctionCompiler(Transformer t) {
        transformer = t;
        imported = new HashMap<>();
    }

    void compile(Query q, ASTQuery ast) {
        imports(q, ast);
        compileFunction(q, ast);
        compileLambda(q, ast);
    }

    void compileFunction(Query q, ASTQuery ast) {
        compile(q, ast, ast.getDefine());
        define(q, ast);
    }

    void compileLambda(Query q, ASTQuery ast) {
        compile(q, ast, ast.getDefineLambda());
        define(ast, ast.getDefineLambda(), q);
    }

    /**
     * defined functions use case: transformation profile PRAGMA: expressions
     * have declared local variables (see ASTQuery Processor)
     */
    void define(Query q, ASTQuery ast) {
        if (ast.getDefine() == null || ast.getDefine().isEmpty()) {
            return;
        }
        if (Access.reject(Access.Feature.FUNCTION_DEFINITION, ast.getLevel())) { //(ast.isUserQuery()) {
            System.out.println("Compiler: extension function not available in server mode");
            return;
        }

        define(ast, ast.getDefine(), q);
    }

    void compile(Query q, ASTQuery ast, ASTExtension ext) {
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

    void compile(Query q, ASTQuery ast, Function fun) {
        if (fun.getMetadata() != null) {
            basicImports(q, ast, fun.getMetadata());
        }
        fun.compile(ast);
        transformer.compileExist(fun, false);
        q.defineFunction(fun);
    }

    // @import <uri> select where 
    void imports(Query q, ASTQuery ast) {
        if (ast.hasMetadata(Metadata.IMPORT)) {
            basicImports(q, ast, ast.getMetadata());
        }
    }
    
    void imports(Query q, ASTQuery ast, Metadata m) {
        if (Access.accept(Access.Feature.LINKED_FUNCTION, ast.getLevel())) {
            basicImports(q, ast, m);
        }
        else 
            if (m.hasMetadata(Metadata.IMPORT)){
            logger.error("Unauthorized import: " + m.getValues(Metadata.IMPORT));
        }
    }
    
    void basicImports(Query q, ASTQuery ast, Metadata m) {
        if (m.hasMetadata(Metadata.IMPORT)) {
            for (String path : m.getValues(Metadata.IMPORT)) {
                try {
                    imports(q, ast, path);
                } catch (EngineException ex) {
                    logger.error("Error in import: " + path);
                    logger.error(ex.toString());
                }
            }
        }
    }

    void imports(Query q, ASTQuery ast, String path) throws EngineException {
        if (imported.containsKey(path)) {
            return;
        }
        if (ast.isDebug()) {
            logger.info("Import: " + path);
        }
        imported.put(path, path);
        ASTQuery ast2 = transformer.getSPARQLEngine().parse(path);
        compile(q, ast, ast2.getDefine());
        define(ast, ast2.getDefine(), q);
        compile(q, ast, ast2.getDefineLambda());
        define(ast, ast2.getDefineLambda(), q);
    }

    void undefinedFunction(Query q, ASTQuery ast) {
        for (Expression exp : ast.getUndefined().values()) {
            boolean ok = Interpreter.isDefined(exp) || q.getExtension().isDefined(exp);
            if (ok) {
            } else {
                ok = Access.accept(Access.Feature.LINKED_FUNCTION, ast.getLevel())
                        && importFunction(q, exp);
                if (!ok) {
                    ast.addError("Undefined expression: " + exp);
                }
            }
        }
    }

    boolean importFunction(Query q, Expression exp) {
        boolean b = getLinkedFunctionBasic(exp.getLabel());
        if (b) {
            return Interpreter.isDefined(exp);
        }
        return false;
    }

    boolean getLinkedFunctionBasic(String label) {
        String path = NSManager.namespace(label);
        if (loaded.containsKey(path)) {
            return true;
        }
        logger.info("Load Linked Function: " + label);
        loaded.put(path, path);
        Query imp = transformer.getSPARQLEngine().parseQuery(path);
        if (imp != null && imp.hasDefinition()) {
            // loaded functions are exported in Interpreter  
            definePublic(Interpreter.getExtension(imp), imp);
            return true;
        }
        return false;
    }

    static void removeLinkedFunction() {
        for (String name : loaded.values()) {
            Interpreter.getExtension().removeNamespace(name);
        }
        loaded.clear();
    }

    /**
     * Define function into Extension Export into Interpreter
     */
    void define(ASTQuery ast, ASTExtension aext, Query q) {
        ASTExtension ext = Interpreter.getCreateExtension(q);
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
            if (Interpreter.getExtension().getHierarchy() == null) {
                Interpreter.getExtension().setHierarchy(new DatatypeHierarchy());
            }
            Interpreter.define(fun);
        }
        fun.setPublic(true);
        if (fun.isSystem()) {
            // export function with exists {} 
            fun.getTerm().setPattern(q);
        }
    }

}
