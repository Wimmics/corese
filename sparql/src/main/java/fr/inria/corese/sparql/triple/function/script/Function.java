package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.GraphProcessor;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.parser.ASTBuffer;
import fr.inria.corese.sparql.triple.parser.Message;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Function definition function xt:fun(x) { exp }
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Function extends Statement {
    private static Logger logger = LoggerFactory.getLogger(Function.class);

    static final IDatatype RDF_TYPE = DatatypeMap.newResource(RDF.RDF+"type");
    public static boolean typecheck = false;
    public static boolean nullcheck = false;
    public static boolean rdftypecheck = false;  
    
    private boolean isDebug = false;
    private boolean isTest = false;
    private boolean isTrace = false;
    private boolean isPublic = false;
    private boolean lambda = false;
    private boolean visited = false;

    private IDatatype dt;
    Term signature;
    Constant type;
    Expression body;
    Metadata annot;
    private HashMap<String, Constant> table;

    public Function() {
    }

    public Function(Term fun, Expression body) {
        this(fun, null, body, false);
    }

    public Function(Term fun, Constant type, Expression body, boolean lambda) {
        super(Processor.FUNCTION, fun, body);
        this.type = type;
        this.signature = fun;
        this.body = body;
        fun.setExpression(this);
        body.setExpression(this);
        table = new HashMap<>();
        if (lambda) {
            defineLambda();
        }
    }

    @Override
    public Term getFunction() {
        return getSignature();
    }

    public Constant getReturnType() {
        return type;
    }

    public IDatatype getReturnDatatype() {
        return (type == null) ? null : type.getDatatypeValue();
    }

    public Term getSignature() {
        return signature;
    }

    @Override
    public Expression getBody() {
        return body;
    }
    
    public void setBody(Expression exp) {
        body = exp;
    }

    // retun the URI of the Function
    @Override
    public IDatatype getDatatypeValue() {
        if (getFunctionDatatypeValue() != null) {
            return getFunctionDatatypeValue();
        }
        return getSignature().getCName().getDatatypeValue();
    }
    
    public IDatatype getFunctionDatatypeValue() {
         return dt;
    }
    
    public void setFunctionDatatypeValue(IDatatype dt) {
        this.dt = dt;
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        return getDatatypeValue();
    }

    public Constant getType(Variable var) {
        return getTable().get(var.getLabel());
    }

    public IDatatype getDatatype(Variable var) {
        Constant cst = getType(var);
        if (cst != null) {
            return cst.getDatatypeValue();
        }
        return null;
    }

    @Override
    public Expression compile(ASTQuery ast) throws EngineException {
        Expression exp = super.compile(ast);
        if (isTrace()) {
            System.out.println(this);
        }
        return exp;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        if (isLambda()) {
            lambda(sb);
        } else {
            sb.append(getLabel(), " ");
            getFunction().toString(sb);
        }
        sb.append(" { ");
        sb.nlincr();
        getBody().toString(sb);
        sb.nldecr().append("}");
        return sb;
    }

    ASTBuffer lambda(ASTBuffer sb) {
        sb.append("function(");
        int i = 0;
        for (Expression var : getSignature().getArgs()) {
            if (i++ > 0) {
                sb.append(", ");
            }
            sb.append(var);
        }
        sb.append(")");
        return sb;
    }

    @Override
    public void toJava(JavaCompiler jc, boolean arg) {
        jc.toJava(this, arg);
    }

    public Metadata getMetadata() {
        return annot;
    }

    public boolean hasMetadata() {
        return annot != null;
    }

    @Override
    public boolean hasMetadata(int type) {
        return annot != null && annot.hasMetadata(type);
    }

    @Override
    public boolean hasMetadata(String type) {
        return annot != null && annot.hasMetadata(type);
    }

    @Override
    public List<String> getMetadataValues(String name) {
        if (getMetadata() == null) {
            return null;
        }
        return getMetadata().getValues(name);
    }

    @Override
    public Collection<String> getMetadataList() {
        if (hasMetadata()) {
            return getMetadata().getMetadataList();
        }
        return null;
    }

    public void annotate(Metadata m) {
        if (m == null) {
            return;
        }       
        if (isLambda()) {
            lambdaAnnotate(m);
        }
        else {
            basicAnnotate(m);
        }              
    }
    
    void basicAnnotate(Metadata m) {
        set(m);
        if (m.getMetadata() != null) {
            set(m.getMetadata());
            annotation(m.getMetadata());
        }
        annotation(m);
    }
    
    void annotation(Metadata m) {
        for (String s : m) {
            annotate(m, s);
        }
    }
    
    // do not set(m) because we do not heritate @type @event etc.
    void lambdaAnnotate(Metadata m) {
        if (m.getMetadata() != null) {
            annotation(m.getMetadata());
        }
        annotation(m);
    }

    void set(Metadata m) {
        if (annot == null) {
            // function annotation
            annot = m;
        } else {
            // package annotation 
            annot.add(m);
        }
    }

    void annotate(Metadata m, String a) {
        switch (m.type(a)) {

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

    /**
     * @return the table
     */
    public HashMap<String, Constant> getTable() {
        return table;
    }

    /**
     * @param table the table to set
     */
    public void setTable(HashMap<String, Constant> table) {
        this.table = table;
    }

    /**
     * @return the lambda
     */
    public boolean isLambda() {
        return lambda;
    }

    void defineLambda() {
        setLambda(true);
        setFunctionDatatypeValue(DatatypeMap.createObject(getDatatypeValue().stringValue(), this));
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(boolean lambda) {
        this.lambda = lambda;
    }

    /**
     * @return the visited
     */
    @Override
    public boolean isVisited() {
        return visited;
    }

    /**
     * @param visited the visited to set
     */
    @Override
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    @Override
    public void visit(ExpressionVisitor v) {
        v.visit(this);
    }
    
    @Override
    public void walk(Walker walker) {
        walker.enter(this);
        getBody().walk(walker);
        walker.leave(this);
    }

    @Override
    public boolean typecheck(ASTQuery ast) {
        Term t = getSignature();
        List<Variable> list = new ArrayList<Variable>();
        int i = 1;
        for (Expression var : t.getArgs()) {
            if (list.contains(var.getVariable())) {
                ast.addErrorMessage(Message.PARAMETER_DUPLICATE, var , toString());
                ast.addFail(true);
                return false;
            } else {
                list.add(var.getVariable());
            }
        }
        tailRecursion();
        return true;
    }

    /**
     * Last statement is recursive call
     */
    public void tailRecursion() {
        getBody().tailRecursion(this);
    }

    private Expression lastStatement() {
        return this;
    }

    /**
     * Replace arg by var in the body
     */
    public Expression rewrite(Variable arg, Variable var) {
        Expression exp = getBody().duplicate();
        return exp.replace(arg, var);
    }

    // Type check
    
    
    boolean check(Computer eval, Binding b, Environment env, Producer p, IDatatype[] param, IDatatype dt) {
        boolean suc = true;
        if (getTable() != null && !getTable().isEmpty()) {
            int i = 0;
            for (Expression exp : getSignature().getArgs()) {
                Variable var = exp.getVariable();
                Constant type = getTable().get(var.getLabel());
                if (type != null) {
                    boolean bb = check(eval, b, env, p, param[i], type.getDatatypeValue());
                    if (!bb) {
                        suc = false;
                        logger.info(getSignature().toString());
                        logger.info(String.format("%s = %s; type = %s", var, param[i], type));
                    }
                }
                i++;
            }
        }
        
        boolean bb = result(eval, b, env, p, param, dt);
        suc = suc && bb;
        return suc;
    }

    boolean result(Computer eval, Binding b, Environment env, Producer p, IDatatype[] param, IDatatype dt) {
        if (dt == null) {
            if (nullcheck) {
                System.out.print("Null result: " + getSignature() + " ");
                for (IDatatype val : param) {
                    System.out.print(val + " ");
                }
                System.out.println();
            }
        } else if (getReturnDatatype() != null) {
            boolean bb = check(eval, b, env, p, dt, getReturnDatatype());
            if (!bb) {
                logger.info(getSignature().toString());
                logger.info(String.format("result = %s; type = %s", dt, getReturnDatatype()));
            return false;
            }
        }
        return true;
    }

    /**
     * type = sh:NodeShape, dt:graph, dt:list, dt:bnode, dt:uri, xsd:integer,
     */
    boolean check(Computer eval, Binding b, Environment env, Producer p, IDatatype value, IDatatype type) {
        switch (type.getLabel()) {
            case IDatatype.URI_DATATYPE:
                return value.isURI();
            case IDatatype.BNODE_DATATYPE:
                return value.isBlank();
            case IDatatype.LITERAL_DATATYPE:
                return value.isLiteral();
        }
        
        GraphProcessor proc = eval.getGraphProcessor();

        if (value.isLiteral()) {
            return value.conform(type);
        } else {
            // uri, bnode
            if (DatatypeMap.isLiteralDatatype(type)) {
                return false;
            }
            if (rdftypecheck) {
                // test xt:exists(value, rdf:type, type)
                boolean bb = exists(env, p, value, RDF_TYPE, type);
                if (!bb) {
                    return false;
                }
            }
        }
        return true;
    }
    
    boolean exists(Environment env, Producer prod, IDatatype s, IDatatype p, IDatatype o) {
        for (Edge e : prod.getEdges(s, p, o, null)) {
            return e != null;
        }
        return false;
    }

   

}
