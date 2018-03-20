
package fr.inria.corese.transform;

import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.transform.TemplatePrinter;
import java.io.IOException;
import org.junit.Test;

/**
 * Compile templates into one file  name.rul 
 * st:name is the URI of the transformation
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Compile {

    static String data =   "/home/corby/NetBeansProjects/corese-github/kgtool/src/main/resources/sttl/";
    static String root = data;
    static String srclib = "/home/corby/NetBeansProjects/corese-github/kgtool/src/main/resources/template/";

    public  void translate() {
        TemplatePrinter p =
                TemplatePrinter.create(root + "spin/template", root + "pprint/lib/spin.rul");
        //TemplatePrinter.create(root + "spin/template", lib + "spin.rul");
        try {
            p.process();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LoadException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        new Compile().pplib();
        //new Compile().specific();
    }
    
    void specific(){
         translate("/home/corby/AATest/test/huto/sttl", "/home/corby/AATest/test/huto/huto.rul");
    }
    
    public void pplib() {
        String lib = srclib;
            
        if (true) {
            translate(root + "datashape/main",      lib + "dsmain.rul");
            translate(root + "datashape/core",      lib + "dscore.rul");
            translate(root + "datashape/path",      lib + "dspath.rul");
            translate(root + "datashape/constraint",lib + "dsconstraint.rul");
            translate(root + "datashape/pprint",    lib + "dspprint.rul");
            translate(root + "datashape/result",    lib + "dsresult.rul");
        }

        translate(root + "list", lib + "list.rul");
        translate(root + "system", lib + "system.rul");
        translate(root + "server", lib + "server.rul");
        translate(root + "spin", lib + "spin.rul");
        translate(root + "spinhtml", lib + "spinhtml.rul");
        translate(root + "sql", lib + "sql.rul");
        translate(root + "rdfxml", lib + "rdfxml.rul");
        translate(root + "rdfxmlhtml", lib + "hrdfxml.rul");
        translate(root + "turtle", lib + "turtle.rul");
        translate(root + "turtle2", lib + "turtle2.rul");
        translate(root + "json", lib + "json.rul");
        translate(root + "jsonterm", lib + "jsonterm.rul");

        translate(root + "trig", lib + "trig.rul");
        translate(root + "sparql", lib + "sparql.rul");
        translate(root + "result", lib + "result.rul");
        translate(root + "web", lib + "web.rul");
        translate(root + "rdfs", lib + "rdfs.rul");
        translate(root + "tospin", lib + "tospin.rul");
        translate(root + "locate", lib + "locate.rul");

        translate(root + "owl", lib + "owl1.rul");
        
        translate(root + "owl2/owl",            lib + "owl.rul");
        translate(root + "owl2/owldecl",        lib + "owldecl.rul");
        translate(root + "owl2/owlclass",       lib + "owlclass.rul");
        translate(root + "owl2/owlproperty",    lib + "owlproperty.rul");
        translate(root + "owl2/owlstatement",   lib + "owlstatement.rul");
        translate(root + "owl2/owlexp",         lib + "owlexp.rul");


        translate(root + "spintc/main", lib + "spintc.rul");
        translate(root + "spintc/template", lib + "spintcbody.rul");

        translate(root + "turtlehtml/template", lib + "turtlehtml.rul");
        translate(root + "turtlehtml/main", lib + "hturtle.rul");

        translate(root + "mix", lib + "mix.rul");

        translate(root + "navtable", lib + "navtable.rul");
        translate(root + "navlab", lib + "navlab.rul");
        translate(root + "dbedit", lib + "dbedit.rul");

        translate(root + "cdn", lib + "cdn.rul");
        translate(root + "calendar", lib + "calendar.rul");
        translate(root + "calcontent", lib + "content.rul");
       
        
        //******************************************
        
        translate(root + "pperror", lib + "pperror.rul");
        
        translate(root + "owlrl/main", lib + "owlrl.rul");
        translate(root + "owlrl/axiom", lib + "axiom.rul");
        translate(root + "owlrl/subexp", lib + "subexp.rul");
        translate(root + "owlrl/superexp", lib + "superexp.rul");
        translate(root + "owlrl/equivexp", lib + "equivexp.rul");
        
        translate(root + "owleltc/main", lib + "owleltc.rul");
        translate(root + "owleltc/axiomowleltc", lib + "axiomowleltc.rul");
        translate(root + "owleltc/classexpowleltc", lib + "classexpowleltc.rul");

        translate(root + "owlqltc/main", lib + "owlqltc.rul");
        translate(root + "owlqltc/axiomowlqltc", lib + "axiomowlqltc.rul");
        translate(root + "owlqltc/subexpowlqltc", lib + "subexpowlqltc.rul");
        translate(root + "owlqltc/superexpowlqltc", lib + "superexpowlqltc.rul");

        translate(root + "owltc/main", lib + "owltc.rul");
        translate(root + "owltc/axiomowltc", lib + "axiomowltc.rul");
        translate(root + "owltc/classexpconformityowltc", lib + "classexpconformityowltc.rul");
             
    }

    void translate(String src, String tgt) {
        TemplatePrinter p = TemplatePrinter.create(src, tgt);
        try {
            p.process();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LoadException e) {
            e.printStackTrace();
        }
    }
}
