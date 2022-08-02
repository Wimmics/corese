package fr.inria.corese.gui.query;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.query.StorageFactory;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.STORAGE_PATH;
//import fr.inria.corese.storage.jenatdb1.JenaDataManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manage dataset and/or db storage according to Property
 * Create one DataManager in StorageFactory for each db path 
 * STORAGE_PATH = path1;path2
 * STORAGE = dataset|db|db_all
 */
public class DatasetManager {
    private static Logger logger = LogManager.getLogger(DatasetManager.class);
    
    private DataManager dataManager;
    private String path;
    
    public DatasetManager init() {
        String[] pathList = Property.pathValueList(STORAGE_PATH);

        if (pathList != null && pathList.length>0) {
            defineDataManager(pathList);

            if (isStorage()) {
                // default mode is db storage 
                setDataManager(StorageFactory.getDataManager(getPath()));
                logger.info("Storage: " + getPath());
            }
        }
        
        return this;
    }
    
    // Create one DataManager in StorageFactory for each db path
    // first db path is default db
    void defineDataManager(String[] pathList) {
        int i = 0;
        for (String path : pathList) {
            defineDataManager(path);
            if (i++ == 0) {
                setPath(path);
            }
        }
    }
    
    // define db data manager, whatever mode is
    void defineDataManager(String path) {
        logger.info("Create data manager for: " + path);
        //StorageFactory.defineDataManager(path, new JenaDataManager(path));
    }
    
    public QueryProcess createQueryProcess(Graph g) {
        if (isDataset()) {
            return QueryProcess.create(g);
        }
        return createStorageQueryProcess(g);
    }
    
    public QueryProcess createStorageQueryProcess(Graph g) {
        if (isStorageAll()) {
            return createStorageQueryProcessList(g);
        }
        return createStorageQueryProcessBasic(g);
    }
       
    public QueryProcess createStorageQueryProcessBasic(Graph g) {
        return QueryProcess.create(g, getDataManager());
    }
    
    public QueryProcess createStorageQueryProcessList(Graph g) {
        DataManager[] dmList = StorageFactory.getDataManagerList()
                .toArray(new DataManager[StorageFactory.getDataManagerList().size()]);
        return QueryProcess.create(g, dmList);
    }
       
    public QueryProcess createQueryProcess() {
        return createQueryProcess(Graph.create());
    }
    
    public Load createLoad(Graph g) {
        Load load = Load.create(g);
        if (isStorage()) {
            load.setDataManager(getDataManager());
        }
        return load;
    }
    
    public boolean isDataset() {
        return Property.isDataset();
    }
    
    public boolean isStorage() {
        return Property.isStorage();
    }
    
    public boolean isStorageAll() {
        return Property.isStorageAll();
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
}
