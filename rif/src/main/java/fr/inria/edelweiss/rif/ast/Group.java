package fr.inria.edelweiss.rif.ast;

import java.util.Vector;

import fr.inria.edelweiss.rif.api.IRule;

/** Group of rules or nested groups */
public class Group extends Vector<IRule> {

	private Annotation meta ;
	
	private static final long serialVersionUID = 1L;

	private Vector<Group> nestedGroups ;
	
	private Group() {}
	
	public static Group create() {
		return new Group() ;
	}
	
	public void addNestedGroup(Group g) {
		if(nestedGroups == null) nestedGroups = new Vector<Group>() ;
		this.nestedGroups.add(g) ;
	}

	public void setMeta(Annotation meta) {
		this.meta = meta;
	}

	public Annotation getMeta() {
		return meta;
	}
	
}
