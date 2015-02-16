package fr.inria.acacia.corese.persistent.ondisk;

import fr.inria.acacia.corese.persistent.api.Parameters;
import fr.inria.acacia.corese.persistent.api.Parameters.type;
import fr.inria.acacia.corese.persistent.cache.LRUCache;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
        //fh in the pool
        if (this.containsKey(fid)) {
            return super.get(fid);
            //not in the pool, but in the maintenence list
        } else if (allFiles.containsKey(fid)) {
            return this.getFileHandler(fid, allFiles.get(fid));
            //not anywhere
        } else {
            System.out.println("File handler [id" + fid + "] not yet initialized!");
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
        System.out.println("[FH manager]: new file " + fhForWrite.getFid() + " created");
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
