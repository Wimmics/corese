/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author edemairy
 */
public class CoreseTimer {

	private final static Logger LOGGER = Logger.getLogger(CoreseTimer.class.getName());
	public final static String[][] inputs = {
		{"data.nq", "data_db"}
	};

	public final static String[] queries = {
		//		"select (count(*) as ?count) where { graph ?g {?x ?p ?y}}",
		"select * where {<http://prefix.cc/popular/all.file.vann>  ?p ?y .}",// limit 10000",
		"select * where { ?x  a ?y } limit 10000",
		"select * where { <http://prefix.cc/popular/all.file.vann>  ?p ?y . ?y ?q <http://prefix.cc/popular/all.file.vann> .} limit 10000"
//		"select * where { ?x ?p ?y . ?y ?q ?x }" // Intractable: if there are 10^6 edges, requests for 10^12 edges
	};

	public final static int WARMUP_THRESHOLD = 20;
	public final static int SAMPLES = 20;
	public final static boolean DATA_IN_MEMORY = false;
	public final static String PREFIX = "bd_";
	public CoreseAdapter adapter;
	public String adapterName;
	private static String outputRoot;

	public enum Profile {
		DB, MEMORY
	};
	private Profile profile;

	/**
	 *
	 * @param adapterName class name for the adapter to the version of
	 * corese used.
	 * @param runProfile kind of usage of corese (currently "db" or
	 * "memory"). Used to classify the results and stats done.
	 */
	public CoreseTimer(String adapterName, Profile runProfile) {
		this.adapterName = adapterName;
		// create output directory of the form ${OUTPUT_ROOT}
		outputRoot = getEnvWithDefault("OUTPUT_ROOT", "./");
		outputRoot = ensureEndWith(outputRoot, "/");
		outputRoot += runProfile;
		outputRoot = ensureEndWith(outputRoot, "/");
		createDir(outputRoot, "rwxr-x---");
		this.profile = runProfile;
	}

	public static String makeFileName(String prefix, String suffix, int nbInput, int nbQuery) {
		return outputRoot + prefix + "input_" + nbInput + "_query_" + nbQuery + ".xml";
	}

	public void run() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		String inputRoot = getEnvWithDefault("INPUT_ROOT", "./");
		inputRoot = ensureEndWith(inputRoot, "/");

		// Loading the nq data in corese, then applying several times the query.
		for (int nbInput = 0; nbInput < inputs.length; nbInput++) {
			LOGGER.info("beginning with input #" + nbInput);
			// require to have a brand new adapter for each new input set.
			adapter = (CoreseAdapter) Class.forName(adapterName).newInstance();

			String inputFileName = inputRoot;
			if (profile == Profile.MEMORY) {
				inputFileName += inputs[nbInput][0];
				adapter.preProcessing(inputFileName, true);
			} else if (profile == Profile.DB) {
				inputFileName += inputs[nbInput][1];
				System.setProperty("fr.inria.corese.tinkerpop.dbinput", inputFileName);
				System.out.println("property = " + System.getProperty("fr.inria.corese.tinkerpop.dbinput"));
				adapter.preProcessing(inputFileName, false);
			}

			for (int nbQuery = 0; nbQuery < queries.length; nbQuery++) {
				String query = queries[nbQuery];
				LOGGER.info("processing nbQuery #" + nbQuery);
				DescriptiveStatistics stats = new DescriptiveStatistics();
				for (int i = 0; i < SAMPLES + WARMUP_THRESHOLD; i++) {
					LOGGER.info("iteration #" + i);
					final long startTime = System.currentTimeMillis();
					adapter.execQuery(query);
					final long endTime = System.currentTimeMillis();
					long delta = endTime - startTime;
					if (i > WARMUP_THRESHOLD) {
						stats.addValue(delta);
					}
				}

				String resultsFileName = makeFileName("result_", ".txt", nbInput, nbQuery);
				adapter.saveResults(resultsFileName);
				writeStats(nbInput, nbQuery, stats);

			}
		}
		adapter.postProcessing();
	}

	/**
	 *
	 * @param envName Environment variable to read.
	 * @param defaultResult Default result provided if the environment
	 * variable is not set.
	 * @return The content of the environment variable if set, defaultResult
	 * otherwise.
	 */
	private String getEnvWithDefault(String envName, String defaultResult) {
		String result = System.getenv(envName);
		if (result == null) {
			result = defaultResult;
		}
		return result;
	}

	private void writeStats(int nbInput, int nbQuery, DescriptiveStatistics stats) {
		String query = queries[nbQuery];
		String statsFilename = makeFileName("stats_", ".stats", nbInput, nbQuery);
		try {
			FileWriter output = new FileWriter(statsFilename);
			output.append(query);
			output.append(stats.toString());
			output.append("Q25 = " + stats.getPercentile(25));
			output.append("Q75 = " + stats.getPercentile(75));
			output.close();
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Exception when trying to save results in " + statsFilename, ex);
		}
	}

	private void createDir(String dirName, String permissions) {
		Path dirPath = Paths.get(dirName);
		Set<PosixFilePermission> filePermissions = PosixFilePermissions.fromString(permissions);
		FileAttribute<Set<PosixFilePermission>> attributes = PosixFilePermissions.asFileAttribute(filePermissions);
		try {
			Files.createDirectories(dirPath, attributes);
		} catch (IOException ex) {
			Logger.getLogger(CoreseTimer.class.getName()).log(Level.SEVERE, null, ex);
		}
		LOGGER.info("Directory created at: " + dirPath.toString());
	}

	private String ensureEndWith(String dirName, String end) {
		String result = (dirName.endsWith(end)) ? dirName : dirName + "/";
		return result;
	}

}
