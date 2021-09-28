package fr.inria.corese.kgram.tool;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Matcher;

public class MatcherDefault implements Matcher {

	@Override
	public boolean match(Edge q, Edge r, Environment env) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean match(Node q, Node t, Environment env) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean same(Node qNode, Node q, Node t, Environment env) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setMode(int mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMode() {
		// TODO Auto-generated method stub
		return 0;
	}

}
