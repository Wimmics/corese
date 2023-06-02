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
        public void testSelectXml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectXml = Paths
                                .get(referencesPath, "select", "beatles-select-xml.xml")
                                .toString();
                String pathResBeatlesSelectXml = Paths.get(resultsPath, "select", "beatles-select-xml.xml")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o",
                                pathResBeatlesSelectXml, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectXml, pathResBeatlesSelectXml));
        }

        @Test
        public void testSelectTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTurtle = Paths
                                .get(referencesPath, "select", "beatles-select-turtle.ttl")
                                .toString();
                String pathResBeatlesSelectTurtle = Paths
                                .get(resultsPath, "select", "beatles-select-turtle.ttl")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o",
                                pathResBeatlesSelectTurtle, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectTurtle, pathResBeatlesSelectTurtle));
        }

        @Test
        public void testSelectTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTrig = Paths
                                .get(referencesPath, "select", "beatles-select-trig.trig")
                                .toString();
                String pathResBeatlesSelectTrig = Paths.get(resultsPath, "select", "beatles-select-trig.trig")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o",
                                pathResBeatlesSelectTrig, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectTrig, pathResBeatlesSelectTrig));
        }

        @Test
        public void testSelectJson() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectJson = Paths
                                .get(referencesPath, "select", "beatles-select-json.json")
                                .toString();
                String pathResBeatlesSelectJson = Paths
                                .get(resultsPath, "select", "beatles-select-json.json")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o",
                                pathResBeatlesSelectJson, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectJson, pathResBeatlesSelectJson));
        }

        @Test
        public void testSelectCsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectCsv = Paths
                                .get(referencesPath, "select", "beatles-select-csv.csv")
                                .toString();
                String pathResBeatlesSelectCsv = Paths.get(resultsPath, "select", "beatles-select-csv.csv")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "CSV", "-o",
                                pathResBeatlesSelectCsv, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectCsv, pathResBeatlesSelectCsv));
        }

        @Test
        public void testSelectTSV() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesSelectTsv = Paths
                                .get(referencesPath, "select", "beatles-select-tsv.tsv")
                                .toString();
                String pathResBeatlesSelectTsv = Paths.get(resultsPath, "select", "beatles-select-tsv.tsv")
                                .toString();
                String pathQueryBeatlesAlbum = Paths.get(queriesPath, "select", "beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TSV", "-o",
                                pathResBeatlesSelectTsv, pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesSelectTsv, pathResBeatlesSelectTsv));
        }

        @Test
        public void testAskTrueXml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskXml = Paths.get(referencesPath, "ask", "beatles-ask-xml-true.xml")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-xml-true.xml")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskXml, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseXml() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskXml = Paths.get(referencesPath, "ask", "beatles-ask-xml-false.xml")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-xml-false.xml")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "RDFXML", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskXml, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths
                                .get(referencesPath, "ask", "beatles-ask-turtle-true.ttl")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-turtle-true.ttl")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseTurtle() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths
                                .get(referencesPath, "ask", "beatles-ask-turtle-false.ttl")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-turtle-false.ttl")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TURTLE", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths
                                .get(referencesPath, "ask", "beatles-ask-trig-true.ttl")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-trig-true.ttl")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseTrig() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTurtle = Paths
                                .get(referencesPath, "ask", "beatles-ask-trig-false.ttl")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-trig-false.ttl")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TRIG", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTurtle, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueJson() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJSON = Paths.get(referencesPath, "ask", "beatles-ask-json-true.json")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-json-true.json")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseJson() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskJSON = Paths.get(referencesPath, "ask", "beatles-ask-json-false.json")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-json-false.json")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "JSONLD", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueCsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskCSV = Paths.get(referencesPath, "ask", "beatles-ask-csv-true.csv")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-csv-true.csv")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "CSV", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseCsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskCSV = Paths.get(referencesPath, "ask", "beatles-ask-csv-false.csv")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-csv-false.csv")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "CSV", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueTsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTSV = Paths.get(referencesPath, "ask", "beatles-ask-tsv-true.tsv")
                                .toString();
                String pathResBeatlesAskTrue = Paths.get(resultsPath, "ask", "beatles-ask-tsv-true.tsv")
                                .toString();
                String pathQueryBeatlesAskTrue = Paths.get(queriesPath, "ask", "beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TSV", "-o", pathResBeatlesAskTrue,
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseTsv() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathRefBeatlesAskTSV = Paths.get(referencesPath, "ask", "beatles-ask-tsv-false.tsv")
                                .toString();
                String pathResBeatlesAskFalse = Paths.get(resultsPath, "ask", "beatles-ask-tsv-false.tsv")
                                .toString();
                String pathQueryBeatlesAskFalse = Paths.get(queriesPath, "ask", "beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TSV", "-o", pathResBeatlesAskFalse,
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals(out.toString(), "");
                assertEquals(err.toString(), "");
                assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskFalse));
        }

        @Test
        public void testInsertXml() throws IOException {
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
        public void testInsertJson() throws IOException {
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
        public void testInsertCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "CSV", "-o",
                                "references/insert/beatles-insert-csv.csv",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsert = Paths.get(queriesPath, "insert", "beatlesInsertRock.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TSV", "-o",
                                "references/insert/beatles-insert-tsv.tsv",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: TSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereXml() throws IOException {
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
        public void testInsertWhereCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "CSV", "-o",
                                "references/insert-where/beatles-insertwhere-csv.csv",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testInsertWhereTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = Paths.get(queriesPath, "insert-where", "beatlesAge.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TSV", "-o",
                                "references/insert-where/beatles-insertwhere-tsv.tsv",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: TSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteXml() throws IOException {
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
        public void testDeleteJson() throws IOException {
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
        public void testDeleteCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "CSV", "-o",
                                "references/delete/beatles-delete-csv.csv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete", "deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TSV", "-o",
                                "references/delete/beatles-delete-tsv.tsv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: TSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereXml() throws IOException {
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
        public void testDeleteWhereJson() throws IOException {
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
        public void testDeleteWhereCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "CSV", "-o",
                                "references/delete-where/beatles-delete-where-csv.csv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testDeleteWhereTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesDelete = Paths.get(queriesPath, "delete-where", "deleteLenon.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TSV", "-o",
                                "references/delete-where/beatles-delete-where-tsv.tsv",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: TSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testConstructXml() throws IOException {
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
        public void testConstructJson() throws IOException {
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
        public void testConstructCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "CSV", "-o",
                                "references/construct/beatles-construct-csv.csv", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: CSV is not a valid output format for insert, delete or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.startsWith(expectedOutput));
        }

        @Test
        public void testConstructTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = Paths.get(inputPath, "beatles.ttl").toString();
                String pathQueryBeatlesConstruct = Paths.get(queriesPath, "construct", "albumBeatles.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "TSV", "-o",
                                "references/construct/beatles-construct-tsv.tsv", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: TSV is not a valid output format for insert, delete or construct requests.";
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