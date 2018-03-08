package fr.inria.corese.sparql.storage.api;

/**
 *
 * Interface that needs to be implemented for persistenting literal/string
 * 
 * @author Fuqi Song, WImmics Inria I3S
 * @date 13 janv. 2015
 */
public interface IStorage {

    //public final static int STORAGE_RAM = 10;
    public final static int STORAGE_DB = 20;
    public final static int STORAGE_FILE = 30;

    /**
     * Write the string to persistent storage
     *
     * @param id
     * @param literal
     * @return 
     */
    public boolean write(int id, String literal);

    /**
     * Read the string by id
     *
     * @param id
     * @return
     */
    public String read(int id);

    /**
     * Delete the string by its id
     *
     * @param id
     */
    public void delete(int id);

    /**
     * Check if the current manager can be used
     *
     * @param str
     * @return
     */
    public boolean check(String str);
    public boolean check(int length);

    /**
     * Get the status of manager
     * 
     * @return 
     */
    public boolean enabled();

    /**
     * Enable or distable the manager
     * 
     * @param enabled 
     */
    public void enable(boolean enabled);

    /**
     * Return the type of storage that current manager manages
     *
     * @return
     */
    public int getStorageType();

    /**
     * Initialization, ex, setup connection, set parameters, etc
     */
    public void init();

    /**
     * Clean up, ex, close connections
     */
    public void clean();
  
    
}
