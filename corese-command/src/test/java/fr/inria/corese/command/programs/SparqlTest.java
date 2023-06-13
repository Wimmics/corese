package fr.inria.corese.command.programs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import picocli.CommandLine;

public class SparqlTest {

        private Sparql sparql = new Sparql();
        private CommandLine cmd = new CommandLine(sparql);

        private StringWriter out = new StringWriter();
        private StringWriter err = new StringWriter();

        private String inputPath = SparqlTest.class.getResource("/fr/inria/corese/command/programs/sparql/input/")
                        .getPath();
        private String referencesPath = SparqlTest.class
                        .getResource("/fr/inria/corese/command/programs/sparql/references/")
                        .getPath();
        private String resultsPath = SparqlTest.class.getResource("/fr/inria/corese/command/programs/sparql/results")
                        .getPath();
        private String queriesPath = SparqlTest.class.getResource("/fr/inria/corese/command/programs/sparql/queries/")
                        .getPath();

        @Before
        public void setUp() throws Exception {
                PrintWriter out = new PrintWriter(this.out);
                PrintWriter err = new PrintWriter(this.err);
                cmd.setOut(out);
                cmd.setErr(err);
        }

        private boolean compareFiles(String filePath1, String filePath2) throws IOException {
                // Créer deux sets pour stocker les lignes de chaque fichier
                Set<String> file1Lines = new HashSet<>();
                Set<String> file2Lines = new HashSet<>();

                // Lire le premier fichier et stocker chaque ligne dans le set
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath1))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                                file1Lines.add(line);
                        }
                }

                // Lire le deuxième fichier et stocker chaque ligne dans le set
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath2))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                                file2Lines.add(line);
                        }
                }

                // Vérifier que les deux sets sont identiques
                return file1Lines.equals(file2Lines);
        }

        @Test
        public void testSelectRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectRdfxml = Paths
                                .get(referencesPath, "select", "beatles-select-rdfxml.rdf")
                                .toString();
                String pathResBeatlesSelectRdfxml = Paths.get(resultsPath, "select", "beatles-select-rdfxml.rdf")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesSelectRdfxml,
                                pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectRdfxml, pathResBeatlesSelectRdfxml));
        }

        @Test
        public void testSelectTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTurtle = Paths
                                .get(referencesPath, "select", "beatles-select-turtle.ttl")
                                .toString();
                String pathResBeatlesSelectTurtle = Paths.get(resultsPath, "select", "beatles-select-turtle.ttl")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o",
                                pathResBeatlesSelectTurtle, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectTurtle, pathResBeatlesSelectTurtle));
        }

        @Test
        public void testSelectTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTrig = Paths.get(referencesPath, "select", "beatles-select-trig.trig")
                                .toString();
                String pathResBeatlesSelectTrig = Paths.get(resultsPath, "select", "beatles-select-trig.trig")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesSelectTrig,
                                pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectTrig, pathResBeatlesSelectTrig));
        }

        @Test
        public void testSelectJsonld() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectJsonld = Paths
                                .get(referencesPath, "select", "beatles-select-jsonld.jsonld")
                                .toString();
                String pathResBeatlesSelectJsonld = Paths.get(resultsPath, "select", "beatles-select-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o",
                                pathResBeatlesSelectJsonld, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectJsonld, pathResBeatlesSelectJsonld));
        }

        @Test
        public void testSelectBidingXml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectXml = Paths
                                .get(referencesPath, "select", "beatles-select-bidingxml.xml")
                                .toString();
                String pathResBeatlesSelectXml = Paths.get(resultsPath, "select", "beatles-select-xml.xml")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o",
                                pathResBeatlesSelectXml, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectXml, pathResBeatlesSelectXml));
        }

        @Test
        public void testSelectBidingTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTurtle = Paths
                                .get(referencesPath, "select", "beatles-select-bidingturtle.ttl")
                                .toString();
                String pathResBeatlesSelectTurtle = Paths
                                .get(resultsPath, "select", "beatles-select-turtle.ttl")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o",
                                pathResBeatlesSelectTurtle, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectTurtle, pathResBeatlesSelectTurtle));
        }

        @Test
        public void testSelectBidingTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTrig = Paths
                                .get(referencesPath, "select", "beatles-select-bidingtrig.trig")
                                .toString();
                String pathResBeatlesSelectTrig = Paths.get(resultsPath, "select", "beatles-select-trig.trig")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o",
                                pathResBeatlesSelectTrig, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectTrig, pathResBeatlesSelectTrig));
        }

        @Test
        public void testSelectBidingJson() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectJson = Paths
                                .get(referencesPath, "select", "beatles-select-bidingjson.json")
                                .toString();
                String pathResBeatlesSelectJson = Paths
                                .get(resultsPath, "select", "beatles-select-json.json")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o",
                                pathResBeatlesSelectJson, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectJson, pathResBeatlesSelectJson));
        }

        @Test
        public void testSelectBidingCsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectCsv = Paths
                                .get(referencesPath, "select", "beatles-select-bidingcsv.csv")
                                .toString();
                String pathResBeatlesSelectCsv = Paths.get(resultsPath, "select", "beatles-select-csv.csv")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o",
                                pathResBeatlesSelectCsv, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectCsv, pathResBeatlesSelectCsv));
        }

        @Test
        public void testSelectBidingTSV() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTsv = Paths
                                .get(referencesPath, "select", "beatles-select-bidingtsv.tsv")
                                .toString();
                String pathResBeatlesSelectTsv = Paths.get(resultsPath, "select", "beatles-select-tsv.tsv")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o",
                                pathResBeatlesSelectTsv, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectTsv, pathResBeatlesSelectTsv));
        }

        @Test
        public void testAskTrueRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskRdfxml = Paths.get(referencesPath, "ask", "beatles-ask-rdfxml-true.rdf")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-rdfxml-true.rdf")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskRdfxml, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskRdfxml = Paths.get(referencesPath, "ask", "beatles-ask-rdfxml-false.rdf")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-rdfxml-false.rdf")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskRdfxml, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths.get(referencesPath, "ask", "beatles-ask-turtle-true.ttl")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-turtle-true.ttl")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths.get(referencesPath, "ask", "beatles-ask-turtle-false.ttl")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-turtle-false.ttl")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrigTrue() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesTrig = Paths.get(referencesPath, "ask", "beatles-ask-trig-true.trig").toString();
                String pathResBeatlesTrig = Paths.get(resultsPath, "ask", "beatles-ask-trig-true.trig").toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesTrig,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesTrig, pathResBeatlesTrig));
        }

        @Test
        public void testAskTrigFalse() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesTrig = Paths.get(referencesPath, "ask", "beatles-ask-trig-false.trig").toString();
                String pathResBeatlesTrig = Paths.get(resultsPath, "ask", "beatles-ask-trig-false.trig").toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesTrig,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesTrig, pathResBeatlesTrig));
        }

        @Test
        public void testAskTrueJsonld() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJsonld = Paths.get(referencesPath, "ask", "beatles-ask-jsonld-true.jsonld")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-jsonld-true.jsonld")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskJsonld, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseJsonld() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJsonld = Paths.get(referencesPath, "ask", "beatles-ask-jsonld-false.jsonld")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-jsonld-false.jsonld")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskJsonld, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingXml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskXml = Paths.get(referencesPath, "ask", "beatles-ask-bidingxml-true.xml")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-bidingxml-true.xml")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskXml, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingXml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskXml = Paths.get(referencesPath, "ask", "beatles-ask-bidingxml-false.xml")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-bidingxml-false.xml")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskXml, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths
                                .get(referencesPath, "ask", "beatles-ask-bidingturtle-true.ttl")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-bidingturtle-true.ttl")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths
                                .get(referencesPath, "ask", "beatles-ask-bidingturtle-false.ttl")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-bidingturtle-false.ttl")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths
                                .get(referencesPath, "ask", "beatles-ask-bidingtrig-true.ttl")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-bidingtrig-true.ttl")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths
                                .get(referencesPath, "ask", "beatles-ask-bidingtrig-false.ttl")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-bidingtrig-false.ttl")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingJson() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJSON = Paths.get(referencesPath, "ask", "beatles-ask-bidingjson-true.json")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-bidingjson-true.json")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingJson() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJSON = Paths.get(referencesPath, "ask", "beatles-ask-bidingjson-false.json")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-bidingjson-false.json")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingCsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskCSV = Paths.get(referencesPath, "ask", "beatles-ask-bidingcsv-true.csv")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-bidingcsv-true.csv")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingCsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskCSV = Paths.get(referencesPath, "ask", "beatles-ask-bidingcsv-false.csv")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-bidingcsv-false.csv")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingTsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTSV = Paths.get(referencesPath, "ask", "beatles-ask-bidingtsv-true.tsv")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-bidingtsv-true.tsv")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingTsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTSV = Paths.get(referencesPath, "ask", "beatles-ask-bidingtsv-false.tsv")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-bidingtsv-false.tsv")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskFalse));
        }

        @Test
        public void testInsertRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsert = Paths.get(referencesPath, "insert", "beatles-insert-rdfxml.xml")
                                .toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsert = Paths.get(referencesPath, "insert", "beatles-insert-turtle.ttl")
                                .toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-turtle.ttl")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsert = Paths.get(referencesPath, "insert", "beatles-insert-trig.trig")
                                .toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-trig.trig")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertJsonld() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsert = Paths.get(referencesPath, "insert", "beatles-insert-jsonld.jsonld")
                                .toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertBidingXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-bidingrdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: BIDING_XML is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertBidingTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-bidingturtle.ttl")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o",
                                pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: BIDING_TURTLE is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertBidingTrigInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-bidingtrig.trig")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: BIDING_TRIG is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertBidingJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-bidingjsonld.jsonld")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o",
                                pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: BIDING_JSON is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertBidingCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-bidingrdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o",
                                pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: BIDING_CSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertBidingTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-bidingrdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o",
                                pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: BIDING_TSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = Paths
                                .get(referencesPath, "insert-where", "beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = Paths
                                .get(referencesPath, "insert-where", "beatles-insertwhere-bidingturtle.ttl").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-bidingturtle.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = Paths
                                .get(referencesPath, "insert-where", "beatles-insertwhere-bidingtrig.trig").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-bidingtrig.trig").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereJsonLd() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = Paths
                                .get(referencesPath, "insert-where", "beatles-insertwhere-bidingjsonld.jsonld")
                                .toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-bidingjsonld.jsonld").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereBidingxmlInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o",
                                pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: BIDING_XML is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereBidingturtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o",
                                pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: BIDING_TURTLE is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereBidingTrigInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o",
                                pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: BIDING_TRIG is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereBidingjsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o",
                                pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: BIDING_JSON is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereBidingCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o",
                                "references/insert-where/beatles-insertwhere-bidingcsv.csv",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: BIDING_CSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereBidingTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o",
                                "references/insert-where/beatles-insertwhere-bidingtsv.tsv",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: BIDING_TSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths.get(referencesPath, "delete", "beatles-delete-rdfxml.xml")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete", "beatles-delete-rdfxml.xml").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths.get(referencesPath, "delete", "beatles-delete-turtle.ttl")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete", "beatles-delete-turtle.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths.get(referencesPath, "delete", "beatles-delete-trig.trig")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete", "beatles-delete-trig.trig").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteJsonld() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths.get(referencesPath, "delete", "beatles-delete-jsonld.jsonld")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete", "beatles-delete-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteBidingXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o",
                                "references/delete/beatles-delete-xml.xml",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_XML is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteBidingTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o",
                                "references/delete/beatles-delete-turtle.ttl",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_TURTLE is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteBidingTrigInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o",
                                "references/delete/beatles-delete-trig.trig",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_TRIG is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteBidingJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o",
                                "references/delete/beatles-delete-jsonld.jsonld",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_JSON is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteBidingCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o",
                                "references/delete/beatles-delete-csv.csv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_CSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteBidingTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o",
                                "references/delete/beatles-delete-tsv.tsv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_TSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths
                                .get(referencesPath, "delete-where", "beatles-delete-where-rdfxml.xml")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete-where", "beatles-delete-where-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths
                                .get(referencesPath, "delete-where", "beatles-delete-where-turtle.ttl")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete-where", "beatles-delete-where-turtle.ttl")
                                .toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths
                                .get(referencesPath, "delete-where", "beatles-delete-where-trig.trig")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete-where", "beatles-delete-where-trig.trig")
                                .toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereJsonld() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths
                                .get(referencesPath, "delete-where", "beatles-delete-where-jsonld.jsonld")
                                .toString();
                String pathResBeatlesDelete = Paths
                                .get(resultsPath, "delete-where", "beatles-delete-where-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereBidingXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o",
                                "references/delete-where/beatles-delete-where-xml.xml",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_XML is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereBidingTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o",
                                "references/delete-where/beatles-delete-where-turtle.ttl",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_TURTLE is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereBidingTrigInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o",
                                "references/delete-where/beatles-delete-where-trig.trig",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_TRIG is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereBidingJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o",
                                "references/delete-where/beatles-delete-where-json.json",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_JSON is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereBidingCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o",
                                "references/delete-where/beatles-delete-where-csv.csv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_CSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereBidingTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o",
                                "references/delete-where/beatles-delete-where-tsv.tsv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: BIDING_TSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testConstructRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesConstruct = Paths.get(referencesPath, "construct", "beatles-construct-rdfxml.xml")
                                .toString();
                String pathResBeatlesConstruct = Paths.get(resultsPath, "construct", "beatles-construct-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesConstruct,
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testConstructTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesConstruct = Paths.get(referencesPath, "construct", "beatles-construct-turtle.ttl")
                                .toString();
                String pathResBeatlesConstruct = Paths.get(resultsPath, "construct", "beatles-construct-turtle.ttl")
                                .toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesConstruct,
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testConstructTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesConstruct = Paths.get(referencesPath, "construct", "beatles-construct-trig.trig")
                                .toString();
                String pathResBeatlesConstruct = Paths.get(resultsPath, "construct", "beatles-construct-trig.trig")
                                .toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesConstruct,
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testConstructJsonld() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesConstruct = Paths
                                .get(referencesPath, "construct", "beatles-construct-jsonld.jsonld")
                                .toString();
                String pathResBeatlesConstruct = Paths.get(resultsPath, "construct", "beatles-construct-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesConstruct,
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testBidingConstructXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o",
                                "references/construct/beatles-construct-xml.xml", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: BIDING_XML is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingConstructTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o",
                                "references/construct/beatles-construct-turtle.ttl", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: BIDING_TURTLE is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingConstructTrigInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o",
                                "references/construct/beatles-construct-trig.trig", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: BIDING_TRIG is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingConstructJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o",
                                "references/construct/beatles-construct-jsonld.jsonld", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: BIDING_JSON is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingConstructCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o",
                                "references/construct/beatles-construct-csv.csv", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: BIDING_CSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingConstructTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o",
                                "references/construct/beatles-construct-tsv.tsv", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: BIDING_TSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDescribeRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDescribe = Paths.get(referencesPath, "describe", "beatles-describe-rdfxml.xml")
                                .toString();
                String pathResBeatlesDescribe = Paths.get(resultsPath, "describe", "beatles-describe-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesDescribe,
                                pathQueryBeatlesDescribe);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
        }

        @Test
        public void testDescribeTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDescribe = Paths.get(referencesPath, "describe", "beatles-describe-turtle.ttl")
                                .toString();
                String pathResBeatlesDescribe = Paths.get(resultsPath, "describe", "beatles-describe-turtle.ttl")
                                .toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesDescribe,
                                pathQueryBeatlesDescribe);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
        }

        @Test
        public void testDescribeTrigl() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDescribe = Paths.get(referencesPath, "describe", "beatles-describe-trig.trig")
                                .toString();
                String pathResBeatlesDescribe = Paths.get(resultsPath, "describe", "beatles-describe-trig.trig")
                                .toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesDescribe,
                                pathQueryBeatlesDescribe);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
        }

        @Test
        public void testDescribeJsonld() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDescribe = Paths.get(referencesPath, "describe", "beatles-describe-jsonld.jsonld")
                                .toString();
                String pathResBeatlesDescribe = Paths.get(resultsPath, "describe", "beatles-describe-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesDescribe,
                                pathQueryBeatlesDescribe);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
        }

        @Test
        public void testBidingsDescribeXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_XML", "-o",
                                "references/describe/beatles-describe-xml.xml", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: BIDING_XML is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingsDescribeTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TURTLE", "-o",
                                "references/describe/beatles-describe-turtle.ttl", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: BIDING_TURTLE is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingsDescribeTrigInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TRIG", "-o",
                                "references/describe/beatles-describe-trig.trig", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: BIDING_TRIG is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingsDescribeJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_JSON", "-o",
                                "references/describe/beatles-describe-json.json", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: BIDING_JSON is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingsDescribeCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_CSV", "-o",
                                "references/describe/beatles-describe-csv.csv", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: BIDING_CSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testBidingsDescribeTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDescribe = Paths.get(queriesPath, "describe", "describeBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "BIDING_TSV", "-o",
                                "references/describe/beatles-describe-tsv.tsv", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: BIDING_TSV is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testExecute_WhenInputFileDoesNotExist_ThrowsException() {
                String nonExistentFile = "non_existent_file.rq";
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", nonExistentFile, "-f", "TURTLE", "-o", "output.ttl",
                                pathQueryBeatlesConstruct);

                assertEquals(1, exitCode);
                assertTrue(err.toString().contains("Error while loading"));
        }

        @Test
        public void testExecute_WhenQueryFileDoesNotExist_ThrowsException() {
                String nonExistentQueryFile = "non_existent_query_file.rq";
                String validInputFile = Paths.get(inputPath, "beatles.ttl").toString();

                int exitCode = cmd.execute("-i", validInputFile, "-f", "TURTLE", "-r", "TURTLE", "-o",
                                "output.ttl", nonExistentQueryFile);

                assertEquals(1, exitCode);
                assertTrue(err.toString().contains("Error while loading"));
        }

        @Test
        public void testExecute_WhenInvalidQuery_ThrowsException() {
                String validInputFile = Paths.get(inputPath, "beatles.ttl").toString();

                int exitCode = cmd.execute("-i", validInputFile, "-f", "TURTLE", "-r", "TURTLE", "-o",
                                "output.ttl", "SERRORELECT * WHERE { ?s ?p ?o }");

                assertEquals(1, exitCode);
                assertTrue(err.toString().contains("Error when executing SPARQL query"));
        }

}