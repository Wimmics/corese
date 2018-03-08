package fr.inria.corese.sparql.triple.parser;


import fr.inria.corese.sparql.triple.cst.KeywordPP;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class has been created to manage the distance between 2 concepts.<br>
 * It implements score ?s { PATTERN }
 * <br>
 * @author Olivier Corby
 * @deprecated
 */

public class Score extends And {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	String score;
	Atom id;

  public Score() {
  }

  /**
   * Model the scope of graph ?src { pattern }
   *
   */
  public Score(String name, Exp exp) {
    super(exp);
    score = name;
  }
  
  public static Score create(Atom name, Exp exp){
	  if (! (exp instanceof And)){
		  exp = new And(exp);
	  }
	  Score s = new Score(name.getName(), exp);
	  s.id = name;
	  return s;
  }
  
  public Atom getName(){
	  return id;
  }
  
  public boolean isScore(){
	  return true;
  }


  Exp duplicate(){
    Score exp = new Score();
    exp.score = score;
    return exp;
  }

//  void setScore(Vector<String> names){
//    Vector<String> vec = names;
//    if (! names.contains(score)){
//      vec = new Vector<String>();
//      vec.addAll(names);
//      vec.add(score);
//    }
//    eget(0).setScore(vec);
//  }

  public String toString(){
      return KeywordPP.SCORE + KeywordPP.SPACE + score + KeywordPP.OPEN_BRACKET + super.toString() + KeywordPP.CLOSE_BRACKET;
  }


}