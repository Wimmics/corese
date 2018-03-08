/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.distribution;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgdqp.core.CentralizedInferrencingNoSpin;
import fr.inria.corese.kgtool.load.LoadException;
import java.io.IOException;
import org.apache.logging.log4j.Level;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class RulesProvTest {

    public RulesProvTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    @Ignore
    public void hello() {
        try {
            String commandLine = "-l /Users/gaignard/Desktop/out/ -r /Users/gaignard/Desktop/VIP-rules/";
            CentralizedInferrencingNoSpin.main(commandLine.split(" "));
        } catch (ParseException ex) {
            LogManager.getLogger(RulesProvTest.class.getName()).log(Level.ERROR, "", ex);
        } catch (EngineException ex) {
            LogManager.getLogger(RulesProvTest.class.getName()).log(Level.ERROR, "", ex);
        } catch (InterruptedException ex) {
            LogManager.getLogger(RulesProvTest.class.getName()).log(Level.ERROR, "", ex);
        } catch (IOException ex) {
            LogManager.getLogger(RulesProvTest.class.getName()).log(Level.ERROR, "", ex);
        } catch (LoadException ex) {
            LogManager.getLogger(RulesProvTest.class.getName()).log(Level.ERROR, "", ex);
        }
    }
}
