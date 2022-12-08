package fr.inria.corese.gui.query;

import fr.inria.corese.core.query.DatasetManager;
import fr.inria.corese.core.query.StorageFactory;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.jena.JenaTdb1DataManagerBuilder;
import fr.inria.corese.rdf4j.Rdf4jModelDataManagerBuilder;

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
            DataManager dataManager = new JenaTdb1DataManagerBuilder().storagePath(param).build();
            StorageFactory.defineDataManager(id, dataManager);
        } else if (typeDB == TypeDataBase.RDF4J_MODEL) {
            DataManager dataManager = new Rdf4jModelDataManagerBuilder().build();
            StorageFactory.defineDataManager(id, dataManager);
        }
    }

}
