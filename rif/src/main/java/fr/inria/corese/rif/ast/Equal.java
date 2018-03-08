package fr.inria.corese.rif.ast;

/** Equal ::= TERM '=' TERM */
public class Equal extends BinaryOp {

	private Equal(Term left, Term right) {
		this.first = left ;
		this.second = right ;
	}
	
	public static Equal create(Term left, Term right) {
		return new Equal(left, right) ;
	}
	
	public Term getLeft() {
		return this.first ;
	}
	
	public Term getRight() {
		return this.second ;
	}
}
