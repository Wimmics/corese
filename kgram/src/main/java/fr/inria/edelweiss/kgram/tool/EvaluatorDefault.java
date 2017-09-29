package fr.inria.edelweiss.kgram.tool;

import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.filter.Extension;
import fr.inria.edelweiss.kgram.filter.Proxy;

public class EvaluatorDefault implements Evaluator {

	@Override
	public Node eval(Filter f, Environment e, Producer p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> evalList(Filter f, Environment e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean test(Filter f, Environment e) {
		// TODO Auto-generated method stub
		return true;
	}
        
        public boolean test(Filter f, Environment e, Producer p) {
		// TODO Auto-generated method stub
		return true;
	}
       

	@Override
	public Mappings eval(Filter f, Environment e, List<Node> nodes) {
		// TODO Auto-generated method stub
		return new Mappings();
	}

	@Override
	public void setMode(int mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object eval(Expr f, Environment e, Producer p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMode() {
		// TODO Auto-generated method stub
		return KGRAM_MODE;
	}

    @Override
    public void setProducer(Producer p) {
    }

    @Override
    public void addResultListener(ResultListener rl) {
    }

    @Override
    public void setKGRAM(Object o) {
    }

    @Override
    public Node cast(Object obj, Environment e, Producer p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start(Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void finish(Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object eval(Expr f, Environment e, Producer p, Object[] values, Extension ext) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object eval(Expr f, Environment e, Producer p, Object[] values) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object eval(Expr f, Environment e, Producer p, Object[] values, Expr ee) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Expr getDefine(Environment env, String name, int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compare(Environment env, Producer p, Node n1, Node n2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Proxy getProxy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Expr getDefine(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getEval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDebug(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Expr getDefine(Expr exp, Environment env, Producer p, int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Expr getDefineMethod(Environment env, String name, Object type, Object[] values) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object eval(String name, Environment e, Producer p, Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object eval(Expr f, Environment e, Producer p, Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    

}
