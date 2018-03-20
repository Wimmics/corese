/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.bgp;


import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.kgram.core.Mappings;
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
    

     
    public static void main(String[]  args) throws EngineException, LoadException {
        //Test default KGRAM
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadWE(TestBGP.class.getClassLoader().getResource("demographie").getPath()+"/cog-2012.ttl");
        ld.loadWE(TestBGP.class.getClassLoader().getResource("demographie").getPath()+"/popleg-2010.ttl");
        QueryProcess exec = QueryProcess.create(g);
        long start = System.currentTimeMillis();
        Mappings m = exec.query(query);
        System.out.println("resutls: "+m.size());
        System.out.println("== Querying time:" + (System.currentTimeMillis() - start) + "ms ==\n\n");
    }
    
}
