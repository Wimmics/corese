package fr.inria.edelweiss.kgram.tool;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.filter.Proxy;

public class ProxyDefault implements Proxy, ExprType {

	@Override
	public Object eval(Expr exp, Environment env, Producer p, Object o1, Object o2) {
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
	public Object getConstantValue(Object value) {
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

}
