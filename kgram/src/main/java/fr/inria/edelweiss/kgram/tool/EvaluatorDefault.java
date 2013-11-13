package fr.inria.edelweiss.kgram.tool;

import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;

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

}
