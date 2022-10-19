package fr.inria.corese.gui.query;

import fr.inria.corese.core.query.DatasetManager;
import fr.inria.corese.core.query.StorageFactory;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.rdf4j.Rdf4jDataManagerBuilder;
import fr.inria.corese.storage.jenatdb1.JenaDataManagerBuilder;

/**
 *
 */
public class DatasetManagerGui
        extends DatasetManager {

    public DatasetManagerGui() {
    }

    @Override
    public DatasetManagerGui init() {
        super.init();
        return this;
    }

    // define db data manager, whatever mode is
    @Override
    public void defineDataManager(TypeDataBase typeDB, String id, String param) {
        super.defineDataManager(typeDB, id, param);

        if (typeDB == TypeDataBase.JENA_TDB1) {
            DataManager dataManager = new JenaDataManagerBuilder().storagePath(param).build();
            StorageFactory.defineDataManager(id, dataManager);
        } else if (typeDB == TypeDataBase.RDF4J) {
            DataManager dataManager = new Rdf4jDataManagerBuilder().build();
            StorageFactory.defineDataManager(id, dataManager);
        }
    }

}
