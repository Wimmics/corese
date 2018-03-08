package fr.inria.corese.sparql.storage.fs;

import fr.inria.corese.sparql.storage.cache.LRUCache;
import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.storage.api.Parameters;
import fr.inria.corese.sparql.storage.api.Parameters.type;
import fr.inria.corese.sparql.storage.cache.ICache;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager all the strings that have been stored in file, including a cache for
 * quick access the read strings
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015 new
 */
public final class StringManager implements IStorage {

    private boolean enabled = true;
    private Map<Integer, StringMeta> stringsOnDisk = null;
    private Map<Integer, StringMeta> stringsToDelete = null;

    private StringManagerDelegate delegate = null;
    private ICache<Integer, String> cache = null;
    private Parameters params = null;

    private StringManager(Parameters params) {
        this.init();
        this.params = (params == null) ? Parameters.create() : params;

        delegate = new StringManagerDelegate(this.params);
        cache = new LRUCache<Integer, String>(this.params.get(type.CACHED_STRING_NB));
        stringsOnDisk = new HashMap< Integer, StringMeta>();
        stringsToDelete = new HashMap< Integer, StringMeta>();
    }

    /**
     * Create storage manager usign default parameters
     *
     * @return
     */
    public final static StringManager create() {
        return create(null);
    }

    /**
     * Create storage manager using specified parameters
     *
     * @param params
     * @return
     */
    public final static StringManager create(Parameters params) {
        return new StringManager(params);
    }

    /**
     * Add a literals to disk
     *
     * @param id
     * @param literal
     * @return
     */
    @Override
    public final synchronized boolean write(int id, String literal) {
        StringMeta meta = delegate.write(id, literal);
        this.stringsOnDisk.put(id, meta);
        return true;
    }

    @Override
    public final String read(int id) {
        if (this.cache.containsKey(id)) {
            return this.cache.get(id);
        } else if (this.stringsOnDisk.containsKey(id)) {
            StringMeta meta = this.stringsOnDisk.get(id);
            String literal = delegate.read(meta);
            if (literal != null) {
                this.cache.put(id, literal);
            }
            return literal;
        } else {
            return null;
        }
    }

    @Override
    public final synchronized void delete(int nid) {
        if (this.stringsOnDisk.containsKey(nid)) {
            //1. Add the literal to ToDelete list temporarily 
            this.stringsToDelete.put(nid, this.stringsOnDisk.get(nid));

            //2. Remove from literals on disk list
            this.stringsOnDisk.remove(nid);

            //3. check if the size of ToDelete is exceed the default size
            //perform batch delete
            if (this.stringsToDelete.size() == this.params.get(type.THRESHOLD_TO_DELETE_NB)) {
                delegate.delete(stringsToDelete, stringsOnDisk);
                stringsToDelete.clear();
            }
        }
    }

    @Override
    public boolean check(String str) {
        return (str == null) ? false : check(str.length());
    }

    @Override
    public boolean check(int length) {
        return length > this.params.get(type.MAX_LIT_LEN);
    }

//    public int getLiteralsOnDiskSize() {
//        return this.stringsOnDisk.size();
//    }
//
//    public StringMeta getLiteralsOnDiskMeta(int id) {
//        return this.stringsOnDisk.get(id);
//    }

    @Override
    public void init() {

    }

    @Override
    public void clean() {
        stringsToDelete.clear();
        stringsOnDisk.clear();
        cache.clear();
        delegate.clean();
    }

    @Override
    public String toString() {
        return "[Manager]: " + stringsOnDisk.size() + " records on disk, " + cache.size() + " in cache, " + (stringsToDelete.size()) + " to delete \n" + this.delegate;
    }

    @Override
    public int getStorageType() {
        return IStorage.STORAGE_FILE;
    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public void enable(boolean enable) {
        this.enabled = enable;
    }
}
