/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.db_memory;

import fr.inria.corese.w3c.validator.W3CMappingsValidator;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.SPARQLResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author edemairy
 */
public class DbMemoryTest {

    @DataProvider(name = "getResults")
    public static Object[][] getResults() {
        int nbResults = 1;
        String dbInputTemplate = "/Users/edemairy/Developpement/Corese-master/persistency-tools/coreseTimer-common/MEMORY/result_input_0_query_%d.xml";
        String memoryInputTemplate = "/Users/edemairy/Developpement/Corese-master/persistency-tools/coreseTimer-common/MEMORY/result_input_0_query_%d.xml";
        Object[][] result = new Object[nbResults][];
        for (int i = 0; i < nbResults; i++) {
            String s1 = String.format(dbInputTemplate, i);
            String s2 = String.format(memoryInputTemplate, i);
            Object[] val = {s1, s2};
            result[i] = val;
        }
        return result;
    }


    @Test(dataProvider = "getResults", enabled = false)
    public static void checkEqual(String inputMemory, String inputDb) {
        System.out.println("inputMemory = " + inputMemory);
        System.out.println("inputDb = " + inputDb);
        Graph g_db = new Graph();
        SPARQLResult result_db = SPARQLResult.create(g_db);
        Mappings map_db = null;
        try {
            map_db = result_db.parse(inputDb);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(DbMemoryTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        Graph g_memory = new Graph();
        SPARQLResult result_memory = SPARQLResult.create(g_memory);
        Mappings map_memory = null;
        try {
            map_memory = result_memory.parse(inputMemory);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(DbMemoryTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        W3CMappingsValidator tester = new W3CMappingsValidator();
        boolean result = tester.validate(map_db, map_memory);
        System.out.println("result = " + result);
        assert (result);
    }
}
