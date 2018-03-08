package fr.inria.corese.rif.ast;


/** Racine des elements non terminaux du langage */
public abstract class Statement {
	
	private Annotation meta ;
	
	public Annotation getMeta() {
		return this.meta ;
	}
	
	public void setMeta(Annotation a) {
		this.meta = a ;
	}
}
