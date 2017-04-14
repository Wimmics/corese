package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.api.ExpressionVisitor;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.corese.compiler.java.JavaCompiler;
import fr.inria.edelweiss.kgram.api.core.ExprType;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie 
 */

public class Variable extends Atom {
	
	private boolean isBlankNode = false;
	private boolean isPath = false; // use case ?x $path ?y
	private boolean isVisited = false;
	private int index = ExprType.UNBOUND;
        private int type  = ExprType.GLOBAL;
        
	List<Variable> lVar;
        private Variable proxy;
	// var as IDatatype for comparing variables in KGRAM
	IDatatype dt;
	
	public Variable(String str){
		super(str);
	}
	
	public static Variable create(String str){
		return new Variable(str);
	}
	
        @Override
	public StringBuffer toString(StringBuffer sb){
		if (isBlankNode()) {
			if (isBlankVariable(name)){
				// variable for blank node, replace ?_ by _:
				sb.append( KeywordPP.BN + name.substring(2,name.length()));
			}
			else {
				// remove ?
				sb.append(KeywordPP.BN + name.substring(1,name.length()));
			}
		} 
		else { 
			sb.append( name);
		}
		return sb;
	}
        
       
    @Override
    public void toJava(JavaCompiler jc) {
        jc.toJava(this);
    }
	
        @Override
	public boolean equals(Object o){
		if (o instanceof Variable){
			Variable var = (Variable) o;
			if (getName().equals(var.getName())){
				return true;
			}
		}
		return false;
	}
        
        public String getSimpleName(){
            return getName().substring(1);
        }
	
	public void setPath(boolean b){
		isPath = b;
	}
	
        @Override
	public boolean isPath(){
		return isPath;
	}
	
	public void addVariable(Variable var){
		if (lVar == null) lVar = new ArrayList<Variable>();
		lVar.add(var);
	}
	
	public List<Variable> getVariableList(){
		return lVar;
	}
	
        @Override
	public boolean isVisited(){
		return isVisited;
	}
	
        @Override
	public void setVisited(boolean b){
		isVisited = b;
	}
	
	public static boolean isBlankVariable(String name){
		return name.startsWith(ASTQuery.BNVAR);
	}
	
	/**
	 * use case: select fun(?x) as ?y
	 * rewrite occurrences of ?y as fun(?x)
	 */
        @Override
	public Expression process(ASTQuery ast){
		if (isVisited()){
			setVisited(false);
			return null;
		}
		Expression exp = ast.getExpression(name);
		if (exp != null && (! exp.isFunctional())){ // || ! ast.isKgram())){
			// use case: do not rewrite ?val
			// xpath() as ?val
			// xsd:integer(?val)
			setVisited(true);
			Expression ee = exp.process(ast);
			setVisited(false);
			return ee;
		}
		else {
			return this;
		}
	}
	
	public boolean  isType (int type){
		return false;
	}
	
        @Override
	public boolean isVariable(){
		return true;
	}
	
        @Override
	public boolean isSimpleVariable(){
		return ! isBlankNode();
	}
	
	
        @Override
	Bind validate(Bind env){
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
	public int type(){
		return ExprType.VARIABLE;
	}
	

        @Override
        public void getVariables(List<String> list, boolean excludeLocal) {
                // TODO Auto-generated method stub
		if (! list.contains(getName()) 
                        && !(excludeLocal && isLocal())){
                        list.add(getName());                                    
		}
        }
        
        @Override
	public int getIndex() {
		return index ;
	}

	@Override
	public void setIndex(int n) {
		index = n;
	}
	
	// fake value in case where a variable node is used as a target value node
	// see ProducerDefault
	// use case: project a query graph on itself
        @Override
	public Object getValue(){
		return getDatatypeValue();
	}
	
	public IDatatype getDatatypeValue2(){
		if (dt == null){
			dt = Constant.create(name).getDatatypeValue();
		}
		return dt;
	}
	
	public Constant getConstant2(){
		if (isBlankNode()){
			return Constant.createBlank(getLabel());
		}
		else {
			return Constant.create(getLabel());
		}
	}
        
        @Override
        public IDatatype getDatatypeValue(){
		if (dt == null){
			dt = getConstant().getDatatypeValue();
		}
		return dt;
	}
	
        @Override
	public Constant getConstant(){
            return Constant.createBlank(getLabel());
	}

        @Override
        public Variable copy(Variable o, Variable n){
            if (this.equals(o)){
                Variable var = create(n.getName());
                return var;
            }
            else {
                return this;
            }
        }
        
        @Override
        void visit(ExpressionVisitor v){
            v.visit(this);
        }
        
       public boolean isLocal(){
            //return index == ExprType.LOCAL;
            return type == ExprType.LOCAL;
        }
       
       public void localize(){
          // index = ExprType.LOCAL;
           setType(ExprType.LOCAL);
       }
       
       void undef(){
          //index = ExprType.UNDEF; 
          setType(ExprType.UNDEF);
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
 
	
}