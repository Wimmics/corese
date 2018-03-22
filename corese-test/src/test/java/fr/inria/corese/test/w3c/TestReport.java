package fr.inria.corese.test.w3c;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.inria.corese.test.w3c.model.TestCase;
import fr.inria.corese.test.w3c.model.TestCaseSet;

/**
 * Helper class for generating report of test results
 *
 * @author Fuqi Song Wimmics inria i3s
 * @date Feb. 2014
 */
public class TestReport {

    /**
     * Generate EARL report
     *
     * @param ts test suite
     * @param base base URI of test
     * @return String form of EARL report
     */
    public static String toEarl(TestCaseSet ts, String base) {
        Earl earl = new Earl();
        for (TestCase tc : ts.getTests()) {
            if (!tc.isTested()) {
                earl.skip(base + "#" + tc.getName());
            } else {
                earl.define(base + "#" + tc.getName(), tc.isPassed());
            }
        }

        return earl.toString();
    }

    /**
     * Generate the report in HTML format and write to file
     *
     * @param ts Test suite
     * @param file File name
     * @param head Title of the html page
     */
    public static void toHtml(TestCaseSet ts, String file, String head) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head>");
        sb.append("<title>Corese 3.0/KGRAM " + head + "</title>");

        sb.append("<style type = 'text/css'>");
        sb.append(".success   {background:lightgreen}");
        sb.append("body {font-family: Verdana, Arial, Helvetica, Geneva, sans-serif}");
        sb.append("</style>");
        sb.append("<link rel='stylesheet' href='kgram.css' type='text/css'  />");
        sb.append("</head><body>");
        sb.append("<h2>Corese 3.0 KGRAM  " + head + "</h2>");
        sb.append("<p> Olivier Corby, Fuqi Song - Wimmics - INRIA I3S</p>");
        sb.append("<p>" + new Date() + " - Corese 3.0 <a href='http://wimmics.inria.fr/corese'>homepage</a></p>");
        sb.append("<table border='1'>");
        sb.append("<tr>");
        sb.append("<th/> <th>test</th><th>success</th><th>failure</th><th>ratio</th>");
        sb.append("</tr>");
        int[] rs = stats(ts.getTests());
        sb.append("<th/> <th>total</th><th>" + rs[0] + "</th><th>" + rs[1] + "</th><th>"
                + (100 * rs[0]) / (ts.size() == 0 ? 1 : ts.size()) + "%</th>");
        int i = 1;

        for (Map.Entry<String, List<TestCase>> e : ts.classify().entrySet()) {
            String title = e.getKey();
            int[] rs2 = stats(e.getValue());

            int suc = rs2[0];
            int fail = rs2[1];

            String att = "";
            if (fail == 0 && suc != 0) {
                att = " class='success'";
            }
            sb.append("<tr" + att + ">");
            sb.append("<th>" + i++ + "</th>");
            sb.append("<th>" + title + "</th>");
            sb.append("<td>" + suc + "</td>");
            sb.append("<td>" + fail + "</td>");

            int ratio = 0;
            try {
                ratio = 100 * suc / (suc + fail);
            } catch (Exception ex) {
            }
            sb.append("<td>" + ratio + "%</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");
        int j = 0, k = 1;
        if (rs[1] > 0) {
            sb.append("<h2>Failure</h2>");
            sb.append(statsString(ts, true, false, false, true).replace("\n", "<br>"));
        }
        sb.append("</body><html>");

        toFile(file, sb.toString());
    }

    /**
     * Write text to file
     *
     * @param file file name
     * @param text text that needs to write
     */
    public static void toFile(String file, String text) {
        File f = new File(file);
        try {
            FileWriter w = new FileWriter(f);
            BufferedWriter b = new BufferedWriter(w);
            b.write(text);
            b.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a statistics of test results
     *
     * @param ts Test case set
     * @param pFailed If print failed test
     * @param pPassed If print passed test
     * @param pNotTested If print skipped test
     * @param html If return string in html format
     * @return Statics string
     */
    public static String statsString(TestCaseSet ts, boolean pFailed, boolean pPassed, boolean pNotTested, boolean html) {
        StringBuilder sb = new StringBuilder();

        StringBuilder sbPassed = new StringBuilder();
        StringBuilder sbFailed = new StringBuilder();
        StringBuilder sbNotTested = new StringBuilder();

        int passed = 0, failed = 0, na = 0;
        for (TestCase tc : ts.getTests()) {
            String toString = html ? tc.toHtmlString() : tc.toString();
            if (tc.isTested()) {
                if (tc.isPassed()) {
                    sbPassed.append("[" + (++passed) + "]: " + toString);
                } else {
                    sbFailed.append("[" + (++failed) + "]: " + toString);
                }
            } else {
                sbNotTested.append("[" + (++na) + "]: " + toString);
            }
        }

        String info = "************\n" + ts.getName() + "[" + ts.getUri() + "]\n";
        info += "[Total:" + ts.size() + ", skipped:" + na + "]\n";
        int total = passed + failed;
        total = total == 0 ? 1 : total;
        info += "[Passed:" + passed + ", failed:" + failed + ", ratio:" + 100 * passed / (total) + "]\n\n";
        //todo
        //add info about each type
        sb.append(info);
        if (pFailed) {
            sb.append("------Failed tests-------\n" + sbFailed);
        }
        if (pNotTested) {
            sb.append("\n------Skipped tests-------\n" + sbNotTested);
        }
        if (pPassed) {
            sb.append("\n------Passed tests-------\n" + sbPassed);
        }
        sb.append("*************");
        return sb.toString();
    }

    /**
     * Calculate the number of failed(passed, skipped) tests
     *
     * @param list
     * @return an array with 3 elements [passed, failed, skipped]
     */
    public static int[] stats(List<TestCase> list) {
        int passed = 0, failed = 0, skipped = 0;
        for (TestCase tc : list) {

            if (tc.isTested()) {
                if (tc.isPassed()) {
                    passed++;
                } else {
                    failed++;
                }
            } else {
                skipped++;
            }
        }
        return new int[]{passed, failed, skipped};
    }
}
