/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgtool.transform;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.LoadFormat;
import static fr.inria.edelweiss.kgtool.transform.Transformer.STL_IMPORT;
import static fr.inria.edelweiss.kgtool.transform.Transformer.STL_PROFILE;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Manage load, import, profile
 * TODO: level()
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class Loader {
    
    private static Logger logger = Logger.getLogger(Loader.class);
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
    
     QueryEngine load(String pp) {
        QueryEngine qe = QueryEngine.create(graph); 
        RuleEngine re  = RuleEngine.create(graph);       
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
                load(ld, pp);
            } catch (LoadException e) {
                // TODO Auto-generated catch block
                logger.error(e);
                logger.error("Transformer Load Error: " + pp);
            }

            if (qe.isEmpty()) {

                if (! re.isEmpty()) {

                    for (Rule r : re.getRules()) {
                        Query q = r.getQuery();
                        qe.defQuery(q);
                    }
                }
            }

            for (Query q : qe.getTemplates()) {
                q.setPPrinter(pp, trans);
            }
            
            for (Query q : qe.getNamedTemplates()) {
                q.setPPrinter(pp, trans);
            }
            
        }
          
        return qe;

    }
     
  
    
       /**
     * Predefined transformations loaded from Corese resource or ns.inria.fr server
     */
    void load(Load ld, String pp) throws LoadException {
        String name = null;
        pp = clean(pp);
        if (nsm.inNamespace(pp, STL)) {
            // predefined pprinter: st:owl st:spin
            // loaded from Corese resource
            name = nsm.strip(pp, STL);
        }  else {
            ld.parseDirRec(pp);
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
//        if (uri.contains("#")){
//            return uri.substring(0, uri.indexOf("#"));
//        }
//        return uri;
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
                fr.inria.edelweiss.kgenv.parser.Transformer tr = fr.inria.edelweiss.kgenv.parser.Transformer.create();
                tr.definePublic(qprofile.getExtension(), qprofile, false);
                // share function definitions in  templates
                for (Query t : qe.getTemplates()) {             
                    t.addExtension(qprofile.getExtension());
                }
                for (Query t : qe.getNamedTemplates()) {             
                    t.addExtension(qprofile.getExtension());
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
   
   /**
    * Use case: 
    * Transformer st:profile with st:import(uri)
    * executed by init(st:profile)
    * 
    */
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
    
    /**
     * The st:profile named template may contain a st:define statement such as:
     * st:define(st:process(?in) = st:uri(?in))
     * st:define(st:default(?in) = st:turtle(?in))
     * where st:process(?in) represents the processing of a variable in template clause
     * default processing is st:apply-templates(?x)
     * it can be overloaded
     * eg spin transformation uses st:uri(?var).
     */
    void profile(Query q){
        init(q, q.getSelectFun());
    }
    
 
    void init(Query q, List<Exp> select) {
        //q.setExtension(new Extension());
        for (Exp exp : select) {
            if (exp.getFilter() != null) {
                initExp(q, exp.getFilter().getExp());
            }
        }
    }
    
    void initExp(Query q, Expr exp) {
        switch (exp.oper()) {
            
//            case ExprType.STL_DEFINE:
//                if (exp.getExpList().size() == 1
//                        && exp.getExp(0).getExpList().size() == 2) {
//                    init(q, exp);
//                }
//                break;
                
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
