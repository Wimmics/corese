package fr.inria.edelweiss.engine.model.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.api.ElementClause;
import fr.inria.acacia.corese.triple.cst.RDFS;

import fr.inria.edelweiss.engine.model.api.Clause;

public class ClauseImpl implements Clause {
	
	private List<ElementClause> elements;
	private Triple triple;
	
	boolean isGround = false;
	
	/**
	 * constructor instantiating the attributes of the instance of the clause
	 */
	public ClauseImpl(ASTQuery ast, Exp exp) {
				
		//get the triple to transform to an object clause
		triple=(Triple)exp;
		
		//initialize the list of clause-elements
		elements=new ArrayList<ElementClause>();
				
		elements.add(triple.getSubject());
		elements.add(triple.getPredicate());
		elements.add(triple.getObject());
		
		if (triple.getSource() != null){
			elements.add(triple.getSource());
		}
		//checkGround();
	}
	
	public String toString(){
		return triple.toSparql();
	}

	public Iterator<ElementClause> iterator() {
		return elements.iterator();
	}
	
	public ElementClause get(int i){
		return elements.get(i);
	}
	
	public int size(){
		return elements.size();
	}
	
	public Triple getTriple(){
		return triple;
	}

	/**
	 * Triple that does not need to be proved by rules
	 */
	void checkGround(){
		String object = triple.getObject().getName();
		if (triple.isType()){
			// xxx rdf:type owl:TransitiveProperty
			if (object.startsWith(RDFS.OWL)){
				isGround = true;
			}
		}
	}

	
	@Override
	public boolean isGround() {
		return isGround;
	}
	
	public void setGround(boolean b){
		isGround = b;
	}

}
