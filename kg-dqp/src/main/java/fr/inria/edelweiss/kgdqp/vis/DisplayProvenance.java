package fr.inria.edelweiss.kgdqp.vis;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.print.JSOND3Format;
import fr.inria.corese.kgtool.print.JSONFormat;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.lang.time.StopWatch;
//import org.graphstream.graph.Edge;
//import org.graphstream.graph.implementations.MultiGraph;
//import org.graphstream.ui.layout.springbox.implementations.LinLog;
//import org.graphstream.ui.layout.springbox.implementations.SpringBox;
//import org.graphstream.ui.swingViewer.Viewer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class DisplayProvenance {

    private String stylesheet =
            "graph {\n"
            + "\t  fill-color:white;\n"
            + "}\n"
            + "node {\n"
            + "\t  text-size:12;\n"
            + "\t  text-color:black;\n"
            + "\t  text-style:bold;\n"
            + "\t  text-alignment:center;\n"
            + "\t  size:17;\n"
            + "\t  size-mode:fit;\n"
            + "\t  fill-color:lightblue;\n"
            + "\t  shape:circle;\n"
            + "}\n"
            + "node.Literal {\n"
            + "\t  text-size:9;\n"
            + "\t  text-color:black;\n"
            + "\t  text-style:bold;\n"
            + "\t  text-alignment:center;\n"
            + "\t  size:17;\n"
            + "\t  size-mode:fit;\n"
            + "\t  fill-color:orange;\n"
            + "\t  shape:rounded-box;\n"
            + "}\n"
            + "node.Blank {\n"
            + "\t  text-size:9;\n"
            + "\t  text-color:black;\n"
            + "\t  text-style:bold;\n"
            + "\t  text-alignment:center;\n"
            + "\t  size:17;\n"
            + "\t  size-mode:fit;\n"
            + "\t  fill-color:yellow;\n"
            + "\t  shape:circle;\n"
            + "}\n"
            + "node.Class {\n"
            + "\t  text-size:9;\n"
            + "\t  text-color:black;\n"
            + "\t  text-style:bold;\n"
            + "\t  text-alignment:center;\n"
            + "\t  size:17;\n"
            + "\t  size-mode:fit;\n"
            + "\t  fill-color:blue;\n"
            + "\t  shape:circle;\n"
            + "}\n"
            + "edge {\n"
            + "\t  text-color:black;\n"
            + "\t  text-size:12;\n"
            + "\t  size:2;\n"
            + "\t  fill-color:grey;\n"
            + "\t  text-alignment:center;\n"
            + "\t  shape:cubic-curve;\n"
            + "}";
    
    static String sparqlQuery = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>\n"
            + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>\n"
            + "SELECT ?departement ?nom ?popTotale WHERE {\n"
            + "	?region igeo:codeRegion \"24\" .\n"
            + "	?region igeo:subdivisionDirecte ?departement .\n"
            + "	?departement igeo:nom ?nom .\n"
            + "	?departement idemo:population ?popLeg .\n"
            + "	?popLeg idemo:populationTotale ?popTotale .\n"
            + "} ORDER BY ?popTotale";

    static String pathQuery1 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
            + "SELECT distinct * WHERE {"
            + "   <http://i3s/Alban> (foaf:knows/foaf:knows) ?y ."
            + "}";

    
//     public void display(Graph kg) {
//
//        String sujetUri, predicat, objetUri;
//        String temp = "http://www.inria.fr/acacia/corese#Results";
//
//        org.graphstream.graph.Graph graph = new MultiGraph("prov", false, true);
//
//        //temp
////        graph.addNode(temp).addAttribute("ui.style", "fill-color:yellow;");
//
//        String sujet = null;
//        String objet = null;
//
//        Iterable<Entity> edges = kg.getEdges();
//
//        int num = 0;
//        for (Entity ent : edges) {
//
//            fr.inria.edelweiss.kgram.api.core.Edge edge = ent.getEdge();
//            sujetUri = edge.getNode(0).getLabel();
//            objetUri = edge.getNode(1).getLabel();
//
//            predicat = getLabel(edge.getEdgeNode().getLabel());
//
//            sujet = sujetUri;
//            objet = objetUri;
//
//
//            org.graphstream.graph.Node gSubject = graph.getNode(sujetUri);
//            if (gSubject == null) {
//                gSubject = graph.addNode(sujetUri);
//                gSubject.addAttribute("label", sujet);
//
//                if (edge.getNode(0).isBlank()) {
//                    gSubject.setAttribute("ui.class", "Blank");
//                }
//                //temp
////                Edge ee = graph.addEdge("temp" + num, sujetUri, temp);
////                ee.addAttribute("ui.style", "size:1;fill-color:yellow;");
////                num++;
//            }
//
//
//            org.graphstream.graph.Node gObject = graph.getNode(objetUri);
//            //if (find(objetUri, graph.getNodeIterator()) == null) {
//            if (gObject == null) {
//                gObject = graph.addNode(objetUri);
//                gObject.addAttribute("label", objet);
////                    gobj.setAttribute("ui.class", objet);
//                if (edge.getNode(1).isBlank()) {
//                    gObject.setAttribute("ui.class", "Blank");
//                }
//                IDatatype dt = (IDatatype) edge.getNode(1).getValue();
//                if (dt.isLiteral()) {
//                    gObject.setAttribute("ui.class", "Literal");
//                }
//                //temp
////                Edge ee = graph.addEdge("temp" + num, objetUri, temp);
////                ee.addAttribute("ui.style", "size:1;fill-color:yellow;");
////                num++;
//            }
//            Edge ee = graph.addEdge("edge" + num, sujetUri, objetUri, true);
//            ee.addAttribute("label", predicat);
//            num++;
//        }
//        graph.addAttribute("ui.stylesheet", stylesheet);
//        graph.addAttribute("ui.antialias");
//
//        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
//
//        //permet de visualiser correctement le graphe dans l'onglet de Corese
//        SpringBox sLayout = new SpringBox();
////        eb.setForce((float) 0.05);
//        LinLog lLayout = new LinLog();
//        lLayout.setQuality(0.9);
//        lLayout.setGravityFactor(0.9);
//
////        Viewer sgv = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
//        Viewer sgv = graph.display();
//        sgv.enableAutoLayout(lLayout);
////        View sgr = sgv.addDefaultView(false);
////        View sgr = sgv.getDefaultView();
//    }

    private String getLabel(String name) {
        int ind = name.lastIndexOf("#");
        if (ind == -1) {
            ind = name.lastIndexOf("/");
        }
        if (ind == -1 || ind == name.length() - 1) {
            return name;
        }
        return name.substring(ind + 1);
    }
    
    
    
     
     public void displayDistValues(Graph kg) throws EngineException {
        String provQuery = "PREFIX prov:<" + Util.provPrefix + ">"
                + "CONSTRUCT {"
                + " ?x prov:wasAttributedTo ?ds1 ."
                + " ?x rdf:subject ?s1 ."
                + " ?x rdf:predicate ?p1 ."
                + " ?x rdf:object ?o1 ."
                
                + " ?y prov:wasAttributedTo ?ds2 ."
                + " ?y rdf:subject ?s2 ."
                + " ?y rdf:predicate ?p2 ."
                + " ?y rdf:object ?o2 ."
                + "} "
                + " WHERE {"
                + " ?x prov:wasAttributedTo ?ds1 ."
                + " ?x rdf:subject ?s1 ."
                + " ?x rdf:predicate ?p1 ."
                + " ?x rdf:object ?o1 ."
                
                + " ?y prov:wasAttributedTo ?ds2 ."
                + " ?y rdf:subject ?s2 ."
                + " ?y rdf:predicate ?p2 ."
                + " ?y rdf:object ?o2 ."
                + "FILTER ((?ds1 != ?ds2) && ((?s1 = ?s2) || (?p1 = ?p2) || (?o1 = ?o2)))"
//                + "FILTER ((?ds1 != ?ds2) && (?s1 = ?s2))"
                + "}";
        
//         String provQuery2 = "PREFIX prov:<" + Util.provPrefix + ">"
//                + "CONSTRUCT {"
//                + " ?x prov:wasAttributedTo ?ds1 ."
//                + " ?x rdfs:value ?v ."
//                
//                + " ?y prov:wasAttributedTo ?ds2 ."
//                + " ?y rdfs:value ?v ."
//                + "} "
//                + " WHERE {"
//                + " ?x prov:wasAttributedTo ?ds1 ."
//                + " ?x (rdf:subject | rdf:predicate | rdf:object) ?v ."
//                
//                + " ?y prov:wasAttributedTo ?ds2 ."
//                + " ?y (rdf:subject | rdf:predicate | rdf:object) ?v ."
//                + "FILTER (?ds1 != ?ds2)"
//                + "}";


        QueryProcess qp = QueryProcess.create(kg);
        Mappings maps = qp.query(provQuery);
        System.out.println("Displayed graph : #"+((Graph)maps.getGraph()).size());
//        display((Graph) maps.getGraph());
    }
    
     public void displayProvenance(Graph kg) throws EngineException {
        String provQuery = "PREFIX prov:<" + Util.provPrefix + ">"
                + "CONSTRUCT {"
                + " ?s <http://userProv/comesFrom> ?e ."
                + " ?p <http://userProv/comesFrom> ?e ."
                + " ?o <http://userProv/comesFrom> ?e ."
                + "} WHERE {"
                + " ?x prov:wasAttributedTo ?e ."
                + " ?x rdf:subject ?s ."
                + " ?x rdf:predicate ?p ."
                + " ?x rdf:object ?o ."
                + "}";

        QueryProcess qp = QueryProcess.create(kg);
        Mappings maps = qp.query(provQuery);
        System.out.println("Displayed graph : #"+((Graph)maps.getGraph()).size());
//        display((Graph) maps.getGraph());
    }
    
     public void displayLight(Graph kg) throws EngineException {
//        String provQuery = "PREFIX prov:<" + Util.provPrefix + ">"
//                + "CONSTRUCT {"
//                + " ?x ?p ?y ."
//                + "} WHERE {"
//                + " ?x ?p ?y ."
//                + " MINUS { "
//                + "     {{?x rdf:type ?y} UNION {?x prov:wasGeneratedBy ?y}} UNION {?x prov:qualifiedAssociation ?y}."
//                + "} ."
//                + "}";
        
         String provQuery = "PREFIX prov:<" + Util.provPrefix + ">"
                + "CONSTRUCT {"
                + " ?x ?p ?y ."
                + "} WHERE {"
                + " ?x ?p ?y ."
                + " FILTER (?p NOT IN (rdf:type, prov:wasGeneratedBy, prov:qualifiedAssociation, prov:hadPlan, prov:agent, rdfs:comment)) "
                + "} ";

        QueryProcess qp = QueryProcess.create(kg);
        Mappings maps = qp.query(provQuery);
        System.out.println("Displayed graph : #"+((Graph)maps.getGraph()).size());
//        System.out.println(JSOND3Format.create(maps));
//        display((Graph) maps.getGraph());
    }

    
    
    public static void main(String args[]) throws MalformedURLException, EngineException {

        DisplayProvenance provDisplayer = new DisplayProvenance();
        
        
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        
        Graph graph = Graph.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph, true);
        exec.addRemote(new URL("http://localhost:9091/kgram/sparql"), WSImplem.REST);
        exec.addRemote(new URL("http://localhost:9092/kgram/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
//        Mappings map = exec.query(pathQuery1);
        Mappings maps = exec.query(sparqlQuery);
        int dqpSize = maps.size();
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + dqpSize);
        System.out.println(maps.toString());

        
        
        Graph resProv = Graph.create();        
        for (Mapping map : maps) {
            Graph mapGraph = Graph.create();
            for (Entity ent : map.getEdges()) {
                Graph prov = (Graph) ent.getProvenance();
                mapGraph.copy(prov);
            }
            resProv.copy(mapGraph);
        }

        System.out.println("");
        System.out.println(resProv.size());
        System.out.println(resProv);
        System.out.println("");
        
        
        provDisplayer.displayLight(resProv);
//        provDisplayer.displayDistValues(resProv);
//        provDisplayer.displayProvenance(resProv);

    }
}
