/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.w3c.shacl;

import static fr.inria.corese.core.util.Property.Value.LOAD_IN_DEFAULT_GRAPH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.workflow.Data;
import fr.inria.corese.core.workflow.ShapeWorkflow;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataShapeTest {
    static final String SHACL = NSManager.SHAPE + "shacl";
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(DataShapeTest.class);

    static final String data = DataShapeTest.class.getClassLoader()
            .getResource("data/data-shapes/data-shapes-test-suite/tests/").getPath() + "/";

    static String[] names = {
            "core/property",
            "core/path",
            "core/node",
            "core/complex",
            "core/misc",
            "core/targets",
            "core/validation-reports",

            "sparql/property",
            "sparql/node",

            // "sparql/component"
    };
    static String qm = "prefix mf:      <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "prefix sht:     <http://www.w3.org/ns/shacl-test#> ."
            + "select * where {"
            + "?m mf:include ?f"
            + "}";

    static String qres = "prefix sh: <http://www.w3.org/ns/shacl#> ."
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

    static String qdata = "prefix sh: <http://www.w3.org/ns/shacl#> ."
            + "prefix mf: <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "prefix sht:     <http://www.w3.org/ns/shacl-test#> ."
            + "select * where {"
            + "?x sht:dataGraph ?data ;"
            + "   sht:shapesGraph ?shape"
            + "}";

    static String qcheck = "prefix sh: <http://www.w3.org/ns/shacl#> ."
            + "prefix mf:      <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "select * where {"
            + "?x sh:conforms ?b "
            + "}";

    static IDatatype dtsuc, dtfail;
    EarlReport report;
    int count = 0;
    int error = 0;
    int num = 0;
    boolean lds = true;
    boolean benchmark = false, repeat = false, verbose = true;
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
            System.out.println(String.format("<td>%s</td><td>%s</td><td>%s</td><td>%s</td>", i++, name, tjava.get(name),
                    tlds.get(name)));
            System.out.println("</tr>");
            djava += tjava.get(name);
            dlds += tlds.get(name);
        }
        System.out.println(String.format("<tr><td></td><td>Total</td><td>%s</td><td>%s</td></tr>", djava, dlds));

        System.out.println("</table>");
        System.out.println("</body>");
        System.out.println("</html>");
    }

    public void main(String[] args) throws LoadException, EngineException, IOException {
        // new DataShapeTest().testSimple();
        new DataShapeTest().testThread();
    }

    void testThread() {
        for (int i = 0; i < 1; i++) {
            System.out.println("thread:" + i);
            new ShapeThread().start();
        }
    }

    class ShapeThread extends Thread {
        @Override
        public void run() {
            try {
                new DataShapeTest().testSimple();
            } catch (LoadException ex) {
                logger.error(ex.getMessage());
            } catch (EngineException | IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    @Test
    public void testSimple() throws LoadException, EngineException, IOException {
        Property.set(LOAD_IN_DEFAULT_GRAPH, true);
        // Property.set(Property.Value.RDF_STAR_TRIPLE, true);
        // Property.set(Property.Value.DATATYPE_ENTAILMENT, false);
        System.out.println("Property: " + Property.display());
        Date d1 = new Date();
        test();
        Date d2 = new Date();
        System.out.println("Total: " + (d2.getTime() - d1.getTime()) / 1000.0);
    }

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
        // Access.set(Access.Feature.LINKED_FUNCTION, Access.Level.USER_DEFAULT);

        // 4 distinct from 4.0
        // Function.typecheck = true;
        // Function.rdftypecheck = true;
        report = new EarlReport("file://" + data);
        // DatatypeMap.setSPARQLCompliant(true);
        for (String name : names) {

            // if (!name.contains("node")){
            // continue;
            // }

            System.out.println(name);
            process(data + name + "/");
            System.out.println();
        }

        report.write("earl-report-test.ttl");
        System.out.println((error == 0) ? "No error" : ("*** ERRORS: " + error));
    }

    public void process(String name) throws LoadException, EngineException {
        Graph g = manifest(name);
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(qm);

        for (Mapping m : map) {
            IDatatype dt = (IDatatype) m.getValue("?f");
            if (repeat) {
                exec(dt.getLabel());
            } else {
                file(dt.getLabel());
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
        // if (! file.contains("equals-001.ttl")) {
        // return;
        // }
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(file);
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(qdata);
        IDatatype datadt = (IDatatype) map.getValue("?data");
        IDatatype shapedt = (IDatatype) map.getValue("?shape");

        Graph greport = exec(shapedt.getLabel(), datadt.getLabel());

        QueryProcess exec0 = QueryProcess.create(greport);
        // does it conform ?
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
        }

        if (true) {
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
    }

    Graph exec(String shape, String data) throws EngineException, LoadException {
        return execds(shape, data);
    }

    // 4.705 4.645
    // 4.275
    Graph execds(String shape, String data) throws EngineException, LoadException {
        Graph g = load(data);
        Graph sh = (data.equals(shape)) ? g : load(shape, g);

        Shacl shacl = new Shacl(g);
        before(shacl);
        // shacl.setTrace(true);
        Graph res = shacl.eval(sh);
        // shacl.setup(Shacl.SETUP_DETAIL, true);
        after(shacl);
        return res;
    }

    void before(Shacl shacl) {
        if (dtsuc != null) {
            shacl.input().setVariable(shacl.TRACEMAPSUC_VAR, dtsuc);
            shacl.input().setVariable(shacl.TRACEMAPFAIL_VAR, dtfail);
        }
    }

    void setup(Shacl shacl) {
        IDatatype map = DatatypeMap.map();
        map.set(DatatypeMap.newResource(shacl.SETUP_DETAIL), DatatypeMap.TRUE);
        shacl.input().setVariable(shacl.SETUP_VAR, map);
    }

    void after(Shacl shacl) {
        dtsuc = shacl.output().getVariable(shacl.TRACEMAPSUC_VAR);
        dtfail = shacl.output().getVariable(shacl.TRACEMAPFAIL_VAR);
    }

    void trace() {
        trace(dtsuc);
        trace(dtfail);
    }

    void trace(IDatatype dt) {
        IDatatype key = DatatypeMap.newResource(NSManager.SHACL, "subtotal");
        for (IDatatype name : dt.getMap().keySet()) {
            IDatatype val = dt.getMap().get(name);
            IDatatype count = val.getMap().get(key);
            System.out.println(name + " " + count);
        }
    }

    Graph execwf(String shape, String data) throws EngineException {
        ShapeWorkflow wf = new ShapeWorkflow(shape, data, false, lds);
        Data res = wf.process();
        return res.getVisitedGraph();
    }

    Graph load(String path) throws LoadException {
        Graph g = Graph.create();
        load(path, g);
        return g;
    }

    Graph load(String path, DataManager man) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g, man);
        ld.parse(path);
        g.index();
        return g;
    }

    Graph load(String path, Graph g) throws LoadException {
        Load ld = Load.create(g);
        ld.parse(path);
        g.index();
        return g;
    }

    void result(Mappings map) {
    }

    void trace(Graph w3c, Graph kg) {
        try {
            Transformer t1 = Transformer.create(w3c, Transformer.TURTLE);
            Transformer t2 = Transformer.create(kg, Transformer.TURTLE);
            System.out.println("w3c report: ");
            System.out.println(t1.transform());
            System.out.println("__");
            System.out.println("kgram report: ");
            System.out.println(t2.transform());
            System.out.println("==");
        } catch (EngineException e) {
            System.out.println(e.getMessage());
        }
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
        try {
            IDatatype dtw3 = (IDatatype) w3.getValue(var);
            IDatatype dtkg = (IDatatype) kg.getValue(var);
            if (dtw3 != null) {
                if (dtkg == null) {
                    System.out.println(i + mes + dtw3 + " " + dtkg);
                    return false;
                } else if (path && dtw3.isBlank()) {
                    // in kg report graph, path is pretty printed and stored in a st:graph custom
                    // literal datatype
                    // in such a way that once pretty printed, validation report graph path is a
                    // SHACL path
                    // hence here we compare the pretty print of W3C path with pretty print of kg
                    // path
                    Transformer t = Transformer.create(g, Transformer.TURTLE);
                    IDatatype ddw3 = t.process(dtw3);
                    if (!ddw3.stringValue().equals(dtkg.stringValue())) {
                        System.out.println(i + mes);
                        System.out.println("w3c: " + ddw3.stringValue());
                        System.out.println("kg:  " + dtkg.stringValue() + " " + dtkg.getList());
                        System.out.println("__");
                        return false;
                    }
                } else if (!((dtw3.isBlank() && dtkg.isBlank()) || dtw3.equals(dtkg))) {
                    System.out.println(i + mes + dtw3 + " " + dtkg);
                    return false;
                }

            }
            return true;
        } catch (EngineException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    Graph manifest(String dir) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(dir + "manifest.ttl");
        return g;
    }
}
