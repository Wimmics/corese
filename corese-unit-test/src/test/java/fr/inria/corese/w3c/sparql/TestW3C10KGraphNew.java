/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.w3c.sparql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;


/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TestW3C10KGraphNew {

    public static void main(String[] args) {
        new TestW3C11KGraphNew().process(0);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // before2();

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // after2();
    }

    // Number of known failures: 7
    @Test
    public void mytest() {
        int nb_errors = new TestW3C11KGraphNew().process(0);
        Assert.assertEquals(7, nb_errors);
    }
}
