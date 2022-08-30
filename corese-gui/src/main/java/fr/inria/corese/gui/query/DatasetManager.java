package fr.inria.corese.gui.query;

import fr.inria.corese.core.query.StorageFactory;
import fr.inria.corese.storage.jenatdb1.JenaDataManager;


/**
 *
 */
public class DatasetManager 
    extends fr.inria.corese.core.query.DatasetManager {
    
    public DatasetManager() {}
    
    @Override
    public DatasetManager init() {
        super.init();
        return this;
    }

        
    // define db data manager, whatever mode is
    @Override
    public void defineDataManager(String path) {
        super.defineDataManager(path);
        StorageFactory.defineDataManager(path, new JenaDataManager(path));
    }
    
}
