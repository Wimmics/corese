package fr.inria.corese.kgram.tool;

import java.util.Map;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.event.EventManager;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;

public class EnvironmentImpl implements Environment {
	Query query;
	
	public EnvironmentImpl(){
	}
	
	EnvironmentImpl(Query q){
		query = q;
	}
	
	public static EnvironmentImpl create(Query q){
		return new EnvironmentImpl(q);
	}
	
        @Override
	public int count() {
		return 0;
	}
	
        @Override
	public Node getNode(Expr var){
		return null;
	}
        
        @Override
        public int size() {
            return 0;
        }

	@Override
	public Node getNode(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getNode(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getQueryNode(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getQueryNode(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBound(Node node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int pathLength(Node node) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int pathWeight(Node node) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public void aggregate(Evaluator eval, Producer p, Filter f) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public Query getQuery() {
		// TODO Auto-generated method stub
		return query;
	}

	@Override
	public EventManager getEventManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getGraphNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasEventManager() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setObject(Object o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExp(Exp exp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Exp getExp() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getMap() {
		return null;
	}

    @Override
    public Edge[] getEdges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node[] getNodes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node[] getQueryNodes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path getPath(Node qNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mappings getMappings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node get(Expr var) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ASTExtension getExtension() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Binding getBind() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public boolean hasBind() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApproximateSearchEnv getAppxSearchEnv() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBind(Binding b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterable<Mapping> getAggregate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void aggregate(Mapping m, int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mapping getMapping() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Eval getEval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setEval(Eval e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ProcessVisitor getVisitor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setReport(IDatatype dt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDatatype getReport() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

	

}
