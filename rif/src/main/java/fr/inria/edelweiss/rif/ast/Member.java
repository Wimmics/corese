package fr.inria.edelweiss.rif.ast;

/** Member ::= TERM '#' TERM */
public class Member extends BinaryOp {
	
	private Member(Term inst, Term cl) {
		this.first = inst ;
		this.second = cl ;
	}
	
	public static Member create(Term instance, Term classMembership) {
		return new Member(instance, classMembership) ;
	}
	
	public Term getInstance() {
		return this.first ;
	}
	
	public Term getClassMembership() {
		return this.second ;
	}
}
