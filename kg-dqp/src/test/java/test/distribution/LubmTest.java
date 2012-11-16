/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import edu.lehigh.swat.bench.uba.Generator;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author gaignard
 */
public class LubmTest {
    
    public LubmTest() {
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
         String[] args = {"-univ","1","-onto", "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl"};
         Generator.main(args);
     }
}
