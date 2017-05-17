/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.draw.gnuplot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author edemairy
 */
public class GnuplotDrawer {
	
	private static Logger logger = Logger.getLogger(GnuplotDrawer.class.getName());
	
	public enum ScriptVariables {
		DATA_FILENAME,
		TITLE,
		OUTPUT_FILE,
		TITLE_SET1,
		TITLE_SET2
	}

	/**
	 *
	 * @param args Names of the files to parse to extract coordinates of the
	 * plot.
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
					logger.log(Level.INFO, "{0} file {1}", new Object[]{result ? "accepting" : "refusing", name});
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
		Map<Long, Long> dbCoords = new TreeMap<Long, Long>();
		Map<Long, Long> memoryCoords = new TreeMap<Long, Long>();
		for (File currentFile : files) {
			logger.log(Level.INFO, "processing file {0}", currentFile.getCanonicalFile());
			Document document = builder.parse(currentFile);
			long size = guessSize(document);
			logger.log(Level.INFO, "guessed size = {0}", size);
			
			NodeList memoryStats = document.getElementsByTagName("StatsMemory");
			long memoryMedian = extractMedian(memoryStats);
			logger.log(Level.INFO, "memory median = {0}", memoryMedian);
			memoryCoords.put(size, memoryMedian);
			
			NodeList dbStats = document.getElementsByTagName("StatsDb");
			long dbMedian = extractMedian(dbStats);
			logger.log(Level.INFO, "db median = {0}", dbMedian);
			dbCoords.put(size, dbMedian);
		}

		// Building the gnuplot file .dat
		File result = new File("graph.dat");
		BufferedWriter bw = new BufferedWriter(new FileWriter(result));
		bw.append("# number of samples   |  median time in milliseconds");
		bw.newLine();
		bw.append("# Memory results\\n");
		bw.newLine();
		for (long nbSamples : memoryCoords.keySet()) {
			bw.append(nbSamples + " " + memoryCoords.get(nbSamples));
			bw.newLine();
		}
		bw.newLine();
		bw.newLine();
		for (long nbSamples : dbCoords.keySet()) {
			bw.append(nbSamples + " " + dbCoords.get(nbSamples));
			bw.newLine();
		}
		bw.close();

		// Execute gnuplot to generate the .pdf
		Map<String, String> scriptVariables = new HashMap<>();
		scriptVariables.put(ScriptVariables.DATA_FILENAME.name(), result.getAbsolutePath());
		String title = String.format("Logarithmic graph of time duration by sample size\\\\n%s", getGitVersion(repoPath));
		scriptVariables.put(ScriptVariables.TITLE.name(), title);
		scriptVariables.put(ScriptVariables.OUTPUT_FILE.name(), outputPdf);
		scriptVariables.put(ScriptVariables.TITLE_SET1.name(), "memory");
		scriptVariables.put(ScriptVariables.TITLE_SET2.name(), "neo4j");
		File gnuplotScript = File.createTempFile("gnuplotScript", ".plot");
		final BufferedWriter gnuplotBw = new BufferedWriter(new FileWriter(gnuplotScript));
		InputStreamReader gnuplotScriptResource = new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("recession.plot"));
		BufferedReader reader = new BufferedReader(gnuplotScriptResource);
		reader.lines().forEachOrdered((s) -> {
			try {
				for (String variableName : scriptVariables.keySet()) {
					s = s.replaceAll("<" + variableName + ">", scriptVariables.get(variableName));
				}
				gnuplotBw.append(s);
				gnuplotBw.newLine();
			} catch (IOException ex) {
				Logger.getLogger(GnuplotDrawer.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		gnuplotBw.close();
		logger.info("PDF written in " + outputPdf);
		Process gnuplot = Runtime.getRuntime().exec(gnuplotPath + " " + gnuplotScript.getAbsolutePath());
		BufferedReader oreader = new BufferedReader(new InputStreamReader(gnuplot.getInputStream()));
		BufferedReader ereader = new BufferedReader(new InputStreamReader(gnuplot.getErrorStream()));
		oreader.lines().forEachOrdered(System.out::println);
		ereader.lines().forEachOrdered(System.err::println);
	}
	
	private static int extractMedian(NodeList node) {
		Pattern medianExtract = Pattern.compile(".*median: (\\d+)\\.\\d+.*", Pattern.DOTALL);
		String textStat = node.item(0).getTextContent();
		Matcher matcher = medianExtract.matcher(textStat);
		if (matcher.matches()) {
			int result = Integer.parseInt(matcher.group(1));
			return result;
		} else {
			throw new IllegalArgumentException("the node provided does not contain any median");
		}
	}
	
	private static long guessSize(Document document) {
		long size;
		String dbName = document.getElementsByTagName("DbPath").item(0).getTextContent();
		Pattern patternWithAddress = Pattern.compile(".*(?:nq|gz)[_:](\\d+)_(\\d+)_db");
		Pattern patternSize = Pattern.compile(".*(?:nq|gz)[_:](\\d+)_db");
		Pattern patternSimple = Pattern.compile(".*(?:nq|gz)_db");
		Matcher m = patternWithAddress.matcher(dbName);
		if (m.matches()) {
			long start = Long.parseLong(m.group(1));
			long end = Long.parseLong(m.group(2));
			size = end - start + 1;
		} else {
			m = patternSize.matcher(dbName);
			if (m.matches()) {
				size = Long.parseLong(m.group(1));
			} else {
				m = patternSimple.matcher(dbName);
				if (m.matches()) {
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
		String result = output.lines().filter(s -> s.contains("Date") || s.contains("commit")).collect(Collectors.joining("\\\\n"));
		return result;
	}
}
