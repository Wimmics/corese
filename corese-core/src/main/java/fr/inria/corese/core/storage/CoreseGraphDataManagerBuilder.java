package fr.inria.corese.core.storage;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.storage.api.dataManager.DataManagerBuilder;

public class CoreseGraphDataManagerBuilder implements DataManagerBuilder {

    //////////////////////////
    // Mandatory parameters //
    //////////////////////////

    /////////////////////////
    // Optional parameters //
    /////////////////////////

    private Graph graph;
    private boolean defGraph = false;

    //////////////////
    // Constructors //
    //////////////////

    /**
     * Create a CoreseGraphDataManagerBuilder.
     */
    public CoreseGraphDataManagerBuilder() {
    }

    ////////////
    // Setter //
    ////////////

    /**
     * Build the dataManager from an existing Corese Graphn
     * 
     * @param graph Corese Graph.
     * @return this instance.
     */
    public CoreseGraphDataManagerBuilder graph(Graph graph) {
        this.graph = graph;
        this.defGraph = true;
        return this;
    }

    ///////////
    // Build //
    ///////////

    @Override
    public CoreseGraphDataManager build() {
        if (defGraph) {
            return new CoreseGraphDataManager(this.graph);
        }

        return new CoreseGraphDataManager();
    }
}
