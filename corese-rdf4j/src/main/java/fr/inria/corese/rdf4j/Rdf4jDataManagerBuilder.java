package fr.inria.corese.rdf4j;

import org.eclipse.rdf4j.model.Model;

import fr.inria.corese.core.storage.api.dataManager.DataManagerBuilder;

/**
 * Builder for Rdf4jDataManager.
 */
public class Rdf4jDataManagerBuilder implements DataManagerBuilder {

    //////////////////////////
    // Mandatory parameters //
    //////////////////////////

    /////////////////////////
    // Optional parameters //
    /////////////////////////

    private Model model;
    private boolean defModel = false;

    //////////////////
    // Constructors //
    //////////////////

    /**
     * Create a Rdf4jDataManagerBuilder.
     */
    public Rdf4jDataManagerBuilder() {
    }

    ////////////
    // Setter //
    ////////////

    /**
     * Build the dataManager from an existing Rdf4j Model.
     * 
     * @param model Rdf4j Model
     * @return this instance.
     */
    public Rdf4jDataManagerBuilder model(Model model) {
        this.model = model;
        this.defModel = true;
        return this;
    }

    ///////////
    // Build //
    ///////////

    @Override
    public Rdf4jDataManager build() {
        if (defModel) {
            return new Rdf4jDataManager(this.model);
        }

        return new Rdf4jDataManager();
    }

}
