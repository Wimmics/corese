package fr.inria.acacia.corese.persistent.ondisk;

import fr.inria.acacia.corese.persistent.cache.LRUCache;
import fr.inria.acacia.corese.persistent.api.IOperation;
import fr.inria.acacia.corese.persistent.api.Parameters;
import fr.inria.acacia.corese.persistent.api.Parameters.type;
import fr.inria.acacia.corese.persistent.cache.ICache;
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

    private boolean enabled = true;
    private Map<Integer, StringOnDiskMeta> stringsOnDisk = null;
    private Map<Integer, StringOnDiskMeta> stringsToDelete = null;

    private static StringOnDiskManager manager ;
    private StringManagerDelegate delegate = null;
    private ICache<Integer, String> cache = null;
    private Parameters params = null;

    private StringOnDiskManager() {
        this(null);
    }

    private StringOnDiskManager(Parameters params) {
        this.init();
        if (params == null) {
            params = Parameters.create();
        }

        this.params = params;

        delegate = new StringManagerDelegate(this.params);
        cache = new LRUCache<Integer, String>(this.params.get(type.CACHED_STRING_NB));
        stringsOnDisk = new HashMap< Integer, StringOnDiskMeta>();
        stringsToDelete = new HashMap< Integer, StringOnDiskMeta>();
    }

//    public static StringOnDiskManager getInstance() {
//        return manager;
//    }

    public static StringOnDiskManager create() {
        return create(null);
    }

    public static StringOnDiskManager create(Parameters params) {
        manager = new StringOnDiskManager(params);
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
    public synchronized boolean write(int id, String literal) {
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
    public synchronized void delete(int nid) {
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
            if (this.stringsToDelete.size() == this.params.get(type.THRESHOLD_TO_DELETE_NB)) {
                //todo
                //clear cache
                delegate.delete(stringsToDelete, stringsOnDisk);
            }
        }
    }

    @Override
    public boolean check(String str) {
        return str == null ? false : str.length() > this.params.get(type.MAX_LIT_LEN);
    }

    public int getLiteralsOnDiskSize() {
        return this.stringsOnDisk.size();
    }

    public StringOnDiskMeta getLiteralsOnDiskMeta(int id) {
        return this.stringsOnDisk.get(id);
    }

    @Override
    public void init() {

    }

    @Override
    public void clean() {
        cache.clear();
        delegate.clean();
    }

    @Override
    public String toString() {
        return "[Manager]: " + stringsOnDisk.size() + " records on disk, " + cache.size() + " in cache, " + (stringsToDelete.size()) + " to delete \n" + this.delegate;
    }

    @Override
    public int getStorageType() {
        return IOperation.STORAGE_FILE;
    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public void enable(boolean enable) {
        this.enabled = enable;
    }

    /**
     * Load meta data from xml file to RAM
     * @param meta
     */
    @Override
    public void load(String meta) {
        //to be extended
    }

    /**
     * Save the meta data and file information to file, for reusing next time
     * @param dir
     */
    @Override
    public void export(String dir) {
        //meta.rdf
        //data 
        // - file1.txt
        // - file2.txt
        //...
    }

}
