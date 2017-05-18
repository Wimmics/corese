package fr.inria.wimmics.coresedb.benchmark.auto;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author edemairy
 */
public class DatabaseBuilder implements Runnable {

	@Override
	public void run() {
		String java = System.getProperty("java.home") + "/bin/java";
		URL[] classpath = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
//		Stream<String> classpathStrings = Arrays.stream(classpath).
//			map(URL::toString).
//			collect(Collectors.joining(File.pathSeparator));
//		String.join(classpathStrings.toArray(), File.pathSeparatorChar);
//		ProcessBuilder builder = new ProcessBuilder(java, "-classpath", classpath.join(File.pathSeparatorChar));
	}
}
