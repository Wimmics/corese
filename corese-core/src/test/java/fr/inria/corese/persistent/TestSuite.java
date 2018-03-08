package fr.inria.corese.persistent;

import static fr.inria.corese.sparql.storage.fs.Constants.KB;
import static fr.inria.corese.sparql.storage.fs.Constants.MB;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * TestSuite.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 26 janv. 2015
 */
public class TestSuite {

    public static void main(String args[]) throws IOException {
        testSpeedInSingleFiles();
        //testCorrectness();
        //testSpeed();
    }

    private static void testSpeedInSingleFiles() throws IOException {
        int[][] Test1 = {
            {64, 512},
            {512, KB},
            {1 * KB, 16 * KB},
            {16 * KB, 64 * KB},
            {64 * KB, 128 * KB},
            {128 * KB, 256 * KB},
            {256 * KB, 512 * KB},
            {512 * KB, 1 * MB},
            {1 * MB, 2 * MB},};

        //Constants.MAX_LIT_LEN = 0;
        new TestSuite(Test1, 100, Test1.length, 256 * MB).test();
    }

    private static void testSpeed() throws IOException {
        int[][] Test1 = {
            {400 * KB, 1 * MB},
            {1 * MB, 3 * MB}
        };

        int[][] Test2 = {
            {64, 512},
            {512, KB},
            {1 * KB, 16 * KB},
            {16 * KB, 64 * KB},
            {64 * KB, 128 * KB},
            {128 * KB, 256 * KB},
            {256 * KB, 512 * KB}
        };

        new TestSuite(Test2, 1000, Test2.length).test();
//        
//        int[] buffer_size = {1 * MB, 2 * MB, 4 * MB, 8 * MB, 16 * MB, 32 * MB};
//        for (int c : buffer_size) {
//            System.out.println("\n======= " + convert(c, true) + " ========");
//            //Parameters.BUF_SIZE = c;
//            
//            new TestSuite(Test1, 100, Test1.length).test();
//        }

    }

    private static void testCorrectness() {
        Test tCorrectness = new Test(KB, 1 * MB);
        tCorrectness.testCorrectness(50, 10, true);
    }
    private final static String[] titles = {
        "Length (min):",
        "Length (max):",
        "Average Len.:",
        //"W. all (s):",
        //"W. (ms):",
        "W.nio all (s):",
        "W.nio (ms):",
        "R. all (s)",
        "R. (ms):   "
    };

    private int[][] tests;
    private int numOfTests;
    private int runTimes;
    private int fileSize = -1;

    public TestSuite(int[][] tests, int runTimes, int numOfTests) {
        this.tests = tests;
        this.runTimes = runTimes;
        this.numOfTests = numOfTests;
    }

    public TestSuite(int[][] tests, int runTimes, int numOfTests, int fileSize) {
        this(tests, runTimes, numOfTests);
        this.fileSize = fileSize;
    }

    public void test() throws IOException {
        //calculation
        double[][] rs = new double[tests.length][titles.length];
        for (int i = 0;
                i < tests.length;
                i++) {
            Test t = new Test(tests[i][0], tests[i][1]);
            if (fileSize != -1) {
                rs[i] = t.testSpeed(2 * fileSize / (tests[i][0] + tests[i][1]));
            } else {
                rs[i] = t.testSpeed(runTimes);
            }

            t.close();
        }

        print(rs);
    }

    public void print(double[][] rs) {
        String NL = "\n", TB = "\t";
        DecimalFormat df = new DecimalFormat("0.###");
        StringBuilder sb = new StringBuilder();

        //print
        for (int i = 0;
                i < titles.length;
                i++) {
            //title
            sb.append(titles[i]).append(TB);
            //results
            for (int j = 0; j < numOfTests; j++) {
                sb.append(df.format(rs[j][i])).append(TB);
            }
            sb.append(NL);

            if (i == 2) {
                String bytesAvg = "Bytes.avg:" + TB, bytesAll = "Bytes.all:" + TB;

                //results
                for (int j = 0; j < numOfTests; j++) {
                    bytesAvg += (convert((long) rs[j][2], false)) + TB;
                    bytesAll += (convert((long) (rs[j][2]) * this.runTimes, false)) + TB;
                }
                sb.append(bytesAvg + NL);
                sb.append(bytesAll + NL + NL);
            }
        }

        System.out.println(sb);
    }

    public static String convert(long bytes, boolean precise) {
        String s;
        if (bytes < KB) {
            s = bytes + "B";
        } else if (bytes < (MB)) {
            s = bytes / KB + "K ";
            s += precise ? convert(bytes % KB, true) : "~";
        } else {
            s = bytes / MB + "M ";
            s += precise ? convert(bytes % MB, true) : "~";
        }
        return s;
    }

    public static String[] texts = {"1 Definitions\n",
        "1.1 Mental faculty, organ or instinct\n",
        "1.2 Formal symbolic system\n",
        "1.3 Tool for communication\n",
        "1.4 Unique status of human language\n",
        "2 Origin\n",
        "3 The study of language\n",
        "3.1 Subdisciplines\n",
        "3.2 Early history\n",
        "3.3 Contemporary linguistics\n",
        "4 Physiological and neural architecture of language and speech\n",
        "4.1 The brain and language\n",
        "4.2 Anatomy of speech\n",
        "5 Structure\n",
        "5.1 Semantics\n",
        "5.2 Sounds and symbols\n",
        "5.3 Grammar\n",
        "5.3.1 Grammatical categories\n",
        "5.3.2 Word classes\n",
        "5.3.3 Morphology\n",
        "5.3.4 Syntax\n",
        "5.4 Typology and universals\n",
        "6 Social contexts of use and transmission\n",
        "6.1 Usage and meaning\n",
        "6.2 Language acquisition\n",
        "6.3 Language and culture\n",
        "6.4 Writing, literacy and technology\n",
        "6.5 Language change\n",
        "6.6 Language contact\n",
        "7 Linguistic diversity\n",
        "7.1 Languages and dialects\n",
        "7.2 Language families of the world\n",
        "7.3 Language endangerment\n",
        "8 See also\n",
        "9 Notes\n",
        "9.1 Commentary notes\n",
        "9.2 Citations\n",
        "10 Works Cited\n",
        "11 External links\n"};
}
