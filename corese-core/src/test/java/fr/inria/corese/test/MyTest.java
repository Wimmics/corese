
package fr.inria.corese.test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.Date;
import org.junit.Test;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class MyTest {
    
    public static void main(String[] args) throws EngineException, LoadException {
        new MyTest().process();
    }
    
    @Test
    public void process() throws LoadException, EngineException {
        Graph g = Graph.create();
        String q = "select (kg:sparql('select * where { values ?i { unnest(xt:iota(5))} }') as ?res) where {"
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        System.out.println(dt.getNodeObject());
        Date d;
        //assertEquals(10, dt.intValue());
    } 

}
