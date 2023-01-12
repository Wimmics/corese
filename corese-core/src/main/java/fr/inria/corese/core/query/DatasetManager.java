package fr.inria.corese.core.query;

import java.security.InvalidParameterException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.storage.CoreseGraphDataManagerBuilder;
import fr.inria.corese.core.storage.DataManagerJava;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.util.Property;

/**
 * Manage dataset and/or db storage according to Property
 * Create one DataManager in StorageFactory for each db path
 * STORAGE = type1,id1,[param1];type2,id2,[param2]
 * STORAGE_MODE = dataset|db|db_all
 */
public class DatasetManager {
    private static Logger logger = LoggerFactory.getLogger(DatasetManager.class);

    private DataManager dataManager;
    private String id;

    public DatasetManager() {
    }

    public DatasetManager init() {
        List<List<String>> storages = Property.getSingleton().getStorageparameters();

        if (storages != null && storages.size() > 0) {
            defineDataManager(storages);

            if (isStorage()) {
                // default mode is db storage
                setDataManager(StorageFactory.getDataManager(getId()));
                logger.info("Storage: " + getId());
            }
        }

        return this;
    }

    public enum TypeDataBase {
        RDF4J_MODEL,
        JENA_TDB1,
        CORESE_GRAPH,
        JAVA,
    }

    // Create one DataManager in StorageFactory for each db path
    // first db path is default db
    void defineDataManager(List<List<String>> storages) {
        int i = 0;
        for (List<String> storage : storages) {
            String type = storage.get(0);
            String id = storage.get(1);
            String param = null;
            if (storage.size() == 3) {
                param = storage.get(2);
            }

            TypeDataBase typeDB;
            switch (type) {
                case "jena_tdb1":
                    typeDB = TypeDataBase.JENA_TDB1;
                    break;
                case "rdf4j_model":
                    typeDB = TypeDataBase.RDF4J_MODEL;
                    break;
                case "corese_graph":
                    typeDB = TypeDataBase.CORESE_GRAPH;
                    break;
                case "java":
                    typeDB = TypeDataBase.JAVA;
                    break;

                default:
                    throw new InvalidParameterException("Unknown database type: " + type);
            }

            defineDataManager(typeDB, id, param);
            if (i++ == 0) {
                setId(id);
            }
        }
    }

    // define db data manager, whatever mode is
    // overloaded in corese gui and server
    public void defineDataManager(TypeDataBase typeDB, String id, String param) {
        logger.info("Create data manager " + typeDB + " with id " + id + " with config " + param);
        if (typeDB == TypeDataBase.JAVA) {
            StorageFactory.defineDataManager(param, new DataManagerJava(param));
        } else if (typeDB == TypeDataBase.CORESE_GRAPH) {
            StorageFactory.defineDataManager(id, new CoreseGraphDataManagerBuilder().build());
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

    public String getId() {
        return id;
    }

    public void setId(String path) {
        this.id = path;
    }

}
