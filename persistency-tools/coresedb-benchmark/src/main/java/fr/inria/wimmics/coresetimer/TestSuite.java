package fr.inria.wimmics.coresetimer;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.edelweiss.kgtool.load.LoadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.inria.corese.coresetimer.utils.VariousUtils.ensureEndWith;

public class TestSuite {
    private static final Logger logger = LogManager.getLogger(TestSuite.class.getName());
    private final String OUTPUT_FILE_FORMAT = "%s/result_%s.xml";
    private String inputMem;
    private RdfToGraph.DbDriver driver = RdfToGraph.DbDriver.TITANDB;
    private int defaultWarmupCycles = 5;
    private int defaultMeasuredCycles = 20;
    private String inputRoot;
    private String outputRoot;
    private String testId;
    private String databasePath;
    private RDFFormat format;
    private String outputFilename;
    private CoreseTimer memoryTimer;
    private CoreseTimer memoryDatabase;

    private TestSuite(String id) {
        this.testId = id;
    }

    static public TestSuite build(String id) {
        return new TestSuite(id);
    }


    public String getId() {
        return testId;
    }

    public TestSuite setWarmupCycles(int warmup) {
        this.defaultWarmupCycles = warmup;
        return this;
    }

    public TestSuite setMeasuredCycles(int warmup) {
        this.defaultMeasuredCycles = warmup;
        return this;
    }

    public TestSuite setInputFilesPattern(String input, RDFFormat format) {
        this.inputMem = input;
        this.format = format;
        databasePath = input.replaceFirst("\\..+", "_db");
        return this;
    }

    private String getInputFilesPattern() {
        return inputRoot + inputMem;
    }

    public TestSuite setInputFilesPattern(String input) {
        return this.setInputFilesPattern(input, Rio.getParserFormatForFileName(input).orElse(RDFFormat.NQUADS));
    }

    public TestSuite setDriver(RdfToGraph.DbDriver driver) {
        this.driver = driver;
        return this;
    }

    public String getInputRoot() {
        return inputRoot;
    }

    public TestSuite setInputRoot(String path) {
        inputRoot = ensureEndWith(path, "/");
        return this;
    }

    public String getOutputFile() {
        return this.outputFilename;
    }

    public TestSuite setOutputFile(String filename) {
        this.outputFilename = filename;
        return this;
    }

    public String getOutputRoot() {
        return outputRoot;
    }

    @Deprecated
    public TestSuite setOutputRoot(String path) {
        outputRoot = ensureEndWith(path, "/");
        return this;
    }

    public TestSuite createDatabase(DatabaseCreation mode) throws Exception {
        String databasePath = getDatabasePath();
        if (Files.exists(Paths.get(databasePath)) && (mode == DatabaseCreation.IF_NOT_EXIST)) {
            logger.info("Not creating database since it already exists at {}", databasePath);
        } else {
            logger.info("Creating database at {}", databasePath);
            RdfToGraph.build().setDriver(driver).convertFileToDb(getInputFilesPattern(), format, getDatabasePath());
        }
        return this;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    /**
     * Set the path to the db.
     *
     * @param path
     * @return
     * @see
     */
    public TestSuite setDatabasePath(String path) {
        this.databasePath = path;
        return this;
    }

    public RDFFormat getFormat() {
        return format;
    }

    public TestSuite setFormat(RDFFormat newFormat) {
        format = newFormat;
        return this;
    }

    public TestDescription buildTest(String request) {
        return buildTest(request, "");
    }

    public TestDescription buildTest(String request, String idSuffix) {
        String testId = String.format("%s_%s_%s", this.testId, idSuffix, Integer.toHexString(request.hashCode()));
        TestDescription newTest = TestDescription.build(testId, this)
                .setMeasuredCycles(defaultMeasuredCycles)
                .setWarmupCycles(defaultWarmupCycles)
                .setInputFilesPattern(getInputFilesPattern())
                .setOutputPath(String.format(OUTPUT_FILE_FORMAT, getOutputRoot(), testId + "_%s"))
                .setRequest(request)
                .setInputDb(getDatabasePath());
        return newTest;

    }

    public CoreseTimer getMemoryTimer(TestDescription test) throws ClassNotFoundException, IOException, InstantiationException, LoadException, IllegalAccessException {
        if (memoryTimer == null) {
            memoryTimer = CoreseTimer.build(test).setMode(CoreseTimer.Profile.MEMORY).init().load();
        } else {
            memoryTimer.setTest(test);
        }
        return memoryTimer;
    }

    public CoreseTimer getDatabaseTimer(TestDescription test) throws ClassNotFoundException, IOException, InstantiationException, LoadException, IllegalAccessException {
        if (memoryDatabase == null) {
            memoryDatabase = CoreseTimer.build(test).setMode(CoreseTimer.Profile.DB).init().load();
        } else {
            memoryDatabase.setTest(test);
        }
        return memoryDatabase;
    }

    public enum DatabaseCreation {
        IF_NOT_EXIST,
        ALWAYS
    }

}
