/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql;

import fr.inria.corese.sparql.exceptions.QueryLexicalException;
import fr.inria.corese.sparql.exceptions.QuerySyntaxException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.ParserSparql1;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gaignard
 */
public class ParsingTest {

    public ParsingTest() {
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
    //@Ignore
    public void hello1() throws QueryLexicalException, QuerySyntaxException {
        String query = "insert data {\n"
                + "<http://dbpedia.org/resource/Alban_Bagbin> <http://xmlns.com/foaf/0.1/name> \"Alban Kingsford Sumana Bagbin\"\n"
                + "<http://dbpedia.org/resource/Alban_Berg> <http://xmlns.com/foaf/0.1/name> \"Alban Berg\"\n"
                + "<http://dbpedia.org/resource/Alban_Ceray> <http://xmlns.com/foaf/0.1/name> \"Alban Ceray\"\n"
                + "<http://dbpedia.org/resource/Alban_Maginness> <http://xmlns.com/foaf/0.1/name> \"Alban Maginness\"\n"
                + "}";

        ASTQuery ast = ASTQuery.create(query);
       // ast.setKgram(true);
        ParserSparql1.create(ast).parse();
        System.out.println(ast);
    }
}
