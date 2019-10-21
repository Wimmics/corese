/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.test.research;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.workflow.Data;
import fr.inria.corese.core.workflow.ShapeWorkflow;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataShapeTest {
    static final String SHACL = NSManager.SHAPE+"shacl";

    //static String data = "/user/corby/home/AATest/data-shapes/data-shapes-test-suite/tests/";
    static final String data = 
       DataShapeTest.class.getClassLoader().getResource("data/data-shapes/data-shapes-test-suite/tests/").getPath()+"/";
    
    static String[] names = {
        "core/property"
            , "core/path", "core/node", "core/complex", "core/misc", "core/targets", "core/validation-reports",
       "sparql/property"
            
       //,"sparql/node" ,
       //  "sparql/component"
    };
    static String qm =
            "prefix mf:      <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "prefix sht:     <http://www.w3.org/ns/shacl-test#> ."
            + "select * where {"
            + "?m mf:include ?f"
            + "}";
    static String qres =
            "prefix sh: <http://www.w3.org/ns/shacl#> ."
            + "prefix mf:      <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "select * where {"
            + "?x sh:conforms ?b "
            + "optional { ?res mf:result ?x }"
            + "optional { "
            + "?x sh:result ?r "
            + "?r sh:focusNode ?f ; "
            + "   sh:sourceConstraintComponent ?c "
            + "optional { ?r sh:sourceShape ?sh } "
            + "optional { ?r sh:value ?val } "
            + "optional { ?r sh:resultPath ?p }"
            + "optional { ?r sh:resultSeverity ?s }"
            + "optional { ?r sh:resultMessage ?m }"
            + "}"
            + "}"
            + "order by ?f ?val ";
    static String qdata =
            "prefix sh: <http://www.w3.org/ns/shacl#> ."
            + "prefix mf: <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "prefix sht:     <http://www.w3.org/ns/shacl-test#> ."
            + "select * where {"
            + "?x sht:dataGraph ?data ;"
            + "   sht:shapesGraph ?shape"
            + "}";
    static String qcheck =
            "prefix sh: <http://www.w3.org/ns/shacl#> ."
            + "prefix mf:      <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "select * where {"
            + "?x sh:conforms ?b "
            + "}";
    EarlReport report;
    int count = 0;
    int error = 0;
    boolean lds = true;
    boolean benchmark = false, repeat = false, verbose=true;
    boolean done = false;
    HashMap<String, Double> tjava, tlds;

    public DataShapeTest() {
        tjava = new HashMap<String, Double>();
        tlds = new HashMap<String, Double>();

    }

    void display() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : tjava.keySet()) {
            list.add(s);
        }
        Collections.sort(list);
        double djava = 0, dlds = 0;
        int i = 1;
        System.out.println("<html>");
        System.out.println("<head>");
        System.out.println("<style>");
        System.out.println(".table tr:nth-child(even) { background:#fafafa }");
        System.out.println(".table tr:nth-child(odd)  { background:#eeeeee }");
        System.out.println("body { font-family: arial }");
        System.out.println("</style>");
        System.out.println("</head>");
        System.out.println("<body>");
        System.out.println("<table class='table'>");
        System.out.println("<tr><th>Num</th><th>Test</th><th>Java</th><th>LDScript</th></tr>");
        for (String name : list) {
            System.out.println("<tr>");
            System.out.println(String.format("<td>%s</td><td>%s</td><td>%s</td><td>%s</td>", i++, name, tjava.get(name), tlds.get(name)));
            System.out.println("</tr>");
            djava += tjava.get(name);
            dlds += tlds.get(name);
        }
        System.out.println(String.format("<tr><td></td><td>Total</td><td>%s</td><td>%s</td></tr>", djava, dlds));

        System.out.println("</table>");
        System.out.println("</body>");
        System.out.println("</html>");
    }

    public static void main(String [] args) throws LoadException, EngineException, IOException {
        //new DataShapeTest().testSimple();
        new DataShapeTest().testThread();
    }
    
    void testThread() {
        for (int i = 0; i<1; i++) {
            new ShapeThread().start();
        }
    }
    
    class ShapeThread extends Thread {
        @Override
        public void run() {
            try {
                new DataShapeTest().testSimple();
            } catch (LoadException ex) {
                Logger.getLogger(DataShapeTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EngineException ex) {
                Logger.getLogger(DataShapeTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DataShapeTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
      @Test
     public void testSimple() throws LoadException, EngineException, IOException {
        //Graph.METADATA_DEFAULT = true;
        Date d1 = new Date();
        //lds = false; // Java
        test();
        Date d2 = new Date();
        System.out.println("Total: " + (d2.getTime() - d1.getTime()) / 1000.0);
    }

    

    //@Test
    /**
     * Java : 5.6014 LDS : 5.1073
     */
    public void benchmark() throws LoadException, EngineException, IOException {
        benchmark = true;
        lds = true;
        System.out.println("LDScript");
        process();
        lds = false;
        System.out.println("Java");
        process();
        display();
    }

    public void process() throws LoadException, EngineException, IOException {
        for (int i = 0; i < 5; i++) {
            // warm up
            test();
        }
        repeat = true;

        Date d1 = new Date();
        test();
        repeat = false;
        Date d2 = new Date();
        System.out.println("Total: " + (d2.getTime() - d1.getTime()) / 1000.0);

    }

   
    public void test() throws LoadException, EngineException, IOException {
          Access.set(Access.Feature.LINKED_FUNCTION, Access.Level.DEFAULT);

        // 4 distinct from 4.0
//        Function.typecheck = true;
//        Function.rdftypecheck = true;
        report = new EarlReport("file://" + data);
        //DatatypeMap.setSPARQLCompliant(true);
        for (String name : names) {

//            if (!name.contains("path")){
//                continue;
//            }

            System.out.println(name);
            process(data + name + "/");
            System.out.println();
            //break;
        }

        //report.write("/home/corby/AATest/data-shapes/earl-report-test.ttl");
        report.write("earl-report-test.ttl");
        System.out.println((error == 0) ? "No error" : ("*** ERRORS: " + error));
    }

    public void process(String name) throws LoadException, EngineException {
        Graph g = manifest(name);
//Binding.DEBUG_DEFAULT = true;
//Memory.DEBUG_DEFAULT = true;
//Mapping.DEBUG_DEFAULT = true;
//Eval.NAMED_GRAPH_DEFAULT = true;
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(qm);

        for (Mapping m : map) {
            IDatatype dt = (IDatatype) m.getValue("?f");
            if (repeat) {
                exec(dt.getLabel());
            } else //if (dt.getLabel().contains("qualifiedMinCountDisjoint-001.ttl"))
            {
                file(dt.getLabel());
                //break;
            }
        }
    }

    void exec(String file) throws EngineException, LoadException {
        Date d1 = new Date();
        for (int i = 0; i < 10; i++) {
            file(file);
        }
        Date d2 = new Date();
        record(file, (d2.getTime() - d1.getTime()) / 10000.0);
        System.out.println(file + " " + (d2.getTime() - d1.getTime()) / 10000.0);
    }

    void record(String f, double d) {
        if (lds) {
            tlds.put(f, d);
        } else {
            tjava.put(f, d);
        }
    }

    void file(String file) throws EngineException, LoadException {
//        if (file.contains("datatype-001.ttl")) { // || file.contains("and-001")){
//            //ok
//        }
//        else {
//            //return;
//        }

        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(file);
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(qdata);
        IDatatype datadt  = (IDatatype) map.getValue("?data");
        IDatatype shapedt = (IDatatype) map.getValue("?shape");
        
        Graph greport    = exec2(shapedt.getLabel(), datadt.getLabel());

//        Graph greport = exec(shapedt.getLabel(), datadt.getLabel());
//        if (greport.size() != ggg.size()) {
//            System.out.println("error: " + greport.size() + " " + ggg.size());
//            System.out.println(Transformer.create(greport, Transformer.TURTLE).process().getLabel());
//            System.out.println(Transformer.create(ggg, Transformer.TURTLE).process().getLabel());
//
//        }

        QueryProcess exec0 = QueryProcess.create(greport);
        Mappings mm = exec0.query(qcheck);

        QueryProcess exec1 = QueryProcess.create(greport);
        Mappings mapkgram = exec1.query(qres);

        QueryProcess exec2 = QueryProcess.create(g);
        Mappings mapw3c = exec2.query(qres);

        String mes = "";
        if (mapkgram.size() != mapw3c.size()) {
            mes = "*** ";
            error++;
        }

        if (!benchmark || mapkgram.size() != mapw3c.size()) {
            if (verbose) {
                System.out.println(count++ + " " + mes + file + " " + mapw3c.size() + " " + mapkgram.size());
            }
//            System.out.println("w3c: \n"   + mapw3c);
//            System.out.println("--");
//            System.out.println("kgram: \n" + mapkgram);
        }
        if (mm.size() != 1) {
            System.out.println("**** " + mm.size() + " reports");
        }

        if (mapkgram.size() == mapw3c.size()) {
            boolean suc = compare(file, g, mapw3c, mapkgram);
            report.result(mapw3c, suc);
            if (!suc) {
                error++;
            }
        } else {
            trace(g, greport);
            report.result(mapw3c, false);
        }
    }
    
    Graph exec(String shape, String data) throws EngineException {
        ShapeWorkflow wf = new ShapeWorkflow(shape, data, false, lds);
        Data res = wf.process();
        return res.getVisitedGraph();
    }
    
    
    Graph load(String path) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(path);
        g.index(); 
        return g;
    }
    
    Graph exec2(String shape, String data) throws EngineException, LoadException {
        Graph g  = load(data);        
        Graph sh = (data.equals(shape)) ? g : load(shape);  
        Shacl shacl = new Shacl(g);
        //shacl.setTrace(true);
        Graph res = shacl.eval(sh);
        return res;
    }

    
    
    void result(Mappings map) {
    }

    void trace(Graph w3c, Graph kg) {
        Transformer t1 = Transformer.create(w3c, Transformer.TURTLE);
        Transformer t2 = Transformer.create(kg, Transformer.TURTLE);
        System.out.println("w3c report: ");
        System.out.println(t1.transform());
        System.out.println("__");
        System.out.println("kgram report: ");
        System.out.println(t2.transform());
        System.out.println("==");
    }

    boolean compare(String file, Graph g, Mappings w3c, Mappings kgram) {
        int i = 0;
        boolean suc = true;
        for (Mapping m1 : w3c) {
            Mapping m2 = kgram.get(i);

            boolean b1 = compare(i, " confo: ", m1, m2, "?b", false);
            boolean b2 = compare(i, " focus: ", m1, m2, "?f", false);
            boolean b3 = compare(i, " value: ", m1, m2, "?val", false);
            boolean b4 = compare(i, " path: ", g, m1, m2, "?p", true);
            boolean b5 = compare(i, " const: ", m1, m2, "?c", false);
            boolean b6 = compare(i, " sever: ", m1, m2, "?s", false);
            boolean b7 = compare(i, " shape: ", m1, m2, "?sh", false);
            boolean b8 = compare(i, " mess: ", m1, m2, "?m", false);

            suc = suc && b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8;
            i++;
        }

        return suc;
    }

    boolean compare(int i, String mes, Mapping w3, Mapping kg, String var, boolean path) {
        return compare(i, mes, null, w3, kg, var, path);
    }

    boolean compare(int i, String mes, Graph g, Mapping w3, Mapping kg, String var, boolean path) {
        IDatatype dt1 = (IDatatype) w3.getValue(var);
        IDatatype dt2 = (IDatatype) kg.getValue(var);
        if (dt1 != null) {
            if (dt2 == null) {
                System.out.println(i + mes + dt1 + " " + dt2);
                return false;
            } else if (path && dt1.isBlank()) {
                // in kg report graph,  path is pretty printed and stored in a st:graph custom literal datatype
                // in such a way that once pretty printed, validation report graph path is a SHACL path
                // hence here we compare the pretty print of  W3C path with pretty print of kg path
                Transformer t = Transformer.create(g, Transformer.TURTLE);
                IDatatype res = t.process(dt1);
                if (!res.stringValue().equals(dt2.stringValue())) {
                    System.out.println(i + mes);
                    System.out.println(res.stringValue() + "\n" + dt2.stringValue());
                    System.out.println("__");
                    return false;
                }
            } else //if ((!bn || !p1.isBlank()) && !p1.equals(p2)) {
            if (!((dt1.isBlank() && dt2.isBlank()) || dt1.equals(dt2))) {
                System.out.println(i + mes + dt1 + " " + dt2);
                return false;
            }

        }
        return true;
    }

    Graph manifest(String dir) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(dir + "manifest.ttl");
        return g;
    }
}
