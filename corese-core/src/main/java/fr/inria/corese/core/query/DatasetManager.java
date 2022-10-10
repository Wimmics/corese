package fr.inria.corese.core.query;

import static fr.inria.corese.core.util.Property.Value.STORAGE_PATH;

import java.security.InvalidParameterException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.producer.DataManagerJava;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.Property.Pair;

/**
 * Manage dataset and/or db storage according to Property
 * Create one DataManager in StorageFactory for each db path
 * STORAGE_PATH = path1;path2
 * STORAGE = dataset|db|db_all
 */
public class DatasetManager {
    private static Logger logger = LoggerFactory.getLogger(DatasetManager.class);

    private DataManager dataManager;
    private String path;

    public DatasetManager() {
    }

    public DatasetManager init() {
        List<Pair> pathList = Property.getSingleton().getValueListBasic(STORAGE_PATH);

        if (pathList != null && pathList.size() > 0) {
            defineDataManager(pathList);

            if (isStorage()) {
                // default mode is db storage
                setDataManager(StorageFactory.getDataManager(getPath()));
                logger.info("Storage: " + getPath());
            }
        }

        return this;
    }

    public enum TypeDataBase {
        RDF4J,
        JENATDVB1,
        JAVA
    }

    // Create one DataManager in StorageFactory for each db path
    // first db path is default db
    void defineDataManager(List<Pair> pathList) {
        int i = 0;
        for (Pair typePath : pathList) {
            String type = typePath.getKey();

            TypeDataBase typeDB;
            switch (type) {
                case "jenatdb1":
                    typeDB = TypeDataBase.JENATDVB1;
                    break;
                case "rdf4jmodel":
                    typeDB = TypeDataBase.RDF4J;
                    break;
                    
                case "java":
                    typeDB=TypeDataBase.JAVA;
                    break;
                    
                default:
                    throw new InvalidParameterException("Unknown database type: " + type);
            }

            String path = typePath.getPath();
            defineDataManager(typeDB, path);
            if (i++ == 0) {
                setPath(path);
            }
        }
    }

    // define db data manager, whatever mode is
    // overloaded in corese gui and server
    public void defineDataManager(TypeDataBase typeDB, String path) {
        logger.info("Create data manager for: " + path);
        if (typeDB == TypeDataBase.JAVA) {
            StorageFactory.defineDataManager(path, new DataManagerJava(path));
        }
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

    public RuleEngine createRuleEngine(Graph g) {
        if (isDataset() || getDataManager() == null) {
            return RuleEngine.create(g);
        } else {
            return RuleEngine.create(g, getDataManager());
        }
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

    public DataManager getDataManager(String path) {
        return StorageFactory.getDataManager(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
