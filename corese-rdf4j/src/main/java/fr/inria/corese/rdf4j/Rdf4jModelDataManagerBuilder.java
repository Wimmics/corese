package fr.inria.corese.rdf4j;

import org.eclipse.rdf4j.model.Model;

import fr.inria.corese.core.storage.api.dataManager.DataManagerBuilder;

/**
 * Builder for Rdf4jModelDataManagerBuilder.
 */
public class Rdf4jModelDataManagerBuilder implements DataManagerBuilder {

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
     * Create a Rdf4jModelDataManagerBuilder.
     */
    public Rdf4jModelDataManagerBuilder() {
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
    public Rdf4jModelDataManagerBuilder model(Model model) {
        this.model = model;
        this.defModel = true;
        return this;
    }

    ///////////
    // Build //
    ///////////

    @Override
    public Rdf4ModeljDataManager build() {
        if (defModel) {
            return new Rdf4ModeljDataManager(this.model);
        }

        return new Rdf4ModeljDataManager();
    }

}
