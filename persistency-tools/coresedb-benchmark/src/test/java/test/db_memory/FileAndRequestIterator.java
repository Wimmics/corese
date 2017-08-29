package test.db_memory;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.wimmics.coresetimer.Main;
import fr.inria.wimmics.coresetimer.TestDescription;

import java.util.ArrayList;
import java.util.Iterator;

public class FileAndRequestIterator implements Iterator<Object[]> {
    private boolean started = false;
    private int cptInputFiles = 0;
    private int cptRequests = 0;
    private Main.TestSuite currentSuite;
    private String[] inputFiles;
    private ArrayList<String> requests;
    private String inputRoot;
    private String outputRoot;
    private Main.TestSuite.DatabaseCreation creationMode = Main.TestSuite.DatabaseCreation.IF_NOT_EXIST;

    public FileAndRequestIterator(String[] _inputFiles, ArrayList<String> _requests) {
        inputFiles = _inputFiles;
        requests = _requests;
    }

    public FileAndRequestIterator setInputRoot(String inputRoot) {
        this.inputRoot = inputRoot;
        return this;
    }

    public FileAndRequestIterator setOutputRoot(String outputRoot) {
        this.outputRoot = outputRoot;
        return this;
    }

    public FileAndRequestIterator setCreationMode(Main.TestSuite.DatabaseCreation mode) {
        this.creationMode = mode;
        return this;
    }

    @Override
    public boolean hasNext() {
        if (inputFiles.length == 0 || requests.isEmpty()) {
            return false;
        }
        if (started) {
            return !(cptInputFiles == inputFiles.length - 1 && cptRequests == requests.size() - 1);
        } else {
            return true;
        }
    }

    @Override
    public Object[] next() {
        if (started) {
            cptRequests++;
            if (cptRequests >= requests.size()) {
                cptRequests = 0;
                cptInputFiles++;
            }
            if (cptInputFiles >= inputFiles.length) {
                throw new IllegalArgumentException("no more elements");
            }
        } else {
            started = true;
        }

        String inputFilePattern = inputFiles[cptInputFiles];
        String request = requests.get(cptRequests);

        if (cptRequests == 0) {
            // build the template of the test, ie
            // an object describing how to proceed
            // the test. Only the request is updated
            // between each test.
            currentSuite = Main.TestSuite.build("test_" + inputFilePattern).
                    setDriver(RdfToGraph.DbDriver.NEO4J).
                    setWarmupCycles(2).
                    setMeasuredCycles(5).
                    setInputFilesPattern(inputFilePattern).
                    setInputDb(GdbDriver.filePatternToDbPath(inputFilePattern)).
                    setInputRoot(inputRoot).
                    setOutputRoot(outputRoot);
            try {
                currentSuite.createDb(creationMode);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        TestDescription[] result = {currentSuite.buildTest(request)};
        return result;
    }
}
