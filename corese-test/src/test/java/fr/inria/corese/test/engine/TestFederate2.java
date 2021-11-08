package fr.inria.corese.test.engine;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TestFederate2 {

    String corese = "corese.inria.fr";
    String local  = "localhost:8080";
    private QueryProcess queryProcess;
    
    String server() {
        return local;
    }
    
     @Test
    public void service3() throws EngineException {
        String param = "method=post&format=json&mode=log";
        String q = "select * where {"
                + "service <http://%s/sparql?%s> {"
                + "?s ?p ?o"
                + "}} limit 1";

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        setQueryProcess(exec);
        Mappings map = exec.query(String.format(q, server(), param));

        ContextLog log = exec.getLog();
        System.out.println(log);
         System.out.println(log.getFormatList());
        
        //Assert.assertEquals(true, map.size()>0);                     
    }
    
    @Test
    public void service2() throws EngineException {
        Mappings map = select("method=get&format=xml");
        Assert.assertEquals(true, map.size()>0);
        map = select("method=get&format=json");
        Assert.assertEquals(true, map.size()>0);
                
        map = select("method=post&format=xml");
        Assert.assertEquals(true, map.size()>0);
        map = select("method=post&format=json");
        Assert.assertEquals(true, map.size()>0);                
    }
    
    @Test
    public void service1() throws EngineException {
        Mappings map = construct("method=get&format=jsonld");
        Assert.assertEquals(true, ((Graph)map.getGraph()).size()>0);
        map = construct("method=get&format=turtle");
        Assert.assertEquals(true, ((Graph)map.getGraph()).size()>0);
        map = construct("method=get&format=rdfxml");
        Assert.assertEquals(true, ((Graph)map.getGraph()).size()>0);
        
        map = construct("method=post&format=jsonld");
        Assert.assertEquals(true, ((Graph)map.getGraph()).size()>0);
        map = construct("method=post&format=turtle");
        Assert.assertEquals(true, ((Graph)map.getGraph()).size()>0);
        map = construct("method=post&format=rdfxml");
        Assert.assertEquals(true, ((Graph)map.getGraph()).size()>0);
        
    }
    
    Mappings select(String param) throws EngineException {      
        String q = 
           "@federate <http://%s/sparql?%s>"
         + "select * where {?s ?p ?o} limit 1";
        
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        setQueryProcess(exec);
        Mappings map = exec.query(String.format(q, server(), param));
        
        return map;
    }
    Mappings construct(String param) throws EngineException {      
        String q = 
           "@federate <http://%s/sparql?%s>"
         + "construct where {?s ?p ?o} limit 1";
        
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        Mappings map = exec.query(String.format(q, server(), param));
        
        return map;
    }
    
    void trace(Mappings map) {
        System.out.println("result: " + map);
        if (map.getGraph() != null) {
            System.out.println("graph: ");
            System.out.println(((Graph)map.getGraph()).display());
        }
    }

    public QueryProcess getQueryProcess() {
        return queryProcess;
    }

    public void setQueryProcess(QueryProcess queryProcess) {
        this.queryProcess = queryProcess;
    }
}
