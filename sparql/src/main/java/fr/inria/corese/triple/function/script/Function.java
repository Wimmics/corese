package fr.inria.corese.triple.function.script;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.api.ExpressionVisitor;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.corese.compiler.java.JavaCompiler;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private boolean lambda = false;
    private boolean visited = false;
    
    private IDatatype dt;
    
    Metadata annot;
    private HashMap<String, Constant> table;
       

    public Function(Term fun, Expression body) {
        super(Processor.FUNCTION, fun, body);
        fun.setExpression(this);
        body.setExpression(this);
        table = new HashMap<>();
    }

    @Override
    public Term getFunction() {
        return getSignature();
    }
    
    public Term getSignature(){
        return getArg(0).getTerm();
    }
    
    @Override
    public IDatatype getDatatypeValue(){
        if (dt != null){
            return dt;
        }
        return getFunction().getCName().getDatatypeValue();
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        return getDatatypeValue();
    }

    @Override
    public Expression getBody() {
        return getArg(1);
    }
    
    public Constant getType(Variable var){
        return getTable().get(var.getLabel());
    }
    
    public IDatatype getDatatype(Variable var){
        Constant cst = getType(var);
        if (cst != null){
            return cst.getDatatypeValue();
        }
        return null;
    }
    
    @Override
    public Expression compile(ASTQuery ast){
         Expression exp = super.compile(ast);
         typecheck(ast);
         if (isTrace()){
             System.out.println(this);
         }
         return exp;
    }

    @Override
    public StringBuffer toString(StringBuffer sb) {
        if (getMetadata() != null){
//            sb.append(getMetadata());
//            sb.append(Term.NL);
        }
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
    
    
    public Metadata getMetadata(){
        return annot;
    }
    
    public boolean hasMetadata(){
        return annot != null;
    }
    
    public boolean hasMetadata(int type) {
        return annot != null && annot.hasMetadata(type);
    }
    
    @Override
    public List<String> getMetadataValues(String name){
        if (getMetadata() == null) return null;
        return getMetadata().getValues(name);
    }
    
    public void annotate(Metadata m){
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
    
    public void defineLambda(){
        setLambda(true);;
        dt = DatatypeMap.createObject(getDatatypeValue().stringValue(), this);
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
    
    boolean typecheck(ASTQuery ast){
        Term t = getSignature();
        List<Variable> list = new ArrayList<Variable>();
        int i = 1;
        for (Expression var : t.getArgs()) {
            if (list.contains(var.getVariable())){
                ast.addError("Duplicate parameter: " + var + " in: \n" + toString());
                ast.addFail(true);
                return false;
            }
            else {
                list.add(var.getVariable());
            }
        }
        return true;
    }   

}
