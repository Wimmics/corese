package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
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
 * Parser of RDF profile that defines:
 * 1- a set of profile (eg specifying a Transformation)
 * 2- a set of server with specific data to be loaded in dedicated TripleStore
 * profile is used with profile argument: /template?profile=st:sparql
 * server is used by Tutorial service that manage specific TripleStores
 * each server specify the RDF content of a TripleStore
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Profile {
    
    static final String NL = System.getProperty("line.separator");
    static final String DATA    = "/webapp/data/" ;
    static final String QUERY   = "/webapp/query/" ;
    
    HashMap<String,  Service> map, servers; 
    NSManager nsm;
    
    boolean isProtected = false;
    
   
    
    Profile(){
        this(false);
    }
    
    Profile(boolean b){
        map = new HashMap();
        servers = new HashMap();
        nsm = NSManager.create();
        isProtected = b;
    }
    
    void setProtect(boolean b){
        isProtected = b;
    }
    
    boolean isProtected(){
        return isProtected;
    }
    
    /**
     * Complete service parameters according to a profile
     * e.g. get transformation from profile
     */
     Param complete(Param par) throws IOException {
        String value = par.getValue();
        String query = par.getQuery();
        String name = par.getName();
        String transform = par.getTransform();
        String profile = par.getProfile();
        String uri = par.getUri();
        
        if (profile != null) {
            // prodile declare a construct where query followed by a transformation
            String uprofile = nsm.toNamespace(profile);
            // may load a new profile            
            define(uprofile);
            Service s = getService(uprofile);
            if (s != null){
                if (name == null) {
                    // parameter name overload profile name
                    name = s.getQuery();
                }
                if (transform == null) {
                    // transform parameter overload profile transform
                    transform = s.getTransform();
                }
                if (uri != null) {
                    // resource given as a binding value to the query
                    // generate values clause if profile specify variable
                    value = getValues(s.getVariable(), uri);
                }
                if (s.getLang() != null){
                    par.setLang(s.getLang());
                }
            }
        }

        if (query == null && name != null) {
            // load query definition
            query = loadQuery(name);
        }

        if (value != null && query != null) {
            // additional values clause
            query += value;
        }

        par.setTransform(transform);
        par.setUri(uri);
        par.setName(name);
        par.setQuery(query);
        return par;

    }
    
     public Collection<Service> getServices(){
            return map.values();
        }
     
      public Collection<Service> getServers(){
            return servers.values();
        }
    
    void define(String name){
        if (! map.containsKey(name) && ! isProtected){
            init(DATA, name);
        }
    }
  
    void init(String path, String name){       
        try {
            Graph g = load(path, name);
            process(g);
        } catch (IOException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    void process(Graph g) throws IOException, EngineException {
        init(g);
        initLoad(g);
        initLoader(g);
    }
    
    GraphStore load(String path, String name) throws IOException, LoadException {
        GraphStore g = GraphStore.create();
        String str = getResource(path, name);
        Load load = Load.create(g);
        load.loadString(str, name, Load.TURTLE_FORMAT);
        return g;
    }
    
    GraphStore getGraph(String name){
        return getGraph(DATA, name);
    }
    
    GraphStore getGraph(String path, String name){
        try {
            return load(path, name);
        } catch (IOException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return GraphStore.create();
    }
    
    // name = webapp/data/...
    GraphStore loadResource(String name) throws LoadException {
        GraphStore g = GraphStore.create();
        Load ld = Load.create(g);
        ld.loadResource(name, ld.getFormat(name, Load.TURTLE_FORMAT));
        return g;
    }
    
     String loadQuery(String name) throws IOException {
        if (isProtected) {
            // only predefined queries
            return getResource(Profile.QUERY + name);
        }
        // local or external query 
        return getResource(Profile.QUERY, name);
    }
    
     
     Service getService(String name){
         return map.get(name);
     }
     

    
    String getValues(String var, String resource) {
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
        Node prof    = m.getNode("?p");
        Node query   = m.getNode("?q");
        Node var     = m.getNode("?v");
        Node trans   = m.getNode("?t");
        Node serv    = m.getNode("?s");
        Node lang    = m.getNode("?l");
        
        Service s = new Service(prof.getLabel());
        
        if (trans != null){
            s.setTransform(trans.getLabel());
        }
        if (query != null){
            s.setQuery(query.getLabel());
        }
        if (var != null){
            s.setVariable(var.getLabel());
        }             
        if (serv != null){
            s.setServer(serv.getLabel());
        }
        if (lang != null){
            s.setLang(lang.getLabel());
        }
        
        map.put(prof.getLabel(), s);
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
        Service s = new Service(profile.getLabel());
        s.setLoad(list);
        map.put(profile.getLabel(), s);
    }
    
    
    void initLoader(Graph g) throws EngineException{
        String str = "select * where {"
                + "?s a st:Server "
                + "?s ?p ?d "
                + "?d st:uri ?u "
                + "?d st:name ?n "
                + "values ?p { st:data st:schema st:context }"
                + "}";
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);
        
        for (Mapping m :map){
            Node sn = m.getNode("?s");
            Node p = m.getNode("?p");
            Node u = m.getNode("?u");
            Node n = m.getNode("?n");
            
           Service s = findServer(sn.getLabel());
           s.add(p.getLabel(), u.getLabel(), (n != null)?n.getLabel():null); 
        }
                
    }
    
    Service findServer(String name){
        Service s = map.get(name);
        if (s == null){
            s = new Service(name);
            map.put(name, s);
            servers.put(name, s);
        }
        return s;
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
        InputStream stream = Profile.class.getResourceAsStream(name);
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
