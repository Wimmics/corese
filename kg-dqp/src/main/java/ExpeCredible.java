/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.time.StopWatch;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 * Experiment deployment : - I3S neurolog.unice.fr:8443 - Irisa
 * neurolog.irisa.fr:8443 - Asclepios neurolog.inria.fr:8443 - IFR49
 * neurolog.imed.jussieu.fr:8443 - Gin terpsi.ujf-grenoble:8443
 *
 * @author gaignard
 */
public class ExpeCredible {

    public static void main(String args[]) throws MalformedURLException, EngineException, FileNotFoundException, IOException {

        String qGado = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
                + "PREFIX examination-subject: <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#>"
                + "SELECT distinct ?patient ?study ?dataset ?dsName WHERE { "
                //                + "     ?patient iec:is-referred-to-by/linguistic-expression:has-for-name ?dsName ."
                + "     ?patient iec:is-referred-to-by ?dataset  ."
                + "     ?dataset linguistic-expression:has-for-name ?dsName ."
                + "     ?patient examination-subject:has-for-subject-identifier ?clinID .                "
                //                + "     ?patient iec:is-referred-to-by ?dataset ."
                + "     ?study study:involves-as-patient ?patient ."
                + "     FILTER ((?clinID ~ 'MS') && (?dsName ~ 'GADO')) "
                + "}";

        String qT2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
                + "SELECT distinct ?subject ?d ?name WHERE {"
                + "     ?d linguistic-expression:has-for-name ?name ."
                + "     ?subject iec:is-referred-to-by ?d ."
                //                + "     FILTER ((?name ~ 'T2') && (?subject ~ 'IFR'))"
                + "     FILTER (?name ~ 'T1')"
                + "}";

        
        // Find all subclasses of DTI in NeuroLEX
        // Find all NeuroLOG dataset of this modality ("property:label" used as a bridge between NeuroLOG and NeuroLEX)
        // Find associated dataset name and patient ID
        String nlex = "PREFIX property: <http://neurolex.org/wiki/Special:URIResolver/Property-3A>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
                + "PREFIX examination-subject: <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#>"
                + "SELECT DISTINCT ?patient ?clinID ?datasetName ?label WHERE {"
                //                + "SELECT DISTINCT ?s ?label WHERE {"
                //                + "     ?t property:Label \"MRI protocol\"^^xsd:string ."
                //                + "     ?t property:Label \"T2 weighted protocol\"^^xsd:string ."
                + "     ?t property:Label \"Diffusion magnetic resonance imaging protocol\"^^xsd:string ."
                + "     ?s rdfs:subClassOf* ?t ."
                + "     ?s property:Label ?label ."
                //                + "     ?x property:Label ?label ."
                //                + "     OPTIONAL {?x property:Synonym ?syn}"
                //                + "     ?dataset property:Label \"Diffusion magnetic resonance imaging protocol\"^^xsd:string ."
                + "     ?dataset property:Label ?label ."
                + "     ?dataset linguistic-expression:has-for-name ?datasetName ."
                + "     ?patient iec:is-referred-to-by ?dataset ."
                + "     ?patient examination-subject:has-for-subject-identifier ?clinID ."
                + "}";


        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg3 = RemoteProducerServiceClient.getPort("http://localhost:8093/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg4 = RemoteProducerServiceClient.getPort("http://localhost:8094/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg1.initEngine();
        kg2.initEngine();
        kg3.initEngine();
        kg4.initEngine();

        kg1.loadRDF("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-i3s.rdf");
        kg2.loadRDF("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-irisa.rdf");
        kg3.loadRDF("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-ifr49.rdf");
        kg4.loadRDF("/Users/gaignard/Desktop/Open-LS-LinkedData/nlx_stage_all.owl");
        kg4.loadRDF("/Users/gaignard/Desktop/Open-LS-LinkedData/bridgeNeuroLEX.rdf");

        Graph graph = Graph.create();

        QueryProcessDQP exec = QueryProcessDQP.create(graph);
        exec.addRemote(new URL("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://localhost:8093/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://localhost:8094/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        StopWatch sw = new StopWatch();
        sw.start();
        System.out.println("--------");
//        Mappings maps = exec.query(nlex);
//        Mappings maps = exec.query(qT2);
        Mappings maps = exec.query(qGado);

        System.out.println("---- Results in " + sw.getTime() + " ms ----");
        System.out.println(maps.size());
        System.out.println(XMLFormat.create(maps));
        System.out.println("----");

    }
}
