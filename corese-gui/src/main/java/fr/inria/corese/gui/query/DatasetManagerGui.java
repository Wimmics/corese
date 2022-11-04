package fr.inria.corese.gui.query;

import java.util.HashMap;

import fr.inria.corese.core.query.DatasetManager;
import fr.inria.corese.core.query.StorageFactory;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.rdf4j.Rdf4jDataManagerBuilder;
import fr.inria.corese.storage.jenatdb1.JenaDataManagerBuilder;
import fr.inria.corese.utils.settings.storage.StorageConfig;
import fr.inria.corese.utils.settings.storage.StorageTypeEnum;

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
    public void defineDataManager(StorageConfig storageConfig) {
        super.defineDataManager(storageConfig);

        String id = storageConfig.getId();
        StorageTypeEnum type = storageConfig.getType();
        HashMap<String, String> parameters = storageConfig.getParameters();

        if (type == StorageTypeEnum.JENA_TDB1) {
            DataManager dataManager;
            if (parameters.get("path") != null) {
                dataManager = new JenaDataManagerBuilder().storagePath(parameters.get("path")).build();
            } else {
                dataManager = new JenaDataManagerBuilder().build();
            }
            StorageFactory.defineDataManager(id, dataManager);
        } else if (type == StorageTypeEnum.RDF4J) {
            DataManager dataManager = new Rdf4jDataManagerBuilder().build();
            StorageFactory.defineDataManager(id, dataManager);
        }
    }

}
