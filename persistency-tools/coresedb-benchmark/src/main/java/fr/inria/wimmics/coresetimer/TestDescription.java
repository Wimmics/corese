/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

import fr.inria.corese.rdftograph.RdfToGraph;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author edemairy
 */
public class TestDescription implements Cloneable {

    private final String RDF_OUTPUT_FILE_FORMAT = "%s/rdf_result_%s_%s.xml";
    private final Main.TestSuite suite;
    private String testId;
    private String request;
    private boolean resultsEqual;
    private String inputFilesPattern;
    private String inputDb;
    private int warmupCycles;
    private int measuredCycles;
    private String resultFileName;
    private String outputPrefixFilename;
    private int size = -1;

    private TestDescription(String id, Main.TestSuite suite) {
        testId = id;
        this.suite = suite;
    }

    static public TestDescription build(String id, Main.TestSuite suite) {
        return new TestDescription(id, suite);
    }

    public String getId() {
        return testId;
    }

    public void computeSize() {
        if (size == -1) {
            try {
                InputStream input = RdfToGraph.makeStream(inputFilesPattern);
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                size = 0;
                while (br.readLine() != null) {
                    size++;
                }
            } catch (Exception ex) {
                Logger.getGlobal().log(Level.SEVERE, "Impossible to compute size for {0}", inputFilesPattern);
                ex.printStackTrace();
            }
        }
    }

    public int getSize() {
        computeSize();
        return size;
    }

    public String getRequest() {
        return request;
    }

    public TestDescription setRequest(String request) {
        this.request = request;
        return this;
    }

    public Boolean getResultsEqual() {
        return resultsEqual;
    }

    public TestDescription setResultsEqual(boolean result) {
        this.resultsEqual = result;
        return this;
    }

    public int getWarmupCycles() {
        return this.warmupCycles;
    }

    public TestDescription setWarmupCycles(int n) {
        this.warmupCycles = n;
        return this;
    }

    public int getMeasuredCycles() {
        return this.measuredCycles;
    }

    public TestDescription setMeasuredCycles(int n) {
        this.measuredCycles = n;
        return this;
    }

    public String getResultFileName(CoreseTimer.Profile mode) {
        resultFileName = String.format(RDF_OUTPUT_FILE_FORMAT, suite.getOutputRoot(), testId, mode);
        return resultFileName;
    }

    public TestDescription setInputFilesPattern(String input) {
        this.inputFilesPattern = input;
        return this;
    }

    /**
     * @return name of the RDF inputFilesPattern file.
     */
    public String getInput() {
        return inputFilesPattern;
    }

    /**
     * @return Path for the DB.
     */
    public String getInputDb() {
        return inputDb;
    }

    public TestDescription setInputDb(String input) {
        this.inputDb = input;
        return this;
    }

    public TestDescription setOutputPath(String oPath) {
        outputPrefixFilename = oPath;
        return this;
    }

    public String getOutputPath(String type) {
        return String.format(outputPrefixFilename, type);
    }
}
