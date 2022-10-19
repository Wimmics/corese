package fr.inria.corese.core.storage.api.dataManager;

/**
 * Builder for DataManager
 */
public interface DataManagerBuilder {

    /**
     * Creates a new DataManager associated to the current configuration.
     * 
     * @return a new DataManager associated to the current configuration.
     */
    public DataManager build();
}