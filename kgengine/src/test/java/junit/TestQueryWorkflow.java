package junit;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.corese.kgtool.workflow.Data;
import fr.inria.corese.kgtool.workflow.SemanticWorkflow;
import fr.inria.corese.kgtool.workflow.WorkflowParser;
import fr.inria.corese.kgtool.workflow.WorkflowProcess;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import static junit.TestQuery1.data;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TestQueryWorkflow {
    
    
     @Test
   public void testWorkflow22() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w22.ttl");           
       Data res = w.process(new Data(GraphStore.create()));
        System.out.println(res.getGraph().display());
       assertEquals(5, res.getGraph().size());
   }  
      
    
   @Test  
   public void testWorkflow21() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w21.ttl");           
       Data res = w.process(new Data(GraphStore.create())); 
       assertEquals(5, res.getGraph().size());
   }  
        
           @Test  
   public void testWorkflow20() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w20.ttl");           
       Data res = w.process(new Data(GraphStore.create())); 
       IDatatype dt =  res.getValue("?v");
       assertEquals(10, dt.intValue());             
   }  
   
           @Test  
   public void testWorkflow19() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w19.ttl");           
       Data res = w.process(new Data(GraphStore.create())); 
       IDatatype dt =  res.getValue("?v");
       assertEquals(20, dt.intValue());             
   }  
        
      
       @Test  
   public void testWorkflow18() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w18.ttl");           
       Data res = w.process(new Data(GraphStore.create())); 
       IDatatype dt =  res.getValue("?v");
       assertEquals(null, dt);             
   } 
       
   @Test
   public void testWorkflow17() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w17.ttl");           
       Data res = w.process(new Data(GraphStore.create())); 
       IDatatype dt =  res.getValue("?v");
       assertEquals(10, dt.intValue());             
   }     
       
  @Test
   public void testWorkflow15() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w15.ttl", NSManager.SWL+"test");           
       Data res = w.process(new Data(GraphStore.create())); 
       WorkflowProcess wf = w.getProcessLast();
       int count = 0;
       for (WorkflowProcess ww : wf.getProcessList()){
           if (ww.getData() != null){
             count += ww.getData().getMappings().size();
           }
       }
       System.out.println("total : " + count);
       assertEquals(589, count);
       
       Graph g = res.getGraph();
       assertEquals(true, g.getNamedGraph(Context.STL_CONTEXT) != null);
       
   } 
    
    
        @Test
   public void testWorkflow14() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w14.ttl");           
       Data res = w.process();  
       for (Data data : res.getDataList()){
           assertEquals(1, data.getGraph().size());
       }
   } 
    
       @Test
   public void testWorkflow13() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w13.ttl");
       
       String i = "insert data { us:John a foaf:Person}";
       Graph g = Graph.create();
       QueryProcess exec = QueryProcess.create(g);
       exec.query(i);
       w.setGraph(g);
       Data res = w.process();  
       assertEquals(1, res.getMappings().size());
   } 
       
    
   @Test
   public void testWorkflow12() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w10.ttl");
       Data res = w.process();  
       IDatatype dt = (IDatatype) res.getMappings().getValue("?r");
       assertEquals(2, dt.intValue());
   } 
          
    @Test
   public void testWorkflow11() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w9.ttl");
       Data res = w.process();       
       assertEquals(false, res.getDatatype().booleanValue());
   } 
    
    @Test
   public void testWorkflow10() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w8.ttl");
       Data res = w.process();       
        assertEquals(false, res.getDatatype().booleanValue());
   } 
    
      @Test
    public void testWorkflow9() throws EngineException, LoadException {
        WorkflowParser wp = new WorkflowParser();
        SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w7.ttl");
        Data res = w.process();
        IDatatype dt = (IDatatype) res.getMappings().getValue("?t");
        assertEquals(5, dt.intValue());
        assertEquals(5, res.getGraph().size());
    }
    
    @Test
   public void testWorkflow8() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w6.ttl");
       Data res = w.process();       
        assertEquals(7, res.getMappings().size());
   } 
    
   @Test
   public void testWorkflow7() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w4.ttl");
       Data res = w.process();       
       assertEquals(169, res.getGraph().size());
       assertEquals(15, res.getMappings().size());
   } 
    
            @Test
   public void testWorkflow6() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w3.ttl");
       Data res = w.process();
       assertEquals(1, res.getGraph().size());
       assertEquals(1, res.getMappings().size());
   }

    
          @Test
   public void testWorkflow5() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w2.ttl");
       Data res = w.process();
       assertEquals(1, res.getGraph().size());
       assertEquals(1, res.getMappings().size());
   }

    
       @Test
   public void testWorkflow4() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w2/w1.ttl");
       Data res = w.process();
       assertEquals(15, res.getGraph().size());
       assertEquals(1, res.getMappings().size());
   }

    
      @Test
   public void testWorkflow3() throws EngineException, LoadException {
       WorkflowParser wp = new WorkflowParser();
       SemanticWorkflow w = wp.parse(data + "junit/workflow/w1/workflow.ttl");
       Data res = w.process();
       assertEquals(23, res.getGraph().size());
   }
     
    @Test
    public void testWorkflow2() throws EngineException, LoadException {
        SemanticWorkflow sub = new SemanticWorkflow()
                .addQuery("insert  { "
                + "us:John rdf:value ?val ; st:loop ?loop ; rdfs:label 'Jon' ;"
                + "foaf:knows []} "
                + "where { bind (st:get(st:index) as ?val) bind (st:get(st:loop) as ?loop)}")
                ;
        
        SemanticWorkflow w = new SemanticWorkflow()
                .addQuery("construct where {}")
                .add(sub)
                .addTemplate(Transformer.TURTLE)
                ;
        
        Context c = new Context()
                .export(Context.STL_TEST, DatatypeMap.newInstance(5)); 
        sub.setLoop(5);
        sub.setContext(new Context());
        w.setContext(c);
        Graph g = Graph.create();
        Data res = w.process(new Data(g));        
        assertEquals(12, res.getGraph().size());
    }
   
       
     
       @Test
    public void testWorkflow() throws EngineException, LoadException{
        SemanticWorkflow w = new SemanticWorkflow()
        .addQuery("insert data {"
                + "graph us:g1 {us:John a foaf:Person ; rdfs:label 'Jon'}"
                + "graph us:g2 {us:Jim  a foaf:Person ; rdfs:label 'Jim'}"
                + "}"
                )
       .addQuery("construct {?x ?p ?y ?z ?p ?t } where {?x ?p ?y graph ?g {?z ?p ?t}}")
       .addRule(RuleEngine.OWL_RL_LITE)
       .addTemplate(Transformer.TURTLE)
                ;
        
        Context c = new Context()
                .setName("test", DatatypeMap.newInstance(10));        
        w.setContext(c);
        Dataset ds = Dataset.create().addFrom(NSManager.USER+"g1").addNamed(NSManager.USER+"g2");
        w.setDataset(ds);
        Graph g = Graph.create();
        Data res = w.process(new Data(g));
        String str = res.stringValue();
        assertEquals(303, str.length());
    }
    
}
