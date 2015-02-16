package fr.inria.acacia.corese.persistent.ondisk;

/**
 * Constants
 *
 * Parameters.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015 new
 */
public class Constants {
    public final static int BEGIN = 0;
    public final static int KB = 1024;
    public final static int MB = KB * KB;
    public final static int GB = KB * MB;

    public final static String FILE_NAME = "CORESE_";
    public final static String EXT_TXT = ".txt";
    public final static String ENCODING = "UTF-8";
    public final static String CHANNEL_MODE = "rw";
    
    //if save the strings to file and persistent
    //false: when system exit, all files and infor will be deleted
    //true: files will be kept after exiting the system
    protected static boolean PER_PERSISTENT = false;
}
