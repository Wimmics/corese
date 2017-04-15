/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

import static fr.inria.corese.coresetimer.utils.VariousUtils.*;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.wimmics.coresetimer.Main.TestDescription;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author edemairy
 */
public class CoreseTimer {

	private final static Logger LOGGER = Logger.getLogger(CoreseTimer.class.getName());

	public CoreseAdapter adapter;
	public String adapterName;
	private Mappings mappings;

	public enum Profile {
		DB, MEMORY
	};

	private static String outputRoot;
	private Profile mode = Profile.MEMORY;
	private boolean initialized;
	private DescriptiveStatistics stats;

	/**
	 *
	 * @param adapterName class name for the adapter to the version of
	 * corese used.
	 * @param runProfile kind of usage of corese (currently "db" or
	 * "memory"). Used to classify the results and stats done.
	 */
	public CoreseTimer() {
		this.adapterName = CoreseAdapter.class.getCanonicalName();
		initialized = false;
	}

	public CoreseTimer setMode(Profile mode) {
		this.mode = mode;
		return this;
	}

	public CoreseTimer init() {
		switch (mode) {
			case DB: {
				System.setProperty("fr.inria.corese.factory", "fr.inria.corese.tinkerpop.Factory");
				break;
			}
			case MEMORY: {
				System.setProperty("fr.inria.corese.factory", "");
				break;
			}
		};

		// create output directory of the form ${OUTPUT_ROOT}
		outputRoot = getEnvWithDefault("OUTPUT_ROOT", "./");
		outputRoot = ensureEndWith(outputRoot, "/");
		outputRoot += mode;
		outputRoot = ensureEndWith(outputRoot, "/");
		createDir(outputRoot, "rwxr-x---");
		initialized = true;
		return this;
	}

	public static String makeFileName(String prefix, String suffix, int nbInput, int nbQuery) {
		return outputRoot + prefix + "input_" + nbInput + "_query_" + nbQuery + ".xml";
	}

	public CoreseTimer run(TestDescription test) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, LoadException {
		LOGGER.entering(CoreseTimer.class.getName(), "run");
		assert (initialized);

		// Loading the nq data in corese, then applying several times the query.
		LOGGER.log(Level.INFO, "beginning with input #{0}", test.getInput());
		// require to have a brand new adapter for each new input set.
		adapter = (CoreseAdapter) Class.forName(adapterName).newInstance();

		String inputFileName = "";
		switch (mode) {
			case MEMORY: {
				inputFileName += test.getInput();
				adapter.preProcessing(inputFileName, true);
				break;
			}
			case DB: {
				inputFileName += test.getInputDb();
				System.setProperty("fr.inria.corese.tinkerpop.dbinput", inputFileName);
				adapter.preProcessing(inputFileName, false);
				break;
			}
		}

		String query = test.getRequest();
		LOGGER.log(Level.INFO, "processing nbQuery #{0}", query);
		stats = new DescriptiveStatistics();
		int nbCycles = test.getMeasuredCycles() + test.getWarmupCycles();
		for (int i = 0; i < nbCycles; i++) {
			LOGGER.log(Level.INFO, "iteration #{0}", i);
			System.gc();
			final long startTime = System.currentTimeMillis();
			LOGGER.log(Level.INFO, "before query");
			adapter.execQuery(query);
			LOGGER.log(Level.INFO, "after query");
			final long endTime = System.currentTimeMillis();
			long delta = endTime - startTime;
			LOGGER.info(String.format("elapsed time = %d ms", delta));
			if (i >= test.getWarmupCycles()) {
				stats.addValue(delta);
			}
		}
		adapter.saveResults(test.getResultFileName(mode));
		mappings = adapter.getMappings();
		adapter.postProcessing();
		LOGGER.exiting(CoreseTimer.class.getName(), "run");
		return this;
	}

	public Mappings getMapping() {
		return mappings;
	}

	public DescriptiveStatistics getStats() {
		return stats;
	}
}
