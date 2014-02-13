package fr.inria.edelweiss.rif.ast;

/** Subclass ::= TERM '##' TERM */
public class Subclass extends BinaryOp {
	
	private Subclass(Term subC, Term superC) {
		this.first = subC ;
		this.second = superC ;
	}
	
	public static Subclass create(Term subC, Term superC) {
		return new Subclass(subC, superC) ;
	}
	
	public Term getSubclass() {
		return this.first ;
	}
	
	public Term getSuperClass() {
		return this.second ;
	}
}
