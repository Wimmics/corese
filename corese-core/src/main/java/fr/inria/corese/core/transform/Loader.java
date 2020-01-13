/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.core.transform;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.LoadFormat;
import static fr.inria.corese.core.transform.Transformer.STL_IMPORT;
import static fr.inria.corese.core.transform.Transformer.STL_PROFILE;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage load, import, profile
 * TODO: level()
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class Loader {
    
    private static Logger logger = LoggerFactory.getLogger(Loader.class);
    private String STL = NSManager.STL;
    public static final String PPLIB = "/template/";
    
    
    Graph graph;
    NSManager nsm;
    Transformer trans;
    HashMap<String, String> loaded;
    // Dataset to be set into query templates at compile time
    // Use case: st:apply-templates-graph(st:turtle, stname)
    Dataset ds;
    
    Loader(Transformer t){
        graph = t.getGraph();
        nsm = t.getNSM();
        trans = t;
        loaded = new HashMap();
    }
    
    
    // Dataset to be set into query templates at compile time
    // Use case: st:apply-templates-graph(st:turtle, stname)    
    void setDataset(Dataset ds){
        this.ds = ds;
    }
    
    /**
     * Load transformation from .rq or .rul file(s)
     * .rq loaded in QueryEngine
     * .rul loaded in RuleEngine
     * Templates are eventually stored in QueryEngine
     */
     QueryEngine load(String pp) {
        QueryEngine qe = QueryEngine.create(graph); 
        RuleEngine re  = RuleEngine.create(graph); 
        // PP as list (not in a thread)
        qe.getQueryProcess().setListPath(true);
        qe.setTransformation(true);
        re.setQueryEngine(qe);
        qe.setDataset(ds);
        re.setDataset(ds);
                
        if (pp == null) {
            // skip;
        } else {
            loaded.put(pp, pp);
            Load ld = Load.create(graph);
            ld.setEngine(qe);
            ld.setEngine(re);           
            try {
                //ld.loadWE(pp);
                load(ld, qe, pp);
            } catch (LoadException e) {
                // TODO Auto-generated catch block
                logger.error("Transformer Load Error: " + pp, e);
            }
        }
          
        return qe;

    }
     
     /**
      * If path is file:///somewhere/sttl/
      * return the path /somewhere/sttl/
      * Hence load has access to transformation directory if any
      * Use case:
      * st:transform <demo/sttl/>
      */
     String toFile(String path) {
        try {
            URL url = new URL(path);
            if (url.getProtocol().startsWith("file")){
                return url.getFile();
            }
        } catch (MalformedURLException ex) {            
        }
        return path;
     }
       
       /**
     * Predefined transformations loaded from Corese resource or ns.inria.fr server
     */
    void load(Load ld, QueryEngine qe, String pp) throws LoadException {
        //System.out.println("Load: " + pp);    
        String name = null;
        pp = clean(pp);
        // base for templates
        // use case: format { <format/main.html> ?x }
        qe.setBase(NSManager.toURI(pp));
        if (nsm.inNamespace(pp, STL)) {
            // predefined pprinter: st:owl st:spin
            // loaded from Corese resource
            name = nsm.strip(pp, STL);
        }  else {
            ld.parseDir(toFile(pp), Load.QUERY_FORMAT);
            return;
        }           
        String src = PPLIB + name;

        if (!ld.isRule(src)) {
            src = src + LoadFormat.RULE;                  
        }
        InputStream stream = getClass().getResourceAsStream(src);
        if (stream == null) { 
            try {
                URL uri = new URL(pp);                
                stream = uri.openStream();
            } catch (MalformedURLException ex) {
                throw LoadException.create(ex);
            } catch (IOException ex) {                
                throw LoadException.create(ex);
            }
            if (stream == null){
                throw LoadException.create(new IOException(pp));
            }
        }
        // use non synchronized load method because we may be inside a query 
        // with a read lock       
        ld.loadRule(new InputStreamReader(stream), src);

    }
    
    // remove #
    String clean(String uri){
        return trans.getURI(uri);
    }
        
    /**
    * 
    * @param tqe: Transformer global QE
    * @param qe:  current or imported QE 
    */
    void profile(QueryEngine tqe, QueryEngine qe) {
        Query qprofile = qe.getTemplate(STL_PROFILE);
        if (qprofile != null) {
            // st:import, st:level
            // draft: profile may load st:import
            // st:import skip its st:start, st:default
            // when they exist in this qe
            // st:profile of import is skipped           
            // TODO: level()
            profile(qprofile);
            
            if (qprofile.getExtension() != null){
                // share profile function definitions in templates
                fr.inria.corese.compiler.parser.Transformer tr = fr.inria.corese.compiler.parser.Transformer.create();
                ASTExtension ext = Interpreter.getExtension(qprofile);
                tr.definePublic(ext, qprofile, false);
                for (Query t : qe.getTemplates()) {             
                    addExtension(t, ext);
                }
                for (Query t : qe.getNamedTemplates()) {             
                    addExtension(t, ext);
                }           
            } 
            
            Expr imp = qprofile.getProfile(STL_IMPORT);
            if (imp != null) {
                String uri = imp.getExp(0).getLabel();
                if (! loaded.containsKey(uri)){
                    loadImport(tqe, imp.getExp(0).getLabel());
                }
            }
        }
    }
    
    void addExtension(Query q, ASTExtension ext){
        if (ext == null){
            return;
        }
        if (q.getExtension() == null){
            q.setExtension(ext);
        }
        else {
            //q.getExtension().add(ext);
            Interpreter.getExtension(q).add(ext);
        }
    }
   
   /**
    * Use case: 
    * Transformer st:profile with st:import(uri)
    * executed by init(st:profile)
    * 
    */
    @Deprecated
    void loadImport(QueryEngine tqe, String uri) {
        QueryEngine eng = load(uri);
        profile(tqe, eng);
        include(tqe, eng);
     }

       
     /**
      * TODO:
      * Transformer st:default template overload imported st:default template
      */
     void include(QueryEngine tqe, QueryEngine eng) {

        for (Query q : eng.getNamedTemplates()) {
            if (q.getName().equals(Transformer.STL_PROFILE)) {
                // overloaded by transformer profile
            } else if (tqe.getTemplate(q.getName()) == null) {
                tqe.defTemplate(q);
            } else {
                logger.error("Imported template already exist: " + q.getName());
            }
        }

        for (Query q : eng.getTemplates()) {
            tqe.defTemplate(q);
        }

    }
    
    @Deprecated
    void profile(Query q){
        init(q, q.getSelectFun());
    }
    
 
    void init(Query q, List<Exp> select) {
        for (Exp exp : select) {
            if (exp.getFilter() != null) {
                initExp(q, exp.getFilter().getExp());
            }
        }
    }
    
    void initExp(Query q, Expr exp) {
        switch (exp.oper()) {
                            
            case ExprType.STL_IMPORT:
                if (exp.getExpList().size() >= 1){
                    q.setFilter(STL_IMPORT, exp.getFilter());
                    //loadImport(exp.getExp(0).getLabel());
                }
                break;

            case ExprType.STL_CONCAT:
                for (Expr ee : exp.getExpList()) {
                    initExp(q, ee);
                }
                break;
        }
    }
    
    /**
     * st:define(st:process(?in) = st:uri(?in))
     * st:define(st:default(?in) = st:turtle(?in))
     * @deprecated
     */
    void init(Query q, Expr exp) {
        //System.out.println("PP: " + exp);
        exp = exp.getExp(0);
        if (! check(exp)){
            logger.error("Incorrect profile expression: " + exp);
            return ;        
        }
        Expr ee  = exp.getExp(1);
        Expr fun = exp.getExp(0);
        
        switch (fun.oper()) {
                                     
            case ExprType.LEVEL:
                IDatatype dt = (IDatatype) ee.getValue();
                trans.setLevelMax(dt.intValue());                
                break;
                
        }
    }
    
    boolean check(Expr exp){
        if (exp.getExp(0).oper() == ExprType.LEVEL){
            return exp.getExp(1).type() == ExprType.CONSTANT;
        }
        return true ; //(exp.getExp(1).type() == ExprType.FUNCTION);
                //&& exp.getExp(1).oper() != ExprType.UNDEF); 
    }

    
}
