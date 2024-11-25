package fr.inria.corese.jena;

import org.apache.jena.query.Dataset;

import fr.inria.corese.core.storage.api.dataManager.DataManagerBuilder;

/**
 * Builder for JenaTdb1DataManagerBuilder.
 */
public class JenaTdb1DataManagerBuilder implements DataManagerBuilder {

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
     * Create a JenaTdb1DataManagerBuilder.
     */
    public JenaTdb1DataManagerBuilder() {
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
    public JenaTdb1DataManagerBuilder storagePath(String storagePath) {
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
    public JenaTdb1DataManagerBuilder dataset(Dataset dataset) {
        this.dataset = dataset;
        this.defDataset = true;
        return this;
    }

    ///////////
    // Build //
    ///////////

    @Override
    public JenaTdb1DataManager build() {
        if (defStoragePath && defDataset) {
            return new JenaTdb1DataManager(this.dataset, this.storagePath);
        }

        if (this.defStoragePath) {
            return new JenaTdb1DataManager(this.storagePath);
        }

        if (this.defDataset) {
            throw new IllegalArgumentException(
                    "You must give the destination path associated to the Dataset (null if in memory) if you build a JenaTdb1DataManager with a Dataset");
        }

        return new JenaTdb1DataManager();
    }

}
