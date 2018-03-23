package fr.inria.corese.sparql.storage.fs;

import fr.inria.corese.sparql.storage.api.Parameters;
import fr.inria.corese.sparql.storage.api.Parameters.type;
import fr.inria.corese.sparql.storage.cache.LRUCache;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;

/**
 * Manager all the files and file handlers, including a poop of handlers for
 * quick access to disk file
 *
 * FileHandlerCache.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 3 févr. 2015
 */
public class FileHandlersManager extends LRUCache<Integer, FileHandler> {

    private final Map<Integer, String> allFiles;
    private int counter = 1;
    private FileHandler fhForWrite;
    private final Parameters params;

    public FileHandlersManager(Parameters params) {
        super(params.get(type.CONNECTED_FH_NB));
        this.params = params;
        this.allFiles = new HashMap<Integer, String>();
    }

    @Override
    public boolean removeEldestEntry(Entry eldest) {
        boolean remove = super.removeEldestEntry(eldest);
        FileHandler fh = (FileHandler) eldest.getValue();
        //dont remove if it is the hander for file writing
        if (fhForWrite != null && fh.getFid() == fhForWrite.getFid()) {
            return false;
        } else if (remove) {
            //close all the connections of this handler before remove the entry
            fh.close();
        }
        return remove;
    }

    @Override
    public void clear() {
        counter = 0;
        allFiles.clear();
        for (FileHandler v : this.values()) {
            v.close();
        }
        super.clear();
        createNewFile();//create a file handler for writing
    }

    @Override
    public FileHandler put(Integer key, FileHandler value) {
        allFiles.put(key, value.getFname());
        return super.put(key, value);
    }

    @Override
    public FileHandler get(Object key) {
        if (key == null || !(key instanceof Integer)) {
            return fhForWrite;
        }
        int fid = (Integer) key;

        if (this.containsKey(fid)) { //fh in the pool
            return super.get(fid);

        } else if (allFiles.containsKey(fid)) { //not in the pool, but in the maintenence list
            return this.getFileHandler(fid, allFiles.get(fid));
        } else {//not anywhere
            LoggerFactory.getLogger(FileHandlersManager.class.getName()).warn("File handler id [{}]  not yet initialized!", fid);
            return null;
        }
    }

    /**
     * Get the handler of file which is ready for writing
     *
     * @return
     */
    public FileHandler get() {
        return this.get(null);
    }

    /**
     * create a new file and file handler
     *
     * @return
     */
    public final FileHandler createNewFile() {
        FileHandler fhNew = this.getFileHandler(counter++, null);
        fhForWrite = fhNew;
        return fhForWrite;
    }

    /**
     * Get a file handler that not existing in the pool with existing file
     *
     * @param fid
     * @param file
     * @return
     */
    private FileHandler getFileHandler(int fid, String file) {
        FileHandler fh = new FileHandler(fid, file, this.params);
        this.put(fid, fh);
        return fh;
    }

    @Override
    public String toString() {
        String s = "[FH Manager]: [" + allFiles.size() + " files, " + size() + " in cache ]\n";
        for (Entry<Integer, String> entrySet : allFiles.entrySet()) {
            Integer id = entrySet.getKey();
            String path = entrySet.getValue();
            s += "\tFid:" + id + ", " + path;
            if (this.containsKey(id)) {
                s += " [cached]";
            }

            if (this.fhForWrite.getFid() == id) {
                s += " [for writing]";
            }

            s += "\n";
        }
        return s;
    }
}
