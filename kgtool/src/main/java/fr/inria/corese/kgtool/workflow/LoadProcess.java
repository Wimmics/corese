package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Load a directory.
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class LoadProcess extends WorkflowProcess {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(LoadProcess.class);
    
    public static final String FILE = "file://";
    String name;
    boolean rec = false;
    private boolean named = false;
    String text;
    int format = Load.UNDEF_FORMAT;
    private int[] FORMATS =  { Load.TURTLE_FORMAT, Load.RDFXML_FORMAT, Load.JSONLD_FORMAT };
    
    public LoadProcess(String path){
        this.path = path;
    }
    
    public LoadProcess(String str, int format){
        this.text = str;
        this.format = format;
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
    
    public static LoadProcess createStringLoader(String str){
        return new LoadProcess(str, Load.UNDEF_FORMAT);
    }
    
    public static LoadProcess createStringLoader(String str, int format){
        return new LoadProcess(str, format);
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
            
            if (text != null){
                loadString(ld);
            }
            else {
                if (path.startsWith(FILE)) {
                    path = path.substring(FILE.length());
                }

                if (!hasMode()) {
                    ld.parseDir(path, name, rec);
                } else if (getModeString().equals(WorkflowParser.SPIN)) {
                    loadSPARQLasSPIN(path, g);
                } else if (getMode().isNumber()) {
                    for (int i = 0; i < getMode().intValue(); i++) {
                        ld.parseDir(path, name, rec);
                    }
                } else {
                    ld.parseDir(path, name, rec);
                }
            }
            
        } catch (LoadException ex) {
            throw new EngineException(ex);
        }
        return new Data(this, null, g);
    }
    
    /**
     * Try Turtle RDF/XML JSON-LD formats
     */
    void loadString(Load ld) throws LoadException {
        if (format == Load.UNDEF_FORMAT) {
            for (int ft : FORMATS) {
                try {
                    ld.loadString(text, ft);
                    return;
                } catch (LoadException ex) {
                    // not right format
                    logger.warn("Load RDF string format: " + ft);
                    logger.warn(ex);
                }
            }
        } else {
            ld.loadString(text, format);
        }
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
