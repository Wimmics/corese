/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgtool.load.LoadException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.inria.corese.coresetimer.utils.VariousUtils.*;

/**
 * @author edemairy
 */
public class CoreseTimer {

    private final static Logger LOGGER = Logger.getLogger(CoreseTimer.class.getName());
    private static String outputRoot;
    private CoreseAdapter adapter;
    private String adapterName;
    private Mappings mappings;

    private Profile mode = Profile.MEMORY;
    private boolean initialized;
    private DescriptiveStatistics stats;
    private DescriptiveStatistics statsMemory;
    private TestDescription test;

    private CoreseTimer(TestDescription test) {
        this.adapterName = CoreseAdapter.class.getCanonicalName();
        initialized = false;
        this.test = test;
    }

    static public CoreseTimer build(TestDescription test) {
        return new CoreseTimer(test);
    }

    public CoreseTimer setMode(Profile mode) {
        this.mode = mode;
        return this;
    }

    public CoreseTimer init() {
        // create output directory of the form ${OUTPUT_ROOT}
        outputRoot = getEnvWithDefault("OUTPUT_ROOT", "./");
        outputRoot = ensureEndWith(outputRoot, "/");
        outputRoot += mode;
        outputRoot = ensureEndWith(outputRoot, "/");
        createDir(outputRoot, "rwxr-x---");
        initialized = true;
        return this;
    }

    public CoreseTimer load() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, LoadException {
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
        return this;
    }

    public CoreseTimer run() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, LoadException {
        LOGGER.entering(CoreseTimer.class.getName(), "run");
        assert (initialized);

        // Process the query
        String inputFileName = "";
        Map<String, String> tagReplacement = new HashMap<>();
        switch (mode) {
            case MEMORY: {
                tagReplacement.put("<AT_DB>", "");
                tagReplacement.put("<BEGIN_SERVICE>", "");
                tagReplacement.put("<END_SERVICE>", "");
                break;
            }
            case DB: {
                tagReplacement.put("<AT_DB>", String.format("@db <%s>", test.getTestSuite().getDatabasePath()));
                tagReplacement.put("<BEGIN_SERVICE>", String.format("service <db:%s> {", test.getTestSuite().getDatabasePath()));
                tagReplacement.put("<END_SERVICE>", "}");
                break;
            }
        }
        String query = test.getRequest();
        for (String key : tagReplacement.keySet()) {
            query = query.replaceAll(key, tagReplacement.get(key));
        }

        LOGGER.log(Level.INFO, "processing nbQuery #{0}", query);
        stats = new DescriptiveStatistics();
        statsMemory = new DescriptiveStatistics();
        int nbCycles = test.getMeasuredCycles() + test.getWarmupCycles();
        boolean measured = true;
        for (int i = 0; i < nbCycles; i++) {
            LOGGER.log(Level.INFO, "iteration #{0}", i);
            System.gc();
            final long startTime = System.currentTimeMillis();
            LOGGER.log(Level.INFO, "before query");

            ExecutorService executor = Executors.newSingleThreadExecutor();
            final String finalQuery = query;
            Future<?> future = executor.submit(() -> adapter.execQuery(finalQuery));

            try {
                future.get(1, TimeUnit.HOURS);
                measured = true;
            } catch (InterruptedException | TimeoutException e) {
                future.cancel(true);
                measured = false;
                LOGGER.log(Level.WARNING, "Terminated!");
            } catch (ExecutionException ex) {
                Logger.getLogger(CoreseTimer.class.getName()).log(Level.SEVERE, null, ex);
            }
            executor.shutdownNow();

            LOGGER.log(Level.INFO, "after query");
            final long endTime = System.currentTimeMillis();
            long delta = endTime - startTime;
            long memoryUsage = getMemoryUsage();
            LOGGER.info(String.format("elapsed time = %d ms", delta));
            LOGGER.info(String.format("used memory = %d bytes", memoryUsage));
            if (i >= test.getWarmupCycles()) {
                if (!measured) {
                    while (i < nbCycles) {
                        stats.addValue(-100);
                        statsMemory.addValue(memoryUsage);
                        i++;
                    }
                } else {
                    stats.addValue(delta);
                    statsMemory.addValue(memoryUsage);
                }
            }
        }
        mappings = adapter.getMappings();
        adapter.postProcessing();
        LOGGER.exiting(CoreseTimer.class.getName(), "run");
        return this;
    }

    public void writeResults() {
        String resultsFileName = test.getOutputPath("results_" + mode);
        LOGGER.log(Level.INFO, "Writing results in {0}", resultsFileName);
        adapter.saveResults(resultsFileName);
    }

    public Mappings getMapping() {
        return mappings;
    }

    private DescriptiveStatistics getStats() {
        return stats;
    }

    private DescriptiveStatistics getStatsMemory() {
        return statsMemory;
    }

    private long getMemoryUsage() {
        long before = getGcCount();
        System.gc();
        while (getGcCount() == before) ;
        return getCurrentlyUsedMemory();
    }

    private long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) {
                sum += count;
            }
        }
        return sum;
    }

    private long getCurrentlyUsedMemory() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()
                + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    public void writeStatistics() {
        Document doc;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();
            Element rootElement = doc.createElement("TestResult");
            Element size = doc.createElement("Size");
            Text sizeText = doc.createTextNode("" + test.getSize());
            size.appendChild(sizeText);
            rootElement.appendChild(size);

            Element inputs = doc.createElement("Inputs");

            Element inputFile = doc.createElement("Input");
            Text inputFileText = doc.createTextNode(test.getInput());
            inputFile.appendChild(inputFileText);

            Element request = doc.createElement("Request");
            Text requestText = doc.createTextNode(test.getRequest());
            request.appendChild(requestText);

            Element timestamp = doc.createElement("Timestamp");
            Text timestampText = doc.createTextNode(LocalDateTime.now().toString());
            timestamp.appendChild(timestampText);

            Element[] subElements = {inputFile, request, timestamp};
            for (Element e : subElements) {
                inputs.appendChild(e);
            }

            Element outputs = doc.createElement("Statistics");

            Element statsMemory = doc.createElement("CPU");
            Text statsMemoryText = doc.createTextNode(getStats().toString());
            statsMemory.appendChild(statsMemoryText);

            Element statsMemoryCoreseMem = doc.createElement("Memory");
            Text statsMemoryCoreseMemText = doc.createTextNode(getStatsMemory().toString());
            statsMemoryCoreseMem.appendChild(statsMemoryCoreseMemText);

            Element[] subElements2 = {statsMemory, statsMemoryCoreseMem};
            for (Element e : subElements2) {
                outputs.appendChild(e);
            }

            rootElement.appendChild(inputs);
            rootElement.appendChild(outputs);

            doc.appendChild(rootElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(test.getOutputPath("stats_" + mode)));
            transformer.transform(source, streamResult);
            LOGGER.log(Level.INFO, "Results were written in: {}", test.getOutputPath(mode.name()));
        } catch (ParserConfigurationException | TransformerException ex) {
            LOGGER.log(Level.INFO, "Error when writing results:", ex.getMessage());
            ex.printStackTrace();
        }

    }

    public void setTest(TestDescription test) {
        this.test = test;
    }

    public enum Profile {
        DB, MEMORY
    }

}
