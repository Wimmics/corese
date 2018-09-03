/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.test.w3c;

import static fr.inria.corese.test.w3c.TestW3C11KGraphNew.before;
import static fr.inria.corese.test.w3c.TestW3C11KGraphNew.after;
import static fr.inria.corese.test.w3c.TestW3C11KGraphNew.after2;
import static fr.inria.corese.test.w3c.TestW3C11KGraphNew.before2;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
       //before2(); 
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        //after2();
    }
    
    @Test
    public  void mytest() {
        new TestW3C11KGraphNew().process(0);
    }
}
