/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.time.StopWatch;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class G5k_VIP_Dist_Prov {

    public static void main(String args[]) throws MalformedURLException, EngineException, FileNotFoundException, IOException {

        String queryRuleFantom = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX opmo: <http://openprovenance.org/model/opmo#>"
            + "PREFIX opmv: <http://purl.org/net/opmv/ns#>"
            + "PREFIX vip: <http://www.i3s.unice.fr/modalis/vip#>"
            + "PREFIX ws: <http://www.irisa.fr/visages/team/farooq/ontologies/web-service-owl-lite.owl#>"
            + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
            + "CONSTRUCT { "
            + "     ?out vip:has-for-fantom ?in"
            + "} \n"
//            + "SELECT distinct ?x ?out ?in ?cIn ?role ?tech "
            + "WHERE { "
//            + "     ?agent (iec:refers-to/rdf:type) <http://vip.cosinus.anr.vip.fr/vip-simulation.owl#image-reconstruction-simulator-component>"
            + "     ?agent iec:refers-to ?ref1 ."
            + "     ?ref1 rdf:type <http://vip.cosinus.anr.vip.fr/vip-simulation.owl#image-reconstruction-simulator-component> ."
            + "     ?wcb opmo:cause ?agent ."
            + "     ?wcb opmo:effect ?x  ."
            + "     ?x rdf:type opmv:Process ."
            + "     ?wgb opmo:cause ?x ."
            + "     ?wgb opmo:effect ?out ."
//                        + "     ?out (opmo:avalue/opmo:content) ?cOut ."
            //            + ""
            + "     ?agent2 iec:refers-to ?ref2 ."
            + "     ?ref2 rdf:type <http://vip.cosinus.anr.vip.fr/vip-simulation.owl#parameters-generation-simulator-component> ."
            + "     ?wcb2 opmo:cause ?agent2 ."
            + "     ?wcb2 opmo:effect ?y  ."
            + "     ?y rdf:type opmv:Process ."
            + "     ?used opmo:cause ?in ."
            + "     ?used opmo:effect ?y ."
//            + "     ?in (opmo:avalue/opmo:content) ?cIn ."
            + "     ?used opmo:role ?role ."
            + "     ?role rdfs:label ?techRole ."
            
            + "     ?agent2 ws:has-input ?inPort ."
            + "     ?inPort iec:refers-to ?ref3 ."
            + "     ?ref3 rdf:type <http://vip.cosinus.anr.fr/vip-model.owl#geometrical-phantom-object-model> ."
            + "     ?inPort rdfs:comment ?techRole"
            
            + "}";
        
        
        RemoteProducer rp1 = RemoteProducerServiceClient.getPort("http://" + args[0] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        rp1.loadRDF("/home/agaignard/data/VIP-dist-prov-dataset/sorteo-prov-1.rdf");
        RemoteProducer rp2 = RemoteProducerServiceClient.getPort("http://" + args[1] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        rp2.loadRDF("/home/agaignard/data/VIP-dist-prov-dataset/sorteo-prov-2.rdf");
        RemoteProducer rp3 = RemoteProducerServiceClient.getPort("http://" + args[2] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        rp3.loadRDF("/home/agaignard/data/VIP-dist-prov-dataset/LMF2RAWSINO-test.rdf");
        rp3.loadRDF("/home/agaignard/data/VIP-dist-prov-dataset/compileProtocole-test.rdf");

        ///////////////////////
        Graph graphDist = Graph.create();
        QueryProcessDQP execDqp = QueryProcessDQP.create(graphDist);
        execDqp.addRemote(new URL("http://" + args[0] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        execDqp.addRemote(new URL("http://" + args[1] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        execDqp.addRemote(new URL("http://" + args[2] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

        StopWatch sw1 = new StopWatch();
        sw1.start();
        Mappings res1 = execDqp.query(queryRuleFantom);
        System.out.println("----Distributed inference----");
        System.out.println("Results  in " + sw1.getTime() + " ms");
        System.out.println(RDFFormat.create(res1).toString());
        
        
        ///////////////////////
        Graph graphCentralized = Graph.create();
        QueryProcess exec = QueryProcess.create(graphCentralized);

        Load.create(graphCentralized).load("/home/agaignard/data/VIP-dist-prov-dataset/sorteo-prov-1.rdf");
        Load.create(graphCentralized).load("/home/agaignard/data/VIP-dist-prov-dataset/sorteo-prov-2.rdf");
        Load.create(graphCentralized).load("/home/agaignard/data/VIP-dist-prov-dataset/compileProtocole-test.rdf");
        Load.create(graphCentralized).load("/home/agaignard/data/VIP-dist-prov-dataset/LMF2RAWSINO-test.rdf");
        StopWatch sw = new StopWatch();
        sw.start();
        Mappings maps = exec.query(queryRuleFantom);
        sw.stop();
        System.out.println("----Centralized inference----");
        System.out.println("Results in " + sw.getTime() + " ms");
        System.out.println(RDFFormat.create(maps).toString());
        sw.reset();
    }
}
