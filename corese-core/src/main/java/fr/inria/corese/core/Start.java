package fr.inria.corese.core;

import java.util.Date;
import java.util.ArrayList;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Access;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Start {

    ArrayList<String> load = new ArrayList<>();
    ArrayList<String> query = new ArrayList<>();
    ArrayList<String> loadquery = new ArrayList<>();
    ArrayList<String> sttl = new ArrayList<>();
    ArrayList<String> shacl = new ArrayList<>();
    NSManager nsm = NSManager.create();
    boolean debugRule = false;
    boolean rdfs = false;
    boolean owl = false;
    boolean display = true;
    boolean verbose = false;
    boolean execShacl = false;
    String param = null;

    /**
     * Corese as command line take path and query as argument load the docs from
     * path java -cp corese.jar fr.inria.corese.core.Start -load
     * dataset.rdf -query "select * where {?x ?p ?y}" java -cp
     * kggui-3.2.1-SNAPSHOT-jar-with-dependencies.jar
     * fr.inria.corese.kgtool.Start -load rdf: -sttl st:turtle st:rdfxml
     * st:json
     *
     */
    public static void main(String[] args) throws LoadException {
        Start st = new Start();
        st.process(args);
        st.start();
    }

    void process(String[] args) {
        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-rdfs")) {
                i++;
                rdfs = true;               
            } 
            else if (args[i].equals("-owl")) {
                i++;
                owl = true;              
            } 
            else if (args[i].equals("-load")) {
                i++;
                while (i < args.length && !args[i].startsWith("-")) {
                    load.add(expand(args[i++]));
                }
            } else if (args[i].equals("-query")) {
                i++;
                while (i < args.length && !args[i].startsWith("-")) {
                    query.add(args[i++]);
                }
            } 
            else if (args[i].equals("-loadquery")) {
                i++;
                while (i < args.length && !args[i].startsWith("-")) {
                    loadquery.add(args[i++]);
                }
            } 
            else if (args[i].equals("-sttl")) {
                i++;
                while (i < args.length && !args[i].startsWith("-")) {
                    sttl.add(expand(args[i++]));
                }
            }
            else if (args[i].equals("-shacl")) {
                i++;
                while (i < args.length && !args[i].startsWith("-")) {
                    shacl.add(expand(args[i++]));
                }
            }
            else if (args[i].equals("-execshacl")) {
                execShacl = true;
                i++;
            }
            else if (args[i].equals("-su")) {
                Access.skip(true);
                i++;
            }
            else if (args[i].equals("-var")) {
                i++;
                // -arg var=val;var=val
                // global var for SPARQL query LDSCript
                param = args[i++];
            }
            else if (args[i].equals("-s") || args[i].equals("-silent")) {
                i++;
                display = false;
            }
        }
    }

    String expand(String str) {
        return nsm.toNamespaceBN(str);
    }

    void start() throws LoadException {
        Date d1 = new Date();
        Graph g = Graph.create(rdfs);
        Load ld = Load.create(g);
        
        for (String doc : load) {
            try {
                ld.parseDir(doc);
            } catch (LoadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        if (owl){
            RuleEngine re = RuleEngine.create(g);
            re.setProfile(RuleEngine.OWL_RL);
            try {
                re.process();
            } catch (EngineException ex) {
                Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Date d2 = new Date();
        try {
            QueryProcess exec = QueryProcess.create(g);
            
            for (String q : query) {
                Mappings map = exec.query(q, param(param));
                ResultFormat f = ResultFormat.create(map);
                if (display) {
                    System.out.println(f);
                }
            }
            
            for (String file : loadquery) {
                QueryLoad ql = QueryLoad.create();
                String q = ql.readWE(file);
                if (verbose) {
                    System.out.println(q);
                    System.out.println();
                }
                Mappings map = exec.query(q, param(param));
                ResultFormat f = ResultFormat.create(map);
                System.out.println(f);
            }
            
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        for (String shape : shacl) {
            try {
                shacl(g, shape);
            } catch (EngineException ex) {
                Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (execShacl) {
            try {
                execShacl(g);              

            } catch (EngineException ex) {
                Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (String stl : sttl) {
            Transformer t = Transformer.create(g, stl);
            try {
                System.out.println(t.transform());
            } catch (EngineException ex) {
                Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // var=val;var=val
    Binding param(String param) {
        Binding b = Binding.create();
        if (param != null) {
            String[] list = param.split(";");

            for (String elem : list) {
                String[] decl = elem.split("=");
                String var = decl[0];
                if (!var.startsWith("?")) {
                    var = "?".concat(var);
                }
                b.setVariable(var, DatatypeMap.newInstance(decl[1]));
            }
        }
        return b;
    }
    
    void shacl(Graph g, String shape) throws LoadException, EngineException {
        Shacl sh = new Shacl(g);
        Graph gg = Graph.create();
        Load load = Load.create(gg);
        load.parse(shape);
        Graph res = sh.eval(gg);
        System.out.println("shacl: " + shape);
        System.out.println(Transformer.turtle(res));
    }
    
    void execShacl(Graph g) throws LoadException, EngineException {
        Shacl sh = new Shacl(g);       
        Graph res = sh.eval();
        System.out.println(Transformer.turtle(res));
    }
}
