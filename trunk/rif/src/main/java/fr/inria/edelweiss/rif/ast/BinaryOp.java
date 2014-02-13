package fr.inria.edelweiss.rif.ast;

public abstract class BinaryOp extends Atomic {
	
	protected Term first, second ;
	
	public Term getFirst() {
		return this.first ;
	}
	
	public Term getSecond() {
		return this.second ;
	}
}
