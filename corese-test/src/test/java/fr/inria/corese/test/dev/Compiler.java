package fr.inria.corese.test.dev;

import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.transform.TemplatePrinter;
import java.io.IOException;

/**
 * Compile templates into one file name.rul st:name is the URI of the
 * transformation
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Compiler {

    static final String WHERE = "/home/corby/NetBeansProjects/corese-github-v4/corese-core/src/main/resources/";
    static final String ROOT   = WHERE + "sttl/";
    static final String SRCLIB = WHERE + "template/";


    public static void main(String[] args) {
        //new Compiler().pplib();
        new Compiler().d3();
    }
    
    
    void translate(String src, String tgt) {
        TemplatePrinter p = TemplatePrinter.create(src, tgt);
        try {
            p.process();
        } catch (IOException | LoadException e) {
            e.printStackTrace();
        }
    }

    public void d3() {
        String lib = SRCLIB;
//        translate(ROOT + "turtlehtml/template", lib + "turtlehtml.rul");
//        translate(ROOT + "turtlehtml/main", lib + "hturtle.rul");
        
        //translate(ROOT + "test",       lib + "test.rul");       
        
        translate(ROOT + "logger", lib + "logger.rul");
        //translate(ROOT + "turtle", lib + "turtle.rul");
//
//                translate(ROOT + "cdn", lib + "cdn.rul");
//                translate(ROOT + "dbedit", lib + "dbedit.rul");
//                translate(ROOT + "dbhistory", lib + "dbhistory.rul");
 //       translate(ROOT + "turtlehtml/template", lib + "turtlehtml.rul");

//        translate(ROOT + "datashape/main",       lib + "dsmain.rul");       
//        translate(ROOT + "datashape/core",       lib + "dscore.rul");
//        translate(ROOT + "datashape/constraint", lib + "dsconstraint.rul");
//        translate(ROOT + "datashape/path",       lib + "dspath.rul");

        //translate(ROOT + "d3", lib + "d3.rul");
//        translate(ROOT + "json", lib + "json.rul");
//            translate(ROOT + "datashape/pprint", lib + "dspprint.rul");
//            translate(ROOT + "datashape/result", lib + "dsresult.rul");
    }
    
    public void pplib() {
        String lib = SRCLIB;

        if (true) {
            translate(ROOT + "datashape/main", lib + "dsmain.rul");
            translate(ROOT + "datashape/core", lib + "dscore.rul");
            translate(ROOT + "datashape/path", lib + "dspath.rul");
            translate(ROOT + "datashape/constraint", lib + "dsconstraint.rul");
            translate(ROOT + "datashape/pprint", lib + "dspprint.rul");
            translate(ROOT + "datashape/result", lib + "dsresult.rul");
        }

        translate(ROOT + "combine", lib + "combine.rul");
        translate(ROOT + "d3", lib + "d3.rul");
        translate(ROOT + "sensor", lib + "sensor.rul");
        translate(ROOT + "list", lib + "list.rul");
        translate(ROOT + "system", lib + "system.rul");
        translate(ROOT + "server", lib + "server.rul");
        translate(ROOT + "spin", lib + "spin.rul");
        translate(ROOT + "spinhtml", lib + "spinhtml.rul");
        translate(ROOT + "sql", lib + "sql.rul");
        translate(ROOT + "rdfxml", lib + "rdfxml.rul");
        translate(ROOT + "rdfxmlhtml", lib + "hrdfxml.rul");
        translate(ROOT + "turtle", lib + "turtle.rul");
        translate(ROOT + "logger", lib + "logger.rul");
        translate(ROOT + "jsonld-light", lib + "jsonld-light.rul");
        translate(ROOT + "jsonld-light-term", lib + "jsonld-light-term.rul");
        translate(ROOT + "json", lib + "json.rul");
        translate(ROOT + "jsonterm", lib + "jsonterm.rul");

        translate(ROOT + "trig", lib + "trig.rul");
        translate(ROOT + "sparql", lib + "sparql.rul");
        translate(ROOT + "result", lib + "result.rul");
        translate(ROOT + "web", lib + "web.rul");
        translate(ROOT + "rdfs", lib + "rdfs.rul");
        translate(ROOT + "tospin", lib + "tospin.rul");
        translate(ROOT + "locate", lib + "locate.rul");

        translate(ROOT + "owl", lib + "owl1.rul");

        translate(ROOT + "owl2/owl", lib + "owl.rul");
        translate(ROOT + "owl2/owldecl", lib + "owldecl.rul");
        translate(ROOT + "owl2/owlclass", lib + "owlclass.rul");
        translate(ROOT + "owl2/owlproperty", lib + "owlproperty.rul");
        translate(ROOT + "owl2/owlstatement", lib + "owlstatement.rul");
        translate(ROOT + "owl2/owlexp", lib + "owlexp.rul");

        translate(ROOT + "spintc/main", lib + "spintc.rul");
        translate(ROOT + "spintc/template", lib + "spintcbody.rul");

        translate(ROOT + "turtlehtml/template", lib + "turtlehtml.rul");
        translate(ROOT + "turtlehtml/main", lib + "hturtle.rul");

        translate(ROOT + "mix", lib + "mix.rul");

        translate(ROOT + "navtable", lib + "navtable.rul");
        translate(ROOT + "navlab", lib + "navlab.rul");
        translate(ROOT + "dbedit", lib + "dbedit.rul");
        translate(ROOT + "dbhistory", lib + "dbhistory.rul");

        translate(ROOT + "cdn", lib + "cdn.rul");
        translate(ROOT + "calendar", lib + "calendar.rul");
        translate(ROOT + "calcontent", lib + "content.rul");

        //******************************************
        translate(ROOT + "pperror", lib + "pperror.rul");

        translate(ROOT + "owlrl/main", lib + "owlrl.rul");
        translate(ROOT + "owlrl/axiom", lib + "axiom.rul");
        translate(ROOT + "owlrl/subexp", lib + "subexp.rul");
        translate(ROOT + "owlrl/superexp", lib + "superexp.rul");
        translate(ROOT + "owlrl/equivexp", lib + "equivexp.rul");

        translate(ROOT + "owleltc/main", lib + "owleltc.rul");
        translate(ROOT + "owleltc/axiomowleltc", lib + "axiomowleltc.rul");
        translate(ROOT + "owleltc/classexpowleltc", lib + "classexpowleltc.rul");

        translate(ROOT + "owlqltc/main", lib + "owlqltc.rul");
        translate(ROOT + "owlqltc/axiomowlqltc", lib + "axiomowlqltc.rul");
        translate(ROOT + "owlqltc/subexpowlqltc", lib + "subexpowlqltc.rul");
        translate(ROOT + "owlqltc/superexpowlqltc", lib + "superexpowlqltc.rul");

        translate(ROOT + "owltc/main", lib + "owltc.rul");
        translate(ROOT + "owltc/axiomowltc", lib + "axiomowltc.rul");
        translate(ROOT + "owltc/classexpconformityowltc", lib + "classexpconformityowltc.rul");

    }

}
