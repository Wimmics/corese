package fr.inria.corese.sparql.triple.parser;


import fr.inria.corese.sparql.triple.cst.KeywordPP;
import java.util.List;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class implements graph ?src { PATTERN }
 * <br>
 * @author Olivier Corby
 */

public class Source extends And {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	//String source;
	Atom asource;
	boolean state = false;
	boolean leaf = false;
	private boolean isRec = false;

  public Source() {}

  /**
   * Model the scope of graph ?src { pattern }
   *
   */
  public Source(Atom src, Exp exp) {
    super(exp);
    //source = src.getName();
    asource = src;
  }
  
  public static Source create(Atom src, Exp exp){
	  if (! exp.isAnd()){
		  exp = new BasicGraphPattern(exp);
	  }
	  Source s = new Source(src, exp);
	  return s;
  }
  
        @Override
  public Source copy() {
      Source exp = super.copy().getNamedGraph();
      exp.asource = asource;
      return exp;
  }
  
  @Override
  public Source getNamedGraph(){
      return this;
  }
  
  public Atom getSource(){
	  return asource;
  }
  
  @Override
  void getVariables(List<Variable> list) {
      super.getVariables(list);
      getSource().getVariables(list);
  }
  
        @Override
  public Exp getBodyExp() {
      return get(0);
  }
  
  public void setState(boolean s){
	  state = s;
  }
  
  public void setLeaf(boolean s){
	  leaf = s;
  }
  
  boolean isState(){
	  return state;
  }
  
        @Override
  public boolean isGraph(){
	  return true;
  }
  
        @Override
  public void setRec(boolean b){
	  isRec = b;
  }
  
  public boolean isRec(){
	  return isRec;
  }
  
 
//  Exp duplicate(){
//    Source exp = new Source();
//    exp.asource = asource;
//    exp.source = source;
//    exp.state = state;
//    exp.leaf = leaf;
//    exp.isRec = isRec;
//    return exp;
//  }


        @Override
    public ASTBuffer toString(ASTBuffer sb) {
        sb.append(KeywordPP.GRAPH + KeywordPP.SPACE);
        sb.append(asource);
        sb.append(KeywordPP.SPACE);
        getBodyExp().pretty(sb);
        return sb;       
    }

 
        @Override
  public boolean validateData(ASTQuery ast){
	  if (asource.isVariable()) return false;

	  Exp ee = this;
	  if (size() == 1 && get(0) instanceof And){
		  // dive into {}
		  ee = get(0);
	  }

	  for (Exp exp : ee.getBody()){
		  if (! (exp.isTriple() && exp.validateData(ast))){
			  return false;
		  }
	  }
	  return true;
  }
  
  
        @Override
  public boolean validate(ASTQuery ast, boolean exist){
	  if (asource.isVariable()){
		  ast.bind(asource.getVariable());
		  if (! exist){
			  ast.defSelect(asource.getVariable());
		  }
	  }
	  return super.validate(ast, exist);
  }

  

}