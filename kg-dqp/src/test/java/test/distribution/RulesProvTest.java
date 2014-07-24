/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.CentralizedInferrencingNoSpin;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.ParseException;
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
            Logger.getLogger(RulesProvTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(RulesProvTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(RulesProvTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RulesProvTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(RulesProvTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
