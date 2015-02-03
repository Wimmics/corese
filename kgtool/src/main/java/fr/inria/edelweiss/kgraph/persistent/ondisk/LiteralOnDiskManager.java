package fr.inria.edelweiss.kgraph.persistent.ondisk;

import fr.inria.acacia.corese.cg.datatype.persistent.IOperation;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.GB;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LiteralOnDiskManager.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015 new
 */
public final class LiteralOnDiskManager implements IOperation {

    private Map<Integer, LiteralOnDiskMeta> literalsOnDisk = null;
    private Map<Integer, LiteralOnDiskMeta> literalsToDelete = null;

    private static LiteralOnDiskManager manager = new LiteralOnDiskManager(false);
    private DiskFileManager fileManager = null;
    private LRUCache<Integer, String> cache = null;

    private LiteralOnDiskManager(boolean newFileManager) {
        init();
        fileManager = DiskFileManager.getInstance(null, newFileManager);
        cache = new LRUCache<Integer, String>(Parameters.CACHE_LITERAL_SIZE);
        literalsOnDisk = new HashMap< Integer, LiteralOnDiskMeta>();
        literalsToDelete = new HashMap< Integer, LiteralOnDiskMeta>();
    }

    public static LiteralOnDiskManager getInstance() {
        return manager;
    }

    public static LiteralOnDiskManager getInstance(boolean newInstance) {
        if (newInstance) {
            manager = new LiteralOnDiskManager(newInstance);
        }
        return manager;
    }

    /**
     * Add a literals to disk
     *
     * @param id
     * @param literal
     */
    //refactor, change paramters to Node
    @Override
    public void write(int id, String literal) {
        try {
            LiteralOnDiskMeta meta = fileManager.write(id, literal);
            //System.out.println(meta);
            this.cache.put(id, literal);
            this.literalsOnDisk.put(id, meta);
        } catch (IOException ex) {
            Logger.getLogger(LiteralOnDiskManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String read(int id) {
        try {
            LiteralOnDiskMeta meta = this.literalsOnDisk.get(id);
            //System.out.println(meta);
            if (this.cache.containsKey(id)) {
                return this.cache.get(id);
            } else {
                return fileManager.read(meta);
            }

        } catch (IOException ex) {
            Logger.getLogger(LiteralOnDiskManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void delete(int nid) {
        if (this.literalsOnDisk.containsKey(nid)) {
            //1. Add the literal to ToDelete list temporarily 
            this.literalsToDelete.put(nid, this.literalsOnDisk.get(nid));

            //2. Remove from literals on disk list
            this.literalsOnDisk.remove(nid);

            //3. check if the size of ToDelete is exceed the default size
            //perform batch delete
            //the condition can be changed
            //or using a seperated thread to monitor and execute the deletion
            //to be refined
            if (this.literalsToDelete.size() == Parameters.THRESHOLD_TO_DELETE_NB) {
                //todo
                //clear cache
                fileManager.delete(literalsToDelete, literalsOnDisk);
            }
        }
    }

    @Override
    public boolean threshold(int length) {
        return length >= Parameters.MAX_LIT_LEN;
    }

    public int getLiteralsOnDiskSize() {
        return this.literalsOnDisk.size();
    }

    public LiteralOnDiskMeta getLiteralsOnDiskMeta(int id) {
        return this.literalsOnDisk.get(id);
    }

    @Override
    public void init() {
        Parameters.MAX_FILE_SIZE = Parameters.MAX_FILE_SIZE > 2 * GB ? 2 * GB : Parameters.MAX_FILE_SIZE;
    }

    @Override
    public void close() {
        cache.clear();
        fileManager.closeConnection();
    }

    @Override
    public String toString() {
        return "[Manager]: " + literalsOnDisk.size() + " records on disk " + (literalsToDelete.size()) + " to delete \n";
    }

    @Override
    public int storage(String literal) {
        if (literal.length() > Parameters.MAX_LIT_LEN) {
            return IOperation.STORAGE_FILE;
        } else {
            return IOperation.STORAGE_RAM;
        }
    }
}
