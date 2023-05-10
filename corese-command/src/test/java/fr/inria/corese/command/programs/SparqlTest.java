package fr.inria.corese.command.programs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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

        private String readFileAsString(String path) {
                Path filePath = Paths.get(path);
                String data = "";
                try {
                        data = new String(Files.readAllBytes(filePath));
                } catch (IOException e) {
                        System.err.println("Error: " + e.getMessage());
                        e.printStackTrace();
                }
                return data;
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
        public void testSelectRDFXML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectRDFXMl = Paths.get(referencesPath, "select", "beatles-select-rdfxml.xml")
                                .toString();
                String pathResBeatlesSelectRDFXMl = Paths.get(resultsPath, "select", "beatles-select-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o",
                                pathResBeatlesSelectRDFXMl, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectRDFXMl, pathResBeatlesSelectRDFXMl));
        }

        @Test
        public void testSelectTURTLE() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTURTLE = Paths.get(referencesPath, "select", "beatles-select-turtle.ttl")
                                .toString();
                String pathResBeatlesSelectTURTLE = Paths.get(resultsPath, "select", "beatles-select-turtle.ttl")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o",
                                pathResBeatlesSelectTURTLE, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectTURTLE, pathResBeatlesSelectTURTLE));
        }

        @Test
        public void testSelectTRIG() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTRIG = Paths.get(referencesPath, "select", "beatles-select-trig.trig")
                                .toString();
                String pathResBeatlesSelectTRIG = Paths.get(resultsPath, "select", "beatles-select-trig.trig")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o",
                                pathResBeatlesSelectTRIG, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectTRIG, pathResBeatlesSelectTRIG));
        }

        @Test
        public void testSelectJSONLD() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectJSONLD = Paths.get(referencesPath, "select", "beatles-select-jsonld.jsonld")
                                .toString();
                String pathResBeatlesSelectJSONLD = Paths.get(resultsPath, "select", "beatles-select-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o",
                                pathResBeatlesSelectJSONLD, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectJSONLD, pathResBeatlesSelectJSONLD));
        }

        @Test
        public void testSelectRESULT_XML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectRESULT_XML = Paths
                                .get(referencesPath, "select", "beatles-select-resultxml.xml")
                                .toString();
                String pathResBeatlesSelectRESULT_XML = Paths.get(resultsPath, "select", "beatles-select-resultxml.xml")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_XML", "-o",
                                pathResBeatlesSelectRESULT_XML, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectRESULT_XML, pathResBeatlesSelectRESULT_XML));
        }

        @Test
        public void testSelectRESULT_TURTLE() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectRESULT_TURTLE = Paths
                                .get(referencesPath, "select", "beatles-select-resultturtle.ttl")
                                .toString();
                String pathResBeatlesSelectRESULT_TURTLE = Paths
                                .get(resultsPath, "select", "beatles-select-resultturtle.ttl")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TURTLE", "-o",
                                pathResBeatlesSelectRESULT_TURTLE, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectRESULT_TURTLE, pathResBeatlesSelectRESULT_TURTLE));
        }

        @Test
        public void testSelectRESULT_JSON() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectRESULT_JSON = Paths
                                .get(referencesPath, "select", "beatles-select-resultjson.json")
                                .toString();
                String pathResBeatlesSelectRESULT_JSON = Paths
                                .get(resultsPath, "select", "beatles-select-resultjson.json")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_JSON", "-o",
                                pathResBeatlesSelectRESULT_JSON, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectRESULT_JSON, pathResBeatlesSelectRESULT_JSON));
        }

        @Test
        public void testSelectRESULT_CSV() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectRESULT_CSV = Paths
                                .get(referencesPath, "select", "beatles-select-resultcsv.csv")
                                .toString();
                String pathResBeatlesSelectRESULT_CSV = Paths.get(resultsPath, "select", "beatles-select-resultcsv.csv")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_CSV", "-o",
                                pathResBeatlesSelectRESULT_CSV, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectRESULT_CSV, pathResBeatlesSelectRESULT_CSV));
        }

        @Test
        public void testSelectRESULT_TSV() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectRESULT_TSV = Paths
                                .get(referencesPath, "select", "beatles-select-resulttsv.tsv")
                                .toString();
                String pathResBeatlesSelectRESULT_TSV = Paths.get(resultsPath, "select", "beatles-select-resulttsv.tsv")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TSV", "-o",
                                pathResBeatlesSelectRESULT_TSV, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectRESULT_TSV, pathResBeatlesSelectRESULT_TSV));
        }

        @Test
        public void testAskTrueRDFXML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskRDFXML = Paths.get(referencesPath, "ask", "beatles-ask-rdfxml-true.xml")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-rdfxml-true.xml").toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskRDFXML, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseRDFXML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskRDFXML = Paths.get(referencesPath, "ask", "beatles-ask-rdfxml-false.xml")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-rdfxml-false.xml").toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskRDFXML, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskTrueTURTLE() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTURTLE = Paths.get(referencesPath, "ask", "beatles-ask-turtle-true.ttl")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-turtle-true.ttl").toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTURTLE, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseTURTLE() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTURTLE = Paths.get(referencesPath, "ask", "beatles-ask-turtle-false.ttl")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-turtle-false.ttl")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTURTLE, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueTRIG() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTRIG = Paths.get(referencesPath, "ask", "beatles-ask-trig-true.trig")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-trig-true.trig").toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTRIG, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseTRIG() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTRIG = Paths.get(referencesPath, "ask", "beatles-ask-trig-false.trig")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-trig-false.trig").toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTRIG, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueJSONLD() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJSONLD = Paths.get(referencesPath, "ask", "beatles-ask-jsonld-true.jsonld")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-jsonld-true.jsonld")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskJSONLD, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseJSONLD() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJSONLD = Paths.get(referencesPath, "ask", "beatles-ask-jsonld-false.jsonld")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-jsonld-false.jsonld")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskJSONLD, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskTrueRESULT_XML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskRESULT_XML = Paths.get(referencesPath, "ask", "beatles-ask-resultxml-true.xml")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-resultxml-true.xml")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_XML", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskRESULT_XML, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseRESULT_XML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskRESULT_XML = Paths.get(referencesPath, "ask", "beatles-ask-resultxml-false.xml")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-resultxml-false.xml")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_XML", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskRESULT_XML, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueRESULT_TURTLE() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskRESULT_TURTLE = Paths
                                .get(referencesPath, "ask", "beatles-ask-resultturtle-true.ttl")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-resultturtle-true.ttl")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TURTLE", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskRESULT_TURTLE, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseRESULT_TURTLE() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskRESULT_TURTLE = Paths
                                .get(referencesPath, "ask", "beatles-ask-resultturtle-false.ttl")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-resultturtle-false.ttl")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TURTLE", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskRESULT_TURTLE, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueRESULT_JSON() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJSON = Paths.get(referencesPath, "ask", "beatles-ask-resultjson-true.json")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-resultjson-true.json")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_JSON", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseRESULT_JSON() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJSON = Paths.get(referencesPath, "ask", "beatles-ask-resultjson-false.json")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-resultjson-false.json")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_JSON", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueRESULT_CSV() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskCSV = Paths.get(referencesPath, "ask", "beatles-ask-resultcsv-true.csv")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-resultcsv-true.csv")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_CSV", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseRESULT_CSV() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskCSV = Paths.get(referencesPath, "ask", "beatles-ask-resultcsv-false.csv")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-resultcsv-false.csv")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_CSV", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueRESULT_TSV() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTSV = Paths.get(referencesPath, "ask", "beatles-ask-resulttsv-true.tsv")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-resulttsv-true.tsv")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TSV", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseRESULT_TSV() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTSV = Paths.get(referencesPath, "ask", "beatles-ask-resulttsv-false.tsv")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-resulttsv-false.tsv")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TSV", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskFalse));
        }

        @Test
        public void testInsertRDFXML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsert = Paths.get(referencesPath, "insert", "beatles-insert-rdfxml.xml")
                                .toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-rdfxml.xml").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsert = Paths.get(referencesPath, "insert", "beatles-insert-turtle.ttl")
                                .toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-turtle.ttl").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsert = Paths.get(referencesPath, "insert", "beatles-insert-trig.trig")
                                .toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-trig.trig").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertJSONLD() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsert = Paths.get(referencesPath, "insert", "beatles-insert-jsonld.jsonld")
                                .toString();
                String pathResBeatlesInsert = Paths.get(resultsPath, "insert", "beatles-insert-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesInsert,
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertResultXMLInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_XML", "-o",
                                "references/insert/beatles-insert-resultxml.xml",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: RESULT_XML is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertResultTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TURTLE", "-o",
                                "references/insert/beatles-insert-resultturtle.ttl",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: RESULT_TURTLE is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertResultJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_JSON", "-o",
                                "references/insert/beatles-insert-resultjson.json",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: RESULT_JSON is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertResultCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_CSV", "-o",
                                "references/insert/beatles-insert-resultcsv.csv",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: RESULT_CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertResultTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TSV", "-o",
                                "references/insert/beatles-insert-resulttsv.tsv",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: RESULT_TSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereRdfxml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = Paths
                                .get(referencesPath, "insert-where", "beatles-insertwhere-rdfxml.xml").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-rdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = Paths
                                .get(referencesPath, "insert-where", "beatles-insertwhere-turtle.ttl").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-turtle.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = Paths
                                .get(referencesPath, "insert-where", "beatles-insertwhere-trig.trig").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-trig.trig").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereJsonLd() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = Paths
                                .get(referencesPath, "insert-where", "beatles-insertwhere-jsonld.jsonld").toString();
                String pathResBeatlesInsertwhere = Paths
                                .get(resultsPath, "insert-where", "beatles-insertwhere-jsonld.jsonld").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesInsertwhere,
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereResultXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_XML", "-o",
                                "references/insert-where/beatles-insertwhere-resultxml.xml",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: RESULT_XML is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereResultTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TURTLE", "-o",
                                "references/insert-where/beatles-insertwhere-resultturtle.ttl",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: RESULT_TURTLE is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereResultJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_JSON", "-o",
                                "references/insert-where/beatles-insertwhere-resultjson.json",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: RESULT_JSON is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereResultCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_CSV", "-o",
                                "references/insert-where/beatles-insertwhere-resultcsv.csv",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: RESULT_CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereResultTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TSV", "-o",
                                "references/insert-where/beatles-insertwhere-resulttsv.tsv",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: RESULT_TSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteRDFXML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths.get(referencesPath, "delete", "beatles-delete-rdfxml.xml")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete", "beatles-delete-rdfxml.xml").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteJSONLD() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesDelete = Paths.get(referencesPath, "delete", "beatles-delete-jsonld.jsonld")
                                .toString();
                String pathResBeatlesDelete = Paths.get(resultsPath, "delete", "beatles-delete-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesDelete,
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteResultXMLInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_XML", "-o",
                                "references/delete/beatles-delete-resultxml.xml",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_XML is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteResultTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TURTLE", "-o",
                                "references/delete/beatles-delete-resultturtle.ttl",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_TURTLE is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteResultJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_JSON", "-o",
                                "references/delete/beatles-delete-resultjson.json",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_JSON is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteResultCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_CSV", "-o",
                                "references/delete/beatles-delete-resultcsv.csv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteResultTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TSV", "-o",
                                "references/delete/beatles-delete-resulttsv.tsv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_TSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereRDFXML() throws IOException {
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereJSONLD() throws IOException {
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereResultXMLInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_XML", "-o",
                                "references/delete-where/beatles-delete-where-resultxml.xml",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_XML is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereResultTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TURTLE", "-o",
                                "references/delete-where/beatles-delete-where-resultturtle.ttl",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_TURTLE is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereResultJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_JSON", "-o",
                                "references/delete-where/beatles-delete-where-resultjson.json",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_JSON is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereResultCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_CSV", "-o",
                                "references/delete-where/beatles-delete-where-resultcsv.csv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereResultTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TSV", "-o",
                                "references/delete-where/beatles-delete-where-resulttsv.tsv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: RESULT_TSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testConstructRDFXML() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesConstruct = Paths.get(referencesPath, "construct", "beatles-construct-rdfxml.xml")
                                .toString();
                String pathResBeatlesConstruct = Paths.get(resultsPath, "construct", "beatles-construct-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesConstruct,
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testConstructJSONLD() throws IOException {
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
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testConstructResultXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_XML", "-o",
                                "references/construct/beatles-construct-resultxml.xml", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: RESULT_XML is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testConstructResultTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TURTLE", "-o",
                                "references/construct/beatles-construct-resultturtle.ttl", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: RESULT_TURTLE is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testConstructResultJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_JSON", "-o",
                                "references/construct/beatles-construct-resultjson.json", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: RESULT_JSON is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testConstructResultCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_CSV", "-o",
                                "references/construct/beatles-construct-resultcsv.csv", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: RESULT_CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testConstructResultTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RESULT_TSV", "-o",
                                "references/construct/beatles-construct-resulttsv.tsv", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: RESULT_TSV is not a valid output format for insert, delete or construct requests.";
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
                assertTrue(err.toString().contains("Error reading input file"));
        }

        @Test
        public void testExecute_WhenQueryFileDoesNotExist_ThrowsException() {
                String nonExistentQueryFile = "non_existent_query_file.rq";
                String validInputFile = Paths.get(inputPath, "beatles.ttl").toString();

                int exitCode = cmd.execute("-i", validInputFile, "-f", "TURTLE", "-r", "TURTLE", "-o",
                                "output.ttl", nonExistentQueryFile);

                assertEquals(1, exitCode);
                assertTrue(err.toString().contains("Error when reading the query file"));
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