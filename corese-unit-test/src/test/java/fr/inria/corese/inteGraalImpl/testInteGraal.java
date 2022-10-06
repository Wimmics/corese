package fr.inria.corese.inteGraalImpl;

import org.junit.Test;

import fr.boreal.model.kb.api.FactBase;
import fr.boreal.storage.builder.StorageBuilder;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.storage.inteGraal.InteGraalDataManager;

public class testInteGraal {

    @Test
    public void démoCoraal() throws LoadException, EngineException {

        ///////////////////////
        // Setup DataManager //
        ///////////////////////
        FactBase factBase = StorageBuilder.defaultBuilder().useHSQLDB("Music").build().get();
        DataManager dataManager = new InteGraalDataManager(factBase);

        ///////////////
        // Load data //
        ///////////////
        Load dataLoader = Load.create();
        dataLoader.setDataManager(dataManager);
        dataLoader.parse(
                "/user/rceres/home/Documents/Corese/corese-learning/Presentations/demos/Cycle_1/utils/rdf-files/music.ttl");

        /////////////////
        // Query graph //
        /////////////////

        // Nombre de musique de chaque artiste.
        String queryString = ""
                + "prefix music: <http://example.com/music/> "
                + "SELECT ?name (count(?song) AS ?nb_song) "
                + "WHERE { "
                + "     ?artist a music:SoloArtist . "
                + "     ?artist music:name ?name . "
                + "     ?song music:artist ?artist . "
                + "} "
                + "GROUP BY ?artist "
                + "ORDER BY DESC(?nb_song) ";

        // String queryString = "SELECT * WHERE {?s ?p ?o}";

        QueryProcess exec = QueryProcess.create(dataManager);
        Mappings map = exec.query(queryString);

        ////////////////////
        // Prinbt results //
        ////////////////////
        for (Mapping m : map) {
            String result = "";
            result += "?name = " + m.getValue("?name");
            result += ", ";
            result += "?nb_song = " + m.getValue("?nb_song");
            System.out.println(result);
        }
    }

}
