/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

import fr.inria.corese.w3c.validator.W3CMappingsValidator;
import fr.inria.edelweiss.kgram.core.Mappings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author edemairy
 */
public class Main {

    public final static String[] queries = { // @TODO afficher pour chaque requête le nombre de résultats.
            // @TODO jointure
            // @ select distinct ?p  where {?e ?p ?y}  [order by ?p]
            // @TODO tester les littéraux
            //		"select (count(*) as ?count) where { graph ?g {?x ?p ?y}}",
            //		"select * where {<http://prefix.cc/popular/all.file.vann>  ?p ?y .}",// limit 10000",
            //		"select * where { ?x  a ?y }", // biaisé car beaucoup de données sont typées
            //		"select (count(*) as ?c) where {?x a ?y}", // permet de supprimer le coût de construction du résultat.
            //				"select * where { <http://prefix.cc/popular/all.file.vann>  ?p ?y . ?y ?q <http://prefix.cc/popular/all.file.vann> .} limit 10000"
            //				"select * where { ?x ?p ?y . ?y ?q ?x }" // Intractable: if there are 10^6 edges, requests for 10^12 edges. @TODO Traiter la jointure.
            //		"select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c"
            // Campagne de tests
            //
            //
            // Famille de tests 1
            // BGP: on connait une valeur, la bd sait rechercher cette valeur instantanément ? Efficacité
    /*		s ?p ?o
	               ?s ?p o 2 cas principaux :URI (1cas), Literal (String, int, double,
		humans : avec des requêtes
		  X ?p ?y . ?z ?q ?y
		  X ?p ?y . ?y ?q ?z
		 URI ?p ?y .
		 ?x ?p URI. ?p ?z ?t
		 ?x p ?x

		 tester d'abord les propriétés fixées
		 tester ensuite les propriétés libres

	Famille de tests 2
		?x ?p ?y . filter(contains(?y, " ")) . ?y ?q ?z  (même chose que précédemment, mais avec des filtres)
	Famille de tests 3
		Cycles de longueur 2, 3, etc.

	Famille de tests 4
		tester les 16 cas.

	1. Sémantique
	2. Benchmark
	 */};
    private static Logger logger = LogManager.getLogger(Main.class.getName());

    public static boolean compareResults(Mappings map_db, Mappings map_memory) {
        W3CMappingsValidator tester = new W3CMappingsValidator();
        return tester.validate(map_db, map_memory) && tester.validate(map_memory, map_db) && map_memory.size() == map_db.size();
    }


}
