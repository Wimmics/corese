package fr.inria.edelweiss.rif.ast;

public class Annotation extends Statement {
	private Const identifier = null ;
	private Frame meta = null ;
	private And<Frame> frameConj = null ;
	
	private Annotation() {}
	
	private Annotation(Const identifier) {
		this.setIdentifier(identifier);
	}
	
	private Annotation(Frame meta) {
		this.setFrameMeta(meta);
	}
	
	private Annotation(And<Frame> frameConj) {
		this.setFrameConj(frameConj);
	}
	
	private Annotation(Const identifier, Frame meta) {
		this(identifier) ;
		this.setFrameMeta(meta);
	}
	
	private Annotation(Const identifier, And<Frame> frameConj) {
		this(identifier) ;
		this.setFrameConj(frameConj);
	}
	
	public static Annotation create() {
		return new Annotation() ;
	}
	
	public static Annotation create(Const identifier) {
		return new Annotation(identifier) ;
	}
	
	public static Annotation create(Frame meta) {
		return new Annotation(meta) ;
	}
	
	public static Annotation create(And<Frame> frameConj) {
		return new Annotation(frameConj) ;
	}
	
	public static Annotation create(Const identifier, Frame meta) {
		return new Annotation(identifier, meta) ;
	}
	
	public static Annotation create(Const identifier, And<Frame> frameConj) {
		return new Annotation(identifier, frameConj) ;
	}

	public void setIdentifier(Const identifier) {
		this.identifier = identifier;
	}

	public Const getIdentifier() {
		return identifier;
	}

	public void setFrameMeta(Frame meta) {
		this.meta = meta;
	}

	public Frame getFrameMeta() {
		return meta;
	}

	public void setFrameConj(And<Frame> frameConj) {
		this.frameConj = frameConj;
	}

	public And<Frame> getFrameConj() {
		return frameConj;
	}
	
	

}
