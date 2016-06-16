package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.util.SPINProcess;

/**
 * Load a directory.
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class LoadProcess extends WorkflowProcess {
    public static final String FILE = "file://";
    String name;
    boolean rec = false;
    private boolean named = false;
    
    public LoadProcess(String path){
        this.path = path;
    }
    
    public LoadProcess(String path, String name, boolean rec){
        this.path = path;
        this.rec = rec;
        this.name = name;
    }
   
    public LoadProcess(String path, String name, boolean rec, boolean named){
        this(path, name, rec);
        this.named = named;
    }
    
    @Override
    void start(Data data){
        if (isDebug()){
            System.out.println("Load: " + path);
        }
    }
    
     @Override
    void finish(Data data){
         
    }
    
    @Override
    public Data run(Data data) throws EngineException {
        Graph g = data.getGraph();
        Load ld = Load.create(g);
        try {
            if (path.startsWith(FILE)){
                path = path.substring(FILE.length());
            }
            if (getModeString() != null && getModeString().equals(WorkflowParser.SPIN)){
                loadSPARQLasSPIN(path, g);
            }
            else {                
                ld.parseDir(path, name, rec);
            }
        } catch (LoadException ex) {
            throw new EngineException(ex);
        }
        return new Data(this, null, g);
    }
    
    
     void loadSPARQLasSPIN(String uri, Graph g) throws EngineException, LoadException{
        QueryLoad ql = QueryLoad.create();
        String str = ql.readWE(uri);
        if (str != null){
            SPINProcess sp = SPINProcess.create();
            sp.setDefaultBase(path);
            sp.toSpinGraph(str, g);        
        }              
    }
    
    @Override
    public String stringValue(Data data){
        return data.getGraph().toString();
    }

    /**
     * @return the named
     */
    public boolean isNamed() {
        return named;
    }

    /**
     * @param named the named to set
     */
    public void setNamed(boolean named) {
        this.named = named;
    }

}
