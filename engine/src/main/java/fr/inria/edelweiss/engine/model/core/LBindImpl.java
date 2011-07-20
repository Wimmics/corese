package fr.inria.edelweiss.engine.model.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.engine.model.api.Bind;
import fr.inria.edelweiss.engine.model.api.Clause;
import fr.inria.edelweiss.engine.model.api.LBind;
import fr.inria.edelweiss.kgram.core.Query;

public class LBindImpl implements LBind {

	private List<Bind> lBindInstance;
	ASTQuery ast;
	private boolean tripleFound;
	
	public int size(){
		return lBindInstance.size();
	}
	
//	public IResult get(int i){
//		return lBindInstance.get(i);
//	}
	
//	public void remove(IResult r){
//		lBindInstance.remove(r);
//	}

	public List<Bind> getLBindInstance() {
		return lBindInstance;
	}

	public void setLBindInstance(List<Bind> bindInstance) {
		lBindInstance = bindInstance;
	}
	
	public boolean isTripleFound() {
		return tripleFound;
	}

	public void setTripleFounded(boolean tripleFounded) {
		this.tripleFound = tripleFounded;
	}

	/**
	 * constructor which create an instance of the object lBindInstance
	 */
	public LBindImpl() {
		lBindInstance=new ArrayList<Bind>();
		tripleFound=false;
	}
	
	public void setAST(ASTQuery q){
		ast = q;
	}
	
	public ASTQuery getAST(){
		return ast;
	}

	/**
	 * to iterate the list of binds
	 */
	public Iterator<Bind> iterator() {
		// TODO Auto-generated method stub
		return lBindInstance.iterator();
	}
	
	public String toString(){
		String str = "{";
		for (Bind b : lBindInstance){
			str += b.toString() + "\n";
		}
		str += "}";
		return str;
	}
	
	/**
     *	add the values of the list of binds given 
     *  to the list of binds calling the method
     */
	public LBind union(LBind lBind) {
		if(lBind != null){
			for (Bind bind:lBind){
				if (! contains(bind)){
					add(bind);
				}
			}
		}
		
		//to say that we obtain a result in the list of binds ; the list may be empty while we have a result in the process
		tripleFound=tripleFound || lBind.isTripleFound();
		
		return this;
	}
	
	public LBind union(Bind bind) {
		if (! contains(bind)){
			add(bind);

			// to say that we obtain a result in the list of binds ; 
			// the list may be empty while we have a result in the process
			tripleFound = tripleFound || bind.size() > 0;
		}

		return this;
	}

	
	boolean contains(Bind b){
		for (Bind bind : lBindInstance){
			if (bind.equivalent(b)){
				return true;
			}
		}
		return false;
	}
	

	public void add(Bind bind) {
		// TODO Auto-generated method stub
		lBindInstance.add(bind);
	}

	/**
	 * bind variables from clause to value from rule (or rule bind)
     * clauseQuery: John grandFather ?y
     * list of binds calling the method : {{?a = John, ?c = Mark}}
     * rule : construct {?a grandFather ?c} where {?a father ?b . ?b father ?c} 
     * ruleConclusion : ?a grandFather ?c
     * 
     * RETURNS : {{?a = John, ?y = Mark}}
     */
	public LBind rename(Clause ruleConclusion, Clause clauseQuery, Bind oldBind) {
		//List of binds to return
		LBind lBind=new LBindImpl();
		
		for (Bind bind : this){
			//for each object bind of the list calling the method
			//To iterate the element of the conclusion of the rule
			
			Bind bind2 = bind.unify(clauseQuery, ruleConclusion);
			
			if (bind2 != null){
				// add  the old bind to the new bind : 
				// because we omit it in the method Backward.backward
				bind2.put(oldBind);

				lBind.add(bind2);
			}
		}
		
		lBind.setTripleFounded(tripleFound);
		
		return lBind;
	}
	
	


}
