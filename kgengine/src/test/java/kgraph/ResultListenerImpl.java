package kgraph;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.path.Path;

public class ResultListenerImpl implements ResultListener {

	int count = 0;
	
	int getCount(){
		return count;
	}
	
	@Override
	public boolean process(Environment env) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(Path path) {
		// TODO Auto-generated method stub
		//System.out.println(path);
		count ++;
		return false;
	}

	@Override
	public boolean enter(Entity ent, Regex exp, int size) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean leave(Entity ent, Regex exp, int size) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public Exp listen(Exp exp, int n) {
        return exp  ;  
        }

    @Override
    public void listen(Expr exp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean listen(Edge edge, Entity ent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
