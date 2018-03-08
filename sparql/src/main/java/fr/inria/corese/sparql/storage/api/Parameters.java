package fr.inria.corese.sparql.storage.api;

import static fr.inria.corese.sparql.storage.fs.Constants.GB;
import static fr.inria.corese.sparql.storage.fs.Constants.MB;
import java.util.EnumMap;
import java.util.Map;

/**
 * Parameters for initlizing/running the storage manager
 * 
 * Parameters.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 févr. 2015
 */
public class Parameters {

    //MAX_LIT_LEN: threshold: literals that exceed the length will be saved on disk
    //MAX_FILE_SIZE: the maximum size of single file, when exceeded new file will be created
    //THRESHOLD_TO_DELETE_NB: threshod: when the accumulated number of literals to delete reach this number, perform batch delete
    //CACHED_STRING_NB: the size of string cache
    //BUF_SIZE: buffer size
    //CONNECTED_FH_NB: 
    public enum type {
        MAX_LIT_LEN, MAX_FILE_SIZE, THRESHOLD_TO_DELETE_NB, CACHED_STRING_NB, BUF_SIZE, CONNECTED_FH_NB
    };

    private final Map<type, Integer> paras;

    public Parameters() {
        paras = new EnumMap<type, Integer>(type.class);
        init();
    }

    /**
     * Initlize the default values
     */
    private void init() {
        this.add(type.BUF_SIZE, 4 * MB);
        this.add(type.MAX_LIT_LEN, 100);
        this.add(type.MAX_FILE_SIZE, 256 * MB);
        this.add(type.THRESHOLD_TO_DELETE_NB, 3000);
        this.add(type.CACHED_STRING_NB, 1000);
        this.add(type.CONNECTED_FH_NB, 6);
    }

    /**
     * Create a new isntance of parameters
     * @return 
     */
    public static Parameters create() {
        return new Parameters();
    }

    /**
     * Add parameters
     * 
     * @param key
     * @param value 
     */
    public void add(type key, int value) {

        if (key == type.MAX_FILE_SIZE && value > GB) {
            value = GB;
        }

        if (key == type.CONNECTED_FH_NB && value < 2) {
            value = 2;
        }

        if (key == type.MAX_LIT_LEN && value < 32) {
            value = 32;
        }

        paras.put(key, value);
    }

    /**
     * Get value of a given parameter type
     * 
     * @param key
     * @return 
     */
    public int get(type key) {
        if (paras.containsKey(key)) {
            return paras.get(key);
        }

        return -1;
    }
}
