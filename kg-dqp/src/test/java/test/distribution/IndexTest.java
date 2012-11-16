/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import fr.inria.edelweiss.kgdqp.index.IndexPropriete;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class IndexTest {

    public IndexTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
    public void hello() {
        IndexPropriete source3 = new IndexPropriete("/Users/gaignard/Desktop/LUBM-10-1.3M");
        source3.hashPropriete();
        System.out.println("");
    }
}
