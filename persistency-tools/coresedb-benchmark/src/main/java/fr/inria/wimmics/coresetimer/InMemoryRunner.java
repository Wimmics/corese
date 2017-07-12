/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.wimmics.coresetimer.Main.TestSuite;

import java.io.IOException;

/**
 * @author edemairy
 */
public class InMemoryRunner {
    public static void main(String... args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, LoadException {
        String inputFile = args[0];
        String testName = args[1];
        String prefixOutputFilename = args[2];
        TestDescription test = TestSuite.build("runner").setWarmupCycles(2).setMeasuredCycles(5).buildTest(testName);
        test.setInputFilesPattern(inputFile).setOutputPath(prefixOutputFilename);
        CoreseTimer timer = CoreseTimer.build(test).setMode(CoreseTimer.Profile.MEMORY).init().run();
        timer.writeResults();
        timer.writeStatistics();
    }
}
