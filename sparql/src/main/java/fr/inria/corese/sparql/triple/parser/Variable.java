package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.api.Computer;
import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 * <p>
 * Title: Corese</p>
 * <p>
 * Description: A Semantic Search Engine</p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Acacia</p>
 *
 * @author Olivier Corby & Olivier Savoie
 */
public class Variable extends Atom {

    private boolean isBlankNode = false;
    private boolean isPath = false; // use case ?x $path ?y
    private boolean isVisited = false;
    private int index = ExprType.UNBOUND;
    private int type = ExprType.GLOBAL;

    List<Variable> lVar;
    private Variable proxy;
    private Variable variableDeclaration;
    // var as IDatatype for comparing variables in KGRAM
    IDatatype dt;

    public Variable(String str) {
        super(str);
    }

    public static Variable create(String str) {
        return new Variable(str);
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        if (isBlankNode()) {
            if (isBlankVariable(name)) {
                // variable for blank node, replace ?_ by _:
                sb.append(KeywordPP.BN + name.substring(2, name.length()));
            } else {
                // remove ?
                sb.append(KeywordPP.BN + name.substring(1, name.length()));
            }
        } else {
            sb.append(name);
        }
        return sb;
    }

    @Override
    public void toJava(JavaCompiler jc) {
        jc.toJava(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Variable) {
            Variable var = (Variable) o;
            if (getName().equals(var.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    void getVariables(List<Variable> list) {
        if (!list.contains(this)) {
            list.add(this);
        }
    }

    public String getSimpleName() {
        return getName().substring(1);
    }

    public void setPath(boolean b) {
        isPath = b;
    }

    @Override
    public boolean isPath() {
        return isPath;
    }

    public void addVariable(Variable var) {
        if (lVar == null) {
            lVar = new ArrayList<Variable>();
        }
        lVar.add(var);
    }

    public List<Variable> getVariableList() {
        return lVar;
    }

    @Override
    public boolean isVisited() {
        return isVisited;
    }

    @Override
    public void setVisited(boolean b) {
        isVisited = b;
    }

    public static boolean isBlankVariable(String name) {
        return name.startsWith(ASTQuery.BNVAR);
    }

    /**
     * use case: select fun(?x) as ?y rewrite occurrences of ?y as fun(?x)
     */
    @Override
    public Expression process(ASTQuery ast) {
        if (isVisited()) {
            setVisited(false);
            return null;
        }
        Expression exp = ast.getExpression(name);
        if (exp != null && (!exp.isFunctional())) { // || ! ast.isKgram())){
            // use case: do not rewrite ?val
            // xpath() as ?val
            // xsd:integer(?val)
            setVisited(true);
            Expression ee = exp.process(ast);
            setVisited(false);
            return ee;
        } else {
            return this;
        }
    }

    public boolean isType(int type) {
        return false;
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    @Override
    public boolean isSimpleVariable() {
        return !isBlankNode();
    }

    @Override
    Bind validate(Bind env) {
        env.bind(getName());
        return env;
    }

    @Override
    public Variable getVariable() {
        return this;
    }

    @Override
    public boolean isBlankNode() {
        return isBlankNode;
    }

    public void setBlankNode(boolean isBlankNode) {
        this.isBlankNode = isBlankNode;
    }

    /**
     * KGRAM
     */
    @Override
    public int type() {
        return ExprType.VARIABLE;
    }

    @Override
    public void getVariables(List<String> list, boolean excludeLocal) {
        // TODO Auto-generated method stub
        if (!list.contains(getName())
                && !(excludeLocal && isLocal())) {
            list.add(getName());
        }
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int n) {
        index = n;
    }

    // fake value in case where a variable node is used as a target value node
    // see ProducerDefault
    // use case: project a query graph on itself
    @Override
    public Object getValue() {
        return getDatatypeValue();
    }

    @Override
    public IDatatype getDatatypeValue() {
        if (dt == null) {
            dt = getConstant().getDatatypeValue();
            dt.setVariable(true);
        }
        return dt;
    }

    @Override
    public Constant getConstant() {
        return Constant.createBlank(getLabel());
     }

    @Override
    public Variable copy(Variable o, Variable n) {
        if (this.equals(o)) {
            Variable var = create(n.getName());
            return var;
        } else {
            return this;
        }
    }

    @Override
    public void visit(ExpressionVisitor v) {
        v.visit(this);
    }

    public boolean isLocal() {
        return type == ExprType.LOCAL;
    }

    public void localize() {
        setType(ExprType.LOCAL);
    }

    void undef() {
        //setType(ExprType.UNDEF);
        setType(ExprType.LOCAL);
    }

    /**
     * @return the type
     */
    @Override
    public int subtype() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void setSubtype(int type) {
        setType(type);
    }

    /**
     * @return the var
     */
    public Variable getVariableProxy() {
        return proxy;
    }

    public Variable getProxyOrSelf() {
        return (proxy == null) ? this : proxy;
    }

    /**
     * @param var the var to set
     */
    public void setVariableProxy(Variable var) {
        this.proxy = var;
    }

    @Override
    public Variable getDefinition() {
        return getDeclaration();
    }

    /**
     * @return the superVariable
     */
    public Variable getDeclaration() {
        return variableDeclaration;
    }

    /**
     * @param superVariable the superVariable to set
     */
    public void setDeclaration(Variable superVariable) {
        this.variableDeclaration = superVariable;
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        Node node = env.getNode(this);
        if (node == null) {
            return null;
        }
        return (IDatatype) node.getDatatypeValue();
    }

}
