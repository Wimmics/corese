package fr.inria.edelweiss.kgraph.persistent.ondisk;

/**
 * Manage all the paramters
 * 
 * Parameters.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015 new
 */
public class Parameters {

    public final static int BEGIN = 0;
    //public final static int B = 1;
    public final static int KB = 1024;
    public final static int MB = KB * KB;
    public final static int GB = KB * MB;

    //threshold: literals that exceed the length will be saved on disk
    public static int MAX_LIT_LEN = 0;//
    //literals that are greater then this length will be saved to signle files
    //should be greater than MAX_LIT_LEN
    //public static int MAX_LIT_LEN_InFile = 512 * MB;//

    public static long MAX_FILE_SIZE = 512 * MB;

    //threshod: when the accumulated number of literals to delete reach this number
    //perform batch delete
    public static int THRESHOLD_TO_DELETE_NB = 300;
    public static int THRESHOLD_TO_DELETE_SIZE = 256 * MB;
    public static int CACHE_LITERAL_SIZE = 100;

    public final static String MODE = "rw";

    //buffer size
    //!! has to be greater than MAX_LIT_LEN_InFile
    public static int BUF_SIZE = 4 * MB;
    public static int CACHE_FILE_SIZE = 4;
    
    //if save the strings to file and persistent
    //false: when system exit, all files and infor will be deleted
    //true: files will be kept after exiting the system
    public static boolean PERSISTENT = false;

    //number of buffers, currently only support 1
    //public static int BUF_NUM = 1;
    public final static String FILE_NAME = "LIT_CORESE_";
    public final static String EXT = ".lit";
    public final static String EXT_TXT = ".txt";
    public final static String ENCODING = "UTF-8";
}
