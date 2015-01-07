package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Profile {
    
    static final String NL = System.getProperty("line.separator");
    static final String DATA    = "/webapp/data/" ;
    static final String QUERY   = "/webapp/query/" ;
    
    HashMap<String,  Service> map; 
    NSManager nsm;
    
    public class Service {
        private String query;
        private String transform;
        private String variable ;
        private String[] load;
        
        Service(String t, String q, String v){
            query = q;
            transform = t;
            variable = v;
        }
        
        Service(String[] l){
            load = l;
        }

        /**
         * @return the query
         */
        public String getQuery() {
            return query;
        }

        /**
         * @param query the query to set
         */
        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * @return the transform
         */
        public String getTransform() {
            return transform;
        }

        /**
         * @param transform the transform to set
         */
        public void setTransform(String transform) {
            this.transform = transform;
        }
        
        /**
         * @return the variable
         */
        public String getVariable() {
            return variable;
        }

        /**
         * @param variable the variable to set
         */
        public void setVariable(String variable) {
            this.variable = variable;
        }

        /**
         * @return the load
         */
        public String[] getLoad() {
            return load;
        }

        /**
         * @param load the load to set
         */
        public void setLoad(String[] load) {
            this.load = load;
        }
    }
    
    Profile(){
        map = new HashMap();
        nsm = NSManager.create();
    }
    
     public Collection<Service> getServices(){
            return map.values();
        }
    
    void define(String name){
        if (! map.containsKey(name)){
            init(DATA, name);
        }
    }
    
    void init(String path, String name){       
        try {
            Graph g = load(path, name);
            init(g); 
            initLoad(g);
        } catch (IOException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    Graph load(String path, String name) throws IOException, LoadException {
        Graph g = Graph.create();
        String str = getResource(path, name);
        Load load = Load.create(g);
        load.loadString(str, name, Load.TURTLE_FORMAT);
        return g;
    }
    
    Graph getGraph(String path, String name){
        try {
            return load(path, name);
        } catch (IOException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Graph.create();
    }
    
    String getQuery(String profile){
        return map.get(profile).getQuery();
    }
    
    String getVariable(String profile){
        return map.get(profile).getVariable();
    }
    
    String getTransform(String profile){
        return map.get(profile).getTransform();
    }
    
    String getValues(String profile, String resource) {
        String var = getVariable(profile);
        if (var != null) {
            return "values " + var + " { <" + resource + "> }";
        }
        return null;
    }
    
    void init(Graph g) throws IOException, EngineException{
        String str = getResource(QUERY + "profile.rq");
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);
        for (Mapping m : map){
            init(m);
        }
    }
    
    void init(Mapping m){
        Node profile = m.getNode("?p");
        Node query   = m.getNode("?q");
        Node var     = m.getNode("?v");
        Node trans   = m.getNode("?t");
        Service s = new Service(
                (trans==null)?null:trans.getLabel(), 
                (query==null)?null:query.getLabel(), 
                (var==null)?null: var.getLabel());
        map.put(profile.getLabel(), s);
    }
    
     void initLoad(Graph g) throws IOException, EngineException{
        String str = getResource(QUERY + "profileLoad.rq");
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);
        for (Mapping m : map){
            initLoad(m);
        }
    }
    
    void initLoad(Mapping m){
        Node profile = m.getNode("?p");
        Node load    = m.getNode("?ld");
        if (load == null){
            return;
        }
        String [] list = load.getLabel().split(";");
        Service s = new Service(list);
        map.put(profile.getLabel(), s);
    }
    
    String getResource(String path, String name) throws IOException {
        String res = "";
        try {
            res = getResource(path + name);
        } catch (IOException ex) {
            QueryLoad ql = QueryLoad.create();
            res = ql.read(name);
            if (res == null){
                throw new IOException(name);
            }
        }
        return res;
    }
    
    String getResource(String name) throws IOException {
        InputStream stream = SPARQLRestAPI.class.getResourceAsStream(name);
        if (stream == null) {
            throw new IOException(name);
        }
        Reader fr = new InputStreamReader(stream);
        String str = read(fr);
        return str;
    }
    

    String read(Reader fr) throws IOException {
        BufferedReader fq = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String str;
        while (true) {
            str = fq.readLine();
            if (str == null) {
                fq.close();
                break;
            }
            sb.append(str);
            sb.append(NL);
        }
        return sb.toString();
    }

}
