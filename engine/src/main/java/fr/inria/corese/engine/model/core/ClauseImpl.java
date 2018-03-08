package fr.inria.corese.engine.model.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.api.ElementClause;
import fr.inria.corese.sparql.triple.cst.RDFS;

import fr.inria.corese.engine.model.api.Clause;

public class ClauseImpl implements Clause {
	
	private List<ElementClause> elements;
	private Triple triple;
	
	boolean isGround = false;
	
	/**
	 * constructor instantiating the attributes of the instance of the clause
	 */
	public ClauseImpl(ASTQuery ast, Exp exp, boolean isBlank) {
				
		//get the triple to transform to an object clause
		triple=(Triple)exp;
		
		//initialize the list of clause-elements
		elements=new ArrayList<ElementClause>();
		
		// Use case:  SPARQL query return result using select *
		// must transform blanks into std variables
		Atom sub = triple.getSubject();
		Atom obj = triple.getObject();

		if (sub.isBlankNode() && ! isBlank){
			sub.getVariable().setBlankNode(false);
		}
		if (obj.isBlankNode() && ! isBlank){
			obj.getVariable().setBlankNode(false);
		}
		
		elements.add(triple.getSubject());
		elements.add(triple.getPredicate());
		elements.add(triple.getObject());
		
		if (triple.getSource() != null){
			elements.add(triple.getSource());
		}
		
	}
	
	// construct need blank
	public static ClauseImpl conclusion(ASTQuery ast, Exp exp){
		return new ClauseImpl(ast, exp, true);
	}
	
	// where need no blank but variable
	public static ClauseImpl condition(ASTQuery ast, Exp exp){
		return new ClauseImpl(ast, exp, false);
	}
	

	
	public String toString(){
		return triple.toString();
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
