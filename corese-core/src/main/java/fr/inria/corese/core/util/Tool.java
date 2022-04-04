package fr.inria.corese.core.util;

import java.util.Date;

/**
 *
 */
public class Tool {
    
    private static final double MEGABYTE = 1024L * 1024L;

    public static double getMemoryUsageMegabytes() {
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory_bytes = runtime.totalMemory() - runtime.freeMemory();

        return bytesToMegabytes(memory_bytes);
    }


    public static double bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }
    
    public static double time(Date d1, Date d2) {
        return (d2.getTime() - d1.getTime()) / 1000.0;
    }
    
    public static double time(Date d1) {
        return time(d1, new Date());
    }
    
    public static void display(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
    }
    
    public static void trace(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
    }
}
