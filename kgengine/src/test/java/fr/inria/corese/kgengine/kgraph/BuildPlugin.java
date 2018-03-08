package fr.inria.corese.kgengine.kgraph;

import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgtool.load.BuildImpl;

public class BuildPlugin extends BuildImpl {

	BuildPlugin(Graph g){
		super(g);
	}
	
	public void statement(AResource subj, AResource pred, ALiteral lit) {
		super.statement(subj, pred, lit);
		System.out.println(subj + " " + pred + " " + lit);
	}
	
	public void statement(AResource subj, AResource pred, AResource lit) {
		super.statement(subj, pred, lit);
		System.out.println(subj + " " + pred + " " + lit);
	}
	
}
