package fr.inria.corese.gui.query;

import fr.inria.corese.core.query.StorageFactory;
import fr.inria.corese.rdf4j.Rdf4jDataManager;
import fr.inria.corese.storage.jenatdb1.JenaDataManager;

/**
 *
 */
public class DatasetManager
        extends fr.inria.corese.core.query.DatasetManager {

    public DatasetManager() {
    }

    @Override
    public DatasetManager init() {
        super.init();
        return this;
    }

    // define db data manager, whatever mode is
    @Override
    public void defineDataManager(TypeDataBase typeDB, String path) {
        super.defineDataManager(typeDB, path);
        if (typeDB == TypeDataBase.JENATDVB1) {
            StorageFactory.defineDataManager(path, new JenaDataManager(path));
        } else if (typeDB == TypeDataBase.RDF4J) {
            StorageFactory.defineDataManager(path, new Rdf4jDataManager());
        }
    }

}
