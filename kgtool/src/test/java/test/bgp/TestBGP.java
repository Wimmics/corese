/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.bgp;


import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
/**
 *
 * @author macina
 */
public class TestBGP {
     private static final String query = ""+
             "PREFIX idemo:<http://rdf.insee.fr/def/demo#> \n" +
             "PREFIX igeo:<http://rdf.insee.fr/def/geo#> \n" +
             "SELECT * WHERE { \n" +
             "    ?region igeo:codeRegion \"24\" .\n" +
             "    ?region igeo:subdivisionDirecte ?departement .\n" +
             "    ?departement igeo:nom ?nom .\n"  +
             "    ?departement idemo:population ?popLeg .\n" +
             "    ?popLeg idemo:populationTotale ?popTotale .\n " +
             "}"; // ORDER BY ?popTotale";
    

     
    public static void main(String[]  args) throws EngineException {
        //Test default KGRAM
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.load(TestBGP.class.getClassLoader().getResource("demographie").getPath()+"/cog-2012.ttl");
        ld.load(TestBGP.class.getClassLoader().getResource("demographie").getPath()+"/popleg-2010.ttl");
        QueryProcess exec = QueryProcess.create(g);
        long start = System.currentTimeMillis();
        Mappings m = exec.query(query);
        System.out.println("resutls: "+m.size());
        System.out.println("== Querying time:" + (System.currentTimeMillis() - start) + "ms ==\n\n");
    }
    
}
