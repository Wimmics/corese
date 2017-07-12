/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.draw.gnuplot;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author edemairy
 */
public class GnuplotDrawer {

    private static Logger logger = Logger.getLogger(GnuplotDrawer.class.getName());

    /**
     * @param args Names of the files to parse to extract coordinates of the
     *             plot.
     */
    public static void main(String... args) throws ParserConfigurationException, SAXException, IOException {
        // read main arguments
        String outputPdf = "/tmp/out.pdf";
        String gnuplotPath = "gnuplot";
        String repoPath = ".";
        ArrayList<String> filenames = new ArrayList<>();
        for (int cptArg = 0; cptArg < args.length; cptArg++) {
            String currentArg = args[cptArg];
            if (currentArg.equals("-o")) {
                outputPdf = args[cptArg + 1];
                cptArg++;
            } else if (currentArg.equals("-g")) {
                repoPath = args[cptArg + 1];
            } else if (args[cptArg].equals("-gnuplot")) {
                gnuplotPath = args[cptArg + 1];
                cptArg++;
            } else {
                filenames.add(args[cptArg]);
            }
        }

        // Build files list to process
        ArrayList<File> files = new ArrayList<>();
        for (String filename : filenames) {
            File root;
            if (filename.startsWith("/")) { // absolute path
                String dirPath = filename.substring(0, filename.lastIndexOf("/") + 1);
                filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
                root = new File(dirPath);
            } else if (filename.contains("/")) { // relative path
                root = new File(filename.substring(0, filename.lastIndexOf("/")));
                filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
            } else {
                root = new File("."); // filename without path
            }
            final String finalFilename = filename;
            File[] newFiles = root.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    boolean result = name.matches(finalFilename);
                    if (result) {
                        logger.log(Level.INFO, "accepting file {0}", name);
                    } else {
                        logger.log(Level.FINE, "refusing file {0}", name);
                    }
                    return result;
                }
            });
            for (File newFile : newFiles) {
                files.add(newFile);
            }
        }

        // Processing the files.
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        Map<Long, Double> databaseCpuCoords = new TreeMap<>();
        Map<Long, Double> inMemoryCpuCoords = new TreeMap<>();
        Map<Long, Double> databaseMemoryCoords = new TreeMap<>();
        Map<Long, Double> inMemoryMemoryCoords = new TreeMap<>();
        String request = "";
        for (File currentFile : files) {
            String filename = currentFile.getCanonicalPath();
            logger.log(Level.INFO, "processing file {0}", filename);
            Document document = builder.parse(currentFile);
            NodeList dbStatsList = document.getElementsByTagName("Request");
            assert (dbStatsList.getLength() == 1);
            String currentRequest = dbStatsList.item(0).getTextContent();
            if (request.equals("")) {
                request = currentRequest;
            } else {
                if (!request.equals(currentRequest)) {
                    logger.log(Level.SEVERE, "All the files do not contain the same requests.");
                    System.exit(-1);
                }
            }

            long size = readSize(document);
            logger.log(Level.INFO, "size = {0}", size);

            double cpuMedian = readMedian(document, "CPU");
            logger.log(Level.INFO, "cpuMedian = {0}", cpuMedian);

            double memoryMedian = readMedian(document, "Memory");
            logger.log(Level.INFO, "memoryMedian = {0}", memoryMedian);

            Map<Long, Double> cpuCoords = null;
            Map<Long, Double> memoryCoords = null;
            if (filename.contains("DB")) {
                cpuCoords = databaseCpuCoords;
                memoryCoords = databaseMemoryCoords;
            } else if (filename.contains("MEMORY")) {
                cpuCoords = inMemoryCpuCoords;
                memoryCoords = inMemoryMemoryCoords;
            } else {
                logger.log(Level.SEVERE, "filename does not permit to know which input was used.");
                System.exit(2);
            }
            cpuCoords.put(size, cpuMedian);
            memoryCoords.put(size, memoryMedian);
        }

        // Building the gnuplot file .dat for cpu consumption
        File resultCpu = new File("graphCpu.dat");
        BufferedWriter bw = new BufferedWriter(new FileWriter(resultCpu));
        bw.append("# number of samples   |  median time in milliseconds");
        bw.newLine();
        bw.append("# Memory results\\n");
        bw.newLine();
        for (long nbSamples : inMemoryCpuCoords.keySet()) {
            bw.append(nbSamples + " " + inMemoryCpuCoords.get(nbSamples));
            bw.newLine();
        }
        bw.newLine();
        bw.newLine();
        for (long nbSamples : databaseCpuCoords.keySet()) {
            bw.append(nbSamples + " " + databaseCpuCoords.get(nbSamples));
            bw.newLine();
        }
        bw.close();

        // Building the gnuplot file .dat for cpu consumption
        File resultMemory = new File("graphMemory.dat");
        bw = new BufferedWriter(new FileWriter(resultMemory));
        bw.append("# number of samples   |  median memory consumption in bytes");
        bw.newLine();
        bw.append("# Memory results\\n");
        bw.newLine();
        for (long nbSamples : inMemoryMemoryCoords.keySet()) {
            bw.append(nbSamples + " " + inMemoryMemoryCoords.get(nbSamples));
            bw.newLine();
        }
        bw.newLine();
        bw.newLine();
        for (long nbSamples : databaseMemoryCoords.keySet()) {
            bw.append(nbSamples + " " + databaseMemoryCoords.get(nbSamples));
            bw.newLine();
        }
        bw.close();

        // Execute gnuplot to generate the .pdf
        Map<String, String> scriptVariables = new HashMap<>();
        scriptVariables.put(ScriptVariables.DATA_FILENAME.name(), resultCpu.getAbsolutePath());
        String title = String.format("Logarithmic graph of time duration by sample size\\\\n%s\\\\n%s", request, getGitVersion(repoPath));
        scriptVariables.put(ScriptVariables.TITLE.name(), title);
        scriptVariables.put(ScriptVariables.OUTPUT_FILE.name(), outputPdf);
        scriptVariables.put(ScriptVariables.TITLE_SET1.name(), "memory");
        scriptVariables.put(ScriptVariables.TITLE_SET2.name(), "neo4j");

        File gnuplotScript = writeFileWithValues(scriptVariables);
        generatePdf(gnuplotScript, outputPdf, gnuplotPath);

        title = String.format("Logarithmic gdurationraph of memory consumption by sample size\\\\n%s\\\\n%s", request, getGitVersion(repoPath));
        scriptVariables.put(ScriptVariables.DATA_FILENAME.name(), resultMemory.getAbsolutePath());
        scriptVariables.put(ScriptVariables.TITLE.name(), title);
        outputPdf = outputPdf.replace(".pdf", "_memory.pdf");
        scriptVariables.put(ScriptVariables.OUTPUT_FILE.name(), outputPdf);
        gnuplotScript = writeFileWithValues(scriptVariables);
        generatePdf(gnuplotScript, outputPdf, gnuplotPath);
    }

    private static void generatePdf(File gnuplotScript, String outputPdf, String gnuplotPath) throws IOException {
        logger.info("PDF written in " + outputPdf);
        Process gnuplot = Runtime.getRuntime().exec(gnuplotPath + " " + gnuplotScript.getAbsolutePath());
        BufferedReader oreader = new BufferedReader(new InputStreamReader(gnuplot.getInputStream()));
        BufferedReader ereader = new BufferedReader(new InputStreamReader(gnuplot.getErrorStream()));
        oreader.lines().forEachOrdered(System.out::println);
        ereader.lines().forEachOrdered(System.err::println);
    }

    private static File writeFileWithValues(Map<String, String> scriptVariables) throws IOException {
        File gnuplotScript = File.createTempFile("gnuplotScript", ".plot");
        logger.info("writing gnuplot script in " + gnuplotScript.getAbsolutePath());
        final BufferedWriter gnuplotBw = new BufferedWriter(new FileWriter(gnuplotScript));
        InputStreamReader gnuplotScriptResource = new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("recession.plot"));
        BufferedReader reader = new BufferedReader(gnuplotScriptResource);

        reader.lines()
                .forEachOrdered((s) -> {
                            try {
                                for (String variableName : scriptVariables.keySet()) {
                                    String value = scriptVariables.get(variableName);
                                    value = value.replace("\"", "\\\\\"");
                                    s = s.replaceAll("<" + variableName + ">", value);
                                }
                                gnuplotBw.append(s);
                                gnuplotBw.newLine();
                            } catch (IOException ex) {
                                Logger.getLogger(GnuplotDrawer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                );
        gnuplotBw.close();
        return gnuplotScript;
    }

    private static double extractMedian(NodeList node) {
        Pattern medianExtract = Pattern.compile(".*median:\\s+(\\S+).*", Pattern.DOTALL);
        String textStat = node.item(0).getTextContent();
        Matcher matcher = medianExtract.matcher(textStat);
        if (matcher.matches()) {
            double result = Double.parseDouble(matcher.group(1));
            return result;
        } else {
            throw new IllegalArgumentException("the node provided does not contain any median");
        }
    }

    private static long guessSize(Document document) {
        long size;
        String dbName = document.getElementsByTagName("Input").item(0).getTextContent();
        Pattern patternWithAddress = Pattern.compile(".*(?:nq|gz)[:](\\d+),(\\d+)");
        Pattern patternSize = Pattern.compile(".*(?:nq|gz)[:](\\d+)");
        Pattern patternSimple = Pattern.compile(".*(?:nq|gz)");
        Pattern for40m = Pattern.compile(".*-00\\(0\\|1\\|2\\|3\\).nq.gz");
        Pattern for100m = Pattern.compile(".*-00\\d+.nq.gz");
        Pattern for1g = Pattern.compile(".*-0\\d+.nq.gz");
        Pattern for4g = Pattern.compile(".*-\\d+.nq.gz");

        Matcher m = patternWithAddress.matcher(dbName);
        if (m.matches()) {
            long start = Long.parseLong(m.group(1));
            long end = Long.parseLong(m.group(2));
            size = end - start + 1;
        } else {
            m = patternSize.matcher(dbName);
            if (m.matches()) {
                size = Long.parseLong(m.group(1));
            } else if (for4g.matcher(dbName).matches()) {
                size = 4_000_000_000L;
            } else if (for1g.matcher(dbName).matches()) {
                size = 1_000_000_000L;
            } else if (for100m.matcher(dbName).matches()) {
                size = 100_000_000L;
            } else if (for40m.matcher(dbName).matches()) {
                size = 40_000_000;
            } else {
                if (patternSimple.matcher(dbName).matches()) {
                    size = 10_000_000;
                } else {
                    size = -1;
                }
            }
        }
        return size;
    }

    private static String getGitVersion(String path) throws IOException {
        Process p = new ProcessBuilder("git", "log", "-1").start();
        BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String result = output.lines().filter(s -> s.contains("Date") || s.contains("commit")).collect(Collectors.joining(" / "));
        return result;
    }

    private static int readSize(Document document) {
        NodeList size = document.getElementsByTagName("Size");
        int result = Integer.parseInt(size.item(0).getTextContent());
        return result;
    }

    private static double readMedian(Document document, String xmlNodeName) {
        NodeList dbStats = document.getElementsByTagName(xmlNodeName);
        double median;
        if (dbStats.getLength() == 0) {
            median = -100;
        } else {
            median = extractMedian(dbStats);
        }
        return median;
    }

    public enum ScriptVariables {
        DATA_FILENAME,
        TITLE,
        OUTPUT_FILE,
        TITLE_SET1,
        TITLE_SET2
    }
}
