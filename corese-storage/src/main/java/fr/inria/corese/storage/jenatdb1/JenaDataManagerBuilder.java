package fr.inria.corese.storage.jenatdb1;

import org.apache.jena.query.Dataset;

import fr.inria.corese.core.storage.api.dataManager.DataManagerBuilder;

/**
 * Builder for JenaDataManager.
 */
public class JenaDataManagerBuilder implements DataManagerBuilder {

    //////////////////////////
    // Mandatory parameters //
    //////////////////////////

    /////////////////////////
    // Optional parameters //
    /////////////////////////

    private String storagePath;
    private boolean defStoragePath = false;

    private Dataset dataset;
    private boolean defDataset = false;

    //////////////////
    // Constructors //
    //////////////////

    /**
     * Create a JenaDataManagerBuilder.
     */
    public JenaDataManagerBuilder() {
    }

    ////////////
    // Setter //
    ////////////

    /**
     * Define a storage path.
     * 
     * @param storagePath Path of the directory where the data is stored.
     * @return this instance.
     */
    public JenaDataManagerBuilder storagePath(String storagePath) {
        this.storagePath = storagePath;
        this.defStoragePath = true;
        return this;
    }

    /**
     * Build the dataManager from an existing Jena Dataset.
     * 
     * @param dataset Jena dataset.
     * @return this instance.
     */
    public JenaDataManagerBuilder dataset(Dataset dataset) {
        this.dataset = dataset;
        this.defDataset = true;
        return this;
    }

    ///////////
    // Build //
    ///////////

    @Override
    public JenaDataManager build() {
        if (defStoragePath && defDataset) {
            return new JenaDataManager(this.dataset, this.storagePath);
        }

        if (this.defStoragePath) {
            return new JenaDataManager(this.storagePath);
        }

        if (this.defDataset) {
            throw new IllegalArgumentException(
                    "You must give the destination path associated to the Dataset (null if in memory) if you build a JenaDataManager with a Dataset");
        }

        return new JenaDataManager();
    }

}
