package fr.inria.corese.core.extension;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Agent Java object accessible in LDScript with xt:agent() (see function/system.rq)
 * It has a singleton, hence each ag:fun() function call is performed on the same object
 * The singleton can be accessed in LDScript using xt:agent()
 * 
 * prefix ag: <function://fr.inria.corese.core.extension.Agent>
 * 
 * IDatatype ag:functionName(IDatatype arg)
 * 
 * IDatatype java:functionName(xt:agent(), IDatatype arg)
 * IDatatype java:functionName(xt:agent(), JavaType arg)
 * .
 */
public class Agent  {
    
    static final String NS = NSManager.USER;
    static final String ENTAILMENT = NS+"entailment";
    static final String TEST = NS+"test";
    
    private static Agent singleton;
    private static IDatatype dt;
    private String name;
    private Graph graph;
    
    private IDatatype value, uri;
    
    static {
        singleton = new Agent("main");
        dt = DatatypeMap.createObject(singleton());
    }
    
    public Agent() {
        this("proxy");
    }
    
    public Agent(String n) {
        setName(n);
    }
    
    /**
     * Function singleton() enables ag:fun() SPARQL Extension Function (Extern) 
     * to be called on the same singleton agent 
     * otherwise an agent object would be created for each function call.
     */
    public static Agent singleton() {
        return singleton;
    }
    

    
    
    
   public IDatatype setURI(IDatatype dt) {
       uri = dt;
       return dt;
   }
    
   public IDatatype getURI() {
        return uri;
   }
     
   public IDatatype message(IDatatype name) {
       switch (name.getLabel()) {
           case ENTAILMENT: entailment(); break;
           case TEST: test(); break;
       }
       return name;
   }
   
   public IDatatype message(IDatatype name, IDatatype dt) {
       switch (name.getLabel()) {
           case ENTAILMENT: entailment(); break;
       }
       return name;
   }
    
   public IDatatype message(IDatatype name, IDatatype... args) {
       switch (name.getLabel()) {
           case ENTAILMENT: entailment(); break;
       }
       return name;
   }
   
   
   
   IDatatype test() {
       System.out.println("test");
       return DatatypeMap.TRUE;
   }
    
    
    void entailment() {
        if (getGraph() != null) {
            RuleEngine re = RuleEngine.create(graph);
            re.setProfile(RuleEngine.OWL_RL);
            try {
                re.process();
            } catch (EngineException ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    
    
    
    public static IDatatype getDatatypeValue() {
        return dt;
    }
    
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public IDatatype getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public IDatatype setValue(IDatatype value) {
        this.value = value;
        return value;
    }

    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }
    
    public void setGraph(Graph g) {
        graph = g;
    }

  
}
