package fr.inria.corese.rif.ast;

/** Racine des terminaux du langage */
public abstract class Term extends Symbol {
	private Annotation meta ;
	
	public Annotation getMeta() {
		return this.meta ;
	}
	
	public void setMeta(Annotation a) {
		this.meta = a ;
	}
}
