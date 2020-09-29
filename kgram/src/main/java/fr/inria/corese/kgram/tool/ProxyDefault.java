package fr.inria.corese.kgram.tool;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.filter.Proxy;
import java.util.List;

public class ProxyDefault implements Proxy, ExprType {

	@Override
	public Object term(Expr exp, Environment env, Producer p, Object o1, Object o2) {
		// TODO Auto-generated method stub
		switch(exp.oper()){
		case EQ: return o1 == o2;
			
		case NEQ: return o1 != o2;
			
		}
		return null;
	}

	@Override
	public Object eval(Expr exp, Environment env, Producer p, Object[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Object function(Expr exp, Environment env, Producer p) {
		return null;
	}
	
	public Object function(Expr exp, Environment env, Producer p, Object o1) {
		return o1;
	}
	
	public Object function(Expr exp, Environment env, Producer p, Object o1, Object o2) {
		return o1;
	}

	@Override
	public Node getConstantValue(Node value) {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public Object getValue(boolean b) {
		// TODO Auto-generated method stub
		return b;
	}

	@Override
	public Object getValue(int value) {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public boolean isTrue(Object value) {
		// TODO Auto-generated method stub
		if (value instanceof Boolean){
			return (Boolean) value;
		}
		return false;
	}

	@Override
	public boolean isTrueAble(Object value) {
		// TODO Auto-generated method stub
		return true;
	}

	public void setPlugin(Proxy p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object aggregate(Expr exp, Environment env, Producer p, Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(double value, String datatype) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMode(int mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEvaluator(Evaluator eval) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Evaluator getEvaluator() {
		// TODO Auto-generated method stub
		return null;
	}

    //@Override
    public Expr createFunction(String name, List<Object> args, Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Proxy getPlugin() {
        return null;    
    }

    @Override
    public Object cast(Object obj, Environment env, Producer p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start(Producer p, Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void finish(Producer p, Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Expr decode(Expr exp, Environment env, Producer p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
//    public int compare(Environment env, Producer p, Node o1, Node o2) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    @Override
    public Object[] createParam(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getBufferedValue(StringBuilder sb, Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValue(Object val, Object obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getResultValue(Object obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProducer(Producer p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object eval(Expr exp, Environment env, Producer p, Object[] args, Expr def) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   // @Override
    public Expr getDefine(Expr exp, Environment env, String name, int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
