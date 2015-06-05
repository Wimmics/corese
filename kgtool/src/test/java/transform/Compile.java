
package transform;

import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.transform.TemplatePrinter;
import java.io.IOException;
import org.junit.Test;

/**
 * Compile templates into one file  name.rul 
 * st:name is the URI of the transformation
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Compile {

    static String data =   "/home/corby/NetBeansProjects/kgram/trunk/kgtool/src/main/resources/sttl/";
    static String root = data;
    static String srclib = "/home/corby/NetBeansProjects/kgram/trunk/kgtool/src/main/resources/template/";

    public void translate() {
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

    @Test
    public void pplib() {
        String lib = srclib;

        translate(root + "system", lib + "system.rul");
        translate(root + "server", lib + "server.rul");
        translate(root + "spin", lib + "spin.rul");
        translate(root + "spinhtml", lib + "spinhtml.rul");
        translate(root + "sql", lib + "sql.rul");
        translate(root + "owl", lib + "owl.rul");
        translate(root + "rdfxml", lib + "rdfxml.rul");
        translate(root + "turtle", lib + "turtle.rul");

        translate(root + "trig", lib + "trig.rul");
        translate(root + "sparql", lib + "sparql.rul");
        translate(root + "result", lib + "result.rul");
        translate(root + "web", lib + "web.rul");
        translate(root + "rdfs", lib + "rdfs.rul");
        translate(root + "tospin", lib + "tospin.rul");
        translate(root + "locate", lib + "locate.rul");

        translate(root + "owlrl/main", lib + "owlrl.rul");
        translate(root + "owlrl/axiom", lib + "axiom.rul");
        translate(root + "owlrl/subexp", lib + "subexp.rul");
        translate(root + "owlrl/superexp", lib + "superexp.rul");
        translate(root + "owlrl/equivexp", lib + "equivexp.rul");

        translate(root + "spintc/main", lib + "spintc.rul");
        translate(root + "spintc/template", lib + "spintcbody.rul");

        translate(root + "turtlehtml/template", lib + "turtlehtml.rul");
        translate(root + "turtlehtml/main", lib + "hturtle.rul");

        translate(root + "mix", lib + "mix.rul");

        translate(root + "navlab", lib + "navlab.rul");

        translate(root + "cdn", lib + "cdn.rul");
      
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
