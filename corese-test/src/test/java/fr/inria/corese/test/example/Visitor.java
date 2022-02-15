package fr.inria.corese.test.example;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.ProcessVisitorDefault;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.List;
import org.junit.Test;


public class Visitor extends ProcessVisitorDefault {
    
    @Test
    public void process() throws EngineException {
        Property.set(Property.Value.RDF_STAR, true);
        
        // update events require @event @update annotation
        String i = "@event @update insert data { "
                + "us:John foaf:knows us:Jack"
                + " {| us:date '2022' |} "
                + "}";
        
        String q = "@event select * where {"
                + "?s ?p ?o"
                + "}";
        
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        
        exec.query(i, this);
        Mappings map = exec.query(q, this);
        System.out.println("res:\n"+map);
    }
    
    
    @Override
    public IDatatype before(Query q) {
        System.out.println("query:\n" + q.getAST());
        return defaultValue();
    }
    
    
    @Override
    public IDatatype insert(IDatatype path, Edge edge) {
        System.out.println("insert: " + edge);
        return defaultValue();
    }
    
    @Override
    public IDatatype delete(Edge edge) { 
        System.out.println("delete: " + edge);
        return defaultValue();
    }

    @Override
    public IDatatype update(Query q, List<Edge> delete, List<Edge> insert) { 
        System.out.println("delete: " + delete);
        System.out.println("insert: " + insert);
        return defaultValue();
    }

    
    
    
}
