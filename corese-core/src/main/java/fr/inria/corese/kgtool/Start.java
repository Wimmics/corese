package fr.inria.corese.kgtool;

import java.util.Date;
import java.util.ArrayList;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.print.ResultFormat;
import fr.inria.corese.kgtool.transform.Transformer;

public class Start {

    ArrayList<String> load = new ArrayList<String>();
    ArrayList<String> query = new ArrayList<String>();
    ArrayList<String> sttl = new ArrayList<String>();
    NSManager nsm = NSManager.create();
    boolean debugRule = false;
    boolean rdfs = false;
    boolean owl = false;

    /**
     * Corese as command line take path and query as argument load the docs from
     * path java -cp corese.jar fr.inria.corese.kgtool.Start -load
     * dataset.rdf -query "select * where {?x ?p ?y}" java -cp
     * kggui-3.2.1-SNAPSHOT-jar-with-dependencies.jar
     * fr.inria.corese.kgtool.Start -load rdf: -sttl st:turtle st:rdfxml
     * st:json
     *
     */
    public static void main(String[] args) {
        Start st = new Start();
        st.process(args);
        st.start();
    }

    void process(String[] args) {
        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-rdfs")) {
                i++;
                while (i < args.length && !args[i].startsWith("-")) {
                    if (args[i++].equals("true")){
                        rdfs = true;
                    }
                }
            } 
            else if (args[i].equals("-owl")) {
                i++;
                while (i < args.length && !args[i].startsWith("-")) {
                    if (args[i++].equals("true")){
                        owl = true;
                    }
                }
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
            } else if (args[i].equals("-sttl")) {
                i++;
                while (i < args.length && !args[i].startsWith("-")) {
                    sttl.add(expand(args[i++]));
                }
            } 
        }
    }

    String expand(String str) {
        return nsm.toNamespaceBN(str);
    }

    void start() {
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
            re.process();
        }

        Date d2 = new Date();
        try {
            QueryProcess exec = QueryProcess.create(g);
            for (String q : query) {
                Mappings map = exec.query(q);
                ResultFormat f = ResultFormat.create(map);
                System.out.println(f);
            }
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (String stl : sttl) {
            Transformer t = Transformer.create(g, stl);
            System.out.println(t.transform());
        }
    }
}
