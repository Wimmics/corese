package fr.inria.corese.core.query;

import java.util.HashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.URLParam;
import java.util.Hashtable;

/**
 * Implements service expression There may be local QueryProcess for some URI
 * (use case: W3C test case) Send query to sparql endpoint using HTTP POST query
 * There may be a default QueryProcess
 *
 * TODO: check use same ProducerImpl to generate Nodes ?
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class ProviderImpl implements Provider, URLParam {

    private static final String DB = "db:";
    private static final String SERVICE_ERROR = "Service error: ";
    static Logger logger = LoggerFactory.getLogger(ProviderImpl.class);
    private static final String LOCAL_SERVICE = "http://example.org/sparql";
    static final String LOCALHOST = "http://localhost:8080/sparql";
    static final String LOCALHOST2 = "http://localhost:8090/sparql";
    static final String DBPEDIA = "http://fr.dbpedia.org/sparql";
    HashMap<String, QueryProcess> table;
    Hashtable<String, Double> version;
    private QueryProcess defaut;
    private int limit = 30;

    private ProviderImpl() {
        table = new HashMap<>();
        version = new Hashtable<>();
    }

    public static ProviderImpl create() {
        ProviderImpl p = new ProviderImpl();
        p.set(LOCALHOST, 1.1);
        p.set(LOCALHOST2, 1.1);
        p.set("https://data.archives-ouvertes.fr/sparql", 1.1);
        p.set("http://corese.inria.fr/sparql", 1.1);
        return p;
    }
    
    public static ProviderImpl create(QueryProcess exec) {
        ProviderImpl pi = ProviderImpl.create();
        pi.setDefault(exec);
        return pi;
    }

    @Override
    public void set(String uri, double version) {
        this.version.put(uri, version);
    }

    @Override
    public boolean isSparql0(Node serv) {
        if (serv.getLabel().startsWith(LOCALHOST)) {
            return false;
        }
        Double f = version.get(serv.getLabel());
        return (f == null || f == 1.0);
    }

    /**
     * Define a QueryProcess for this URI
     */
    public void add(String uri, Graph g) {
        QueryProcess exec = QueryProcess.create(g);
        exec.set(this);
        table.put(uri, exec);
    }

    /**
     * Define a default QueryProcess
     */
    public void add(Graph g) {
        QueryProcess exec = QueryProcess.create(g);
        exec.set(this);
        setDefault(exec);
    }

    /**
     * If there is a QueryProcess for this URI, use it Otherwise send query to
     * spaql endpoint If endpoint fails, use default QueryProcess if it exists
     * When service URL is a constant or a bound variable, serv = URL
     * otherwise serv = NULL
     */
    @Override
    public Mappings service(Node serv, Exp exp, Mappings lmap, Eval eval) 
            throws EngineException {
        Binding b = getBinding(eval.getEnvironment());
        if (Access.reject(Feature.SPARQL_SERVICE, b.getAccessLevel())) {
                throw new SafetyException(TermEval.SERVICE_MESS);
        }
        Mappings map = serviceBasic(serv, exp, lmap, eval);
//        System.out.println("service result");
//        System.out.println(map.toString(true, true, 10));
        return map;
    }
    
     Binding getBinding(Environment env) {
        return (Binding) env.getBind();
    }
    
    /**
     * exp: service statement
     */
    public Mappings serviceBasic(Node serv, Exp exp, Mappings lmap, Eval eval) 
            throws EngineException
    {
        Query qq = eval.getEnvironment().getQuery();
        Exp body = exp.rest();
        // select query inside service statement
        Query q = body.getQuery();
        
        QueryProcess exec = null ;
        
        if (serv != null) {
            exec = table.get(serv.getLabel());
        }

        if (exec == null) {
            
            ProviderService ps = new ProviderService(this, q, lmap, eval);
            ps.setDefault(getDefault());
            Mappings map = ps.send(serv, exp);
           
            if (map == null) {
                map = Mappings.create(q);
                if (q.isSilent()) {
                    map.add(Mapping.create());
                }
            }

            return map;
        }

        ASTQuery ast = exec.getAST(q);
        Mappings map;
        try {
            map = exec.query(ast);
            return map;
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        return new Mappings();
    }
    
    Graph getGraph(Producer p) {
        return (Graph) p.getGraph();
    }

   
    

    
    public QueryProcess getDefault() {
        return defaut;
    }

    
    public void setDefault(QueryProcess defaut) {
        this.defaut = defaut;
    }
}
