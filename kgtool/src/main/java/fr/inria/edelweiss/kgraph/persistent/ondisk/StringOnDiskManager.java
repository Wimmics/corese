package fr.inria.edelweiss.kgraph.persistent.ondisk;

import fr.inria.edelweiss.kgraph.persistent.cache.LRUCache;
import fr.inria.acacia.corese.cg.datatype.persistent.IOperation;
import fr.inria.edelweiss.kgraph.persistent.cache.ICache;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.GB;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager all the strings that have been stored in file, including a cache for
 * quick access the read strings
 *
 * LiteralOnDiskManager.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015 new
 */
public final class StringOnDiskManager implements IOperation {

    private Map<Integer, StringOnDiskMeta> stringsOnDisk = null;
    private Map<Integer, StringOnDiskMeta> stringsToDelete = null;

    private static StringOnDiskManager manager = new StringOnDiskManager(false);
    private StringManagerDelegate delegate = null;
    private ICache<Integer, String> cache = null;

    private StringOnDiskManager(boolean newFileManager) {
        this.init();
        delegate = StringManagerDelegate.getInstance(newFileManager);
        cache = new LRUCache<Integer, String>(Parameters.CACHE_LITERAL_SIZE);
        stringsOnDisk = new HashMap< Integer, StringOnDiskMeta>();
        stringsToDelete = new HashMap< Integer, StringOnDiskMeta>();
    }

    public static StringOnDiskManager getInstance() {
        return manager;
    }

    public static StringOnDiskManager getInstance(boolean newInstance) {
        if (newInstance) {
            manager = new StringOnDiskManager(newInstance);
        }
        return manager;
    }

    /**
     * Add a literals to disk
     *
     * @param id
     * @param literal
     * @return
     */
    //refactor, change paramters to Node
    @Override
    public boolean write(int id, String literal) {
        StringOnDiskMeta meta = delegate.write(id, literal);
        this.stringsOnDisk.put(id, meta);
        return true;
    }

    @Override
    public String read(int id) {
        if (this.cache.containsKey(id)) {
            return this.cache.get(id);
        } else {
            StringOnDiskMeta meta = this.stringsOnDisk.get(id);
            String literal = delegate.read(meta);
            this.cache.put(id, literal);
            return delegate.read(meta);
        }
    }

    @Override
    public void delete(int nid) {
        if (this.stringsOnDisk.containsKey(nid)) {
            //1. Add the literal to ToDelete list temporarily 
            this.stringsToDelete.put(nid, this.stringsOnDisk.get(nid));

            //2. Remove from literals on disk list
            this.stringsOnDisk.remove(nid);

            //3. check if the size of ToDelete is exceed the default size
            //perform batch delete
            //the condition can be changed
            //or using a seperated thread to monitor and execute the deletion
            //to be refined
            if (this.stringsToDelete.size() == Parameters.THRESHOLD_TO_DELETE_NB) {
                //todo
                //clear cache
                delegate.delete(stringsToDelete, stringsOnDisk);
            }
        }
    }

    @Override
    public boolean check(int length) {
        return length >= Parameters.MAX_LIT_LEN;
    }

    public int getLiteralsOnDiskSize() {
        return this.stringsOnDisk.size();
    }

    public StringOnDiskMeta getLiteralsOnDiskMeta(int id) {
        return this.stringsOnDisk.get(id);
    }

    @Override
    public void init() {
        Parameters.MAX_FILE_SIZE = Parameters.MAX_FILE_SIZE > 2L * GB ? 2L * GB : Parameters.MAX_FILE_SIZE;
        Parameters.CACHE_FILE_SIZE = Parameters.CACHE_FILE_SIZE > 0 ? Parameters.CACHE_FILE_SIZE : 1;
    }

    @Override
    public void close() {
        cache.clear();
        delegate.clean();
    }

    @Override
    public String toString() {
        return "[Manager]: " + stringsOnDisk.size() + " records on disk " + (stringsToDelete.size()) + " to delete \n" + this.delegate;
    }

    @Override
    public int getStorageType() {
        return IOperation.STORAGE_FILE;
    }

    /**
     * Load meta data from xml file to RAM
     */
    public void load() {
        //to be extended
    }

    /**
     * Save the meta data and file information to file, for reusing next time
     */
    public void save() {

    }
}
