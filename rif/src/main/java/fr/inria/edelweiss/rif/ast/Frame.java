package fr.inria.edelweiss.rif.ast;

import java.util.HashMap;

import fr.inria.edelweiss.rif.api.IConnectible;

public class Frame extends Atomic implements IConnectible {

	private Term objectIdentifier ;
	
	private HashMap<Term, Term> pairs ;

	private Frame(Term objId) {
		this.objectIdentifier = objId ;
		this.pairs = new HashMap<Term, Term>() ;
	}
	
	private Frame(Term objId, HashMap<Term, Term> pairs) {
		this(objId) ;
		this.pairs = pairs ;
	}
	
	public static Frame create(Term objectIdentifier) {
		return new Frame(objectIdentifier) ;
	}
	
	public static Frame create(Term objectIdentifier, HashMap<Term, Term> pairs) {
		return new Frame(objectIdentifier, pairs) ;
	}

	public Term getObjectIdentifier() {
		return this.objectIdentifier ;
	}
	
	public HashMap<Term, Term> getPairs() {
		return this.pairs ;
	}
	
	public void addAttValPair(Term attribute, Term value) {
		this.pairs.put(attribute, value) ;
	}
	
}
