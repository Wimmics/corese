package test.db_memory;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.wimmics.coresetimer.TestDescription;
import fr.inria.wimmics.coresetimer.TestSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class FileAndRequestIterator implements Iterator<Object[]> {
    private boolean started = false;
    private int cptInputFiles = 0;
    private int cptRequests = 0;
    private TestSuite currentSuite;
    private String[] inputFiles;
    private ArrayList<String> requests;
    private ArrayList<String> requestsName;
    private String inputRoot;
    private String outputRoot;
    private TestSuite.DatabaseCreation creationMode = TestSuite.DatabaseCreation.IF_NOT_EXIST;
    private TestDescription.ResultStrategy resultResultStrategy;

    public FileAndRequestIterator(String[] _inputFiles, ArrayList<String> _requests) {
        inputFiles = _inputFiles;
        requests = _requests;
        requestsName = new ArrayList<>(_requests.size());
        _requests.forEach((String s) ->
                requestsName.add("")
        );
    }

    public FileAndRequestIterator(String[] _inputFiles, String requestFilesPattern) {
        ArrayList<String> requests = new ArrayList<String>();
        ArrayList<String> requestsName = new ArrayList<String>();
        for (File f : searchFiles("./src/test/resources/requests/db/dbpedia/.*\\.rq")) {
            requestsName.add(f.getName());
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    sb.append(currentLine);
                    sb.append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            requests.add(sb.toString());
        }


        inputFiles = _inputFiles;
        this.requests = requests;
        this.requestsName = requestsName;
        assert(requests.size() == requestsName.size());
    }

    public FileAndRequestIterator setInputRoot(String inputRoot) {
        this.inputRoot = inputRoot;
        return this;
    }

    public FileAndRequestIterator setOutputRoot(String outputRoot) {
        this.outputRoot = outputRoot;
        return this;
    }

    public FileAndRequestIterator setCreationMode(TestSuite.DatabaseCreation mode) {
        this.creationMode = mode;
        return this;
    }

    public FileAndRequestIterator setResultResultStrategy(TestDescription.ResultStrategy resultStrategy) {
        this.resultResultStrategy = resultStrategy;
        return this;
    }

    @Override
    public boolean hasNext() {
        if (inputFiles.length == 0 || requests.isEmpty()) {
            return false;
        }
        return !started || !(cptInputFiles == inputFiles.length - 1 && cptRequests == requests.size() - 1);
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
            currentSuite = TestSuite.build("test_" + inputFilePattern).
                    setDriver(RdfToGraph.DbDriver.NEO4J).
                    setWarmupCycles(2).
                    setMeasuredCycles(5).
                    setInputFilesPattern(inputFilePattern).
                    setDatabasePath(GdbDriver.filePatternToDbPath(inputFilePattern)).
                    setInputRoot(inputRoot).
                    setResultStrategy(resultResultStrategy).
                    setOutputRoot(outputRoot);
            try {
                currentSuite.createDatabase(creationMode);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        TestDescription[] result = {currentSuite.buildTest(request, requestsName.get(cptRequests))};
        return result;
    }

    private File[] searchFiles(String pattern) {
        File root;
        String searchedFilename;
        if (pattern.contains("/")) {
            root = new File(pattern.substring(0, pattern.lastIndexOf("/") + 1));
            searchedFilename = pattern.substring(pattern.lastIndexOf("/") + 1, pattern.length());
        } else {
            root = new File("."); // filename without path
            searchedFilename = pattern;
        }
        File[] result = root.listFiles((File dir, String name) -> {
                    System.out.println(name);
                    return name.matches(searchedFilename);
                }
        );
        return result;
    }
}
